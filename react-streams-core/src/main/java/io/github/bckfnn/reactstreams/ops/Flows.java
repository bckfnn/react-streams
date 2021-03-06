/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.reactstreams.ops;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.Func0;
import io.github.bckfnn.reactstreams.Pipe;
import io.github.bckfnn.reactstreams.Proc0;
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Control flow operations.
 */
public class Flows {
    
    /**
     * ContinueWithProc operation.
     * 
     * @param <T> value type.
     */
    public static class ContinueWith<T> extends BaseProcessor<T, T> {
        private Func0<Stream<T>> func;
        private Subscription continueSubscription;
        private boolean cancelled = false;
        
        /**
         * Constructor.
         * @param func function that will be called on complete.
         */
        public ContinueWith(Func0<Stream<T>> func) {
            this.func= func;
        }

        @Override
        public void doNext(T value) {
            sendNext(value);
        }

        @Override
        public void onComplete() {
            if (cancelled) {
                super.onComplete();
                return;
            }
            try {
                func.apply().subscribe(new Subscriber<T>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        continueSubscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(T value) {
                        sendNext(value);
                        continueSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable error) {
                        sendError(error);
                    }

                    @Override
                    public void onComplete() {
                        sendComplete();
                    }
                });
            } catch (Throwable e) {
                sendError(e);
            }
        }
    }
    

    /**
     * WhenDone operation.
     * 
     * @param <I> value type.
     * @param <O> output value type. 
     */
    public static class WhenDone<I, O> extends BaseProcessor<I, O> {
        private Func0<Stream<O>> func;
        private Subscription continueSubscription;
        private boolean cancelled = false;

        /**
         * Constructor.
         * @param func the value that is emitted when this Stream is complete.
         */
        public WhenDone(Func0<Stream<O>> func) {
            this.func = func;
        }

        @Override
        public void doNext(I value) {
            sendRequest();
            handled();
        }

        @Override
        public void sendCancel() {
            cancelled = true;
            if (continueSubscription != null) {
                continueSubscription.cancel();
            } else {
                super.sendCancel();
            }
        }

        @Override
        public void sendRequest(long n) {
            if (continueSubscription != null) {
                continueSubscription.request(n);
            } else {
                super.sendRequest(n);
            }
        }

        @Override
        public void onComplete() {
            if (cancelled) {
                super.onComplete();
                return;
            }
            try {
                func.apply().subscribe(new Subscriber<O>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        continueSubscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(O value) {
                        sendNext(value);
                        //continueSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable error) {
                        sendError(error);
                    }

                    @Override
                    public void onComplete() {
                        sendComplete();
                    }
                });
            } catch (Throwable e) {
                sendError(e);
            }
        }
    }

    /**
     * Finally operation.
     * 
     * @param <T> value type.
     */
    public static class Finally<T> extends BaseProcessor<T, T> {
        private Proc0 func;

        /**
         * Constructor.
         * @param func the function to call when this stream is complete or emit an error.
         */
        public Finally(Proc0 func) {
            this.func = func;
        }

        @Override
        public void doNext(T value) {
            sendNext(value);
            handled();
        }

        @Override
        public void onComplete() {
            try {
                runFinally();
                super.onComplete();
            } catch (Throwable e) {
                sendError(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            try {
                runFinally();
                super.onError(t);
            } catch (Throwable e) {
                sendError(e);
            }
        }

        private void runFinally() throws Throwable {
            func.apply();
        }

    }

    /**
     * Delegate operation.
     * 
     * @param <T> value type.
     */
    public static class Delegate<T> extends BaseProcessor<T, T> {
        private Subscriber<T> target;
        int delegateQueue = 0;

        /**
         * Constructor.
         * @param target the target subscriber.
         */
        public Delegate(Subscriber<T> target) {
            this.target = target;
        }


        @Override
        public void subscribe(Subscriber<? super T> subscriber) {
            super.subscribe(subscriber);

            target.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    delegateQueue += n;
                    int min = 1;
                    sendRequest(min);
                }

                @Override
                public void cancel() {
                    sendCancel();
                }
            });
        }

        @Override
        public void doNext(T value) {
            sendNext(value);
            target.onNext(value);
        }

        @Override
        public void onComplete() {
            sendComplete();
            target.onComplete();
        }

        @Override
        public void onError(Throwable error) {
            sendError(error);
            target.onError(error);
        }

        public String toString() {
            return "Delegate[" + "" + "]";
        }

    }
    
    /**
     * Pipe operation.
     * @param <I> input value type
     * @param <O> output value type;
     */
    public static class PipeX<I, O> implements Pipe<I, O> {
        private BaseProcessor<I, I> head;
        private Stream<O> tail;
        
        /**
         * Constructor.
         * @param head head of the pipe.
         * @param tail tail of the pipe.
         */
        public PipeX(BaseProcessor<I, I> head, Stream<O> tail) {
            this.head = head;
            this.tail = tail;
        }
        
        @Override
        public void onSubscribe(Subscription s) {
            head.onSubscribe(s);
        }

        @Override
        public void onNext(I t) {
            head.sendNext(t);
        }

        @Override
        public void onError(Throwable t) {
            head.sendError(t);
        }

        @Override
        public void onComplete() {
            head.sendComplete();
        }

        @Override
        public void subscribe(Subscriber<? super O> s) {
            tail.subscribe(s);
        }
    };
}
