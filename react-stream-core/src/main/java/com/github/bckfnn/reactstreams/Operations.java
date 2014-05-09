package com.github.bckfnn.reactstreams;

import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Interface that add operations to a publisher.
 *
 * @param <T> 
 */
public interface Operations<T> extends Publisher<T> {
    /**
     * Chain the publisher in this builder to the specified processor.
     * Return a new builder with the processor as the publisher.
     * @param processor the subscriber / publisher that is chained to this.
     * @param <R> type the new builder that is returned.
     * @return a new builder that wraps the processor.
     */
    public <R> Operations<R> then(Processor<T, R> source);

    /**
     * Add a subscriber to this publisher.
     * @param subscriber the subscriber to add.
     */
    public void then(Subscriber<T> target);
    
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
     * Add a <code>last</code> operation to the output from this publisher. 
     * The last operation will ignore all output except the very last element. 
     * @return a new builder that wraps the output.
     */
    public Operations<T> last();
    
    /**
     * Add a <code>skip</code> operation to the output from this publisher. 
     * The skip operation will ignore the first <code>cnt</code> elements in the output and emit the rest of the output. 
     * @return a new builder that wraps the output.
     */
    public Operations<T> skip(int cnt);
    
    /**
     * Add a <code>take</code> operation to the output from this publisher. 
     * The take operation will output the first <code>cnt</code> elements and then 
     * cancel the this publisher. 
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
    
    /**
     * Add a <code>filter</code> operation to the output from this publisher. 
     * The filter operation will emit all the elements where the predicate <code>func</code> 
     * return true. 
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> filter(Func1<T, Boolean> func);

    /**
     * Add a <code>whenDone</code> operation to the output from this publisher. 
     * The whenDone operation will ignore all the input elements and when the publisher 
     * is complete it will emit the single <code>value</code> element
     * @param value the value.
     * @param <R> the type of the output value.
     * @return a new builder that wraps the output.
     */ 
    public <R> Operations<R> whenDone(R value);
    
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
     * Add a <code>continueWithError</code> operation to the output from this publisher. 
     * The continueWithError operation will pass through all the input elements and when 
     * the publisher is complete it will emit the <code>error</code>.
     * @param error the error exception.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> continueWithError(Throwable error);
    
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
     * Add a <code>stdout</code> operation to the output from this publisher. 
     * The stdout operation output debug information about all events that pass through this step
     * to the standard output. The output is prefixed with the <code>name</code>.
 	 * @param name the prefix in the output.
     * @return a new builder that wraps the output.
     */ 
    public Operations<T> stdout(String name);

    /**
     * Add a <code>start</code> operation that will send a <code>request(n)</code> up the 
     * chain of publisher and start the data flowing. 
     * @param elements the number of elements.
     */
    public void start(int elements);
}
