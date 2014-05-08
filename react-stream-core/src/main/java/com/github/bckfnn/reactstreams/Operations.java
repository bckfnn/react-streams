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
    
    /*
     * Filter operations.
     */

    public Operations<T> last();
    
    public Operations<T> skip(int cnt);
    
    public Operations<T> take(int cnt);

    public Operations<T> nop();
    
    public Operations<T> done();
    
    public Operations<T> filter(Func1<T, Boolean> func);

    /*
     * Flow operations.
     */
    public <R> Operations<R> whenDone(R value);
    
    public <R> Operations<R> whenDone(Operations<R> observable);
    
    public Operations<T> delegate(Subscriber<T> x);

    public Operations<T> continueWithError(Throwable error);
    
    public Operations<T> stdout(String name);

    /*
     * Transform
     */

    public Operations<List<T>> toList();

    public Operations<T> accumulate(T initial, Func2<T, T, T> func);

    public void start(int elements);
}
