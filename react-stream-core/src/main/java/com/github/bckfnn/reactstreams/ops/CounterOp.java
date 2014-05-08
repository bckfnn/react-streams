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
	public void subscribe(Subscriber<Integer> subscriber) {
		subscriber.onSubscribe(new ActiveSubscription<Integer>(subscriber) {
			int count = start;
			
			@Override
			public boolean hasMore() {
				return true;
			}

			@Override
			public Integer getOne() {
				return count++;
			}
		});
	}
}
