package com.lynchdt.questions.topn;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Executors.class, TopN.class, TopNTest.class })
@SuppressWarnings("unchecked")
public class TopNTest {
	@Rule public final ExpectedException thrown=ExpectedException.none();

	private TopN topN;
	
	@Before public void setUp() {
		topN = new TopN(ImmutableList.of("file1", "file2"), 5, 6, 7);
	}
	
	
	@Test public void constructor_givenParameters_setsPropertiesCreatesQueueAndHeap() {
		TopN topN = new TopN(ImmutableList.of("file1", "file2"), 5, 6, 7);
		
		assertEquals(5, topN.getHeap().getMaxSize());
		assertEquals(6, topN.workerCount());
		assertEquals(7, topN.getQueueCapacity());
	}
	
	@Test public void constructor_givenNullFiles_throws() {
		thrown.expect(IllegalArgumentException.class);
		new TopN(null, 5, 6, 7);
	}
	
	@Test public void constructor_NisZero_throws() {
		thrown.expect(IllegalArgumentException.class);
		new TopN(ImmutableList.of("file1", "file2"), 0, 6, 7);
	}
	
	@Test public void constructor_zeroWorkers_throws() {
		thrown.expect(IllegalArgumentException.class);
		new TopN(ImmutableList.of("file1", "file2"), 1, 0, 7);
	}
	
	@Test public void constructor_zerQueue_throws() {
		thrown.expect(IllegalArgumentException.class);
		new TopN(ImmutableList.of("file1", "file2"), 1, 1, 0);
	}
	
	@Test public void prepareAndStartWorkerPool_createsPoolSubmitsWork() {
		ExecutorService executor = buildExecutorStub();
	
		List<TopNWorker> fakeWorkers = mock(List.class);
		topN.setWorkers(fakeWorkers);
		
		topN.prepareAndStartWorkerPool();
		
		verify(fakeWorkers, times(topN.workerCount())).add(any(TopNWorker.class));
		verify(executor, times(topN.workerCount())).submit(any(TopNWorker.class));
	}
	
	@Test public void prepareAndStartFileReaders_createsPoolSubmitsWork() {
		ExecutorService executor = buildExecutorStub();
		
		List<NumberFileReader> fakeReaders = mock(List.class);
		topN.setFileReaders(fakeReaders);
		
		topN.prepareAndStartFileReaders();
		
		verify(fakeReaders, times(topN.fileCount())).add(any(NumberFileReader.class));
		verify(executor, times(topN.fileCount())).submit(any(NumberFileReader.class));
	}
	
	private ExecutorService buildExecutorStub() {
		PowerMockito.mockStatic(Executors.class);
		ExecutorService eService = mock(ExecutorService.class);
		Mockito.when(Executors.newFixedThreadPool((anyInt()))).thenReturn(eService);
		return eService;
	}
}
