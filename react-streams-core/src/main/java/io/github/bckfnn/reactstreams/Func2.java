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
package io.github.bckfnn.reactstreams;

/**
 * A functional interface that takes two input value of type T and U and return a R value.
 * @param <A1> the type of the first input value.
 * @param <A2> the type of the second input value
 * @param <R> the return type
 */
public interface Func2<A1, A2, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param arg1 the first function argument
     * @param arg2 the second function argument
     * @return the function result
     * @exception Throwable when an error occurs.
     */
    R apply(A1 arg1, A2 arg2) throws Throwable;
}
