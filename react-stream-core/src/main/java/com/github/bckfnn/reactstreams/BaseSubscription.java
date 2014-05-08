package com.github.bckfnn.reactstreams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class BaseSubscription<T> implements Subscription {
	private Subscriber<T> subscriber;
	private boolean cancelled = false;
	private int pending;
	
	public BaseSubscription(Subscriber<T> subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public void request(int elements) {
		pending += elements;
	}

	protected boolean isCancelled() {
		return cancelled;
	}

	public int getPending() {
		return pending;
	}

	public void sendNext(T value) {
		subscriber.onNext(value);
		pending--;
	}
	
	public void sendComplete() {
		subscriber.onComplete();
		pending = 0;
	}
	
	public void sendError(Throwable t) {
		subscriber.onError(t);
		pending = 0;
	}
	
	public String toString() {
		return "Subscription to " + subscriber;
	}
}
