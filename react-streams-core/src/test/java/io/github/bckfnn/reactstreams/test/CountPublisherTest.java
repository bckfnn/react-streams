package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;

/**
 * TCL test.
 */
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
        return Long.MAX_VALUE;
    }

    @Override
    public long boundedDepthOfOnNextAndRequestRecursion() {
        return 1;
    }
}