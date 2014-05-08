package com.github.bckfnn.reactstreams;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public interface Processor<I, O> extends Subscriber<I>, Publisher<O> {

}
