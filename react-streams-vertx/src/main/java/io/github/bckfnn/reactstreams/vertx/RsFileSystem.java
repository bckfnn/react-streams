package io.github.bckfnn.reactstreams.vertx;

import io.github.bckfnn.reactstreams.Stream;

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


	public Stream<RsAsyncFile> open(String path) {
		return Stream.asOne(subscription -> {
		    fileSystem.open(path, "755", false, true, true, new Handler<AsyncResult<AsyncFile>>() {
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

	public Stream<RsAsyncFile> openWrite(String path) {
	    return Stream.asOne(subscription -> {
	        fileSystem.open(path, new Handler<AsyncResult<AsyncFile>>() {
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
