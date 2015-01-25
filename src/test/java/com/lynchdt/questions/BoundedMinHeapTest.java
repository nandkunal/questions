package com.lynchdt.questions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BoundedMinHeapTest {

	private BoundedMinHeap heap;
	
	@Before
	public void setUp() {
		heap = new BoundedMinHeap(1);
	}
	
	@Test
	public void parent_givenPosition_returnsMid() {
		/** Needs to be the floor if indivisible by 2 */
		int uneven = 5;
		int expectedUnevenResult = 2;
		int even = 8;
		int expectedEvenResult = 4;
		
		int unevenResult = heap.parent(uneven);
		int evenResult = heap.parent(even);
		
		assertEquals(expectedUnevenResult, unevenResult);
		assertEquals(expectedEvenResult, evenResult);
	}
	
	@Test
	public void leftChild_givenPosition_doublesAddsOne() {
		int result = heap.leftChild(5);
		
		assertEquals(11, result);
	}
	
	@Test
	public void rightChild_givenPosition_doublesAddsTwo() {
		int result = heap.rightChild(3);
		
		assertEquals(8, result);
	}
	
	@Test
	public void exchange_givenTwoPointersWithinBounds_exchangesElements() {
		heap = new BoundedMinHeap(Lists.newArrayList(5L, 6L, 7L));
		
		heap.exchange(0, 2);
		
		assertEquals("[7, 6, 5]", heap.toString());
	}
	
	@Test
	public void buildMinHeap_givenSpecificArray_buildsMinHeap() {
		heap = new BoundedMinHeap(Lists.newArrayList(28L, 3L, 1L, 4L, 9L ,6L));
		
		heap.buildMinHeap();
		
		assertTrue(heap.verifyHeapProperty());
	}
	
	@Test 
	public void extractMin_givenHeap_extractsMinLeavesRemainderAsHeap() {
		heap = new BoundedMinHeap(Lists.newArrayList(28L, 3L, 1L, 4L, 9L ,6L));
		heap.buildMinHeap();
		
		heap.extractMin();
		
		assertTrue(heap.verifyHeapProperty());
	}
	
	@Test
	public void insert_emptyHeap_insertsLeavingHeap() {
		heap = new BoundedMinHeap(6);
		Lists.newArrayList(28L, 3L, 1L, 4L, 9L ,6L).forEach((element) -> {
			heap.insert(element);
			/** After every insertion we need a min heap */
			assertTrue(heap.verifyHeapProperty());
		});
	}
	
	
	@Test
	public void verifyHeapProperty_givenMinHeap_returnsTrue() {
		/**
		 * This is a min heap because for each i, element at 
		 * 2 * i + 1 and 2 * i + 2 are greater or equal
		 */
		heap.setHeap(Lists.newArrayList(1L, 3L, 6L, 4L, 9L, 28L));
		
		assertTrue(heap.verifyHeapProperty());
	}
	
	@Test
	/**
	 * Warning - Potential existential debate. Is an empty structure
	 * 			 with potential to act as a min heap
	 * 			 a min heap? ;-) Lets say no for now. 
	 */
	public void verifyHeapProperty_givenEmptyHeap_returnsFalse() {
		heap.setHeap(Lists.newArrayList());
		
		assertFalse(heap.verifyHeapProperty());
	}
	
	@Test
	public void verifyHeap_givenNonMinHeap_returnsFalse() {
		heap.setHeap(Lists.newArrayList(1L, 22L, 6L, 16L, 9L, 28L));
		
		assertFalse(heap.verifyHeapProperty());
	}
}
