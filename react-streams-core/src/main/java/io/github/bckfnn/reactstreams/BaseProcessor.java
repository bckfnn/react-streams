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

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Basic implementation of a processor step that manage both the input subscription where back pressure is applied and
 * a single output subscription where the elements are send.
 *
 * @param <I> type of input elements.
 * @param <O> type of output elements.
 */
public abstract class BaseProcessor<I, O> implements Processor<I, O> {
	private Subscription inputSubscription;
	private BaseSubscription<O> outputSubscription;
	/** the number received elements that have not yet been handled. */
	private int queue = 0;
	/** true when onComplete is received. */
	private boolean complete = false;
	
	
	@Override
	public void onSubscribe(Subscription s) {
		this.inputSubscription = s;
	}

	/**
	 * Implementation method for a baseProcessor. This method will be called for each element received.
	 * It is the responsibility of the implementation to either call sendNext() or sendRequest(1) for each input element, 
	 * and then to handled().
	 * @param value the received value.
	 */
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

	/**
	 * Must be called once for each onNext() that is received.
	 */
	public void handled() {
	    queue--;
        if (complete && queue == 0) {
            sendComplete();
        }
	}

	/**
	 * Send a new value to the output subscription.
	 * @param value the value.
	 */
	public void sendNext(O value) {
		outputSubscription.sendNext(value);
	}

	/**
	 * Send an error to the output subscription.
	 * @param error the error.
	 */
	public void sendError(Throwable error) {
		outputSubscription.sendError(error);
	}

	/**
	 * Send a complete signal to the output subscription.
	 */
	public void sendComplete() {
		outputSubscription.sendComplete();
	}

	/**
	 * Send a cancel signal to the input subscription.
	 */
	public void sendCancel() {
		inputSubscription.cancel();
	}
	
	/**
	 * Send a request signal to the input subscription.
	 * @param n the number of element requested.
	 */
	public void sendRequest(long n) {
	    inputSubscription.request(n);
	}

	/**
	 * Send a request signal for one element to the input subscription.
	 */
	public void sendRequest() {
	    sendRequest(1);
	}
	
	@Override
	public void subscribe(Subscriber<? super O> subscriber) {
		outputSubscription = new BaseSubscription<O>(subscriber) {
			@Override
			public void request(long n) {
				super.request(n);
				if (isActive()) {
				    sendRequest(n);
				}
			}

			@Override
			public void cancel() {
				super.cancel();
				sendCancel();
			}
			
			public void activate() {
			    super.activate();
			    if (getPendingDemand() > 0) {
			        sendRequest(getPendingDemand());
			    }
			}
		};
		subscriber.onSubscribe(outputSubscription);
		outputSubscription.activate();
	}
}
