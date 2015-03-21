package io.github.bckfnn.reactstreams.vertx;

import io.vertx.core.Vertx;

public class RsVertx {
	private Vertx vertx;
	
	public RsVertx(Vertx vertx) {
		this.vertx = vertx;
	}

	public RsFileSystem fileSystem() {
		return new RsFileSystem(vertx);
	}
	
	public RsEventBus eventBus() {
		return new RsEventBus(vertx);
	}
}
