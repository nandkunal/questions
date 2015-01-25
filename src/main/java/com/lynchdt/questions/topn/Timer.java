package com.lynchdt.questions.topn;

public class Timer {
	private long start;
	private long finish;
	
	private Timer() {
		start = System.currentTimeMillis();
	}
	
	public static Timer createAndStart() {
		return new Timer();
	}
	
	public void stop() {
		finish = System.currentTimeMillis();
	}
	
	private long diffInSeconds() {
		return (finish - start)/1000;
	}
	
	@Override
	public String toString() {
		return (diffInSeconds() + " sec(s)");
	}
}
