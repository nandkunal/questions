package com.lynchdt.questions.topn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extract the top-N numbers from a file set of Long integers encoded as
 * plain-text ASCII.
 * 
 * I'm treating this as a producer/consumer problem in order to get decent utilization
 * and increase running times. There can be N readers of files and M processors of numbers.
 * 
 * Number processors maintain their own lists of top-N numbers they have processed. These
 * are periodically and finally merged into a BoundedMinHeap to get the top-N of the top-N in M.
 */
public class TopN {
	
	/**
	 * A bounded blocking queue allows back-off for the producers and 
	 * blocking takes for the workers. Very convenient. 
	 */
	private BlockingQueue<Long> workQueue;

	private ExecutorService workerExecutor;
	private List<TopNWorker> workers;
	private final int workerCount;
	
	private ExecutorService fileReaderExecutor;
	private List<NumberFileReader> fileReaders;
	
	private final int N;
	private final List<String> files;
	
	/**
	 * The top-N as a union of top-Ns from the workers.
	 */
	private final BoundedMinHeap overallHeap;
	
	/** These are arbitrary */
	private final int UPDATE_INTERVAL = 1000;
	private final int BACKOFF_INTERVAL = 100;
	

	public TopN(List<String> files, int N, int workerCount, int queueSize) {
		workQueue = new ArrayBlockingQueue<Long>(queueSize, false);
		overallHeap = new BoundedMinHeap(N);
		this.workerCount = workerCount;
		this.N=N;
		this.files=files;
	}

	public void execute() throws Exception {
		prepareWorkerPool();
		prepareAndStartFileReader();
		reportProgress();
		reportResult();
	}

	/**
	 * These guys will hang around until there is work to do.
	 */
	private void prepareWorkerPool() {
		workerExecutor = Executors.newCachedThreadPool();
		workers = new ArrayList<>();
		for (int i = 0; i < workerCount; i++) {
			TopNWorker worker = new TopNWorker(N, workQueue);
			workers.add(worker);
			workerExecutor.submit(worker);
		}
	}

	private void prepareAndStartFileReader() {
		fileReaderExecutor = Executors.newFixedThreadPool(7);
		fileReaders = new ArrayList<NumberFileReader>();
		for(String file : files) {
			NumberFileReader reader = new NumberFileReader(workQueue, file);
			fileReaders.add(reader);
			fileReaderExecutor.submit(reader);
		}
	}

	private boolean filesRead() {
		boolean read = true;
		for (NumberFileReader reader : fileReaders) {
			read = read && reader.isFinished();
		}
		return read;
	}

	private long linesRead() {
		long sum = 0;
		for (NumberFileReader reader : fileReaders) {
			sum += reader.getRead();
		}
		;
		return sum;
	}

	/**
	 * Making a bet that N will be small, the lines to read will be reasonably 
	 * large and that some user will want feedback while the operation is in progress. 
	 * 
	 * I think user engagement/communication on progress is important. Reassuring the user
	 * the computer is working, and giving a sense of when the results might be ready. 
	 */
	private void reportProgress() throws Exception {
		System.out.println("Partial Results for Top-" + this.N + " -> ");
		while (!filesRead()) {
			for (TopNWorker worker : workers) {
				worker.applyToHeap(overallHeap);
			}
			overallHeap.heapSort();
			System.out.println("Top " + this.N
					+ " results after about " + linesRead() + " lines "
					+ overallHeap.toString());
			Thread.sleep(UPDATE_INTERVAL);
		}
	}

	public void reportResult() throws Exception {
		if(!filesRead()) { 
			throw new RuntimeException("Cannot report results - file reading in progress.");
		}
		waitForQueueToDrain();
		instructWorkersToFinish();
		waitForWorkersToFinish();
		cleanUp();
		mergePartialResults();
		sortAndPrint();
	}
	
	private void sortAndPrint() {
		overallHeap.heapSort();
		System.out.println("Top-" + this.N + " -> "
			+ overallHeap.toString());
	}
	
	private void waitForQueueToDrain() throws Exception {
		while (workQueue.size() > 0) {
			Thread.sleep(BACKOFF_INTERVAL);
		}
	}
	
	private void instructWorkersToFinish() {
		workers.forEach((worker) -> {
			worker.finish();
		});
	}
	
	private void waitForWorkersToFinish() throws Exception {
		while(workersStillWorking()) {
			Thread.sleep(BACKOFF_INTERVAL);
		}
	}
	
	private boolean workersStillWorking() {
		boolean finishingWork = true;
		for (TopNWorker worker : workers) {
			finishingWork = !worker.isDone();
		};
		return finishingWork;
	}
	
	private void mergePartialResults() {
		workers.forEach((worker) -> {
			worker.applyToHeap(overallHeap);
		});
	}

	public void cleanUp() {
		fileReaderExecutor.shutdownNow();
		workerExecutor.shutdownNow();
	}

	public static void main(String argsv[]) throws Exception {
		if(argsv.length<3) {
			System.out.println("> java TopN <n> <workerCount> <queueSize> file1 [file2 .. fileM] ");
			System.exit(-1);
		}
		int N = Integer.parseInt(argsv[0]);
		int workerCount = Integer.parseInt(argsv[1]);
		int queueSize = Integer.parseInt(argsv[2]);
		List<String> files = new ArrayList<>();
		for(int i = 3; i < argsv.length; i++) {
			files.add(argsv[i]);
		}
		TopN topN = new TopN(files, N, workerCount, queueSize);
		Timer timer = Timer.createAndStart();
		topN.execute();
		topN.cleanUp();
		timer.stop();
		System.out.println("Complete in " + timer.toString());
	}
}
