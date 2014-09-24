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

public abstract class MapOp<I, O> extends BaseProcessor<I, O> {

	public abstract O map(I value) throws Throwable;

    @Override
    public void doNext(I value) {
        try {
            sendNext(map(value));
            handled();
        } catch (Throwable error) {
            sendError(error);
            sendCancel();
        }
    }

    public String toString() {
        return "Map[]";
    }
}