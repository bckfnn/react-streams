package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class NopOp<T> extends BaseProcessor<T, T> {
    @Override
    public void doNext(T value) {
        sendNext(value);
    }
}