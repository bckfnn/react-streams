package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Stream;

import org.junit.Test;

public class Examples {

    @Test
    public void testFilter() {
        Stream.from(1, 2, 3, 4, 5, 6, 7)
        .filter(x -> (x % 2) == 0)
        .printStream("filter", System.out)
        .start(1);
    }

    @Test
    public void testLast() {
        Stream.from(1, 2, 3, 4)
        .last()
        .printStream("last", System.out)
        .start(1);
    }
    
    @Test
    public void testBuild() {
        /*
        Stream.as(s -> {
            s.onRequest(x -> {
                s.next("abc");
                s.handled();
            });
            s.onCancel(() -> {});
        });
        */
        
        Stream
        .asOne(s -> {
            System.out.println("req");
            s.sendNext("abc");
            s.sendComplete();
        })
        .printStream("build", System.out)
        .start(1);
    }
}
