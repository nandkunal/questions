package com.lynchdt.questions.topn;

import java.util.ArrayList;

/**
 * A min-heap is a type of binary tree where every Node is greater than or equal to it's parent. 
 *
 * This is useful for Top-N since the minimum property of the heap will 
 * mean the smallest number in the set N so far will be the root node. Removal 
 * of the smallest number can be done in O(1) time. This compares to O(n) time for searching
 * the smallest element in a max heap or unsorted list. After replacement the heap may then be broken,
 * but sifting the replacement number up the heap to restore the heap property
 * can be done in O(h) time. With respect to space, we can use a bounded min-heap of size-N
 * and simply evict the minimum when we hit the bound. The top N numbers are simple O(n) 
 * iteration over the heap. There was no sorting requirement, but heap-sort would fit in 
 * nicely here. I'll come back to that if I have time. 
 * 
 * Implementation adapted from Intro to Algorithms - Chapter 6. 
 * 
 * Guava's Min-Max PriorityQueue could just as easily fit in for top-N with a few tweaks.
 * http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/MinMaxPriorityQueue.html
 **/
public class BoundedMinHeap {
	/**
	 * I'm making the assumption that numbers from Top-N means long-integers. If we have
	 * something other than that, such as doubles or something really really big, or a mixture,
	 * we could generalize this class.
 	 * 
 	 * This is still pretty interesting for Long integers. 
	 */
	private ArrayList<Long>heap;
	/**
	 * Using Array here so N will have to be Integer.MAX_VALUE
	 */
	private int maxSize;
	
	public BoundedMinHeap(int maxSize) {
		this.maxSize = maxSize;
		heap = new ArrayList<>(maxSize);
	}
	
	protected long extractMin() {
		if(heap.size() == 0) { throw new RuntimeException("Heap underflow"); }
		long minimum = heap.get(0);
		heap.set(0, heap.get(heap.size()-1));	
		heap.remove(heap.size()-1);
		/** May have broken heap property */
		minHeapify(0);
		return minimum;
	}
	
	/**
	 * This is the biggest customization here. 
	 * 
	 * Only insert if greater than the smallest node
	 * of the min heap. This leaves us with top-N in the heap
	 * and the minimum element (heap[0]) to evict if necessary.
	 */
	public void insert(long value) {
		if(heap.size()==0) {
			heap.add(value);
		}
		else if (heap.get(0)<value) {
			if(heap.size()==maxSize) {
				extractMin();
			}
			heap.add(Long.MAX_VALUE);
			siftUp(heap.size()-1, value);
		}
	}
	
	/**
	 * I think this is a nicer name than decrease-key. Set the value
	 * at the index, then exchange with parents until in the right position.
	 */
	protected void siftUp(int index, long value) {
		if (index > heap.size()-1) {
			throw new RuntimeException("Index is bigger than heap");
		}
		heap.set(index, value);
		while(index > 0 && heap.get(parent(index)) > heap.get(index)) {
			exchange(index, parent(index));
			index = parent(index);
		}
	}
	
	protected void exchange(int first, int second) {
		long temp = heap.get(first);
	    heap.set(first, heap.get(second));
	    heap.set(second, temp);
	}
	
	/**
	 * There is recursion here - works given some unknown function of heap.size()
	 * and available stack.
	 */
	protected void minHeapify(int currentIndex) {
		int lIndex = leftChild(currentIndex);
		int rIndex = rightChild(currentIndex);
		int smallestIndex;
		if ( lIndex < heap.size() && heap.get(lIndex) < heap.get(currentIndex) ) {
			smallestIndex = lIndex;
		} else {
			smallestIndex = currentIndex;
		}
		if (rIndex < heap.size() && heap.get(rIndex) < heap.get(smallestIndex)) {
			smallestIndex = rIndex;
		}
		if(smallestIndex!=currentIndex) {
			exchange(currentIndex, smallestIndex);
			minHeapify(smallestIndex);
		}
	}
	
	protected void buildMinHeap() {
		for(int i = 0; i <= heap.size()/2; i++) {
			minHeapify(i);
		}
	}
	
	protected boolean verifyHeapProperty() {
		if(heap.size()==0) { return false; }
		for(int parentIndex = 0; parentIndex <= heap.size()/2; parentIndex++) {
			int lIndex = leftChild(parentIndex);
			if(lIndex < heap.size() && heap.get(lIndex) < heap.get(parentIndex)) {
					return false;
			}
			int rIndex = rightChild(parentIndex);
			if(rIndex < heap.size() && heap.get(rIndex) < heap.get(parentIndex)) {
				return false;
			}		
		}
		return true;
	}
	
	public void merge(BoundedMinHeap otherHeap) {
		heap.forEach((element) -> {
			otherHeap.insert(element);
		});
	}
	
	public ArrayList<Long> getHeap() {
		return heap;
	}

	protected int parent(int position) {
		return (position/2);
	}
	
	protected int leftChild(int position) {
		return (2* position) + 1;
	}
	
	protected int rightChild(int position) {
		return (2 * position) + 2;
	}
	
	
	protected BoundedMinHeap(ArrayList<Long> array) {
		this.maxSize=array.size();
		heap = array;
	}
	
	protected void setHeap(ArrayList<Long> heap) {
		this.heap=heap;
	}
	
	@Override
	public String toString() {
		return heap.toString();
	}
}