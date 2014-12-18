package io.github.bckfnn.reactstreams;

import org.reactivestreams.Processor;

/**
 * A pipe interface.
 *
 * @param <I> input type to the pipe.
 * @param <O> output type from the pipe.
 */
public interface Pipe<I, O> extends Processor<I, O>, Stream<O> {

}
