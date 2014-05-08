package com.github.bckfnn.reactstreams.ops;

import java.util.Iterator;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.github.bckfnn.reactstreams.ActiveSubscription;

/**
 * Iter will generate a onNext event for each element in the
 * Iterable.
 * 
 * @param <T> type of the event.
 */
public class FromIteratorOp<T> implements Publisher<T> {
    private Iterator<T> iterator;

    public FromIteratorOp(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
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