package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.BaseSubscription;
import io.github.bckfnn.reactstreams.Builder;
import io.github.bckfnn.reactstreams.Operations;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.file.FileSystem;

public class RsFileSystem {
	//private Vertx vertx;
	private FileSystem fileSystem;

	public RsFileSystem(Vertx vertx) {
		//this.vertx = vertx;
		this.fileSystem = vertx.fileSystem();
	}


	public Operations<RsAsyncFile> open(String path) {
		return Builder.as(new Publisher<RsAsyncFile>() {
			@Override
			public void subscribe(Subscriber<? super RsAsyncFile> subscriber) {
				subscriber.onSubscribe(new BaseSubscription<RsAsyncFile>(subscriber) {
					boolean done = false;
					@Override
					public void request(long elements) {
						System.out.println("request:" + elements);
						super.request(elements);
						if (done) {
							return;
						}
						fileSystem.open(path, "755", false, true, true, new Handler<AsyncResult<AsyncFile>>() {
							@Override
							public void handle(AsyncResult<AsyncFile> event) {
								done = true;
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

	public Operations<RsAsyncFile> openWrite(String path) {
		return Builder.as(subscriber -> {
			subscriber.onSubscribe(new BaseSubscription<RsAsyncFile>(subscriber) {
				boolean done = false;
				@Override
				public void request(long elements) {
					System.out.println("request:" + elements);
					super.request(elements);
					if (done) {
						return;
					}
					fileSystem.open(path, new Handler<AsyncResult<AsyncFile>>() {
						@Override
						public void handle(AsyncResult<AsyncFile> event) {
							done = true;
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
		});
	}
}
