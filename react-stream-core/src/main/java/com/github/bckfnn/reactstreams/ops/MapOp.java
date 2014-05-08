package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

public abstract class MapOp<I, O> extends BaseProcessor<I, O> {

	public abstract O map(I value) throws Throwable;

    @Override
    public void doNext(I value) {
        try {
            sendNext(map(value));
        } catch (Throwable error) {
            sendError(error);
            sendCancel();
        }
    }

    public String toString() {
        return "Map[]";
    }
}