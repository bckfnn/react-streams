package com.github.bckfnn.reactstreams;

/**
 * A functional interface that takes a T value and return a R value.
 * @param <T> the input type
 * @param <R> the return type
 */
public interface Func1<T, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @exception Throwable when an exception occur.
     */
    R apply(T t) throws Throwable;
}
