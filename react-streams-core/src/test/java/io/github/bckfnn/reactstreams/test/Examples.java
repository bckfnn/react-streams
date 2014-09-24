package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Builder;

import org.junit.Test;

public class Examples {

    @Test
    public void testFilter() {
        Builder.from(1, 2, 3, 4, 5, 6, 7)
        .filter(x -> (x % 2) == 0)
        .printStream("filter", System.out)
        .start(1);
    }
}
