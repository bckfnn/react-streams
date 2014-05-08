package com.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.github.bckfnn.reactstreams.ActiveSubscription;

public class FromArrayOp<T> implements Publisher<T> {
    private T[] array;
    int idx = 0;

    public FromArrayOp(T[] array) {
        this.array = array;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new ActiveSubscription<T>(subscriber) {
            private int idx = 0;

            @Override
            public boolean hasMore() {
                return idx < array.length;
            }

            @Override
            public T getOne() {
                return array[idx++];
            }
        });
    }

    public String toString() {
        return "ArraySource[" + idx + "]";
    }

}