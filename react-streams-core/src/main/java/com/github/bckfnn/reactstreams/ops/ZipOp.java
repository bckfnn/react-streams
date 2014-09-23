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

import java.util.LinkedList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.Tuple;


public class ZipOp<T1, T2> implements Publisher<Tuple<T1, T2>> {
	Publisher<T1> o1;
	Publisher<T2> o2;
	Subscription i1;
	Subscription i2;
	List<T1> v1 = new LinkedList<T1>();
	boolean v1stop = false;
	List<T2> v2 = new LinkedList<T2>();
	boolean v2stop = false;
	
	public ZipOp(Publisher<T1> o1, Publisher<T2> o2) {
		this.o1 = o1;
		this.o2 = o2;
	}
	
	@Override
	public void subscribe(final Subscriber<? super Tuple<T1, T2>> subscriber) {
		
		Subscriber<T1> s1 = new Subscriber<T1>() {
			@Override
			public void onSubscribe(Subscription s) {
				i1 = s;
			}

			@Override
			public void onNext(T1 t) {
				if (v2.size() > 0) {
					subscriber.onNext(new Tuple<T1, T2>(t, v2.remove(0)));
					//i2.request(1);
				} else if (v2stop) {
                    subscriber.onComplete();
                    i1.cancel();
                } else {
					v1.add(t);
				}
			}

			@Override
			public void onError(Throwable t) {
				i2.cancel();
				subscriber.onError(t);
			}

			@Override
			public void onComplete() {
			    v1stop = true;
			    if (v2stop || v1.size() == 0) {
			        subscriber.onComplete();
			    }
			}
		};
		o1.subscribe(s1);
		
		
		Subscriber<T2> s2 = new Subscriber<T2>() {
			@Override
			public void onSubscribe(Subscription s) {
				i2 = s;
			}

			@Override
			public void onNext(T2 t) {
				if (v1.size() > 0) {
					subscriber.onNext(new Tuple<T1, T2>(v1.remove(0), t));
					//i1.request(1);
				} else if (v1stop) {
				    subscriber.onComplete();
				    i2.cancel();
				} else {
					v2.add(t);
				}
			}

			@Override
			public void onError(Throwable t) {
				i1.cancel();
				subscriber.onError(t);
			}

			@Override
			public void onComplete() {
                v2stop = true;
                if (v1stop || v2.size() == 0) {
                    subscriber.onComplete();
                }
			}
		};
		o2.subscribe(s2);
		
		subscriber.onSubscribe(new Subscription() {
			@Override
			public void request(long n) {
			    if (!v1stop) {
			        i1.request(n);
			    }
			    if (!v2stop) {
			        i2.request(n);
			    }
			}

			@Override
			public void cancel() {
				i1.cancel();
				i1.cancel();
			}
		});
	}

}
