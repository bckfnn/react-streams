package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.Stream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;

public class RsFileSystem {
	//private Vertx vertx;
	private FileSystem fileSystem;

	public RsFileSystem(Vertx vertx) {
		//this.vertx = vertx;
		this.fileSystem = vertx.fileSystem();
	}


	public Stream<RsAsyncFile> open(String path, OpenOptions options) {
		return Stream.asOne(subscription -> {
		    fileSystem.open(path, options, new Handler<AsyncResult<AsyncFile>>() {
		        @Override
		        public void handle(AsyncResult<AsyncFile> event) {
		            if (event.succeeded()) {
		                subscription.sendNext(new RsAsyncFile(event.result()));
		                subscription.sendComplete();
		            } else {
		                subscription.sendError(event.cause());
		            }								
		        }
		    });
		});
	}
}
