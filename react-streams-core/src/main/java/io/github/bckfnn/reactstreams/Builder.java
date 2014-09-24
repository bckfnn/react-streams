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
package io.github.bckfnn.reactstreams;

import io.github.bckfnn.reactstreams.ops.AccumulatorOp;
import io.github.bckfnn.reactstreams.ops.ConcatOp;
import io.github.bckfnn.reactstreams.ops.ContinueWithErrorOp;
import io.github.bckfnn.reactstreams.ops.ContinueWithProcOp;
import io.github.bckfnn.reactstreams.ops.CounterOp;
import io.github.bckfnn.reactstreams.ops.DelegateOp;
import io.github.bckfnn.reactstreams.ops.DoneOp;
import io.github.bckfnn.reactstreams.ops.FilterOp;
import io.github.bckfnn.reactstreams.ops.FinallyOp;
import io.github.bckfnn.reactstreams.ops.FromArrayOp;
import io.github.bckfnn.reactstreams.ops.FromErrorOp;
import io.github.bckfnn.reactstreams.ops.FromIteratorOp;
import io.github.bckfnn.reactstreams.ops.FromValueOp;
import io.github.bckfnn.reactstreams.ops.LastOp;
import io.github.bckfnn.reactstreams.ops.MapManyOp;
import io.github.bckfnn.reactstreams.ops.MapManyWithOp;
import io.github.bckfnn.reactstreams.ops.MapOp;
import io.github.bckfnn.reactstreams.ops.NopOp;
import io.github.bckfnn.reactstreams.ops.PrintStreamOp;
import io.github.bckfnn.reactstreams.ops.SkipOp;
import io.github.bckfnn.reactstreams.ops.TakeOp;
import io.github.bckfnn.reactstreams.ops.ToListOp;
import io.github.bckfnn.reactstreams.ops.WhenDoneErrorOp;
import io.github.bckfnn.reactstreams.ops.WhenDoneFuncOp;
import io.github.bckfnn.reactstreams.ops.WhenDoneProcOp;
import io.github.bckfnn.reactstreams.ops.WhenDonePublisherOp;
import io.github.bckfnn.reactstreams.ops.WhenDoneValueOp;
import io.github.bckfnn.reactstreams.ops.ZipOp;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Main class for using <code>react-stream</code> classes.
 * @param <T> type of builder.
 */
public class Builder<T> implements Operations<T>, Publisher<T> {
    private Publisher<T> publisher;

    /**
     * Private constructor, used when creating a new Builder.
     * @param publisher the publisher.
     */
    protected Builder(Publisher<T> publisher) {
        this.publisher = publisher;
    }

    /**
     * Wrap a publisher as an Operations object.
     * @param publisher the publisher to wrap.
     * @param <T> the type of publisher.
     * @return the operations object.
     */
    public static <T> Operations<T> as(Publisher<T> publisher) {
    	return new Builder<T>(publisher);
    }

    /**
     * Create and return a new <code>Builder</code> that emit a single value.
     * @param value the value.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    public static <T> Operations<T> from(T value) {
        return new Builder<T>(new FromValueOp<>(value));
    }

    /**
     * Create and return a new <code>Builder</code> that emit a series of values.
     * @param values the values.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    @SafeVarargs
    public static <T> Operations<T> from(T... values) {
        return new Builder<T>(new FromArrayOp<>(values));
    }

    /**
     * Create and return a new <code>Builder</code> that emit the values from a <code>Collection</code>.
     * @param collection the collection.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    public static <T> Operations<T> from(Collection<T> collection) {
        return new Builder<T>(new FromIteratorOp<T>(collection.iterator()));
    }

    /**
     * Create and return a new <code>Builder</code> that emit the supplied exception.
     * @param error the error.
     * @param <T> type of builder.
     * @return the new builder.
     */
    public static <T> Operations<T> error(Throwable error) {
        return new Builder<T>(new FromErrorOp<>(error));
    }

    /**
     * Create and return a new <code>Builder</code> that concatenate all the values from all the supplied <code>Publishers</code>.
     * @param list the list of publishers.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    @SafeVarargs
    public static <T> Operations<T> concat(Publisher<T>... list) {
        return new Builder<T>(new ConcatOp<T>(list));
    }

    /**
     * Create and return a new <code>Builder</code> that emit all integers from 0.
     * @return the new builder.
     */
    public static Operations<Integer> counter() {
        return new Builder<>(new CounterOp(0));
    }

    /**
     * Create and return a new <code>Builder</code> that emit all integers from the specified <code>start</code> value.
     * @param start the start value.
     * @return the new builder.
     */
    public static Operations<Integer> counter(int start) {
        return new Builder<>(new CounterOp(start));
    }

    /**
     * Create and return a new <code>Builder</code> that zip two <code>Publishers</code> together by
     * emitting a <code>Tuple</code> with a value from each publisher.
     * @param p1 the first publisher.
     * @param p2 the second publisher.
     * @param <T1> type of the first publisher.
     * @param <T2> type of the second publisher.
     * @return the new builder.
     */
    public static <T1, T2> Operations<Tuple<T1, T2>> zip(Publisher<T1> p1, Publisher<T2> p2) {
        return new Builder<Tuple<T1, T2>>(new ZipOp<T1, T2>(p1, p2));
    }

    /**
     * @param <T> type of the builder.
     * @return a new pipe
     */
    public static <T> Pipe<T, T> newPipe() {
        BaseProcessor<T, T> head = new NopOp<T>() {
 
        };
        return new PipeImpl<T, T>(head, head);
    }
    
    @Override
    public <R> Operations<R> next(Processor<T, R> processor) {
        publisher.subscribe(processor);
        return new Builder<R>(processor);
    }


    @Override
    public void then(Subscriber<T> subscriber) {
        publisher.subscribe(subscriber);
    }

    /*
     * map operations.
     */

    @Override
    public <R> Operations<R> map(final Func1<T, R> mapFunc) {
        return next(new MapOp<T, R>() {
            @Override
            public R map(T value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }

    @Override
    public <R> Operations<R> mapMany(final Func1<T, Operations<R>> mapFunc) {
        return next(new MapManyOp<T, R>() {
            @Override
            public Operations<R> map(T value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }

    @Override
    public <R> Operations<Tuple<T, R>> mapManyWith(final Func1<T, Operations<R>> mapFunc) {
        return next(new MapManyWithOp<T, R>() {
            @Override
            public Operations<R> map(T value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }
    
    /*
     * Filter operations.
     */
    @Override
    public Operations<T> last() {
        return next(new LastOp<T>());
    }

    @Override
    public Operations<T> skip(int cnt) {
        return next(new SkipOp<T>(cnt));
    }

    @Override
    public Operations<T> take(int cnt) {
        return next(new TakeOp<T>(cnt));
    }

    @Override
    public Operations<T> nop() {
        return next(new NopOp<T>());
    }
    
    public <R> Processor<R, T> asPipe() {
        return null;
    }

    @Override
    public Operations<T> done() {
        return next(new DoneOp<T>());
    }

    @Override
    public Operations<T> filter(Func1<T, Boolean> func) {
        return next(new FilterOp<T>() {
            @Override
            public boolean check(T value) throws Throwable {
                return func.apply(value);
            }
        });
    }

    @Override
    public <R> Operations<R> whenDoneValue(R value) {
        return next(new WhenDoneValueOp<T, R>(value));
    }

    @Override
    public Operations<T> whenDoneError(Throwable error) {
        return next(new WhenDoneErrorOp<T>(error));
    }
    
    @Override
    public Operations<T> whenDone(Proc0 func) {
        return next(new WhenDoneProcOp<T>(func));
    }

    @Override
    public <R> Operations<R> whenDone(Func0<R> func) {
        return next(new WhenDoneFuncOp<T, R>(func));
    }
    
    @Override
    public <R> Operations<R> whenDone(Publisher<R> publisher) {
        return next(new WhenDonePublisherOp<T, R>(publisher));
    }

    @Override
    public Operations<T> continueWithValue(T value) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Operations<T> continueWithError(Throwable error) {
        return next(new ContinueWithErrorOp<>(error));
    }

    @Override
    public Operations<T> continueWith(Proc0 func) {
        // TODO Auto-generated method stub
        return next(new ContinueWithProcOp<T>(func));
    }
    
    @Override
    public Operations<T> continueWith(Publisher<T> publisher) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Operations<T> delegate(Subscriber<T> x) {
        return next(new DelegateOp<T>(x));
    }

    @Override
    public Operations<T> onEach(Proc2<T, BaseProcessor<T, T>> func) {
        return next(new NopOp<T>() {
            @Override
            public void doNext(T value) {
                try {
                    func.apply(value, this);
                } catch (Throwable e) {
                    sendError(e);
                }
            }
        });
    }
    
    @Override
    public Operations<T> each(Proc1<T> func) {
        return next(new NopOp<T>() {
            @Override
            public void doNext(T value) {
                try {
                    func.apply(value);
                    sendNext(value);
                    handled();
                } catch (Throwable e) {
                    sendError(e);
                    sendCancel();
                }
            }
        });
    }
    
    @Override
    public <R> Operations<R> onFinally(Func0<Operations<R>> func) {
        return next(new FinallyOp<T, R>() {
            @Override
            public Operations<R> fin() throws Throwable {
                return func.apply();
            }
        });
    }
    @Override
    public <R> Operations<R> onFinally(Proc0 func) {
        return next(new BaseProcessor<T, R>() {

            @Override
            public void doNext(T value) {
                sendRequest(1);
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
        });
    }


    
    static class PipeImpl<H, T> extends Builder<T> implements Pipe<H, T> {
        Publisher<H> head;
        
        protected PipeImpl(Publisher<H> head, Processor<?, T> tail) {
            super(tail);
            this.head = head;
        }

        @Override
        public void onSubscribe(Subscription s) {
            System.out.println("onSubscribe:" + s);
            head.subscribe(this);
        }

        @Override
        public void onNext(H value) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public <X> Operations<X> next(Processor<T, X> processor) {
            return new PipeImpl<H, X>(head, processor);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Processor<H, T> asPipe() {
            return this;
        }
    }

    @Override
    public Operations<T> printStream(String prefix, PrintStream printStream) {
        return next(new PrintStreamOp<T>(prefix, printStream));
    }

    @Override
    public Operations<List<T>> toList() {
        return next(new ToListOp<T>());
    }

    @Override
    public Operations<T> accumulate(T initial, final Func2<T, T, T> func) {
        return next(new AccumulatorOp<T>(initial) {
            @Override
            public T calc(T value, T nextValue) throws Throwable {
                return func.apply(value, nextValue);
            }
        });
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        publisher.subscribe(s);
    }

    @Override
    public void start(int elements) {
        subscribe(new Subscriber<T>() {
            Subscription inputSubscription;
            @Override
            public void onSubscribe(Subscription s) {
                inputSubscription = s;
                inputSubscription.request(elements);
            }

            @Override
            public void onNext(T value) {
                inputSubscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            public String toString() {
                return "StartOp[" + elements + "]";
            }
        });
    }

}