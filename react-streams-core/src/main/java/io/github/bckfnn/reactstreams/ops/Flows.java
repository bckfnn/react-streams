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
import io.github.bckfnn.reactstreams.Proc0;
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Control flow operations.
 */
public class Flows {
    /**
     * ContinueWithError operation.
     * 
     * @param <T> value type.
     */
    public static class ContinueWithError<T> extends BaseProcessor<T, T> {
        private Throwable error;

        /**
         * Constructor.
         * @param error the error that will be emitted.
         */
        public ContinueWithError(Throwable error) {
            this.error = error;
        }

        @Override
        public void doNext(T value) {
            sendNext(value);
            //handled();
        }

        @Override
        public void onComplete() {
            sendError(error);
        }
    }

    /**
     * ContinueWithProc operation.
     * 
     * @param <T> value type.
     */
    public static class ContinueWithProc<T> extends BaseProcessor<T, T> {
        private Proc0 func;

        /**
         * Constructor.
         * @param func function that will be called on complete.
         */
        public ContinueWithProc(Proc0 func) {
            this.func= func;
        }

        @Override
        public void doNext(T value) {
            sendNext(value);
        }

        @Override
        public void onComplete() {
            try {
                func.apply();
                sendComplete();
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
     * Finally operation.
     * 
     * @param <I> value type.
     * @param <O> output value type. 
     */
    public static class Finally<I, O> extends BaseProcessor<I, O> {
        private Func0<Stream<O>> func;

        /**
         * Constructor.
         * @param func the function to call when this stream is complete or emit an error.
         */
        public Finally(Func0<Stream<O>> func) {
            this.func = func;
        }

        @Override
        public void doNext(I value) {
            sendRequest();
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
            func.apply().subscribe(new Subscriber<O>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(O value) {
                    sendNext(value);
                }

                @Override
                public void onComplete() {
                    sendComplete();
                }

                @Override
                public void onError(Throwable exc) {
                    sendError(exc);
                }
            });
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
}
