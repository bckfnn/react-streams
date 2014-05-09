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

public class BaseSubscription<T> implements Subscription {
	private Subscriber<T> subscriber;
	private boolean cancelled = false;
	private int pending;
	
	public BaseSubscription(Subscriber<T> subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public void request(int elements) {
		pending += elements;
	}

	protected boolean isCancelled() {
		return cancelled;
	}

	public int getPending() {
		return pending;
	}

	public void sendNext(T value) {
		subscriber.onNext(value);
		pending--;
	}
	
	public void sendComplete() {
		subscriber.onComplete();
		pending = 0;
	}
	
	public void sendError(Throwable t) {
		subscriber.onError(t);
		pending = 0;
	}
	
	public String toString() {
		return "Subscription to " + subscriber;
	}
}
