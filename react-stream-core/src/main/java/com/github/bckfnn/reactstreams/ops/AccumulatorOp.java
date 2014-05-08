package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public abstract class AccumulatorOp<T> extends BaseProcessor<T, T> {
    private T acc;
    private boolean initialValueSent = false;

    public AccumulatorOp(T initial) {
        this.acc = initial;
    }

    public abstract T calc(T value, T nextValue) throws Throwable;

    @Override
    public void doNext(T value) {
        if (acc == null) {
            acc = value;
            return;
        }
        if (!initialValueSent) {
            initialValueSent = true;
            sendNext(acc);
        }
        try {
            acc = calc(acc, value);
            sendNext(acc);
        } catch (Throwable exc) {
            sendError(exc);
            sendCancel();
        }
    }
}