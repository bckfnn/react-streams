package io.github.bckfnn.reactstreams.ops;

import java.util.Iterator;

import io.github.bckfnn.reactstreams.ActiveSubscription;
import io.github.bckfnn.reactstreams.BaseSubscription;
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Creating streams operations.
 */
public class Streams {

    /**
     * <code>Array</code> emit the elements in the array as a Stream.
     * @param <T> type of the Stream
     */
    public static class Array<T> implements Stream<T> { 
        private T[] array;
        private int idx = 0;

        /**
         * Constructor.
         * @param array the input array.
         */
        public Array(T[] array) {
            this.array = array;
        }

        @Override
        public void subscribe(final Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new ActiveSubscription<T>(subscriber) {
                private int idx = 0;

                @Override
                public boolean hasMore() {
                    return idx < array.length;
                }

                @Override
                public T getOne() {
                    return array[idx++];
                }
            });
        }

        public String toString() {
            return "ArraySource[" + idx + "]";
        }
    }

    /**
     * Emits the exception as an onError event.
     * 
     * @param <T> the type of output elements.
     */
    public static class Error<T> implements Stream<T> {
        private Throwable exc;
        boolean finished;

        /**
         * Constructor.
         * @param exc the exception.
         */
        public Error(Throwable exc) {
            this.exc = exc;
        }

        @Override
        public void subscribe(final Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new BaseSubscription<T>(subscriber) {
                @Override
                public void request(long elements) {
                    sendError(exc);
                }
            });
        }
    }


    /**
     * <strong>FromIteratorOp</strong> will generate an onNext() event for each element in the
     * Iterable, followed by an onComplete() event.
     * 
     * @param <T> type of the event.
     */
    public static class Iter<T> implements Stream<T> {
        private Iterator<T> iterator;

        /**
         * Constructor.
         * @param iterator the iterator.
         */
        public Iter(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void subscribe(final Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new ActiveSubscription<T>(subscriber) {
                @Override
                public boolean hasMore() {
                    return iterator.hasNext();
                }

                @Override
                public T getOne() {
                    return iterator.next();
                }
            });         
        }
    }

    /**
     * Value will generate a single onNext() event for the specified element, 
     * followed by an onComplete().
     * 
     * @param <T> type of the event.
     */
    public static class Value<T> implements Stream<T>, Publisher<T> {
        private T value;
        private boolean finished = false;

        /**
         * Constructor.
         * @param value the value.
         */
        public Value(T value) {
            this.value = value;
        }

        @Override
        public void subscribe(final Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new BaseSubscription<T>(subscriber) {
                @Override
                public void request(long elements) {
                    if (!finished) {
                        finished = true;
                        sendNext(value);
                        sendComplete();
                    }
                }
            });
        }
    }

    /**
     * <code>Counter</code> emit a sequence of integers, until the Stream is cancelled.
     */
    public static class Counter implements Stream<Integer> {
        private int start;

        /**
         * Constructor.
         * @param start the start value.
         */
        public Counter(int start) {
            this.start = start;
        }

        @Override
        public void subscribe(Subscriber<? super Integer> subscriber) {
            ActiveSubscription<Integer> s = new ActiveSubscription<Integer>(subscriber) {
                int count = start;

                @Override
                public boolean hasMore() {
                    return true;
                }

                @Override
                public Integer getOne() {
                    return count++;
                }
            };
            subscriber.onSubscribe(s);
            //s.activate();
        }
    }

}
