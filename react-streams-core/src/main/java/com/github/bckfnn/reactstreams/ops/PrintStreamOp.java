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

import java.io.PrintStream;

import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class PrintStreamOp<T> extends BaseProcessor<T, T> {
	private String prefix;
	private PrintStream printStream;
	
	public PrintStreamOp(String prefix, PrintStream printStream) {
		this.prefix = prefix;
		this.printStream = printStream;
	}

	public void onSubscribe(Subscription s) {
	    printStream.println(prefix + " onSubScribe:" + s);
	    super.onSubscribe(s);
	}

	@Override
	public void doNext(T value) {
		printStream.println(prefix + " onNext:" + value);
		sendNext(value);
		handled();
	}
	
	@Override
	public void onError(Throwable error) {
		printStream.println(prefix + " onError:" + error);
		super.onError(error);
	}

	@Override
	public void onComplete() {
		printStream.println(prefix + " onComplete");
		super.onComplete();
	}

    @Override
    public void sendCancel() {
        printStream.println(prefix + " cancel");
        super.sendCancel();
    }

    @Override
    public void sendRequest(int n) {
        printStream.println(prefix + " request(" + n + ")");
        //new Exception().printStackTrace();
        super.sendRequest(n);
    }
}
