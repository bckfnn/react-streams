package com.github.bckfnn.reactstreams.ops;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.Operations;
import com.github.bckfnn.reactstreams.BaseProcessor;

public abstract class MapManyOp<T, R> extends BaseProcessor<T, R> {
    List<Publisher<R>> children = new ArrayList<Publisher<R>>();
    boolean completed = false;
    int count = 0;
    
    public abstract Operations<R> map(T value) throws Throwable;

    
    @Override
	public void onSubscribe(Subscription s) {
		// TODO Auto-generated method stub
		super.onSubscribe(s);
	}


	@Override
    public void doNext(T value) {
        try {
            final Publisher<R> child = map(value);
            children.add(child);
            count++;
            drain();
        } catch (Throwable exc) {
            onError(exc);
        }
    }

    @Override
    public void onComplete() {
        if (count == 0) {
            sendComplete();
        } else {
            completed = true;
        }
    }

    private void drain() {
        if (children.size() == 0) {
            return;
        }
        final Publisher<R> child = children.remove(0);
        child.subscribe(new Subscriber<R>() {
        	Subscription childSubscription;
            @Override
            public void onSubscribe(Subscription s) {
            	childSubscription = s;
                s.request(1);
            }
            
            @Override
            public void onNext(R value) {
                sendNext(value);
                childSubscription.request(1);
            }

            @Override
            public void onComplete() {
                count--;
                if (count == 0 && completed) {
                    sendComplete();
                } else {
                	sendRequest(1);
                    drain();
                }
            }

            @Override
            public void onError(Throwable exc) {
                sendError(exc);
            }
        });
    }
    
    @Override
    public String toString() {
        return "MapMany[]";
    }
}