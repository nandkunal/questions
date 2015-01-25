package com.lynchdt.questions.topn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

/**
 * Reads an ASCII file with one signed long integer per line and drops
 * onto a work queue for processing.
 */
public class NumberFileReader implements Runnable {
	/**
	 * Handy for testing on large files. 
	 */
	private long readLimit;
	private long readCount;
	private boolean finished = false;
	
	private final String fileName;
	
	private final BlockingQueue<Long> workQueue;
	
	private FileInputStream inStream;
	private BufferedReader readBuffer;
	
	public NumberFileReader(BlockingQueue<Long> workQueue, String fileName) {
		this(workQueue, fileName, -1);
	}
	
	public NumberFileReader(BlockingQueue<Long> workQueue, String fileName,
							int readLimit) {
		this.workQueue = workQueue; 
		this.readLimit=readLimit;
		this.fileName=fileName;
	}
	
	@Override
	public void run() {
		try {
			execute();
			finished = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void execute() throws Exception {
		openForReading();
		readAndQueue();
		cleanUp();
	}
	
	private void openForReading() throws Exception {
		inStream = new FileInputStream(fileName);
		readBuffer = new BufferedReader(new InputStreamReader(inStream));
	}
	
	private void readAndQueue() throws Exception {
		String line = readBuffer.readLine();
		while(line!=null && shouldContinue()) {
			tryEnqueue(line);
			line = readBuffer.readLine();
			readBuffer.lines();
			readCount++;
		}
	}
	
	private boolean shouldContinue() {
		if(readLimit<=0) { return true; }
		return (readCount < readLimit);
	}
	
	private void tryEnqueue(String value) {
		try{
			workQueue.put(Long.parseLong(value));
		}
		catch(NumberFormatException ex) { 
			System.err.println("Can't coax " + value + " to Long");
		} 
		catch(InterruptedException ex) {
			System.err.println("Interrupted while putting " + value);
		}
		catch(Exception ex) {
			System.err.println(ex.getStackTrace());
		}
	}
	
	private void cleanUp() throws Exception {
		readBuffer.close();
		inStream.close();
	}

	public boolean isFinished() {
		return finished;
	}
	
	public long getRead() {
		return readCount;
	}
}
