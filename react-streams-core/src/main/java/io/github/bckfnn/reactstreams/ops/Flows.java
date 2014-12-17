package io.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.Func0;
import io.github.bckfnn.reactstreams.Proc0;
import io.github.bckfnn.reactstreams.Stream;

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
     * WhenDoneError operation.
     * 
     * @param <T> value type.
     */
    public static class WhenDoneError<T> extends BaseProcessor<T, T> {
        private Throwable error;

        /**
         * Constructor.
         * @param error the error to emit.
         */
        public WhenDoneError(Throwable error) {
            this.error = error;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void onComplete() {
            sendError(error);
        }
    }

    /**
     * WhenDoneFunc operation.
     * 
     * @param <T> value type.
     * @param <R> output type.
     */
    public static class WhenDoneFunc<T, R> extends BaseProcessor<T, R> {
        private Func0<R> func;

        /**
         * Constructor.
         * @param func the function to call.
         */
        public WhenDoneFunc(Func0<R> func) {
            this.func = func;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void onComplete() {
            try {
                sendNext(func.apply());
            } catch (Throwable e) {
                sendError(e);
            }
            sendComplete();
        }
    }

    /**
     * WhenDoneProc operation.
     * 
     * @param <T> value type.
     */
    public static class WhenDoneProc<T> extends BaseProcessor<T, T> {
        private Proc0 func;

        /**
         * Constructor.
         * @param func the function to call.
         */
        public WhenDoneProc(Proc0 func) {
            this.func = func;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void onComplete() {
            try {
                func.apply();
                sendComplete();
            } catch (Throwable error) {
                sendError(error);
            }
        }
    }


    /**
     * WhenDonePublisherFunc operation.
     * 
     * @param <T> value type.
     * @param <R> output value type. 
     */
    public static class WhenDonePublisherFunc<T, R> extends BaseProcessor<T, R> {
        private Func0<Stream<R>> publisher;
        private Subscription continueSubscription;

        /**
         * Constructor.
         * @param publisher the publisher to continue with the this Stream of complete.
         */
        public WhenDonePublisherFunc(Func0<Stream<R>> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void sendCancel() {
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
            try {
                publisher.apply().subscribe(new Subscriber<R>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        continueSubscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(R value) {
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
     * WhenDonePublisherFunc operation.
     * 
     * @param <T> value type.
     * @param <R> output value type. 
     */
    public static class WhenDonePublisher<T, R> extends BaseProcessor<T, R> {
        private Publisher<R> publisher;
        private Subscription continueSubscription;

        /**
         * Constructor.
         * @param publisher the publisher to continue with the this Stream of complete.
         */
        public WhenDonePublisher(Publisher<R> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void sendCancel() {
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
            publisher.subscribe(new Subscriber<R>() {
                @Override
                public void onSubscribe(Subscription s) {
                    continueSubscription = s;
                    s.request(1);
                }

                @Override
                public void onNext(R value) {
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
        }
    }


    /**
     * WhenDoneValue operation.
     * 
     * @param <T> value type.
     * @param <R> output value type. 
     */
    public static class WhenDoneValue<T, R> extends BaseProcessor<T, R> {
        private R value;

        /**
         * Constructor.
         * @param value the value that is emitted when this Stream is complete.
         */
        public WhenDoneValue(R value) {
            this.value = value;
        }

        @Override
        public void doNext(T value) {
            sendRequest();
        }

        @Override
        public void onComplete() {
            sendNext(value);
            sendComplete();
        }
    }

    /**
     * Finally operation.
     * 
     * @param <T> value type.
     * @param <R> output value type. 
     */
    public static abstract class Finally<T, R> extends BaseProcessor<T, R> {

        /**
         * finally function.
         * @return stream to continue with.
         * @throws Throwable
         */
        public abstract Stream<R> fin() throws Throwable;

        @Override
        public void doNext(T value) {
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
            fin().subscribe(new Subscriber<R>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(R value) {
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
     * @param <O> value type.
     */
    public static class Delegate<O> extends BaseProcessor<O, O> {
        private Subscriber<O> target;
        int delegateQueue = 0;

        /**
         * Constructor.
         * @param target the target subscriber.
         */
        public Delegate(Subscriber<O> target) {
            this.target = target;
        }


        @Override
        public void subscribe(Subscriber<? super O> subscriber) {
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
        public void doNext(O value) {
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
