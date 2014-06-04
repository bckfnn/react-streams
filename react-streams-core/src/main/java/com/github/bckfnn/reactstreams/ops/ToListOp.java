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

import java.util.ArrayList;
import java.util.List;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class ToListOp<T> extends BaseProcessor<T, List<T>> {
    private List<T> list = new ArrayList<>();
    
    @Override
    public void doNext(T value) {
        list.add(value);
    }

    @Override
    public void onComplete() {
    	sendNext(list);
    	sendComplete();
    }
}