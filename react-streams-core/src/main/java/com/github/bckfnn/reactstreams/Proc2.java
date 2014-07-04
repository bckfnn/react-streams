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
package com.github.bckfnn.reactstreams;

/**
 * A functional interface that takes two arguments and returns nothing.
 * @param <T> the input type of the first argument
 * @param <U> the input type of the second argument
 */
public interface Proc2<T, U> {
    /**
     * Call the function.
     *
     * @param t the function argument
     * @param u the second function argument
     * @exception Throwable when an exception occur.
     */
    void apply(T t, U u) throws Throwable;
}
