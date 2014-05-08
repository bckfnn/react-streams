package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class WhenDoneValueOp<T, R> extends BaseProcessor<T, R> {
    private R value;

    public WhenDoneValueOp(R value) {
        this.value = value;
    }

    @Override
    public void doNext(T value) {
    }

    @Override
    public void onComplete() {
        sendNext(value);
        sendComplete();
    }
}