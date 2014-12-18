package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Stream;

import org.testng.annotations.Test;



/**
 * Code from examples doc.
 */
public class Examples {

    /**
     * Filter example.
     */
    @Test
    public void testFilter() {
        Stream.from(1, 2, 3, 4, 5, 6, 7)
        .filter(x -> (x % 2) == 0)
        .print("filter", System.out)
        .start(1);
    }

    /**
     * Last example.
     */
    @Test
    public void testLast() {
        Stream.from(1, 2, 3, 4)
        .last()
        .print("last", System.out)
        .start(1);
    }
    
    /**
     * Build example.
     */
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
        .print("build", System.out)
        .start(1);
    }
}
