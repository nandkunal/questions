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
	
	private final int ONE_MILLION_DOLLARS = 10000000;
	
	/** Strings are a nice short-hand when comparing for equality later */
	private final String QUESTION_TEST_RESULT = "[1, 2, 3, 4]";
	private final String OTHER_TEST_RESULT = "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]";
	
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
		List<Object> list = questionList();
		
		Flattener flattener = new Flattener(list);
		
		assertTrue(list == flattener.getIntegerList());
	}
	
	@Test
	/**
	 * The example seems to imply in-order traversal i.e. depth-first printing. 
	 */
	public void getFlattenedByRecursion_givenSimpleList_flattensToArrayInOrder() {
		Flattener flattener = new Flattener(questionList());
		
		List<Integer> flattened = flattener.getFlattenedByRecursion();
		
		assertEquals(QUESTION_TEST_RESULT, flattened.toString());
	}
	
	@Test
	/**
	 * Let's not get too caught up in the nested lists and forget about 
	 * the simple case. Might help catch any bad assumptions made kicking off/terminating
	 * recursion.
	 */
	public void getFlattenedByRecursion_givenOnlyIntegers_flattensInOrder() {
		List<Object> justIntegers = justIntegers(100);
		final String expectedResult = justIntegers.toString();
		Flattener flattener = new Flattener(justIntegers);
		
		List<Integer> flattened = flattener.getFlattenedByRecursion();
		
		assertEquals(expectedResult, flattened.toString());
	}
	
	@Test
	public void getFlattenedByRecursion_givenUnsupportedType_throws() {
		Flattener flattener = new Flattener(ImmutableList.of("String", new ArrayList<Double>()));
		
		thrown.expect(UnsupportedOperationException.class);
		flattener.getFlattenedByRecursion();
	}
	
	@Test
	/**
	 * This is not a unit test - just exploring a concept. Comment out the @Ignore
	 * for some fun!
	 */
	@Ignore 
	public void getFlattened_veryLargeList_veryProbablyCauseStackOverflow() {
		List<Object> largeList = justIntegers(ONE_MILLION_DOLLARS);
		
		Flattener flattener = new Flattener(largeList);
		
		/**
		 * 1 million stack frames created on a stack of 1024kb default. Each frame
		 * has a ref to iterator - lets forget about pointer compression and say
		 * 64-bits per reference plus one local variable (another 64-bits) and
		 * some byte code with a branch - another 1024-bits say. Something like 144Mb. 
		 * 
		 * This will fall over!
		 */
		thrown.expect(StackOverflowError.class);
		flattener.getFlattenedByRecursion();
	}
	
	@Test
	/**
	 * This is too slow to be a frequently run unit test, but given the calculations
	 * above there should be enough Heap for it to complete on a developer machine. The key 
	 * is to mimic how recursion would wind and unwind the stack -
	 * using an actual stack. Since stack will be allocated on the Heap we will have lots
	 * more memory to exhaust.
	 * 
	 * Comment out @Ignore for some more fun
	 */
	@Ignore
	public void getFlattendWithStack_givenLargeList_flattensInOrder() {
		Flattener flattener = new Flattener(justIntegers(ONE_MILLION_DOLLARS));
		
		List<Integer> flattend = flattener.getFlattenedUsingStack();
		
		assertEquals(ONE_MILLION_DOLLARS, flattend.size());
	}
	
	@Test
	public void getFlattendWithStack_givenSimpleList_flattensInOrder() {
		Flattener flattener = new Flattener(questionList());
		
		List<Integer> flattened = flattener.getFlattenedUsingStack();
		
		assertEquals(QUESTION_TEST_RESULT, flattened.toString());
	}
	
	
	@Test
	/**
	 * An example with a bit more nesting - for kicks. 
	 */
	public void getFlattendWithStack_givenOtherList_flattensInOrder() {
		Flattener flattener = new Flattener(otherList());
		
		List<Integer> flattened = flattener.getFlattenedUsingStack();
		
		assertEquals(OTHER_TEST_RESULT, flattened.toString());
	}
	
	
	@Test
	public void getFlattenedUsingStack_givenUnsupportedType_throws() {
		Flattener flattener = new Flattener(ImmutableList.of("String", new ArrayList<Double>()));
		
		thrown.expect(UnsupportedOperationException.class);
		flattener.getFlattenedUsingStack();
	}
	
	@Test
	public void getFlattenedUsingRecursion_selfReference_overflowsStack() {
		Flattener flattener = new Flattener(selfReference());
		
		thrown.expect(StackOverflowError.class);
		flattener.getFlattenedByRecursion();
	}
	
	@Test
	/**
	 * Not a unit test - this will go on for a while before unpredictable things
	 * happen. Commenting out @Ignore will exhaust your heap at least. 
	 */
	@Ignore
	public void getFlattenedUsingRecursion_selfReference_exhaustsHeap() {
		Flattener flattener = new Flattener(selfReference());

		flattener.getFlattenedUsingStack();
	}
	
	
	public List<Object> selfReference() {
		List<Object> root = new ArrayList<>();
		root.add(1);
		root.add(2);
		root.add(root);
		return root;
	}

	public List<Object> questionList() {
		return ImmutableList.of(
					ImmutableList.of(1, 2, 
						ImmutableList.of(3)
				),
				4
		);
	}
	
	public List<Object> otherList() {
		return ImmutableList.of(
					ImmutableList.of(1,2,
						ImmutableList.of(3,4,
							ImmutableList.of(5,6,7,8,
									ImmutableList.of(9, 10)
							)
						)
				),
				11
		);
	}
	
	public List<Object> justIntegers(int count) {
		List<Object> list = new ArrayList<>();	
		for(int i = 0; i < count; i++)
			list.add(i);
		return list;
	}
}
