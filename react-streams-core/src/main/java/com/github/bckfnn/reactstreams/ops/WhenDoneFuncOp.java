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
import com.github.bckfnn.reactstreams.Func0;

public class WhenDoneFuncOp<T, R> extends BaseProcessor<T, R> {
    private Func0<R> func;

    public WhenDoneFuncOp(Func0<R> func) {
        this.func = func;
    }

    @Override
    public void doNext(T value) {
        sendRequest();
    }

    @Override
    public void onComplete() {
        try {
			sendNext(func.apply());
		} catch (Throwable e) {
			sendError(e);
		}
        sendComplete();
    }
}