package io.github.bckfnn.reactstreams.ops;

import java.io.PrintStream;

import org.reactivestreams.Subscription;

import io.github.bckfnn.reactstreams.BaseProcessor;

/**
 * Filtering operations.
 */
public class Filters {
    /**
     * <code>Skip</code> ignore the initial <code>n</code> elements. 
     * @param <T> type of the stream.
     */
    public static class Skip<T> extends BaseProcessor<T, T> {
        final private int num;
        private int count = 0;

        /**
         * Constructor.
         * @param num number of items to skip.
         */
        public Skip(int num) {
            this.num = num;
        }

        @Override
        public void doNext(T value) {
            if (++count > num) {
                sendNext(value);
            } else {
                sendRequest();
            }
            handled();
        }
    }
    
    /**
     * <b>Last</b> will consume all the input and when onComplete() is recieved will 
     * send out an onNext() with the last element, followed by an onComplete(). 
     *
     * @param <T> type of the event.
     */
    public static class Last<T> extends BaseProcessor<T, T> {
        private T value;
        private boolean onNext = false;

        @Override
        public void doNext(T value) {
            onNext = true;
            this.value = value;
            sendRequest();
            handled();
        }

        @Override
        public void onComplete() {
            if (onNext) {
                sendNext(value);
            } 
            super.onComplete();
        }
    }

    /**
     * <code>Filter</code> file call {@link Filter#check(Object)} check on each item and emit only the 
     * items where the method returns true. 
     * @param <T> type of the event.
     */
    public static abstract class Filter<T> extends BaseProcessor<T, T> {
        /**
         * Emit only items where this method returns true.
         * @param value the item value.
         * @return true if the item should it included in the output.
         * @throws Throwable when an error occurs.
         */
        public abstract boolean check(T value) throws Throwable;

        @Override
        public void doNext(T value) {
            try {
                if (check(value)) {
                    sendNext(value);
                } else {
                    sendRequest();
                }
                handled();
            } catch (Throwable exc) {
                sendError(exc);
                sendCancel();
            }
        }
        
        public String toString() {
            return "FilterOp";
        }
    }
    
    /**
     * <code>Take</code> emit the initial <code>n</code> elements and then cancel the input stream. 
     * @param <T> type of the stream.
     */
    public static class Take<T> extends BaseProcessor<T, T> {
        final private int num;
        private int count = 0;
        
        /**
         * Constructor.
         * @param num number of items include in the output.
         */
        public Take(int num) {
            this.num = num;
        }

        @Override
        public void doNext(T value) {
            if (count++ < num) {
                sendNext(value);
                handled();
            } else {
                sendComplete();
                sendCancel();
            }
        }
    }
    

    /**
     * <code>Nop</code> emit the input events unmodified. 
     * @param <T> type of the stream.
     */
    public static class Nop<T> extends BaseProcessor<T, T> {
        @Override
        public void doNext(T value) {
            sendNext(value);
            handled();
        }
    }
    
    /**
     * Done will only generate a complete event.
     * @param <T> type of the event.
     */
    public static class Done<T> extends BaseProcessor<T, T> {
        @Override
        public void doNext(T value) {
            sendComplete();
            sendCancel();
        }
    }
    
    /**
     * Print will print debug information to the PrintStream
     * @param <T> type of the event.
     */
    public static class Print<T> extends BaseProcessor<T, T> {
        private String prefix;
        private PrintStream printStream;
        
        /**
         * Constructor.
         * @param prefix print messages are prefixed with this string.
         * @param printStream the printStream.
         */
        public Print(String prefix, PrintStream printStream) {
            this.prefix = prefix;
            this.printStream = printStream;
        }

        public void onSubscribe(Subscription s) {
            printStream.println(prefix + " onSubScribe:" + s);
            super.onSubscribe(s);
        }

        @Override
        public void doNext(T value) {
            printStream.println(prefix + " onNext:" + value);
            sendNext(value);
            handled();
        }
        
        @Override
        public void onError(Throwable error) {
            printStream.println(prefix + " onError:" + error);
            error.printStackTrace(printStream);
            super.onError(error);
        }

        @Override
        public void onComplete() {
            printStream.println(prefix + " onComplete");
            super.onComplete();
        }

        @Override
        public void sendCancel() {
            printStream.println(prefix + " cancel");
            super.sendCancel();
        }

        @Override
        public void sendRequest(long n) {
            printStream.println(prefix + " request(" + n + ")");
            //new Exception().printStackTrace();
            super.sendRequest(n);
        }
    }

    /**
     * Accumulator operation.
     *
     * @param <T> value type.
     */
    public static abstract class Accumulator<T> extends BaseProcessor<T, T> {
        private T acc;
        private boolean initialValueSent = false;

        /**
         * Constructor.
         * @param initial the initial value.
         */
        public Accumulator(T initial) {
            this.acc = initial;
        }

        /**
         * called for each value.
         * @param value the current value.
         * @param nextValue the next value encounter.
         * @return the new accumulated value.
         * @throws Throwable if an error occurs.
         */
        public abstract T calc(T value, T nextValue) throws Throwable;

        @Override
        public void doNext(T value) {
            if (acc == null) {
                acc = value;
                sendRequest();
                handled();
                return;
            }
            if (!initialValueSent) {
                initialValueSent = true;
                sendNext(acc);
            }
            try {
                acc = calc(acc, value);
                sendNext(acc);
                handled();
            } catch (Throwable exc) {
                sendError(exc);
                sendCancel();
            }
        }
    }
}
