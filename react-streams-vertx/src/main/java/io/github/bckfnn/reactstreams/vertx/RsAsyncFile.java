package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.BaseSubscription;
import io.github.bckfnn.reactstreams.Stream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;

public class RsAsyncFile {
    private AsyncFile asyncFile;

    public RsAsyncFile(AsyncFile asyncFile) {
        this.asyncFile = asyncFile;
    }

    public Stream<Buffer> read() {
        return new Stream<Buffer>() {
            @Override
            public void subscribe(Subscriber<? super Buffer> subscriber) {
                subscriber.onSubscribe(new BaseSubscription<Buffer>(subscriber) {
                    boolean started = false;
                    
                    @Override
					public void cancel() {
						super.cancel();
						asyncFile.pause();
						asyncFile.handler(null);
						asyncFile.exceptionHandler(null);
						asyncFile.endHandler(null);
					}

					@Override
                    public void request(long elements) {
                        super.request(elements);
                        if (getPendingDemand() <= 0) {
                            asyncFile.pause();
                        } else {
                            asyncFile.resume();
                        }

                        if (!started) {
                            started = true;

                            asyncFile.handler(new Handler<Buffer>() {
                                @Override
                                public void handle(Buffer event) {
                                    sendNext(event);
                                }
                            });
                            asyncFile.endHandler(new Handler<Void>() {
                                @Override
                                public void handle(Void event) {
                                    sendComplete();
                                }
                            });
                            asyncFile.exceptionHandler(new Handler<Throwable>() {
                                @Override
                                public void handle(Throwable event) {
                                    sendError(event);
                                }
                            });
                        }
                    }
                });
            }
        };
    }
    
    public Processor<Buffer, Void> write() {
		return new BaseProcessor<Buffer, Void>() {
			@Override
			public void doNext(Buffer value) {
				asyncFile.write(value);
				if (!asyncFile.writeQueueFull()) {
					sendRequest();
					handled();
				} else {
					asyncFile.drainHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							sendRequest();
							handled();
						}
					});
				}
			}
		};
    }

    public Stream<Void> close() {
        return new Stream<Void>() {
            @Override
            public void subscribe(Subscriber<? super Void> subscriber) {
                subscriber.onSubscribe(new BaseSubscription<Void>(subscriber) {
                    boolean started = false;
                    @Override
                    public void request(long elements) {
                        if (started) {
                            return;
                        }
                        started = true;
                        asyncFile.close(new Handler<AsyncResult<Void>>() {
                            @Override
                            public void handle(AsyncResult<Void> event) {
                                if (event.succeeded()) {
                                    sendComplete();
                                } else {
                                    sendError(event.cause());
                                }
                            }
                        });
                    }
                });
            }
        };
    }
}
