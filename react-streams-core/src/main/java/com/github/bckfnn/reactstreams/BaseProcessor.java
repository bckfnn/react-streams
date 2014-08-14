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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class BaseProcessor<I, O> implements Processor<I, O> {
	private Subscription inputSubscription;
	private BaseSubscription<O> outputSubscription;
	private int queue = 0;
	boolean complete = false;
	
	@Override
	public void onSubscribe(Subscription s) {
		this.inputSubscription = s;
	}

	public abstract void doNext(I value);
	
	@Override 
	public void onNext(I value) {
		queue++;
		doNext(value);
	}

	@Override
	public void onError(Throwable t) {
		sendError(t);
	}

	@Override
	public void onComplete() {
	    complete = true;
	    if (queue == 0) {
	        sendComplete();
	    }
	}

	public void sendNext(O value) {
		outputSubscription.sendNext(value);
	}

	public void sendError(Throwable error) {
		outputSubscription.sendError(error);
	}

	public void sendComplete() {
		outputSubscription.sendComplete();
	}

	public void sendCancel() {
		inputSubscription.cancel();
	}
	
	public void sendRequest(int n) {
		inputSubscription.request(n);
	}

	public void sendRequest() {
	    queue--;
	    if (complete && queue == 0) {
	        sendComplete();
	    } else {
	        sendRequest(1);
	    }
	}
	
	@Override
	public void subscribe(Subscriber<O> subscriber) {
		subscriber.onSubscribe(outputSubscription = new BaseSubscription<O>(subscriber) {
			@Override
			public void request(int n) {
				super.request(n);
				sendRequest(n);
			}

			@Override
			public void cancel() {
				super.cancel();
				sendCancel();
			}
			
			public String toString() {
				return "BaseSubscription from " + BaseProcessor.this + " to " + subscriber;
			}
		});
	}

}
