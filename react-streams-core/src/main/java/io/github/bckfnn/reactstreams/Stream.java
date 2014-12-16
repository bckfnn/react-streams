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
import io.github.bckfnn.reactstreams.ops.WhenDonePublisherFuncOp;
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
 * Interface that add operations to a publisher.
 *
 * @param <O> the type of output elements.
 */
public interface Stream<O> extends Publisher<O> {
    
    public static <O> Stream<O> as(Proc2<BaseSubscription<O>, Long> request, Proc1<BaseSubscription<O>> cancel) {
        return new Stream<O>() {
            @Override
            public void subscribe(Subscriber<? super O> s) {
                s.onSubscribe(new BaseSubscription<O>(s) {
                    @Override
                    public void cancel() {
                        try {
                            cancel.apply(this);
                        } catch (Throwable exc) {
                            s.onError(exc);
                        }
                    }

                    @Override
                    public void request(long elements) {
                        try {
                            request.apply(this, elements);
                        } catch (Throwable exc) {
                            s.onError(exc);
                        }
                    }
                });
            }
        };
    }
        
    
    public static <O> Stream<O> asOne(Proc1<BaseSubscription<O>> request) {
        return new Stream<O>() {
            @Override
            public void subscribe(Subscriber<? super O> s) {
                s.onSubscribe(new BaseSubscription<O>(s) {
                    boolean fired = false;
                    @Override
                    public void request(long elements) {
                        if (fired) {
                            return;
                        }
                        fired = true;
                        try {
                            request.apply(this);
                        } catch (Throwable exc) {
                            sendError(exc);
                        }
                    }
                });
            }
        };
    }

    /**
     * Create and return a new {@code Stream<O>} that emit a single value.
     * @param value the value.
     * @param <O> type of the stream.
     * @return the new stream.
     */
    public static <O> Stream<O> from(O value) {
        return new FromValueOp<O>(value);
    }
    
    /**
     * Create and return a new {@code Stream<O>} that emit a series of values.
     * @param values the values.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    @SafeVarargs
    public static <T> Stream<T> from(T... values) {
        return new FromArrayOp<>(values);
    }

    /**
     * Create and return a new {@code Stream<O>} that emit the values from a <code>Collection</code>.
     * @param collection the collection.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    public static <T> Stream<T> from(Collection<T> collection) {
        return new FromIteratorOp<T>(collection.iterator());
    }

    /**
     * Create and return a new {@code Stream<O>} that emit the supplied exception.
     * @param error the error.
     * @param <T> type of stream.
     * @return the new stream.
     */
    public static <T> Stream<T> error(Throwable error) {
        return new FromErrorOp<>(error);
    }

    /**
     * Create and return a new {@code Stream<O>} that concatenate all the values from all the supplied <code>Publishers</code>.
     * @param list the list of publishers.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Publisher<T>... list) {
        return new ConcatOp<T>(list);
    }

    /**
     * Create and return a new {@code Stream<O>} that emit all integers from 0.
     * @return the new stream.
     */
    public static Stream<Integer> counter() {
        return new CounterOp(0);
    }

    /**
     * Create and return a new {@code Stream<O>} that emit all integers from the specified <code>start</code> value.
     * @param start the start value.
     * @return the new stream.
     */
    public static Stream<Integer> counter(int start) {
        return new CounterOp(start);
    }

    /**
     * Create and return a new {@code Stream<O>} that zip two <code>Publishers</code> together by
     * emitting a <code>Tuple</code> with a value from each publisher.
     * @param p1 the first publisher.
     * @param p2 the second publisher.
     * @param <T1> type of the first publisher.
     * @param <T2> type of the second publisher.
     * @return the new stream.
     */
    public static <T1, T2> Stream<Tuple<T1, T2>> zip(Publisher<T1> p1, Publisher<T2> p2) {
        return new ZipOp<T1, T2>(p1, p2);
    }
    
    /**
     * @param <O> type of the stream.
     * @return a new pipe
     */
    public static <O> Pipe<O, O> newPipe() {
         return null;
    }
    
    /**
     * Chain the publisher in this stream to the specified subscriber.
     * Return the subscriber.
     * @param subscriber the subscriber / publisher that is chained to this.
     * @param <S> type of the new stream that is returned.
     * @return the subscriber.
     */
    default public <S extends Subscriber<? super O>> S chain(final S subscriber) {
        this.subscribe(subscriber);
        return subscriber;
    }

    /**
     * Chain the publisher in this stream to the specified processor.
     * Return a new stream with the processor as the publisher.
     * @param processor the processor that is chained to this.
     * @param <X> type the new stream that is returned.
     * @param <S> type the processor that is chained.
     * @return the processor or a Stream that wrap the processor.
     */

    default public <X , S extends BaseProcessor<? super O, X>> Stream<X> chain(final S processor) {
        this.subscribe(processor);
        return (Stream<X>) processor;
    }
    
    /*
     * map operations.
     */

    /**
     * Add a map operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <R> type the output from the transform.
     * @return a new stream that wraps the output.
     */
    default public <R> Stream<R> map(final Func1<O, R> mapFunc) {
        return chain(new MapOp<O, R>() {
            @Override
            public R map(O value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }

    /**
     * Add a mapMany operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <R> type the output from the transform.
     * @return a new stream that wraps the output.
     */
    default public <R> Stream<R> mapMany(final Func1<O, Stream<R>> mapFunc) {
        return chain(new MapManyOp<O, R>() {
            @Override
            public Stream<R> map(O value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }
    
    /**
    * Add a mapManyWith operation to the output from this publisher.
    * @param mapFunc a function that transform each value.
    * @param <R> type the output from the transform.
    * @return a new stream that wraps the output.
    */
    default public <R> Stream<Tuple<O, R>> mapManyWith(final Func1<O, Stream<R>> mapFunc) {
        return chain(new MapManyWithOp<O, R>() {
            @Override
            public Stream<R> map(O value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }
   

    /*
     * Filter operations.
     */
    
    /**
     * Add a <code>last</code> operation to the output from this publisher. 
     * The last operation will ignore all output except the very last element. 
     * @return a new stream that wraps the output.
     */
    default public Stream<O> last() {
        return chain(new LastOp<O>());
    }

    /**
     * Add a <code>skip</code> operation to the output from this publisher. 
     * The skip operation will ignore the first <code>cnt</code> elements in the output and emit the rest of the output.
     * @param cnt the number of elements to skip. 
     * @return a new stream that wraps the output.
     */
    default public Stream<O> skip(int cnt) {
        return chain(new SkipOp<O>(cnt));
    }

    /**
     * Add a <code>take</code> operation to the output from this publisher. 
     * The take operation will output the first <code>cnt</code> elements and then 
     * cancel the this publisher. 
     * @param cnt the number of elements to take.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> take(int cnt) {
        return chain(new TakeOp<O>(cnt));
    }

    /**
     * Add a <code>nop</code> operation to the output from this publisher. 
     * The nop operation does nothing at all.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> nop() {
        return chain(new NopOp<O>());
    }

    /**
     * Return this operation as a pipe.
     * @param <R> the type of the input values to the pipe.
     * @return the operation as a pipe.
     */
    default public <R> Processor<R, O> asPipe() {
        return null;
    }

    
    /**
     * Add a <code>done</code> operation to the output from this publisher. 
     * The done operation will at the first <code>request(n)</code> call cancel this publisher 
     * and emit a <code>onComplete()</code>.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> done() {
        return chain(new DoneOp<O>());
    }

    /**
     * Add a <code>filter</code> operation to the output from this publisher. 
     * The filter operation will emit all the elements where the predicate <code>func</code> 
     * return true. 
     * @param func the predicate function.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> filter(Func1<O, Boolean> func) {
        return chain(new FilterOp<O>() {
            @Override
            public boolean check(O value) throws Throwable {
                return func.apply(value);
            }
        });
    }

    /**
     * Add a <code>whenDoneValue</code> operation to the output from this publisher. 
     * The whenDoneValue operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single <code>value</code> element
     * @param value the value.
     * @param <R> the type of the output value.
     * @return a new stream that wraps the output.
     */ 
    default public <R> Stream<R> whenDoneValue(R value) {
        return chain(new WhenDoneValueOp<O, R>(value));
    }

    /**
     * Add a <code>whenDoneError</code> operation to the output from this publisher. 
     * The whenDoneError operation will ignore all the input elements and when the publisher 
     * is complete it will emit the <code>error</code> event.
     * @param error the error.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> whenDoneError(Throwable error) {
        return chain(new WhenDoneErrorOp<O>(error));
    }

    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will call the function.
     * @param func the function to call.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> whenDone(Proc0 func) {
        return chain(new WhenDoneProcOp<O>(func));
    }


    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single element from the specified <code>func</code>.
     * @param func the function that return the next value.
     * @param <R> the type of the output values.
     * @return a new stream that wraps the output.
     */ 
    default public <R> Stream<R> whenDone(Func0<R> func) {
        return chain(new WhenDoneFuncOp<O, R>(func));
    }
    
    default public <R> Stream<R> whenDoneFunc(Func0<Stream<R>> func) {
        return chain(new WhenDonePublisherFuncOp<O, R>(func));
    }

    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the elements from the specified <code>publisher</code> element.
     * @param publisher the publisher.
     * @param <R> the type of the output values.
     * @return a new stream that wraps the output.
     */ 
    default public <R> Stream<R> whenDone(Publisher<R> publisher) {
        return chain(new WhenDonePublisherOp<O, R>(publisher));
    }

    /**
     * Add a <code>continueWithValue</code> operation to the output from this publisher. 
     * The continueWithValue operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>value</code>.
     * @param value the value.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> continueWithValue(O value) {
        return null;
    }

    /**
     * Add a <code>continueWithError</code> operation to the output from this publisher. 
     * The continueWithError operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>error</code>.
     * @param error the error exception.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> continueWithError(Throwable error) {
        return chain(new ContinueWithErrorOp<O>(error));
    }

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will call the <code>func</code> function.
     * It is the responsibility of the <code>func</code> function to call onComplete().
     * @param func the function to call when all elements is processed.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> continueWith(Proc0 func) {
        return chain(new ContinueWithProcOp<O>(func));
    }

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will pass trough all the elements from the 
     * <code>publisher</code>.
     * @param publisher the publisher.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> continueWith(Publisher<O> publisher) {
        return null;
    }

    /**
     * Add a <code>delegate</code> operation to the output from this publisher. 
     * The delegate operation send all input events to the specified <code>subscriber</code>. 
     * @param subscriber the subscriber.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> delegate(Subscriber<O> subscriber) {
        return chain(new DelegateOp<O>(subscriber));
    }

    /**
     * Add an <code>onEach</code> operation to the output from this publisher.
     * @param func the function to call for each input element. 
     * The input elements are not passed through.
     * @return a new stream that wraps the output.
     */
    default  public Stream<O> onEach(Proc2<O, BaseProcessor<O, O>> func) {
        return chain(new NopOp<O>() {
            @Override
            public void doNext(O value) {
                try {
                    func.apply(value, this);
                } catch (Throwable e) {
                    sendError(e);
                }
            }
        });
    }

    /**
     * Add an <code>each</code> operation to the output from this publisher.
     * @param func the function to call for each input element. 
     * The input elements are passed through.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> each(Proc1<O> func) {
        return chain(new NopOp<O>() {
            @Override
            public void doNext(O value) {
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

    /**
     * Add an <code>onFinally</code> operation to the output from this publisher.
     * After this publisher ends, with either onComplete() or onError(), the elements that 
     * is returned from the <code>func</code> will be emitted.
     * @param func the function to call for each input element. 
     * The input elements are not passed through.
     * @param <R> the type of the output values.
     * @return a new stream that wraps the output.
     */
    default public <R> Stream<R> onFinally(Func0<Stream<R>> func) {
        return chain(new FinallyOp<O, R>() {
            @Override
            public Stream<R> fin() throws Throwable {
                return func.apply();
            }
        });
    }

    /**
     * Add an <code>onFinally</code> operation to the output from this publisher. 
     * After this publisher ends, with either onComplete() or onError(), the <code>func</code>
     * is called and the original end event is passed on.
     * @param <R> the type of the output values.
     * @param func the function that is called when this publisher ends.
     * @return a new stream that wraps the output.
     */
    default public <R> Stream<R> onFinally(Proc0 func) {
        return chain(new BaseProcessor<O, R>() {

            @Override
            public void doNext(O value) {
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

    /**
     * Add a <code>printStream</code> operation to the output from this publisher. 
     * The printStream operation output debug information about all events that 
     * pass through this step to the specified <code>PrintStream</code>.
     * The output is prefixed with the <code>name</code>.
     * @param prefix the prefix in the output.
     * @param printStream the print stream that is written to.
     * @return a new stream that wraps the output.
     */ 
    default public Stream<O> printStream(String prefix, PrintStream printStream) {
        return chain(new PrintStreamOp<O>(prefix, printStream));
    }

    /**
     * Add a <code>toList</code> operation to the output from this publisher. 
     * The toList operation will collect all the input elements in a java.util.List and when 
     * the publisher is complete it will emit the list..
     * @return a new stream that wraps the output.
     */ 
    default public Stream<List<O>> toList() {
        return chain(new ToListOp<O>());
    }

    /**
     * Add a <code>accumulate</code> operation to the output from this publisher. 
     * The accumulate operation call the <code>func</code> on each element sequentually 
     * with the the value of the previous calculation as the first parameter.
     * After each calculation the result is emitted.  
     * @param initial an initial seed value. Can be <code>null</code>.
     * @param func the accumulate function.
     * @return a new stream that wraps the output.
     */
    default public Stream<O> accumulate(O initial, final Func2<O, O, O> func) {
        return chain(new AccumulatorOp<O>(initial) {
            @Override
            public O calc(O value, O nextValue) throws Throwable {
                return func.apply(value, nextValue);
            }
        });
    }

    /**
     * Add a <code>start</code> operation that will send a <code>request(n)</code> up the 
     * chain of publisher and start the data flowing. 
     * @param elements the number of elements.
     */
    default public void start(int elements) {
        subscribe(new Subscriber<O>() {
            Subscription inputSubscription;
            @Override
            public void onSubscribe(Subscription s) {
                inputSubscription = s;
                inputSubscription.request(elements);
            }

            @Override
            public void onNext(O value) {
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
