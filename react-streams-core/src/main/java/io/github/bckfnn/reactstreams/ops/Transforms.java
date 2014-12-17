package io.github.bckfnn.reactstreams.ops;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.BaseSubscription;
import io.github.bckfnn.reactstreams.Stream;
import io.github.bckfnn.reactstreams.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Map operations.
 */
public class Transforms {
    /**
     * The map operation.
     * @param <T> type of input values.
     * @param <R> type of output values.
     */
    public static abstract class Map<T, R> extends BaseProcessor<T, R> {

        /**
         * Map the input <code>value</code> to an output value. 
         * @param value the input value
         * @return the output value.
         * @throws Throwable when an error occur.
         */
        public abstract R map(T value) throws Throwable;

        @Override
        public void doNext(T value) {
            try {
                sendNext(map(value));
                handled();
            } catch (Throwable error) {
                sendError(error);
                sendCancel();
            }
        }

        public String toString() {
            return "Map[]";
        }
    }

    /**
     * The mapMany operation.
     *
     * @param <T> type of input values.
     * @param <R> type of output values.
     */
    public static abstract class MapMany<T, R> extends BaseProcessor<T, R> {
        private List<Publisher<R>> children = new ArrayList<Publisher<R>>();
        private boolean completed = false;
        private int count = 0;
        private Subscription childSubscription;

        /**
         * Map the input <code>value</code> to a stream or output values.
         * @param value the input value.
         * @return a stream of output values.
         * @throws Throwable when errors occur.
         */
        public abstract Stream<R> map(T value) throws Throwable;

        @Override
        public void doNext(T value) {
            try {
                final Publisher<R> child = map(value);
                children.add(child);
                count++;
                drain();
            } catch (Throwable exc) {
                onError(exc);
            }
        }

        @Override
        public void onComplete() {
            if (count == 0) {
                sendComplete();
            } else {
                completed = true;
            }
        }

        @Override
        public void sendRequest(long n) {
            if (childSubscription!= null) {
                childSubscription.request(n);
            } else {
                super.sendRequest(n);
            }
        }


        private void drain() {
            if (children.size() == 0) {
                return;
            }
            final Publisher<R> child = children.remove(0);
            child.subscribe(new Subscriber<R>() {

                @Override
                public void onSubscribe(Subscription s) {
                    childSubscription = s;
                    childSubscription.request(1);
                }

                @Override
                public void onNext(R value) {
                    sendNext(value);
                }

                @Override
                public void onComplete() {
                    childSubscription = null;
                    count--;
                    if (count == 0 && completed) {
                        sendComplete();
                    } else {
                        sendRequest(1);
                        drain();
                    }
                    handled();
                }

                @Override
                public void onError(Throwable exc) {
                    sendError(exc);
                }
            });
        }

        @Override
        public String toString() {
            return "MapMany[]";
        }
    }

    /**
     * The mapManyWith operation.
     *
     * @param <T> type of input values.
     * @param <R> type of output values.
     */
    public static abstract class MapManyWith<T, R> extends BaseProcessor<T, Tuple<T, R>> {
        private List<Tuple<T, Publisher<R>>> children = new ArrayList<>();
        private boolean completed = false;
        private int count = 0;
        private Subscription childSubscription;

        /**
         * Map the input <code>value</code> to a stream or output values.
         * @param value the input value.
         * @return a stream of output values.
         * @throws Throwable when errors occur.
         */
        public abstract Stream<R> map(T value) throws Throwable;

        @Override
        public void doNext(T value) {
            try {
                final Publisher<R> child = map(value);
                children.add(new Tuple<>(value, child));
                count++;
                drain();
            } catch (Throwable exc) {
                onError(exc);
            }
        }

        @Override
        public void onComplete() {
            if (count == 0) {
                sendComplete();
            } else {
                completed = true;
            }
        }

        @Override
        public void sendRequest(long n) {
            if (childSubscription!= null) {
                childSubscription.request(n);
            } else {
                super.sendRequest(n);
            }
        }


        private void drain() {
            if (children.size() == 0) {
                return;
            }
            final Tuple<T, Publisher<R>> child = children.remove(0);
            child.right().subscribe(new Subscriber<R>() {

                @Override
                public void onSubscribe(Subscription s) {
                    childSubscription = s;
                    childSubscription.request(1);
                }

                @Override
                public void onNext(R value) {
                    sendNext(new Tuple<>(child.left(), value));
                }

                @Override
                public void onComplete() {
                    childSubscription = null;
                    count--;
                    if (count == 0 && completed) {
                        sendComplete();
                    } else {
                        sendRequest(1);
                        drain();
                    }
                    handled();
                }

                @Override
                public void onError(Throwable exc) {
                    sendError(exc);
                }
            });
        }

        @Override
        public String toString() {
            return "MapMany[]";
        }
    }
    
    /**
     * ToList operations. 
     * @param <T> type of input values.
     */
    public static class ToList<T> extends BaseProcessor<T, List<T>> {
        private List<T> list = new ArrayList<>();
        
        @Override
        public void doNext(T value) {
            list.add(value);
            sendRequest();
            handled();
        }

        @Override
        public void onComplete() {
            sendNext(list);
            sendComplete();
        }
    }
    
    /**
     * <code>Zip</code> combines two Stream by emitting a Tuple with an item from each stream.
     *
     * @param <T1> type of the first Stream
     * @param <T2> type of the second Stream.
     */
    public static class Zip<T1, T2> implements Stream<Tuple<T1, T2>> {
        Publisher<T1> o1;
        Publisher<T2> o2;
        Subscription i1;
        Subscription i2;
        List<T1> v1 = new LinkedList<T1>();
        boolean v1stop = false;
        List<T2> v2 = new LinkedList<T2>();
        boolean v2stop = false;
        
        /**
         * Constructor.
         * @param o1 the first Stream.
         * @param o2 the second Stream.
         */
        public Zip(Publisher<T1> o1, Publisher<T2> o2) {
            this.o1 = o1;
            this.o2 = o2;
        }
        
        @Override
        public void subscribe(final Subscriber<? super Tuple<T1, T2>> subscriber) {
            
            Subscriber<T1> s1 = new Subscriber<T1>() {
                @Override
                public void onSubscribe(Subscription s) {
                    i1 = s;
                }

                @Override
                public void onNext(T1 t) {
                    if (v2.size() > 0) {
                        subscriber.onNext(new Tuple<T1, T2>(t, v2.remove(0)));
                        //i2.request(1);
                    } else if (v2stop) {
                        subscriber.onComplete();
                        i1.cancel();
                    } else {
                        v1.add(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    i2.cancel();
                    subscriber.onError(t);
                }

                @Override
                public void onComplete() {
                    v1stop = true;
                    if (v2stop || v1.size() == 0) {
                        subscriber.onComplete();
                    }
                }
            };
            o1.subscribe(s1);
            
            
            Subscriber<T2> s2 = new Subscriber<T2>() {
                @Override
                public void onSubscribe(Subscription s) {
                    i2 = s;
                }

                @Override
                public void onNext(T2 t) {
                    if (v1.size() > 0) {
                        subscriber.onNext(new Tuple<T1, T2>(v1.remove(0), t));
                        //i1.request(1);
                    } else if (v1stop) {
                        subscriber.onComplete();
                        i2.cancel();
                    } else {
                        v2.add(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    i1.cancel();
                    subscriber.onError(t);
                }

                @Override
                public void onComplete() {
                    v2stop = true;
                    if (v1stop || v2.size() == 0) {
                        subscriber.onComplete();
                    }
                }
            };
            o2.subscribe(s2);
            
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if (!v1stop) {
                        i1.request(n);
                    }
                    if (!v2stop) {
                        i2.request(n);
                    }
                }

                @Override
                public void cancel() {
                    i1.cancel();
                    i1.cancel();
                }
            });
        }

    }
    
    /**
     * Concat operation.
     * 
     * @param <T> value type.
     */
    public static class Concat<T> implements Stream<T> {
        private BaseSubscription<T> outputSubscription;
        private List<Publisher<T>> list;
        private int i = 0;
        private Subscription currentInputSubscription;

        /**
         * Constructor.
         * @param list an array of publishers.
         */
        public Concat(Publisher<T>[] list) {
            this(Arrays.asList(list));
        }

        /**
         * Constructor.
         * @param list a list of publishers.
         */
        public Concat(List<Publisher<T>> list) {
            this.list = list;
        }
        
        @Override
        public void subscribe(Subscriber<? super T> s) {
            list.get(i++).subscribe(new Sub());
            s.onSubscribe(outputSubscription = new BaseSubscription<T>(s) {
                @Override
                public void request(long elements) {
                    super.request(elements);
                    currentInputSubscription.request(outputSubscription.getPendingDemand());
                }
            });
        }

        private class Sub implements Subscriber<T> {
            @Override
            public void onSubscribe(Subscription s) {
                currentInputSubscription = s;
            }

            @Override
            public void onNext(T t) {
                outputSubscription.sendNext(t);
            }

            @Override
            public void onError(Throwable t) {
                outputSubscription.sendError(t);
            }

            @Override
            public void onComplete() {
                if (i == list.size()) {
                    outputSubscription.sendComplete();
                } else {
                    list.get(i++).subscribe(new Sub());
                    currentInputSubscription.request(outputSubscription.getPendingDemand());
                }
            }
        }
    }

}
