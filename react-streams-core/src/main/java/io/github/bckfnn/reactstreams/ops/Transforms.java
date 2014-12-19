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
import io.github.bckfnn.reactstreams.BaseSubscription;
import io.github.bckfnn.reactstreams.Func1;
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
 * Transform operations.
 */
public class Transforms {
    /**
     * The map operation.
     * @param <I> type of input values.
     * @param <O> type of output values.
     */
    public static class Map<I, O> extends BaseProcessor<I, O> {
        private Func1<I, O> func;
        
        /**
         * Constructor.
         * @param func a function that map an input value to an output value.
         */
        public Map(Func1<I, O> func) {
            this.func = func;
        }
        
        @Override
        public void doNext(I value) {
            try {
                sendNext(func.apply(value));
                handled();
            } catch (Throwable error) {
                sendError(error);
                sendCancel();
            }
        }
    }

    /**
     * The mapMany operation.
     *
     * @param <I> type of input values.
     * @param <O> type of output values.
     */
    public static class MapMany<I, O> extends BaseProcessor<I, O> {
        private List<Publisher<O>> children = new ArrayList<Publisher<O>>();
        private boolean completed = false;
        private int count = 0;
        private Subscription childSubscription;
        private Func1<I, Stream<O>> func;

        /**
         * Constructor.
         * @param func a function that map an input value to a stream of output values.
         */
        public MapMany(Func1<I, Stream<O>> func) {
            this.func = func;
        }
        
        @Override
        public void doNext(I value) {
            try {
                final Publisher<O> child = func.apply(value);
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
            final Publisher<O> child = children.remove(0);
            child.subscribe(new Subscriber<O>() {

                @Override
                public void onSubscribe(Subscription s) {
                    childSubscription = s;
                    childSubscription.request(1);
                }

                @Override
                public void onNext(O value) {
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
     * @param <I> type of input values.
     * @param <O> type of output values.
     */
    public static class MapManyWith<I, O> extends BaseProcessor<I, Tuple<I, O>> {
        private List<Tuple<I, Publisher<O>>> children = new ArrayList<>();
        private boolean completed = false;
        private int count = 0;
        private Subscription childSubscription;
        private Func1<I, Stream<O>> func;
        
        /**
         * Constructor.
         * @param func a function that map an input value to a stream of output values.
         */
        public MapManyWith(Func1<I, Stream<O>> func) {
            this.func = func;
        }
        
        @Override
        public void doNext(I value) {
            try {
                final Publisher<O> child = func.apply(value);
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
            final Tuple<I, Publisher<O>> child = children.remove(0);
            child.right().subscribe(new Subscriber<O>() {

                @Override
                public void onSubscribe(Subscription s) {
                    childSubscription = s;
                    childSubscription.request(1);
                }

                @Override
                public void onNext(O value) {
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
