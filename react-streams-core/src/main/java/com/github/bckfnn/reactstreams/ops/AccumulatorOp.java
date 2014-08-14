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

public abstract class AccumulatorOp<T> extends BaseProcessor<T, T> {
    private T acc;
    private boolean initialValueSent = false;

    public AccumulatorOp(T initial) {
        this.acc = initial;
    }

    public abstract T calc(T value, T nextValue) throws Throwable;

    @Override
    public void doNext(T value) {
        if (acc == null) {
            acc = value;
            sendRequest();
            return;
        }
        if (!initialValueSent) {
            initialValueSent = true;
            sendNext(acc);
        }
        try {
            acc = calc(acc, value);
            sendNext(acc);
            sendRequest();
        } catch (Throwable exc) {
            sendError(exc);
            sendCancel();
        }
    }
}