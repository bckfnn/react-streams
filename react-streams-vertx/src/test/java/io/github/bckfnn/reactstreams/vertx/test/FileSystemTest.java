package io.github.bckfnn.reactstreams.vertx.test;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

public class FileSystemTest extends TestVerticle {
    @Test
    public void testRead() {
        VertxAssert.testComplete();
        /*
        RsVertx v = new RsVertx(vertx);
        v.fileSystem().open("src/test/resources/testfile")
        .printStream("open", System.out)
        .<Void>mapMany(file -> {
            return file
                    .read()
                    .printStream("read", System.out)
                    .onFinally(() -> file.close());
        })
        .printStream("done", System.out)
        .onFinally(() -> { VertxAssert.testComplete(); })
        .start(1);
        */
    }
}
