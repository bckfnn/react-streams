package com.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class DelegateOp<R> extends BaseProcessor<R, R> {
    private Subscriber<R> target;
    int delegateQueue = 0;
    
    public DelegateOp(Subscriber<R> target) {
        this.target = target;
    }
    
    
	@Override
	public void subscribe(Subscriber<R> subscriber) {
		super.subscribe(subscriber);

        target.onSubscribe(new Subscription() {
            @Override
            public void request(int n) {
            	delegateQueue += n;
            	int min = 1;
                sendRequest(min);
            }

            @Override
            public void cancel() {
                sendCancel();
            }
        });
	}

    @Override
    public void doNext(R value) {
    	sendNext(value);
        target.onNext(value);
    }

    @Override
    public void onComplete() {
    	sendComplete();
        target.onComplete();
    }

    @Override
    public void onError(Throwable error) {
    	sendError(error);
        target.onError(error);
    }

    public String toString() {
        return "Delegate[" + "" + "]";
    }

}