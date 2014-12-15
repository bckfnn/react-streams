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
import io.github.bckfnn.reactstreams.Func0;
import io.github.bckfnn.reactstreams.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class WhenDonePublisherFuncOp<T, R> extends BaseProcessor<T, R> {
    private Func0<Stream<R>> publisher;
    private Subscription continueSubscription;

    public WhenDonePublisherFuncOp(Func0<Stream<R>> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void doNext(T value) {
        sendRequest();
    }

    public void sendCancel() {
    	if (continueSubscription != null) {
    		continueSubscription.cancel();
    	} else {
    		super.sendCancel();
    	}
	}
	
	public void sendRequest(int n) {
    	if (continueSubscription != null) {
    		continueSubscription.request(n);
    	} else {
    		super.sendRequest(n);
    	}
	}
	
    @Override
    public void onComplete() {
    	try {
            publisher.apply().subscribe(new Subscriber<R>() {
            	@Override
            	public void onSubscribe(Subscription s) {
            		continueSubscription = s;
            		s.request(1);
            	}

            	@Override
            	public void onNext(R value) {
            		sendNext(value);
            		continueSubscription.request(1);
            	}

            	@Override
            	public void onError(Throwable error) {
            		sendError(error);
            	}

            	@Override
            	public void onComplete() {
                    sendComplete();
            	}
            });
        } catch (Throwable e) {
            sendError(e);
        }
    }
}