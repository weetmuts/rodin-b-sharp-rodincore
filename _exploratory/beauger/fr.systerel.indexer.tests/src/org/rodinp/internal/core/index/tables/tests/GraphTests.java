package org.rodinp.internal.core.index.tables.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rodinp.internal.core.index.tables.Graph;
import org.rodinp.internal.core.index.tables.Node;
import org.rodinp.internal.core.index.tests.IndexTests;
import org.rodinp.internal.core.index.tests.IndexTestsUtil;

public class GraphTests extends IndexTests {

	public GraphTests(String name) {
		super(name, true);
	}

	private static Graph<Integer> graph = new Graph<Integer>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		graph.clear();
		super.tearDown();
	}

	private static void setPreds(Graph<Integer> iter, Integer label,
			Integer... preds) {

		iter.setPredecessors(label, preds);
	}

	private static void remove(Graph<Integer> iter, Integer label) {
		final Node<Integer> node = iter.getOrCreateNode(label);
		iter.remove(node);
	}

	private static List<Integer> getLabels(List<Node<Integer>> nodes) {
		List<Integer> result = new ArrayList<Integer>();
		for (Node<Integer> node : nodes) {
			result.add(node.getLabel());
		}
		return result;
	}

	private void assertLabels(Graph<Integer> gr, Integer... labels) {
		final List<Integer> expected = Arrays.asList(labels);
		final List<Node<Integer>> nodes = gr.getNodes();
		final List<Integer> actual = getLabels(nodes);

		assertEquals("Bad length for: " + nodes, expected.size(), actual.size());

		assertTrue("Not all present in: " + nodes, actual.containsAll(expected));
	}

	private void assertEmptyPreds(Graph<Integer> gr, Integer label) {
		final List<Integer> preds = gr.getPredecessors(label);
		assertTrue("should be empty", preds.isEmpty());
	}

	public void testRemoveFirst() throws Exception {
		setPreds(graph, 2, 1);
		remove(graph, 1);

		assertLabels(graph, 2);
		assertEmptyPreds(graph, 2);
	}

	public void testRemoveLast() throws Exception {
		setPreds(graph, 2, 1);
		remove(graph, 2);

		assertLabels(graph, 1);
	}

	public void testRemoveInner() throws Exception {
		setPreds(graph, 2, 1);
		setPreds(graph, 3, 2);

		remove(graph, 2);

		assertLabels(graph, 1, 3);
		assertEmptyPreds(graph, 3);
	}

	public void testSetGetPredecessors() {
		graph.setPredecessors(2, IndexTestsUtil.makeIntArray(1));
		final List<Integer> predecessors = graph.getPredecessors(2);

		IndexTestsUtil.assertPredecessors(predecessors, 1);
	}

	public void testSetGetSeveralPredecessors() {
		graph.setPredecessors(3, IndexTestsUtil.makeIntArray(1, 2));
		final List<Integer> predecessors = graph.getPredecessors(3);

		IndexTestsUtil.assertPredecessors(predecessors, 1, 2);
	}

	public void testGetNoSuchLabel() throws Exception {
		try {
			graph.getPredecessors(1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	public void testClear() {
		setPreds(graph, 3, 1, 2);

		graph.clear();
		final List<Node<Integer>> nodes = graph.getNodes();

		assertTrue("graph not properly cleared", nodes.isEmpty());
	}

	public void testGetNodes() {
		setPreds(graph, 3, 1, 2);

		final List<Node<Integer>> nodes = graph.getNodes();
		assertEquals("bad nodes size in: " + nodes, 3, nodes.size());

		for (Node<Integer> node : nodes) {
			switch (node.getLabel()) {
			case 1:
				assertEmptyPreds(graph, 1);
			case 2:
				assertEmptyPreds(graph, 2);
			case 3:
				IndexTestsUtil.assertPredecessors(graph.getPredecessors(3), 1,
						2);
			}
		}
	}

	public void testGetOrCreateNodeCreate() {
		final Node<Integer> node = graph.getOrCreateNode(1);
		
		assertEquals("bad node label", Integer.valueOf(1), node.getLabel());
		assertLabels(graph, 1);
		assertEmptyPreds(graph, 1);
	}

	public void testGetOrCreateNodeGet() {
		graph.getOrCreateNode(1);
		
		final Node<Integer> node = graph.getOrCreateNode(1);
		
		assertEquals("bad node label", Integer.valueOf(1), node.getLabel());
		assertLabels(graph, 1);
		assertEmptyPreds(graph, 1);
	}

}
