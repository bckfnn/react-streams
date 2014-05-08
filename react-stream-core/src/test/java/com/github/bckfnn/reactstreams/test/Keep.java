package com.github.bckfnn.reactstreams.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class Keep<T> extends BaseProcessor<T, T> {
    enum State {
        NEXT, COMPLETED, ERROR,
    }

    State state = State.NEXT;

    List<T> list;
    Throwable error;

    public Keep() {
        list = new ArrayList<T>();
    }

    @Override
    public void doNext(T value) {
        if (state != State.NEXT) {
            new IllegalStateException("onNext in state " + state);
        }
        list.add(value);
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

    public Keep<T> assertSuccess() {
        Assert.assertTrue(state == State.COMPLETED);
        return this;
    }

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