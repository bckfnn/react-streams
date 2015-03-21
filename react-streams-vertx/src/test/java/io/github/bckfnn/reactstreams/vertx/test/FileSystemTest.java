package io.github.bckfnn.reactstreams.vertx.test;

import io.vertx.test.core.VertxTestBase;

import org.junit.Test;

public class FileSystemTest extends VertxTestBase {
    @Test
    public void testRead() {
        //VertxAssert.testComplete();
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
