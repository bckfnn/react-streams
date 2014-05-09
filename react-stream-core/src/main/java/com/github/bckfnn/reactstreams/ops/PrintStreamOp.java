package com.github.bckfnn.reactstreams.ops;

import java.io.PrintStream;

import com.github.bckfnn.reactstreams.BaseProcessor;

public class PrintStreamOp<T> extends BaseProcessor<T, T> {
	private String prefix;
	private PrintStream printStream;
	
	public PrintStreamOp(String prefix, PrintStream printStream) {
		this.printStream = printStream;
	}
	
	@Override
	public void doNext(T value) {
		printStream.println(prefix + " onNext:" + value);
		sendNext(value);
	}
	
	@Override
	public void onError(Throwable error) {
		printStream.println(prefix + " onError:" + error);
		super.onError(error);
	}

	@Override
	public void onComplete() {
		printStream.println(prefix + " onComplete");
		super.onComplete();
	}



}
