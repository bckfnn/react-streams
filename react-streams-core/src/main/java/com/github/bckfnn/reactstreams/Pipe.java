package com.github.bckfnn.reactstreams;

import org.reactivestreams.Processor;

public interface Pipe<T, R> extends Processor<T, R>, Operations<R> {

}
