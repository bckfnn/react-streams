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

/**
 * <b>LastOp</b> will consume all the input and when onComplete() is recieved will 
 * send out an onNext() with the last element, followed by an onComplete(). 
 *
 * @param <T>type of the event.
 */
public class LastOp<T> extends BaseProcessor<T, T> {
    private T value;
    private boolean onNext = false;

    @Override
    public void doNext(T value) {
        onNext = true;
        this.value = value;
        sendRequest();
    }

    @Override
    public void onComplete() {
        if (onNext) {
            sendNext(value);
        }
        super.onComplete();
    }
}