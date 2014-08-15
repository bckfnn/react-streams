package com.github.bckfnn.reactstreams.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.github.bckfnn.reactstreams.BaseSubscription;
import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Operations;

public class RsEventBus {
	//private Vertx vertx;
	private EventBus eventBus;

	public RsEventBus(Vertx vertx) {
		//this.vertx = vertx;
		this.eventBus = vertx.eventBus();
	}


	public Operations<JsonObject> send(String address, JsonObject msg) {
		return Builder.as(subscriber -> {
			subscriber.onSubscribe(new BaseSubscription<JsonObject>(subscriber) {
				boolean done = false;
				@Override
				public void request(int elements) {
					System.out.println("request:" + elements);
					super.request(elements);
					if (done) {
						return;
					}
					eventBus.send(address, msg, new Handler<Message<JsonObject>>() {
						@Override
						public void handle(Message<JsonObject> event) {
							sendNext(event.body());
							sendComplete();
						}
					});
				}
			});
		});
	}
}