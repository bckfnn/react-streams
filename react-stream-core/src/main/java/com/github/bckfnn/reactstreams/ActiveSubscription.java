package com.github.bckfnn.reactstreams;

import org.reactivestreams.Subscriber;

public abstract class ActiveSubscription<T> extends BaseSubscription<T> {
	private boolean recursion = false;

	public ActiveSubscription(Subscriber<T> subscriber) {
		super(subscriber);
	}

	public abstract boolean hasMore();

	public abstract T getOne();

	@Override
	public void request(int elements) {
		super.request(elements);
		if (recursion) {
			return;
		}
		recursion = true;
		while (getPending() > 0 && !isCancelled() && hasMore()) {
			sendNext(getOne());
			
		}
		if (!hasMore() && !isCancelled()) {
			sendComplete();
		}
        recursion = false;
	}
}
