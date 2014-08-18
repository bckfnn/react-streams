package com.github.bckfnn.reactstreams;

import org.reactivestreams.Processor;

/**
 * A pipe interface.
 *
 * @param <T> input type to the pipe.
 * @param <R> output type from the pipe.
 */
public interface Pipe<T, R> extends Processor<T, R>, Operations<R> {

}
