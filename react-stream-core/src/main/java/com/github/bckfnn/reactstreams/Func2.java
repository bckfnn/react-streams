package com.github.bckfnn.reactstreams;

/**
 * A functional interface that takes two input value of type T and U and return a R value.
 * @param <T> the type of the first input value.
 * @param <U> the type of the second input value
 * @param <R> the return type
 */
public interface Func2<T, U, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @exception Throwable when an error occurs.
     */
    R apply(T t, U u) throws Throwable;
}
