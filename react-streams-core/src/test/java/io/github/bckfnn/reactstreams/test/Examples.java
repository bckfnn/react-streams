/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
