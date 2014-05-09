package com.github.bckfnn.reactstreams.vertx.test;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;

import com.github.bckfnn.reactstreams.vertx.RsVertx;

public class FileSystemTest extends TestVerticle {
	@Test
	public void testRead() {
		RsVertx v = new RsVertx(vertx);
		v.fileSystem().open("test")
		.printStream("open", System.out)
		.mapMany(file -> file.read())
		.printStream("read", System.out).start(1);
	}
}
