package com.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.github.bckfnn.reactstreams.BaseSubscription;

/**
 * Emits the exception as an onError event.
 * 
 * @param <T>
 */
public class FromErrorOp<T> implements Publisher<T> {
    private Throwable exc;
    boolean finished;
    
    public FromErrorOp(Throwable exc) {
        this.exc = exc;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new BaseSubscription<T>(subscriber) {
            @Override
            public void request(int elements) {
                sendError(exc);
            }
        });
    }
}