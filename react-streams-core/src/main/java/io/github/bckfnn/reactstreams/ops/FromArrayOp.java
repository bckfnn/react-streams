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
package io.github.bckfnn.reactstreams.ops;

import io.github.bckfnn.reactstreams.ActiveSubscription;
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Subscriber;

public class FromArrayOp<T> implements Stream<T> {
    private T[] array;
    int idx = 0;

    public FromArrayOp(T[] array) {
        this.array = array;
    }

    @Override
    public void subscribe(final Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new ActiveSubscription<T>(subscriber) {
            private int idx = 0;

            @Override
            public boolean hasMore() {
                return idx < array.length;
            }

            @Override
            public T getOne() {
                return array[idx++];
            }
        });
    }

    public String toString() {
        return "ArraySource[" + idx + "]";
    }

}