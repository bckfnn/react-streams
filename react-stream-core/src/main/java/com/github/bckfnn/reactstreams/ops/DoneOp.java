package com.github.bckfnn.reactstreams.ops;

import com.github.bckfnn.reactstreams.BaseProcessor;

/**
 * Done will only generate a complete event .
 * 
 * @param <T> type of the event.
 */
public class DoneOp<T> extends BaseProcessor<T, T> {
    public DoneOp() {
    }
    
    @Override
    public void doNext(T value) {
        sendComplete();
        sendCancel();
    }
}