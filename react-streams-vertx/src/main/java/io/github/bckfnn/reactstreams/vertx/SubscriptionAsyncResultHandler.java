package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.BaseSubscription;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

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
