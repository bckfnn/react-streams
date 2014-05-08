package com.github.bckfnn.reactstreams;

import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;


public interface Operations<T> extends Publisher<T> {
    public <R> Operations<R> then(Processor<T, R> source);
    
    public void then(Subscriber<T> target);
    
    /*
     * Map operations
     */
    
    public <R> Operations<R> map(final Func1<T, R> mapFunc);
    
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
