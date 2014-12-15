package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.Stream;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class RsEventBus {
	//private Vertx vertx;
	private EventBus eventBus;

	public RsEventBus(Vertx vertx) {
		//this.vertx = vertx;
		this.eventBus = vertx.eventBus();
	}


	public Stream<JsonObject> send(String address, JsonObject msg) {
		return Stream.asOne(subscription -> {
		    eventBus.send(address, msg, new Handler<Message<JsonObject>>() {
		        @Override
		        public void handle(Message<JsonObject> event) {
		            subscription.sendNext(event.body());
		            subscription.sendComplete();
		        }
		    });
		});
	}
}