package com.github.bckfnn.reactstreams.test;

import org.junit.Test;

import com.github.bckfnn.reactstreams.Builder;

public class Examples {

    @Test
    public void testFilter() {
        Builder.from(1, 2, 3, 4, 5, 6, 7)
        .filter(x -> (x % 2) == 0)
        .printStream("filter", System.out)
        .start(1);
    }
}
