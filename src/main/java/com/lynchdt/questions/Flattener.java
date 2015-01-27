package com.lynchdt.questions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;

/**
 * Takes an arbitrary, nested list of integers and flattens them to an array of integers. There
 * are a couple of steps to making this class work generically i.e. Flattener<T> but the brief
 * was integers, so lets not get generic prematurely. Any classes other than List<Object> or
 * Integer are rejected with an exception.
 * 
 * I didn't have time to implement anything that could possibly detect and break/report cycles 
 * i.e. for the case that some level of nesting contains a reference to a previous level. See the Unit 
 * tests for expectations of what will happen. A Hash<Set> containing references to visited Lists
 * could be built up and examined. Flattening can stop at the first detection of a cycle, recovering 
 * might be a bit trickier.
 */
public class Flattener {

	private List<Object> integerList;
	private List<Integer> flattened;
	
	public Flattener(List<Object> integerList) {
		Preconditions.checkNotNull(integerList, "Please a possibly nested array of integers to flatten");
		
		this.integerList=integerList;
	}
	
	protected List<Object> getIntegerList() {
		return this.integerList;
	}
	
	private void flatten(Iterator<?> iterator) {
		if(!iterator.hasNext()) { return; }
		Object next = iterator.next();
		if(next instanceof Integer) {
			flattened.add((Integer)next);
		} else if(next instanceof List<?>) {
			flatten(((List<?>) next).iterator());
		} else {
			throw new UnsupportedOperationException("Don't know what to do with class " +  next.getClass());
		}
		flatten(iterator);
	}
	
	public List<Integer> getFlattenedByRecursion() {
		flattened = new ArrayList<>();
		flatten(integerList.iterator());
		return flattened;
	}
	
	public List<Integer> getFlattenedUsingStack() {
		Stack<Iterator<?>> stack = new Stack<>();
		flattened = new ArrayList<>();
		/** Seed the stack */
		stack.push(integerList.iterator());
		
		while(!stack.isEmpty()) {
			Iterator<?> currentLevel = stack.peek();
			if(!currentLevel.hasNext()) { 
				stack.pop(); 	/** All elements evaluated in this branch */
			} else  {
				Object next = currentLevel.next();
				if(next instanceof Integer) {
					flattened.add((Integer)next);
				} else if(next instanceof List<?>) {
					stack.push(((List<?>) next).iterator());
				} else {
					throw new UnsupportedOperationException("Don't know what to do with class " +  next.getClass());
				}
			}
		}
		return flattened;
	}
}
