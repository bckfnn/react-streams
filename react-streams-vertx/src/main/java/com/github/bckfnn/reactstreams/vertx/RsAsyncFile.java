package com.github.bckfnn.reactstreams.vertx;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.AsyncFile;

import com.github.bckfnn.reactstreams.BaseProcessor;
import com.github.bckfnn.reactstreams.BaseSubscription;
import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Operations;
import com.github.bckfnn.reactstreams.Processor;

public class RsAsyncFile {
    private AsyncFile asyncFile;

    public RsAsyncFile(AsyncFile asyncFile) {
        this.asyncFile = asyncFile;
    }

    public Operations<Buffer> read() {
        return Builder.as(new Publisher<Buffer>() {
            @Override
            public void subscribe(Subscriber<Buffer> subscriber) {
                subscriber.onSubscribe(new BaseSubscription<Buffer>(subscriber) {
                    boolean started = false;
                    
                    @Override
					public void cancel() {
						super.cancel();
						asyncFile.pause();
						asyncFile.dataHandler(null);
						asyncFile.exceptionHandler(null);
						asyncFile.endHandler(null);
					}

					@Override
                    public void request(int elements) {
                        super.request(elements);
                        if (getPending() <= 0) {
                            asyncFile.pause();
                        } else {
                            asyncFile.resume();
                        }

                        if (!started) {
                            started = true;

                            asyncFile.dataHandler(new Handler<Buffer>() {
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
        });
    }
    
    public Processor<Buffer, Void> write() {
		return new BaseProcessor<Buffer, Void>() {
			@Override
			public void doNext(Buffer value) {
				asyncFile.write(value);
				if (!asyncFile.writeQueueFull()) {
					sendRequest();
				} else {
					asyncFile.drainHandler(new Handler<Void>() {
						@Override
						public void handle(Void event) {
							sendRequest();
						}
					});
				}
			}
		};
    }

    public Operations<Void> close() {
        return Builder.as(new Publisher<Void>() {
            @Override
            public void subscribe(Subscriber<Void> subscriber) {
                subscriber.onSubscribe(new BaseSubscription<Void>(subscriber) {
                    boolean started = false;
                    @Override
                    public void request(int elements) {
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
        });
    }
}
