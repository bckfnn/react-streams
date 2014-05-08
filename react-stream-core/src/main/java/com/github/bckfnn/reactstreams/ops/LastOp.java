package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class LastOp<T> extends BaseProcessor<T, T> {
    private T value;
    private boolean onNext = false;

    @Override
    public void doNext(T value) {
        onNext = true;
        this.value = value;
    }

    @Override
    public void onComplete() {
        if (onNext) {
            sendNext(value);
        }
        super.onComplete();
    }
}