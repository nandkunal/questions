package com.lynchdt.questions.topn;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class TopNWorker implements Runnable {
	private BoundedMinHeap heap;
	private final BlockingQueue<Long> workQueue;
	private final int N;
	private boolean done = false;
	
	private AtomicBoolean running = new AtomicBoolean(true);
	
	/**
	 * Used to stop mutation of our heap while partial results
	 * are merged into another heap.
	 */
	private final ReentrantLock modificationLock = new ReentrantLock();
	
	public TopNWorker(int N, BlockingQueue<Long> workQueue) {
		this.N = N;
		heap = new BoundedMinHeap(N);
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		try {
			while(running.get()) 
				process();
		}
		catch(Exception ex) {
			System.err.println(ex);
		}
		finally {
			this.done=true;
		}
	}
	
	private void process() {
		try {
			Long work = workQueue.take();
			modificationLock.lock();
			heap.insert(work);
		}
		catch(InterruptedException ex) { 
			/** Thread pool is probably being cleaned up.
			 *  Try to finish gracefully */
			finish();
		}
		finally {
			if(modificationLock.isHeldByCurrentThread())
				modificationLock.unlock();
		}
	}
	
	/**
	 * Merge whatever results are available at the time of invocation into another heap. Useful
	 * for merging partial results of multiple workers.
	 */
	public void applyToHeap(BoundedMinHeap overallHeap) {
		acquireWriteLockOnHeap(); /** Has the effect of suspending heap mutation gracefully */
		try {
			heap.merge(overallHeap);
			/**
			 * Ensuring we don't duplicate
			 */
			heap = new BoundedMinHeap(N);
		} finally {
			releaseWriteLockOnHeap();
		}
	}
	
	public void acquireWriteLockOnHeap() {
		modificationLock.lock(); /** Suspend processing when ready */
	}
	
	public void releaseWriteLockOnHeap() {
		if(modificationLock.isHeldByCurrentThread()) /** Resume processing */
			modificationLock.unlock();
	}
	
	public void finish() {
		running.getAndSet(false);
	}
	
	public boolean isDone() {
		return this.done;
	}
}
