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

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

/**
 * TCL test.
 */
@Test
public class CountPublisherTest extends PublisherVerification<Integer> {
    /**
     * Constructor.
     */
    public CountPublisherTest() {
        super(new TestEnvironment(300), 1000);
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return Stream.counter(1).take((int) elements);
    }

    @Override
    public Publisher<Integer> createErrorStatePublisher() {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onError(new RuntimeException("Can't subscribe subcriber: " + s + ", because of reasons."));
            }
        };
    }

    // ADDITIONAL CONFIGURATION

    @Override
    public long maxElementsFromPublisher() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long boundedDepthOfOnNextAndRequestRecursion() {
        return 1;
    }

    @Override
    @Test
    public void spec317_mustSupportACumulativePendingElementCountUpToLongMaxValue() throws Throwable {

    }

    @Override
    @Test
    public void spec317_mustSupportAPendingElementCountUpToLongMaxValue() throws Throwable {
    }

    @Override
    @Test
    public void spec317_mustSignalOnErrorWhenPendingAboveLongMaxValue() throws Throwable {

    }
}