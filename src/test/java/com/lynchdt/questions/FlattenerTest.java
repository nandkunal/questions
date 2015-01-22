package com.lynchdt.questions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

public class FlattenerTest {
	
	@Rule
	public final ExpectedException thrown=ExpectedException.none();
	
	@Test
	/**
	 * This naming might looks strange for Java, but it's descriptive.
	 */
	public void constructor_givenNull_throws() {
		thrown.expect(NullPointerException.class);
		new Flattener(null);
	}
	
	@Test
	public void getIntegerList_givenConstructed_returnsPassedIntegerList() {
		List<Object> list = testList();
		
		Flattener flattener = new Flattener(list);
		
		assertTrue(list == flattener.getIntegerList());
	}
	
	@Test
	/**
	 * The example seems to imply in-order traversal i.e. depth-first printing. 
	 */
	public void getFlattened_givenSimpleList_flattensToArrayInOrder() {
		final String expectedResult = "[1, 2, 3, 4]";
		Flattener flattener = new Flattener(testList());
		
		List<Integer> flattened = flattener.getFlattened();
		
		assertEquals(expectedResult, flattened.toString());
	}
	
	@Test
	/**
	 * Let's not get too caught up in the nested lists and forget about 
	 * the simple case. Might help catch any bad assumptions made kicking off/terminating
	 * recursion.
	 */
	public void getFlattend_givenOnlyIntegers_flattensToArrayInOrder() {
		List<Object> justIntegers = justIntegers();
		final String expectedResult = justIntegers.toString();
		Flattener flattener = new Flattener(justIntegers);
		
		List<Integer> flattened = flattener.getFlattened();
		
		assertEquals(expectedResult, flattened.toString());
	}
	
	@Test
	public void getFlattened_givenUnsupportedType_throws() {
		Flattener flattener = new Flattener(ImmutableList.of("String", new ArrayList<Double>()));
		
		thrown.expect(UnsupportedOperationException.class);
		flattener.getFlattened();
	}
	
	@Test
	public void getFlattened_givenSelfReference_throws() {
		
	}
	
	@Test
	/**
	 * This is not a unit test - just exploring a concept. Comment out the @Ignore
	 * for some fun!
	 */
	@Ignore 
	public void getFlattened_veryLargeList_probablyCauseStackOverflow() {
		List<Object> largeList = new ArrayList<Object>();
		final int ONE_MILLION_DOLLARS = 1000000;
		
		for(int i = 0; i < ONE_MILLION_DOLLARS; i++)
			largeList.add(i);
		
		Flattener flattener = new Flattener(largeList);
		
		/**
		 * 1 million stack frames created on a stack of 1024kb default. Each frame
		 * has a ref to iterator - lets forget about pointer compression and say
		 * 64-bits per reference plus one local variable (another 64-bits) and
		 * some byte code with a branch - another 1024-bits say. Something like 144Mb. 
		 * 
		 * This will fall over!
		 */
		flattener.getFlattened();
	}
	
	public List<Object> testList() {
		/** I like Guava for things like this. Elegant. */
		return ImmutableList.of(
				ImmutableList.of(1, 2, 
						ImmutableList.of(3)
				),
				4
		);
	}
	
	public List<Object> justIntegers() {
		List<Object> list = new ArrayList<>();	
		for(int i = 0; i < 100; i++)
			list.add(i);
		return list;
	}
}
