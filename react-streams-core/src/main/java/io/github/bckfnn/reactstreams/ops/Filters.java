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

import java.io.PrintStream;

import org.reactivestreams.Subscription;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.Func1;
import io.github.bckfnn.reactstreams.Func2;

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
     * <code>Filter</code> will call the func check on each item and emit only the 
     * items where the method returns true. 
     * @param <T> type of the event.
     */
    public static class Filter<T> extends BaseProcessor<T, T> {
        private Func1<T, Boolean> func;
        
        /**
         * Constructor.
         * @param func a function that map an input value to a stream of output values.
         */
        public Filter(Func1<T, Boolean> func) {
            this.func = func;
        }
 
        @Override
        public void doNext(T value) {
            try {
                if (func.apply(value)) {
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
    public static class Accumulator<T> extends BaseProcessor<T, T> {
        private T acc;
        private boolean initialValueSent = false;
        private Func2<T, T, T> func;

        /**
         * Constructor.
         * @param initial the initial value.
         * @param func the function that is called for each valie in the stream.
         */
        public Accumulator(T initial, Func2<T, T, T> func) {
            this.acc = initial;
            this.func = func;
        }

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
                acc = func.apply(acc, value);
                sendNext(acc);
                handled();
            } catch (Throwable exc) {
                sendError(exc);
                sendCancel();
            }
        }
    }
}
