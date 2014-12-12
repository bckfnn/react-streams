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
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class FinallyOp<T, R> extends BaseProcessor<T, R> {

    public abstract Stream<R> fin() throws Throwable;
    
    @Override
    public void doNext(T value) {
        sendRequest();
        handled();
    }

    @Override
    public void onComplete() {
        try {
            runFinally();
            super.onComplete();
        } catch (Throwable e) {
            sendError(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        try {
            runFinally();
            super.onError(t);
        } catch (Throwable e) {
            sendError(e);
        }
    }
    
    private void runFinally() throws Throwable {
        fin().subscribe(new Subscriber<R>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }
            
            @Override
            public void onNext(R value) {
                sendNext(value);
            }

            @Override
            public void onComplete() {
                sendComplete();
            }

            @Override
            public void onError(Throwable exc) {
                sendError(exc);
            }
        });
    }
}
