package com.github.bckfnn.reactstreams.vertx.test;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import com.github.bckfnn.reactstreams.vertx.RsVertx;

public class FileSystemTest extends TestVerticle {
    @Test
    public void testRead() {
        RsVertx v = new RsVertx(vertx);
        v.fileSystem().open("test")
        .printStream("open", System.out)
        .<Void>mapMany(file -> {
            return file
                    .read()
                    .printStream("read", System.out)
                    .onFinally(() -> file.close());
        })
        .printStream("done", System.out)
        .whenDone(() -> { VertxAssert.testComplete(); })
        .start(1);
    }
}
