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
package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.BaseProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;





import org.junit.Assert;

/**
 * A helper subscriber that tracks the output and events from a publisher.
 * @param <T> type of value to keep.
 */
public class Keep<T> extends BaseProcessor<T, T> {
    enum State {
        NEXT, COMPLETED, ERROR,
    }

    State state = State.NEXT;

    List<T> list;
    Throwable error;

    /**
     * Contructor.
     */
    public Keep() {
        list = new ArrayList<T>();
    }

    @Override
    public void doNext(T value) {
        if (state != State.NEXT) {
            new IllegalStateException("onNext in state " + state);
        }
        list.add(value);
        sendRequest();
    }

    @Override
    public void onComplete() {
        if (state != State.NEXT) {
            new IllegalStateException("onCompleted in state " + state);
        }
        state = State.COMPLETED;
    }

    @Override
    public void onError(Throwable error) {
        if (state != State.NEXT) {
            new IllegalStateException("onCompleted in state " + state);
        }
        this.state = State.ERROR;
        this.error = error;
    }

    /**
     * Assert that the publisher called onComplete();
     * @return self.
     */
    public Keep<T> assertSuccess() {
        Assert.assertTrue(state == State.COMPLETED);
        return this;
    }

    /**
     * Assert that the publisher emitted the values and then emitted the exc exception.
     * @param exc the expected exception.
     * @param values the expected values.
     * @return self.
     */
    @SuppressWarnings("unchecked")
    public Keep<T> assertException(Exception exc, T... values) {
        Assert.assertTrue(state == State.ERROR);
        Assert.assertEquals(true, error != null);
        Assert.assertEquals(exc.getMessage(), error.getMessage());
        Assert.assertEquals(exc.getClass(), error.getClass());
        Assert.assertEquals(values.length, list.size());
        Assert.assertEquals(Arrays.asList(values), list);
        return this;
    }

    /**
     * Assert that the publisher emitted the values and then emitted onComplete.
     * @param values the expected values.
     * @return self.
     */
	@SuppressWarnings("unchecked")
    public Keep<T> assertEquals(T... values) {
        assertSuccess();
        Assert.assertEquals(values.length, list.size());
        Assert.assertEquals(Arrays.asList(values), list);
        return this;
    }

    public String toString() {
        return "Keep[" + state + " " + list + " " + error + "]";
    }
}