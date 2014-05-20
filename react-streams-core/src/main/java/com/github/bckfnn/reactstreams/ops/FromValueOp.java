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
package com.github.bckfnn.reactstreams.ops;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.github.bckfnn.reactstreams.BaseSubscription;

/**
 * FromValueOp will generate a single onNext() event for the specified element, 
 * followed by an onComplete().
 * 
 * @param <T> type of the event.
 */
public class FromValueOp<T> implements Publisher<T> {
    private T value;
    private boolean finished = false;

    public FromValueOp(T value) {
        this.value = value;
    }

    @Override
    public void subscribe(final Subscriber<T> subscriber) {
        subscriber.onSubscribe(new BaseSubscription<T>(subscriber) {
            @Override
            public void request(int elements) {
                if (!finished) {
                    finished = true;
                    sendNext(value);
                    sendComplete();
                }
            }
        });
    }
}