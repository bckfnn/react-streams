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

import com.github.bckfnn.reactstreams.ActiveSubscription;

public class CounterOp implements Publisher<Integer> {
	private int start;
	
	public CounterOp(int start) {
		this.start = start;
	}

	@Override
	public void subscribe(Subscriber<? super Integer> subscriber) {
	    ActiveSubscription<Integer> s = new ActiveSubscription<Integer>(subscriber) {
			int count = start;
			
			@Override
			public boolean hasMore() {
				return true;
			}

			@Override
			public Integer getOne() {
				return count++;
			}
		};
		subscriber.onSubscribe(s);
		//s.activate();
	}
}
