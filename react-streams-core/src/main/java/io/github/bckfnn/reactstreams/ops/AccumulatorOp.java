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
package io.github.bckfnn.reactstreams.ops;

import io.github.bckfnn.reactstreams.BaseProcessor;

/**
 * Accumulator operation.
 *
 * @param <T> value type.
 */
public abstract class AccumulatorOp<T> extends BaseProcessor<T, T> {
    private T acc;
    private boolean initialValueSent = false;

    /**
     * Constructor.
     * @param initial the initial value.
     */
    public AccumulatorOp(T initial) {
        this.acc = initial;
    }

    /**
     * called for each value.
     * @param value the current value.
     * @param nextValue the next value encounter.
     * @return the new accumulated value.
     * @throws Throwable if an error occurs.
     */
    public abstract T calc(T value, T nextValue) throws Throwable;

    @Override
    public void doNext(T value) {
        if (acc == null) {
            acc = value;
            sendRequest();
            handled();
            return;
        }
        if (!initialValueSent) {
            initialValueSent = true;
            sendNext(acc);
        }
        try {
            acc = calc(acc, value);
            sendNext(acc);
            handled();
        } catch (Throwable exc) {
            sendError(exc);
            sendCancel();
        }
    }
}