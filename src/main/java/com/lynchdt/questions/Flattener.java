package com.lynchdt.questions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Takes an arbitrary, nested list of integers and flattens them to an array of integers. There
 * are a couple of steps to making this class work generically i.e. Flattener<T> but the brief
 * was integers, so lets not get generic prematurely. 
 */
public class Flattener {
	/**
	 * Having these as members means we don't have to pass a reference
	 * down the call stack with the recursive call to flatten.
	 * 
	 * Also reduces the size of the method signature by one. 
	 */
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
			throw new UnsupportedOperationException("Don't know what to do with type " +  next.getClass());
		}
		flatten(iterator);
	}
	
	public List<Integer> getFlattened() {
		flattened = new ArrayList<>();
		flatten(integerList.iterator());
		return flattened;
	}
	
	
}
