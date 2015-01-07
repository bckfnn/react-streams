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

import io.github.bckfnn.reactstreams.ops.Filters;
import io.github.bckfnn.reactstreams.ops.Flows;
import io.github.bckfnn.reactstreams.ops.Streams;
import io.github.bckfnn.reactstreams.ops.Transforms;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Interface that add operations to a publisher.
 *
 * @param <T> the type of output elements.
 */
public interface Stream<T> extends Publisher<T> {

    /**
     * Create a new stream based on the responses from the <code>request</code> and <code>cancel</code> functions.
     * @param request A function called for each invocation of {@link Subscription#request(long)}.
     * @param cancel A function called for the invocation of {@link Subscription#cancel()}
     * @param <T> type of the stream.
     * @return a new {@link Stream}
     */
    public static <T> Stream<T> as(Proc2<BaseSubscription<T>, Long> request, Proc1<BaseSubscription<T>> cancel) {
        return new Stream<T>() {
            @Override
            public void subscribe(Subscriber<? super T> s) {
                s.onSubscribe(new BaseSubscription<T>(s) {
                    @Override
                    public void cancel() {
                        try {
                            cancel.apply(this);
                        } catch (Throwable exc) {
                            sendError(exc);
                        }
                    }

                    @Override
                    public void request(long elements) {
                        try {
                            request.apply(this, elements);
                        } catch (Throwable exc) {
                            sendError(exc);
                        }
                    }
                });
            }
        };
    }

    /**
     * Return a new Stream that emit elements from the <code>request</code> function.
     * @param request A function, that is only called once and which can emit elements to the stream
     * @param <T> type of the stream.
     * @return a new {@link Stream}
     */
    public static <T> Stream<T> asOne(Proc1<BaseSubscription<T>> request) {
        return new Stream<T>() {
            @Override
            public void subscribe(Subscriber<? super T> s) {
                s.onSubscribe(new BaseSubscription<T>(s) {
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
     * Create and return a new {@code Stream<T>} that emit a single value.
     * @param value the value.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    public static <T> Stream<T> from(T value) {
        return new Streams.Value<T>(value);
    }

    /**
     * Create and return a new {@code Stream<T>} that emit a series of values.
     * @param values the values.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    @SafeVarargs
    public static <T> Stream<T> from(T... values) {
        return new Streams.Array<>(values);
    }

    /**
     * Create and return a new {@code Stream<T>} that emit the values from a <code>Collection</code>.
     * @param collection the collection.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    public static <T> Stream<T> from(Collection<T> collection) {
        return new Streams.Iter<T>(collection.iterator());
    }

    /**
     * Create and return a new {@code Stream<T>} that emit a <code>onComplete</code> event.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    public static <T> Stream<T> complete() {
        return new Streams.Complete<T>();
    }

    /**
     * Create and return a new {@code Stream<T>} that emit the supplied exception.
     * @param error the error.
     * @param <T> type of stream.
     * @return the new stream.
     */
    public static <T> Stream<T> error(Throwable error) {
        return new Streams.Error<>(error);
    }

    /**
     * Create and return a new {@code Stream<T>} that concatenate all the values from all the supplied <code>Publishers</code>.
     * @param list the list of publishers.
     * @param <T> type of the stream.
     * @return the new stream.
     */
    @SafeVarargs
    public static <T> Stream<T> concat(Publisher<T>... list) {
        return new Transforms.Concat<T>(list);
    }

    /**
     * Create and return a new {@code Stream<T>} that emit all integers from 0.
     * @return the new stream.
     */
    public static Stream<Integer> counter() {
        return new Streams.Counter(0);
    }

    /**
     * Create and return a new {@code Stream<T>} that emit all integers from the specified <code>start</code> value.
     * @param start the start value.
     * @return the new stream.
     */
    public static Stream<Integer> counter(int start) {
        return new Streams.Counter(start);
    }

    /**
     * Create and return a new {@code Stream<T>} that zip two <code>Publishers</code> together by
     * emitting a <code>Tuple</code> with a value from each publisher.
     * @param p1 the first publisher.
     * @param p2 the second publisher.
     * @param <T1> type of the first publisher.
     * @param <T2> type of the second publisher.
     * @return the new stream.
     */
    public static <T1, T2> Stream<Tuple<T1, T2>> zip(Publisher<T1> p1, Publisher<T2> p2) {
        return new Transforms.Zip<T1, T2>(p1, p2);
    }

    /**
     * Creates a new Pipe around the Stream returned from the function.
     * A pipe keep track of the head of the sequence.
     * @param func the function that build the stream inside the pipe.  
     * @param <I> type of input to the pipe.
     * @param <T> type of output from the pipe.
     * @return a new pipe
     */
    public static <I, T> Pipe<I, T> asPipe(Func1<Stream<I>, Stream<T>> func) {
        BaseProcessor<I, I> head = new Filters.Nop<I>();
        Stream<T> tail = null;
        try {
            tail = func.apply(head);
        } catch (Throwable e) {
            tail = Stream.error(e);
        }
        return new Flows.PipeX<I, T>(head, tail);
    }

    /**
     * Chain the publisher in this stream to the specified subscriber.
     * Return the subscriber.
     * @param subscriber the subscriber / publisher that is chained to this.
     * @param <S> type of the new stream that is returned.
     * @return the subscriber.
     */
    default public <S extends Subscriber<? super T>> S chain(final S subscriber) {
        this.subscribe(subscriber);
        return subscriber;
    }

    /**
     * Chain the publisher in this stream to the specified processor.
     * Return a new stream with the processor as the publisher.
     * @param processor the processor that is chained to this.
     * @param <X> type of the new stream that is returned.
     * @param <S> type of the processor that is chained.
     * @return the processor or a {@code Stream<X>} that wrap the processor.
     */

    default public <X , S extends BaseProcessor<? super T, X>> Stream<X> chain(final S processor) {
        this.subscribe(processor);
        return (Stream<X>) processor;
    }

    /*
     * map operations.
     */

    /**
     * Add a map operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <O> type of the output from the transform.
     * @return a new {@code Stream<O>}.
     */
    default public <O> Stream<O> map(final Func1<T, O> mapFunc) {
        return chain(new Transforms.Map<T, O>(mapFunc));
    }

    /**
     * Add a mapMany operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <O> type of the output from the transform.
     * @return a new {@code Stream<O>}.
     */
    default public <O> Stream<O> mapMany(final Func1<T, Stream<O>> mapFunc) {
        return chain(new Transforms.MapMany<T, O>(mapFunc));
    }

    /**
     * Add a mapManyWith operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <O> type of the output from the transform.
     * @return a new {@code Stream<Tuple<O, O>>} where each tuple contains the input value and mapped value.
     */
    default public <O> Stream<Tuple<T, O>> mapManyWith(final Func1<T, Stream<O>> mapFunc) {
        return chain(new Transforms.MapManyWith<T, O>(mapFunc));
    }


    /*
     * Filter operations.
     */

    /**
     * Add a <code>last</code> operation to the output from this publisher. 
     * The last operation will ignore all output except the very last element. 
     * @return a new {@code Stream<T>} with a single element.
     */
    default public Stream<T> last() {
        return chain(new Filters.Last<T>());
    }

    /**
     * Add a <code>skip</code> operation to the output from this publisher. 
     * The skip operation will ignore the first <code>cnt</code> elements in the output and emit the rest of the output.
     * @param cnt the number of elements to skip. 
     * @return a new {@code Stream<T>}
     */
    default public Stream<T> skip(int cnt) {
        return chain(new Filters.Skip<T>(cnt));
    }

    /**
     * Add a <code>take</code> operation to the output from this publisher. 
     * The take operation will output the first <code>cnt</code> elements and then 
     * cancel the this publisher. 
     * @param cnt the number of elements to take.
     * @return a new {@code Stream<T>}
     */
    default public Stream<T> take(int cnt) {
        return chain(new Filters.Take<T>(cnt));
    }

    /**
     * Add a <code>nop</code> operation to the output from this publisher. 
     * The nop operation does nothing at all.
     * @return a new {@code Stream<T>}
     */
    default public Stream<T> nop() {
        return chain(new Filters.Nop<T>());
    }

    /**
     * Add a <code>ignore</code> operation to the output from this publisher. 
     * The ignore operation ignore all values and send only {@link Subscriber#onComplete()} and {@link Subscriber#onError(Throwable)}.
     * @return a new {@code Stream<T>}
     */
    default public Stream<T> ignore() {
        return chain(new Filters.Ignore<T>());
    }

    /**
     * Add a <code>done</code> operation to the output from this publisher. 
     * The done operation will at the first <code>request(n)</code> call cancel this publisher 
     * and emit a <code>onComplete()</code>.
     *@return a new {@link Stream}
     */
    default public Stream<T> done() {
        return chain(new Filters.Done<T>());
    }

    /**
     * Add a <code>filter</code> operation to the output from this publisher. 
     * The filter operation will emit all the elements where the predicate <code>func</code> 
     * return true. 
     * @param func the predicate function.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> filter(Func1<T, Boolean> func) {
        return chain(new Filters.Filter<T>(func));
    }

    /**
     * Add a <code>whenDoneValue</code> operation to the output from this publisher. 
     * The whenDoneValue operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single <code>value</code> element
     * @param value the value.
     * @param <O> the type of the output value.
     * @return a new {@link Stream}
     */ 
    default public <O> Stream<O> whenDoneFrom(O value) {
        return whenDone(() -> Stream.from(value));
    }

    /**
     * Add a <code>whenDoneError</code> operation to the output from this publisher. 
     * The whenDoneError operation will ignore all the input elements and when the publisher 
     * is complete it will emit the <code>error</code> event.
     * @param error the error.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> whenDoneError(Throwable error) {
        return whenDone(() -> Stream.error(error));
    }



    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single element from the specified <code>func</code>.
     * @param func the function that return the next value.
     * @param <O> the type of the output values.
     * @return a new {@link Stream}
     */ 
    default public <O> Stream<O> whenDone(Func0<Stream<O>> func) {
        return chain(new Flows.WhenDone<T, O>(func));
    }


    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the elements from the specified <code>publisher</code> element.
     * @param publisher the publisher.
     * @param <O> the type of the output values.
     * @return a new {@link Stream}
     */ 
    default public <O> Stream<O> whenDone(Stream<O> publisher) {
        return whenDone(() -> publisher);
    }

    /**
     * Add a <code>continueWithValue</code> operation to the output from this publisher. 
     * The continueWithValue operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>value</code>.
     * @param value the value.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> continueWithValue(T value) {
        return continueWith(() -> Stream.from(value));
    }

    /**
     * Add a <code>continueWithError</code> operation to the output from this publisher. 
     * The continueWithError operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>error</code>.
     * @param error the error exception.
     * @return a new {@link Stream}
     */
    default public Stream<T> continueWithError(Throwable error) {
        return continueWith(() -> Stream.error(error));
    }

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will call the <code>func</code> function.
     * It is the responsibility of the <code>func</code> function to call onComplete().
     * @param func the function to call when all elements is processed.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> continueWith(Func0<Stream<T>> func) {
        return chain(new Flows.ContinueWith<T>(func));
    }

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will pass trough all the elements from the 
     * <code>publisher</code>.
     * @param stream the stream.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> continueWith(Stream<T> stream) {
        return continueWith(() -> stream);
    }

    /**
     * Add a <code>delegate</code> operation to the output from this publisher. 
     * The delegate operation send all input events to the specified <code>subscriber</code>. 
     * @param subscriber the subscriber.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> delegate(Subscriber<T> subscriber) {
        return chain(new Flows.Delegate<T>(subscriber));
    }

    /**
     * Add an <code>onEach</code> operation to the output from this publisher.
     * @param func the function to call for each input element. 
     * The input elements are not passed through.
     * @return a new {@link Stream}
     */
    default  public Stream<T> each(Proc2<BaseProcessor<T, T>, T> func) {
        return chain(new Filters.Nop<T>() {
            @Override
            public void doNext(T value) {
                try {
                    func.apply(this, value);
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
     * @return a new {@link Stream}
     */
    default public Stream<T> onEach(Proc1<T> func) {
        return chain(new Filters.Nop<T>() {
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

    /**
     * Add a <code>onComplete</code> operation to the output from this publisher. 
     * The onComplete operation will ignore all the input elements and when the publisher 
     * is complete it will call the function.
     * @param func the function to call.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> onComplete(Proc0 func) {
        return chain(new Filters.Nop<T>() {
            @Override
            public void onComplete() {
                try {
                    func.apply();
                    super.onComplete();
                } catch (Throwable e) {
                    sendError(e);
                    sendCancel();
                }
            }
        });
    }

    
    /**
     * Add a <code>onError</code> operation to the output from this publisher. 
     * The onError operation will pass through all the input elements and if the publisher 
     * is emit an onError event, it will call the function.
     * @param func the function to call.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> onError(Proc1<Throwable> func) {
        return chain(new Filters.Nop<T>() {
            @Override
            public void onError(Throwable exc) {
                try {
                    func.apply(exc);
                    super.onError(exc);
                } catch (Throwable e) {
                    sendError(e);
                    sendCancel();
                }
            }
        });
    }

    /**
     * Add an <code>onFinally</code> operation to the output from this publisher. 
     * After this publisher ends, with either onComplete() or onError(), the <code>func</code>
     * is called and the original end event is passed on.
     * @param func the function that is called when this publisher ends.
     * @return a new {@link Stream}
     */
    default public Stream<T> onFinally(Proc0 func) {
        return chain(new Flows.Finally<T>(func));
    }

    /**
     * Add a <code>print</code> operation to the output from this publisher. 
     * The print operation output debug information about all events that 
     * pass through this step to the specified <code>PrintStream</code>.
     * The output is prefixed with the <code>name</code>.
     * @param prefix the prefix in the output.
     * @param printStream the print stream that is written to.
     * @return a new {@link Stream}
     */ 
    default public Stream<T> print(String prefix, PrintStream printStream) {
        return chain(new Filters.Print<T>(prefix, printStream));
    }

    /**
     * Add a <code>toList</code> operation to the output from this publisher. 
     * The toList operation will collect all the input elements in a java.util.List and when 
     * the publisher is complete it will emit the list..
     * @return a new {@link Stream}
     */ 
    default public Stream<List<T>> toList() {
        return chain(new Transforms.ToList<T>());
    }

    /**
     * Add a <code>accumulate</code> operation to the output from this publisher. 
     * The accumulate operation call the <code>func</code> on each element sequentually 
     * with the the value of the previous calculation as the first parameter.
     * After each calculation the result is emitted.  
     * @param initial an initial seed value. Can be <code>null</code>.
     * @param func the accumulate function.
     * @return a new {@link Stream}
     */
    default public Stream<T> accumulate(T initial, final Func2<T, T, T> func) {
        return chain(new Filters.Accumulator<T>(initial, func));
    }

    /**
     * Add a <code>start</code> operation that will send a <code>request(n)</code> up the 
     * chain of publisher and start the data flowing. 
     * @param elements the number of elements.
     */
    default public void start(long elements) {
        subscribe(new Streams.Start<T>(elements));
    }
}
