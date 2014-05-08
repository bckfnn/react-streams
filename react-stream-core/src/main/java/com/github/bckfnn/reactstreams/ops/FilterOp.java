package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public abstract class FilterOp<T> extends BaseProcessor<T, T> {
    public abstract boolean check(T value) throws Throwable;

    @Override
    public void doNext(T value) {
        try {
            if (check(value)) {
                sendNext(value);
            }
        } catch (Throwable exc) {
            sendError(exc);
            sendCancel();
        }
    }
    
    public String toString() {
    	return "FilterOp";
    }
}