package com.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.github.bckfnn.reactstreams.BaseSubscription;

/**
 * Single will generate a single onNext event for the element.
 * 
 * @param <T> type of the event.
 */
public class FromValueOp<T> implements Publisher<T> {
    private T value;
    private boolean finished = false;

    public FromValueOp(T value) {
        this.value = value;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new BaseSubscription<T>(subscriber) {
            @Override
            public void request(int elements) {
                if (!finished) {
                    finished = true;
                    sendNext(value);
                    sendComplete();
                }
            }
        });
    }
}