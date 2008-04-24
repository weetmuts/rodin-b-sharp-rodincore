/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.seqprover.tactics.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eventb.core.seqprover.IProofRule;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.internal.core.seqprover.eventbExtensions.AbstractManualInference;
import org.eventb.internal.core.seqprover.eventbExtensions.AbstractRewriter;
import org.eventb.internal.core.seqprover.eventbExtensions.Conj;
import org.eventb.internal.core.seqprover.eventbExtensions.FunOvr;

/**
 * Common implementation for verifying rule applications to a proof subtree. The
 * intent is to simplify writing tests about tactic application on a proof node
 * (or more exactly the subtree rooted at that node).
 * <p>
 * Clients should use code similar to:
 * 
 * <pre>
 * assertRulesApplied(node, conjI(empty, empty, empty));
 * </pre>
 * 
 * where <code>node</code> is a proof tree node and the second parameter is
 * the expected shape of the proof tree rooted at the given node.
 * </p>
 * 
 * @author Laurent Voisin
 */
public abstract class TreeShape {

	private static class ConjIShape extends TreeShape {

		public ConjIShape(TreeShape[] expChildren) {
			super(expChildren);
		}

		@Override
		protected void checkInput(IReasonerInput input) {
			AbstractRewriter.Input i = (AbstractRewriter.Input) input;
			assertNull(i.getPred());
		}

		@Override
		protected String getReasonerID() {
			return Conj.REASONER_ID;
		}
	}

	private static class EmptyShape extends TreeShape {

		public EmptyShape() {
			super(null);
		}

		public void check(IProofTreeNode node) {
			assertTrue(node.isOpen());
		}

		@Override
		protected void checkInput(IReasonerInput input) {
			assert false;
		}

		@Override
		protected String getReasonerID() {
			assert false;
			return null;
		}
	}

	private static class FunOvrShape extends TreeShape {

		protected final String position;

		public FunOvrShape(String position, TreeShape[] expChildren) {
			super(expChildren);
			this.position = position;
		}

		@Override
		protected void checkInput(IReasonerInput input) {
			AbstractManualInference.Input i = (AbstractManualInference.Input) input;
			assertNull(i.getPred());
			assertEquals(position, i.getPosition().toString());
		}

		@Override
		protected String getReasonerID() {
			return FunOvr.REASONER_ID;
		}
	}

	public static final TreeShape empty = new EmptyShape();

	/**
	 * Ensures that the proof subtree rooted at the given node as the shape
	 * described by the <code>expected</code> parameter.
	 * 
	 * @param node
	 *            a proof tree node
	 * @param expected
	 *            a description of the expected proof subtree shape
	 */
	public static void assertRulesApplied(IProofTreeNode node,
			TreeShape expected) {
		expected.check(node);
	}

	public static TreeShape conjI(TreeShape... children) {
		return new ConjIShape(children);
	}

	public static TreeShape funOvr(String position, TreeShape... children) {
		return new FunOvrShape(position, children);
	}

	protected final TreeShape[] expChildren;

	public TreeShape(TreeShape[] expChildren) {
		this.expChildren = expChildren;
	}

	public void check(IProofTreeNode node) {
		final IProofRule rule = node.getRule();
		assertNotNull(rule);
		assertEquals(getReasonerID(), rule.generatedBy().getReasonerID());
		checkInput(rule.generatedUsing());
		checkChildren(node);
	}

	protected void checkChildren(IProofTreeNode node) {
		final IProofTreeNode[] actChildren = node.getChildNodes();
		final int len = expChildren.length;
		assertEquals(len, actChildren.length);
		for (int i = 0; i < len; ++i) {
			expChildren[i].check(actChildren[i]);
		}
	}

	protected abstract void checkInput(IReasonerInput input);

	protected abstract String getReasonerID();
}