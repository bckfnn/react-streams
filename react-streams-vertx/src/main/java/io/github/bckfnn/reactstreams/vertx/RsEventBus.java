package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.Stream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class RsEventBus {
	//private Vertx vertx;
	private EventBus eventBus;

	public RsEventBus(Vertx vertx) {
		//this.vertx = vertx;
		this.eventBus = vertx.eventBus();
	}


	public Stream<JsonObject> send(String address, JsonObject msg) {
		return Stream.asOne(subscription -> {
		    eventBus.send(address, msg, new Handler<AsyncResult<Message<JsonObject>>>() {
		        @Override
		        public void handle(AsyncResult<Message<JsonObject>> event) {
		            if (event.succeeded()) {
		                subscription.sendNext(event.result().body());
		                subscription.sendComplete();
		            } else {
		                subscription.sendError(event.cause());
		            }
		        }
		    });
		});
	}
}