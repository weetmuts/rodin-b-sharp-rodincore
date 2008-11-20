/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
  *******************************************************************************/

package fr.systerel.editor.documentModel;

import java.util.ArrayList;

import org.eventb.core.IEventBRoot;
import org.rodinp.core.IRodinElement;


/**
 * Maps <code>Intervals</code> to a document.
 * The following rule applies for Intervals:
 * An editable is never next to another editable interval.
 * For each offset there can be at most three intervals at that position.
 * Only editable intervals can be zero length.
 *
 */
public class DocumentMapper {
	
	private ArrayList<Interval> intervals = new ArrayList<Interval>();
	private IEventBRoot root;
	private Interval previous;
	
	/**
	 * Adds an interval to the document mapper at the end of the list.
	 * The intervals must be added in the order they appear in the text!
	 * @param interval
	 * @throws Exception
	 */
	public void addInterval(Interval interval) throws Exception {
		if (intervals.size() > 0) {
			if (intervals.get(intervals.size() -1).compareTo(interval) > 0) {
				throw new Exception("Insertion must be sorted");
			}
		}
		intervals.add(interval);
		
	}

	/**
	 * Adds an interval to the document mapper just after a given interval. If
	 * the given previous interval is not found in the list, the new interval is
	 * added at the end of the list. The intervals must be added in the order
	 * they appear in the text!
	 * 
	 * @param interval
	 * @param previous
	 * @throws Exception
	 */
	public void addIntervalAfter(Interval interval, Interval previous) {
		int index = intervals.indexOf(previous);
		if (index >= 0 && index < intervals.size()) {
			intervals.add(index +1, interval);
		} else {
			try {
				addInterval(interval);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * returns all intervals that are contained in the range starting from offset
	 * @param offset The offset of the range.
	 * @param length The length of the range
	 * @return All intervals that are found in the range.
	 */
	public Interval[] findIntervals(int offset, int length) {
		int index =  findFirstIntervalIndex(offset);
		if (index >= 0) {
			ArrayList<Interval> results =  new ArrayList<Interval>();
			for (int i = index; i < intervals.size(); i++) {
				if (intervals.get(i).getOffset() <= offset +length) {
					results.add(intervals.get(i));
				}
				
			}
			return results.toArray(new Interval[results.size()]);
		}
		else if (intervals.size() > 0 && index < intervals.get(0).getOffset()) {
			ArrayList<Interval> results =  new ArrayList<Interval>();
			for (Interval interval: intervals) {
				if (interval.getOffset() <= offset +length) {
					results.add(interval);
				}
				
			}
			return results.toArray(new Interval[results.size()]);
			
		}
		
		return new Interval[0];
	}
	
	/**
	 * Binary search to find an interval that contains an offset
	 * @param offset
	 * @return
	 */
	private int findIntervalIndex(int offset) {
		int low = 0;
		int high =  intervals.size();
		int mid;
		while  (low <= high ) {
			mid =  (low + high)/2;
			
			if (intervals.get(mid).getOffset() > offset) {
				high =  mid -1;
			}
			else if (intervals.get(mid).getOffset() < offset 
					&& (intervals.get(mid).getOffset() + intervals.get(mid).getLength() < offset)) {
				low =  mid +1;
			}
			else return mid;
		}
		return -1;
	}
	
	/**
	 * Finds the first interval that contains an offset (includes interval
	 * ending at that position)
	 * 
	 * @param offset
	 * @return
	 */
	protected int findFirstIntervalIndex(int offset) {
		int result = findIntervalIndex(offset);
		//check the two previous intervals.
		if (result > 0) {
			Interval previous = intervals.get(result- 1);
			if (previous.getOffset() +previous.getLength() >= offset) {
				result = result -1;
			}
		}
		if (result > 0) {
			Interval previous = intervals.get(result- 1);
			if (previous.getOffset() +previous.getLength() >= offset) {
				result = result -1;
			}
		}
		
		return result;
	}

	/**
	 * Finds an editable interval for a given offset
	 * @param offset
	 * @return the editable interval at the given offset or <code>null</code> if none exists.
	 */
	public Interval findEditableInterval(int offset) {
		int index = findEditableIntervalIndex(offset);
		if (index >= 0) {
			return intervals.get(index);
		}
		return null;
	}
	
	protected int findEditableIntervalIndex(int offset) {
		//an editable is never next to another editable interval (or in the same position)
		//for each offset there can be at most three intervals at that position
		//only editable intervals can be zero length.
		int index = findFirstIntervalIndex(offset);
		if (index >= 0 && index < intervals.size()) {
			Interval interval = intervals.get(index);
			if (interval.isEditable()) {
				return index;
			} 
			// try the next one
			if (index + 1 < intervals.size()) {
				interval = intervals.get(index +1);
				if (offset >= interval.getOffset() && offset <= interval.getOffset() +interval.getLength()) {
					if (interval.isEditable()) {
						return index +1;
					} 
				}
				
			}
		}
		return -1;
	}

	
	protected Interval findFirstInterval(int offset) {
		int index = findFirstIntervalIndex(offset);
		if (index >= 0 && index < intervals.size()) {
			return intervals.get(index);
		}
		return null;
	}
	
	/**
	 * Processes an interval. Creates and adds a new interval, if there exists
	 * none yet. Otherwise updates length and offset. It is expected that this
	 * method is called in the order the intervals appear in the document.
	 * 
	 * @param offset
	 * @param length
	 * @param element
	 * @param contentType
	 */
	public void processInterval(int offset, int length, IRodinElement element, String contentType) {
		Interval inter;
		if (Interval.isEditableType(contentType)) {
			inter = findInterval(element, contentType);
			if (inter != null) {
				inter.setLength(length);
				inter.setOffset(offset);
			} else {
				inter= new Interval(offset, length, element, contentType);
				try {
					addIntervalAfter(inter, previous);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if (intervals.indexOf(previous) < intervals.size()-1){
				inter = intervals.get(intervals.indexOf(previous) +1);
				inter.setLength(length);
				inter.setOffset(offset);
			} else {
				inter= new Interval(offset, length, element, contentType);
				try {
					addInterval(inter);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		previous = inter;
		
		
	}
	
	/**
	 * Finds the first interval that belongs to the given element
	 * 
	 * @param element
	 * @return the first interval that belongs to the given element
	 */
	public Interval findInterval(IRodinElement element) {
		for (Interval interval : intervals) {
			if (element.equals(interval.getElement())) {
				return interval;
			}
		}
		return null;
	}

	/**
	 * Finds the first interval that belongs to the given element and has a
	 * given contentType. 
	 * 
	 * @param element
	 * @param contentType
	 * @return the first interval that belongs to the given element
	 */
	public Interval findInterval(IRodinElement element, String contentType) {
		for (Interval interval : intervals) {
			if (element.equals(interval.getElement()) && interval.getContentType().equals(contentType)) {
				return interval;
			}
		}
		return null;
	}
	
	
	public ArrayList<Interval> getIntervals() {
		return intervals;
	}
	
//	public void resetIntervals() {
//		intervals.clear();
//	}

	public IEventBRoot getRoot() {
		return root;
	}

	public void setRoot(IEventBRoot root) {
		this.root = root;
	}
	
	public void resetPrevious() {
		previous = null;
	}

}
