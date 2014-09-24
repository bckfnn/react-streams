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

import io.github.bckfnn.reactstreams.BaseProcessor;
import io.github.bckfnn.reactstreams.Operations;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class MapManyOp<T, R> extends BaseProcessor<T, R> {
    List<Publisher<R>> children = new ArrayList<Publisher<R>>();
    boolean completed = false;
    int count = 0;
    Subscription childSubscription;
    
    public abstract Operations<R> map(T value) throws Throwable;

	@Override
    public void doNext(T value) {
        try {
            final Publisher<R> child = map(value);
            children.add(child);
            count++;
            drain();
        } catch (Throwable exc) {
            onError(exc);
        }
    }

    @Override
    public void onComplete() {
        if (count == 0) {
            sendComplete();
        } else {
            completed = true;
        }
    }

    @Override
    public void sendRequest(long n) {
        if (childSubscription!= null) {
            childSubscription.request(n);
        } else {
            super.sendRequest(n);
        }
    }


    private void drain() {
        if (children.size() == 0) {
            return;
        }
        final Publisher<R> child = children.remove(0);
        child.subscribe(new Subscriber<R>() {

            @Override
            public void onSubscribe(Subscription s) {
            	childSubscription = s;
            	childSubscription.request(1);
            }
            
            @Override
            public void onNext(R value) {
                sendNext(value);
            }

            @Override
            public void onComplete() {
                childSubscription = null;
                count--;
                if (count == 0 && completed) {
                    sendComplete();
                } else {
                	sendRequest(1);
                    drain();
                }
                handled();
            }

            @Override
            public void onError(Throwable exc) {
                sendError(exc);
            }
        });
    }
    
    @Override
    public String toString() {
        return "MapMany[]";
    }
}