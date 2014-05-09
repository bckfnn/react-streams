package com.github.bckfnn.reactstreams.vertx;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

import com.github.bckfnn.reactstreams.BaseSubscription;

public class SubscriptionAsyncResultHandler<T> implements Handler<AsyncResult<T>> {
	private BaseSubscription<T> subscription;
	
	public SubscriptionAsyncResultHandler(BaseSubscription<T> subscription) {
		this.subscription = subscription;
	}

	@Override
	public void handle(AsyncResult<T> event) {
		if (event.succeeded()) {
			subscription.sendNext(event.result());
			subscription.sendComplete();
		} else {
			subscription.sendError(event.cause());
		}
	}
}
