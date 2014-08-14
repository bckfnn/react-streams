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

public abstract class ActiveSubscription<T> extends BaseSubscription<T> {
	private boolean recursion = false;

	public ActiveSubscription(Subscriber<T> subscriber) {
		super(subscriber);
	}

	public abstract boolean hasMore();

	public abstract T getOne();

	@Override
	public void request(int elements) {
		super.request(elements);
		if (recursion) {
			return;
		}
		recursion = true;
		while (getPendingDemand() > 0 && !isCancelled() && hasMore()) {
			sendNext(getOne());
		}
		if (!hasMore() && !isCancelled()) {
			sendComplete();
		}
        recursion = false;
	}
}
