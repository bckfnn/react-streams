package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class SkipOp<T> extends BaseProcessor<T, T> {
    final private int num;
    private int count = 0;

    public SkipOp(int num) {
        this.num = num;
    }

    @Override
    public void doNext(T value) {
        if (++count > num) {
            sendNext(value);
        }
    }
}