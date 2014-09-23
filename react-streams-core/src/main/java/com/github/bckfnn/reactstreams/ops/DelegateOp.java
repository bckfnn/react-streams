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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class DelegateOp<R> extends BaseProcessor<R, R> {
    private Subscriber<R> target;
    int delegateQueue = 0;
    
    public DelegateOp(Subscriber<R> target) {
        this.target = target;
    }
    
    
	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		super.subscribe(subscriber);

        target.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
            	delegateQueue += n;
            	int min = 1;
                sendRequest(min);
            }

            @Override
            public void cancel() {
                sendCancel();
            }
        });
	}

    @Override
    public void doNext(R value) {
    	sendNext(value);
        target.onNext(value);
    }

    @Override
    public void onComplete() {
    	sendComplete();
        target.onComplete();
    }

    @Override
    public void onError(Throwable error) {
    	sendError(error);
        target.onError(error);
    }

    public String toString() {
        return "Delegate[" + "" + "]";
    }

}