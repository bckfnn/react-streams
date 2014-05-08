package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class ContinueWithErrorOp<T> extends BaseProcessor<T, T> {
    private Throwable error;

    public ContinueWithErrorOp(Throwable error) {
        this.error = error;
    }

    @Override
    public void doNext(T value) {
        sendNext(value);
    }

    @Override
    public void onComplete() {
        sendError(error);
    }
}