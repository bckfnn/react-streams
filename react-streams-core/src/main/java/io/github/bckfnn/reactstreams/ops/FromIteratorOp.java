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

import java.util.Iterator;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * <strong>FromIteratorOp</strong> will generate an onNext() event for each element in the
 * Iterable, followed by an onComplete() event.
 * 
 * @param <T> type of the event.
 */
public class FromIteratorOp<T> implements Publisher<T> {
    private Iterator<T> iterator;

    public FromIteratorOp(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public void subscribe(final Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new ActiveSubscription<T>(subscriber) {
            @Override
            public boolean hasMore() {
                return iterator.hasNext();
            }

            @Override
            public T getOne() {
                return iterator.next();
            }
        });			
    }
}