package org.rodinp.internal.core.index.tables.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

import org.rodinp.internal.core.index.tables.TotalOrder;

public class TotalOrderTests extends TestCase {

	private static final TotalOrder<Integer> order = new TotalOrder<Integer>();

	private static Integer[] makeIntArray(Integer... integers) {
		return integers;
	}

	private static void assertOrderedIteration(TotalOrder<Integer> iter,
			Integer... expectedOrder) {
		for (Integer i : expectedOrder) {
			assertNext(iter, i);
		}
		assertNoNext(iter);
	}

	private static void assertNext(TotalOrder<Integer> iter, Integer i) {
		final boolean hasNext = iter.hasNext();
		assertTrue("Iterator should have next", hasNext);
		final Integer next = iter.next();
		assertEquals("Bad next element", i, next);
	}

	private static void assertAllIteratedOnce(TotalOrder<Integer> iter,
			Integer... expected) {

		final Set<Integer> expSet = new HashSet<Integer>(Arrays
				.asList(expected));

		while (!expSet.isEmpty()) {
			final boolean hasNext = iter.hasNext();

			assertTrue("Iterator should have next", hasNext);

			final Integer next = iter.next();
			final boolean containsNext = expSet.contains(next);

			assertTrue("Unexpected iterated element " + next, containsNext);
			expSet.remove(next);
		}

	}

	private static void assertAllIteratedOnceToEnd(TotalOrder<Integer> iter,
			Integer... expected) {

		assertAllIteratedOnce(iter, expected);
		assertNoNext(iter);
	}

	private static void assertPartitionOrder(TotalOrder<Integer> iter,
			Integer[] before, Integer[] after) {

		assertAllIteratedOnce(iter, before);
		assertAllIteratedOnceToEnd(iter, after);
		assertNoNext(iter);
	}

	private static void assertNoNext(TotalOrder<Integer> iter) {

		final boolean hasNext = iter.hasNext();
		assertFalse("Should not have next", hasNext);
	}

	private static void setPreds(TotalOrder<Integer> iter, Integer label,
			Integer... preds) {

		setPreds(iter, true, label, preds);
	}

	private static void setPreds(TotalOrder<Integer> iter, boolean setToIter,
			Integer label, Integer... preds) {

		iter.setPredecessors(label, preds);

		if (setToIter) {
			setToIter(iter, label);
			setToIter(iter, preds);
		}
	}

	private static void setToIter(TotalOrder<Integer> iter, Integer... ints) {

		for (Integer i : ints) {
			iter.setToIter(i);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		order.clear();
		super.tearDown();
	}

	public void testSetPredecessors() {
		setPreds(order, 2, 1);

		assertOrderedIteration(order, 1, 2);
	}

	public void testSetSeveralPreds() throws Exception {
		setPreds(order, 3, 1, 2);

		assertPartitionOrder(order, makeIntArray(1, 2), makeIntArray(3));
	}

	public void testContains() {
		setPreds(order, 2, 1);
		setPreds(order, 3, 1);

		assertContains(order, 1);
		assertContains(order, 2);
		assertContains(order, 3);
	}

	private void assertContains(TotalOrder<Integer> totalOrder, Integer i) {
		assertTrue("Should contain element " + i, totalOrder.contains(i));
	}

	public void testRemoveFirst() throws Exception {
		setPreds(order, 2, 1);
		order.remove(2);

		assertOrderedIteration(order, 1);
	}

	public void testRemoveLast() throws Exception {
		setPreds(order, 2, 1);
		order.remove(1);

		assertOrderedIteration(order, 2);
	}

	public void testRemoveInner() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);

		order.remove(2);

		assertOrderedIteration(order, 1, 3);
	}

	public void testClear() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 4, 3);

		order.clear();

		assertNoNext(order);
	}

	public void testHasNext() throws Exception {
		setPreds(order, 2, 1);

		assertNext(order, 1);
		assertNext(order, 2);

		assertNoNext(order);
	}

	public void testIterNext() {
		setPreds(order, 2, 1);

		assertNext(order, 1);
	}

	public void testIterNoMoreNext() throws Exception {
		setPreds(order, 2, 1);
		order.next();
		order.next();

		assertNoNext(order);
		try {
			order.next();
		} catch (NoSuchElementException e) {
			return;
		}
		fail("Calling next with no more next element should raise NoSuchElementException");
	}

	public void testIterNoMoreMarked() throws Exception {
		setPreds(order, false, 2, 1);

		assertNoNext(order);
		try {
			order.next();
		} catch (NoSuchElementException e) {
			return;
		}
		fail("Calling next with no more elements set to iter should raise NoSuchElementException");
	}

	public void testIterRemove() {
		setPreds(order, 2, 1);

		order.next();
		order.remove();

		assertNext(order, 2);
	}

	public void testRemoveLabel() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);

		order.remove(2);

		assertAllIteratedOnceToEnd(order, 1, 3);
	}

	public void testSetToIter() throws Exception {
		setPreds(order, false, 2, 1);

		order.setToIter(2);

		assertNext(order, 2);
		assertNoNext(order);
	}

	public void testSetToIterSeveralTimesTheSame() throws Exception {
		setPreds(order, false, 2, 1);

		order.setToIter(2);
		order.setToIter(2);

		assertNext(order, 2);
		assertNoNext(order);
	}

	public void testSetToIterSuccessors() throws Exception {
		setPreds(order, false, 2, 1);
		setPreds(order, false, 3, 1);

		order.setToIter(1);
		order.next();

		order.setToIterSuccessors();

		assertAllIteratedOnceToEnd(order, 2, 3);
	}

	public void testSTISuccCycle() throws Exception {
		setPreds(order, false, 2, 1);
		setPreds(order, false, 3, 2);
		setPreds(order, false, 4, 3);
		setPreds(order, false, 1, 4);

		int i = 3;
		order.setToIter(i);
		while (order.hasNext()) {
			assertNext(order, i);
			order.setToIterSuccessors();
			i = (i % 4) + 1;
		}
	}

	public void testCycle1() throws Exception {
		try {
			setPreds(order, 1, 1);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("Setting a node as a self-predecessor should raise IllegalArgumentException");
	}

	public void testCycle2() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 1, 2);

		assertAllIteratedOnceToEnd(order, 1, 2);
	}

	public void testCycle3() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 1, 3);

		assertAllIteratedOnceToEnd(order, 1, 2, 3);
	}

	public void testCycle4() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 4, 3);
		setPreds(order, 1, 4);

		assertAllIteratedOnceToEnd(order, 1, 2, 3, 4);
	}

	public void testCycle4Iter24() throws Exception {
		setPreds(order, false, 2, 1);
		setPreds(order, false, 3, 2);
		setPreds(order, false, 4, 3);
		setPreds(order, false, 1, 4);

		setToIter(order, 2, 4);
		assertAllIteratedOnceToEnd(order, 2, 4);
	}

	public void testTwoSeparateCycles() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 4, 3);
		setPreds(order, 1, 4);

		setPreds(order, 12, 11);
		setPreds(order, 13, 12);
		setPreds(order, 14, 13);
		setPreds(order, 11, 14);

		assertAllIteratedOnceToEnd(order, 1, 2, 3, 4, 11, 12, 13, 14);
	}

	public void testTwoLinkedCycles() throws Exception {
		// linked by node 4 -> 11
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 4, 3);
		setPreds(order, 1, 4);

		setPreds(order, 12, 11);
		setPreds(order, 13, 12);
		setPreds(order, 14, 13);
		setPreds(order, 11, 14, 4);

		assertAllIteratedOnceToEnd(order, 1, 2, 3, 4, 11, 12, 13, 14);
	}

	public void testTwoCyclesCommonNode() throws Exception {
		// 14 is the common node
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 14, 3, 13);
		setPreds(order, 1, 14);

		setPreds(order, 12, 11);
		setPreds(order, 13, 12);
		setPreds(order, 11, 14);

		assertAllIteratedOnceToEnd(order, 1, 2, 3, 11, 12, 13, 14);
	}

	public void testJoinedCycles() throws Exception {
		setPreds(order, 2, 1);
		setPreds(order, 3, 2);
		setPreds(order, 4, 3, 2);
		setPreds(order, 1, 4);

		assertAllIteratedOnceToEnd(order, 1, 2, 3, 4);
	}

	public void testModifyBeforeIter() throws Exception {
		setPreds(order, 2, 1);

		assertNext(order, 1);
		assertNext(order, 2);

		setPreds(order, 1, 0);

		// iteration should restart from 0
		assertOrderedIteration(order, 0, 1, 2);
	}

	// TODO add tests with modifications during iteration
}
