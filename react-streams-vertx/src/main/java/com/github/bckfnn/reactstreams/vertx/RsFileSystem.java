package com.github.bckfnn.reactstreams.vertx;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.file.FileSystem;

import com.github.bckfnn.reactstreams.BaseSubscription;
import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Operations;

public class RsFileSystem {
	private Vertx vertx;
	private FileSystem fileSystem;
	
	public RsFileSystem(Vertx vertx) {
		this.vertx = vertx;
		this.fileSystem = vertx.fileSystem();
	}
	
	public Operations<RsAsyncFile> open(String path) {
		return Builder.as(new Publisher<RsAsyncFile>() {
			@Override
			public void subscribe(Subscriber<RsAsyncFile> subscriber) {
				subscriber.onSubscribe(new BaseSubscription<RsAsyncFile>(subscriber) {
					@Override
					public void request(int elements) {
						super.request(elements);
						fileSystem.open(path, new Handler<AsyncResult<AsyncFile>>() {
							@Override
							public void handle(AsyncResult<AsyncFile> event) {
								if (event.succeeded()) {
									sendNext(new RsAsyncFile(event.result()));
									sendComplete();
								} else {
									sendError(event.cause());
								}								
							}
						});
					}
				});
			}
		});
	}
}
