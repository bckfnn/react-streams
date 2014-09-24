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

/**
 * A default implementation of Subscription.
 * The class will track the amount of demand that it has received with request() method and which have not 
 * yet been send to the subscriber with the sendNext() method.
 * It also track is the subscription have been cancelled. 
 *
 * @param <T>
 */
public class BaseSubscription<T> implements Subscription {
	private Subscriber<? super T> subscriber;
	private boolean cancelled = false;
	private long pendingDemand;
	private boolean active = false;
	
	/**
	 * Constructor.
	 * @param subscriber2 the output subscriber.
	 */
	public BaseSubscription(Subscriber<? super T> subscriber2) {
		this.subscriber = subscriber2;
	}

	@Override
	public void cancel() {
		cancelled = true;
		subscriber = null;
		//subscriber.onComplete();
	}
	
	@Override
	public void request(long elements) {
	    if (elements <= 0) {
	        throw new IllegalArgumentException("spec 3.9");
	    }
	    pendingDemand += elements;
	}
	
	/**
	 * @return is the subscription have been activated.
	 */
	public boolean isActive() {
	    return active;
	}
	
	/**
	 * Activate the subscription.
	 */
	public void activate() {
	    active = true;
	}

	/**
	 * @return true if the subscription is cancelled (no longer active). 
	 */
	protected boolean isCancelled() {
		return cancelled;
	}

	/**
	 * @return the amount of unfulfilled demand, the difference between request() and sendNext() calls.
	 */
	public long getPendingDemand() {
		return pendingDemand;
	}

	/**
	 * @param value send a value to the out subscriber.
	 * Also decrement the pending demand.
	 */
	public void sendNext(T value) {
		subscriber.onNext(value);
		pendingDemand--;
	}
	
	/**
	 * Send a <code>complete</code> event to the output subscriber.
	 * Also marks the subscription as cancelled.
	 */
	public void sendComplete() {
		subscriber.onComplete();
		cancelled = true;
	}

	/**
	 * Send a <code>error</code> event to the output subscriber.
	 * Also marks the subscription as cancelled.
	 * @param t the error.
	 */
	public void sendError(Throwable t) {
		subscriber.onError(t);
		cancelled = true;
	}
	
	public String toString() {
		return "Subscription to " + subscriber;
	}
}
