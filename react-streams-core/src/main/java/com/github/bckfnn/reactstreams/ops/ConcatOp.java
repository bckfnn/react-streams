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

import java.util.Arrays;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.BaseSubscription;

public class ConcatOp<T> implements Publisher<T> {
    private BaseSubscription<T> outputSubscription;
    private List<Publisher<T>> list;
    private int i = 0;
    private Subscription currentInputSubscription;

    public ConcatOp(Publisher<T>[] list) {
        this(Arrays.asList(list));
    }

    public ConcatOp(List<Publisher<T>> list) {
        this.list = list;
    }
    
    @Override
    public void subscribe(Subscriber<T> s) {
        list.get(i++).subscribe(new Sub());
        s.onSubscribe(outputSubscription = new BaseSubscription<T>(s) {
            @Override
            public void request(int elements) {
                super.request(elements);
                currentInputSubscription.request(outputSubscription.getPendingDemand());
            }
        });
    }

    private class Sub implements Subscriber<T> {
        @Override
        public void onSubscribe(Subscription s) {
            currentInputSubscription = s;
        }

        @Override
        public void onNext(T t) {
            outputSubscription.sendNext(t);
        }

        @Override
        public void onError(Throwable t) {
            outputSubscription.sendError(t);
        }

        @Override
        public void onComplete() {
            if (i == list.size()) {
                outputSubscription.sendComplete();
            } else {
                list.get(i++).subscribe(new Sub());
                currentInputSubscription.request(outputSubscription.getPendingDemand());
            }
        }
    }
}