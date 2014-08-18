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
package com.github.bckfnn.reactstreams;

import java.io.PrintStream;
import java.util.List;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Interface that add operations to a publisher.
 *
 * @param <T> the type of output elements.
 */
public interface Operations<T> extends Publisher<T> {
    /**
     * Chain the publisher in this builder to the specified processor.
     * Return a new builder with the processor as the publisher.
     * @param processor the subscriber / publisher that is chained to this.
     * @param <R> type the new builder that is returned.
     * @return a new builder that wraps the processor.
     */
    public <R> Operations<R> next(Processor<T, R> processor);

    /**
     * Add a subscriber to this publisher.
     * @param subscriber the subscriber to add.
     */
    public void then(Subscriber<T> subscriber);
    
    /**
     * Add a map operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <R> type the output from the transform.
     * @return a new builder that wraps the output.
     */
    public <R> Operations<R> map(final Func1<T, R> mapFunc);

    /**
     * Add a mapMany operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <R> type the output from the transform.
     * @return a new builder that wraps the output.
     */
    public <R> Operations<R> mapMany(final Func1<T, Operations<R>> mapFunc);
    
    /**
     * Add a mapManyWith operation to the output from this publisher.
     * @param mapFunc a function that transform each value.
     * @param <R> type the output from the transform.
     * @return a new builder that wraps the output.
     */
    public <R> Operations<Tuple<T, R>> mapManyWith(final Func1<T, Operations<R>> mapFunc);
    
    /**
     * Add a <code>last</code> operation to the output from this publisher. 
     * The last operation will ignore all output except the very last element. 
     * @return a new builder that wraps the output.
     */
    public Operations<T> last();
    
    /**
     * Add a <code>skip</code> operation to the output from this publisher. 
     * The skip operation will ignore the first <code>cnt</code> elements in the output and emit the rest of the output.
     * @param cnt the number of elements to skip. 
     * @return a new builder that wraps the output.
     */
    public Operations<T> skip(int cnt);
    
    /**
     * Add a <code>take</code> operation to the output from this publisher. 
     * The take operation will output the first <code>cnt</code> elements and then 
     * cancel the this publisher. 
     * @param cnt the number of elements to take.
     * @return a new builder that wraps the output.
     */
    public Operations<T> take(int cnt);

    /**
     * Add a <code>nop</code> operation to the output from this publisher. 
     * The nop operation does nothing at all.
     * @return a new builder that wraps the output.
     */
    public Operations<T> nop();

    /**
     * Add a <code>done</code> operation to the output from this publisher. 
     * The done operation will at the first <code>request(n)</code> call cancel this publisher 
     * and emit a <code>onComplete()</code>.
     * @return a new builder that wraps the output.
     */
    public Operations<T> done();
    
    public <R> Processor<R, T> pipe();
    
    /**
     * Add a <code>filter</code> operation to the output from this publisher. 
     * The filter operation will emit all the elements where the predicate <code>func</code> 
     * return true. 
     * @param func the predicate function.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> filter(Func1<T, Boolean> func);

    /**
     * Add a <code>whenDoneValue</code> operation to the output from this publisher. 
     * The whenDoneValue operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single <code>value</code> element
     * @param value the value.
     * @param <R> the type of the output value.
     * @return a new builder that wraps the output.
     */ 
    public <R> Operations<R> whenDoneValue(R value);

    /**
     * Add a <code>whenDoneError</code> operation to the output from this publisher. 
     * The whenDoneError operation will ignore all the input elements and when the publisher 
     * is complete it will emit the <code>error</code> event.
     * @param error the error.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> whenDoneError(Throwable error);

    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will call the function.
     * @param func the function to call.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> whenDone(Proc0 func);

    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single element from the specified <code>func</code>.
     * @param func the function that return the next value.
     * @param <R> the type of the output values.
     * @return a new builder that wraps the output.
     */ 
    public <R> Operations<R> whenDone(Func0<R> func);
    
    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the elements from the specified <code>publisher</code> element.
     * @param publisher the publisher.
     * @param <R> the type of the output values.
     * @return a new builder that wraps the output.
     */ 
    public <R> Operations<R> whenDone(Publisher<R> publisher);
    
    /**
     * Add a <code>continueWithValue</code> operation to the output from this publisher. 
     * The continueWithValue operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>value</code>.
     * @param value the value.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> continueWithValue(T value);
    
    /**
     * Add a <code>continueWithError</code> operation to the output from this publisher. 
     * The continueWithError operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>error</code>.
     * @param error the error exception.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> continueWithError(Throwable error);

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will call the <code>func</code> function.
     * It is the responsibility of the <code>func</code> function to call onComplete().
     * @param func the function to call when all elements is processed.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> continueWith(Proc0 func);

    /**
     * Add a <code>continueWith</code> operation to the output from this publisher. 
     * The continueWith operation will pass through all the input elements and when 
     * the publisher is complete it will pass trough all the elements from the 
     * <code>publisher</code>.
     * @param publisher the publisher.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> continueWith(Publisher<T> publisher);
    
    /**
     * Add an <code>onEach</code> operation to the output from this publisher.
     * @param func the function to call for each input element. 
     * The input elements are not passed through.
     * @return a new builder that wraps the output.
     */
    public Operations<T> onEach(Proc2<T, BaseProcessor<T, T>> func);

    /**
     * Add an <code>each</code> operation to the output from this publisher.
     * @param func the function to call for each input element. 
     * The input elements are passed through.
     * @return a new builder that wraps the output.
     */
    public Operations<T> each(Proc1<T> func);
    
    /**
     * Add an <code>onFinally</code> operation to the output from this publisher.
     * After this publisher ends, with either onComplete() or onError(), the elements that 
     * is returned from the <code>func</code> will be emitted.
     * @param func the function to call for each input element. 
     * The input elements are not passed through.
     * @param <R> the type of the output values.
     * @return a new builder that wraps the output.
     */
    public <R> Operations<R> onFinally(Func0<Operations<R>> func);

    /**
     * Add an <code>onFinally</code> operation to the output from this publisher. 
     * After this publisher ends, with either onComplete() or onError(), the <code>func</code>
     * is called and the original end event is passed on.
     * @param <R> the type of the output values.
     * @param func the function that is called when this publisher ends.
     * @return a new Builder that wraps the output.
     */
    public <R> Operations<R> onFinally(Proc0 func);


    /**
     * Add a <code>delegate</code> operation to the output from this publisher. 
     * The delegate operation send all input events to the specified <code>subscriber</code>. 
     * @param subscriber the subscriber.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> delegate(Subscriber<T> subscriber);

    /**
     * Add a <code>toList</code> operation to the output from this publisher. 
     * The toList operation will collect all the input elements in a java.util.List and when 
     * the publisher is complete it will emit the list..
     * @return a new builder that wraps the output.
     */ 
    public Operations<List<T>> toList();

    /**
     * Add a <code>accumulate</code> operation to the output from this publisher. 
     * The accumulate operation call the <code>func</code> on each element sequentually 
     * with the the value of the previous calculation as the first parameter.
     * After each calculation the result is emitted.  
     * @param initial an initial seed value. Can be <code>null</code>.
     * @param func the accumulate function.
     * @return a new builder that wraps the output.
     */
    public Operations<T> accumulate(T initial, Func2<T, T, T> func);

    /**
     * Add a <code>printStream</code> operation to the output from this publisher. 
     * The printStream operation output debug information about all events that 
     * pass through this step to the specified <code>PrintStream</code>.
     * The output is prefixed with the <code>name</code>.
 	 * @param name the prefix in the output.
 	 * @param stream the print stream that is written to.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> printStream(String name, PrintStream stream);

    /**
     * Add a <code>start</code> operation that will send a <code>request(n)</code> up the 
     * chain of publisher and start the data flowing. 
     * @param elements the number of elements.
     */
    public void start(int elements);

}
