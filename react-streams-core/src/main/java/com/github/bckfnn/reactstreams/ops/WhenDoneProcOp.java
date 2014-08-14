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

import com.github.bckfnn.reactstreams.BaseProcessor;
import com.github.bckfnn.reactstreams.Proc0;

public class WhenDoneProcOp<T> extends BaseProcessor<T, T> {
	private Proc0 func;
	
	public WhenDoneProcOp(Proc0 func) {
		this.func = func;
	}

    @Override
    public void doNext(T value) {
        sendRequest();
    }

    @Override
    public void onComplete() {
    	try {
    		func.apply();
            sendComplete();
    	} catch (Throwable error) {
    		sendError(error);
    	}
    }
}