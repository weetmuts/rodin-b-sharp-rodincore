/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.seqprover.proofBuilderTests;

import static org.eventb.core.seqprover.proofBuilderTests.Factory.P;
import static org.eventb.core.seqprover.proofBuilderTests.Factory.Q;
import static org.eventb.core.seqprover.proofBuilderTests.Factory.bfalse;
import static org.eventb.core.seqprover.proofBuilderTests.Factory.land;
import static org.eventb.core.seqprover.proofBuilderTests.Factory.makeProofTreeNode;
import static org.eventb.core.seqprover.proofBuilderTests.Factory.not;
import static org.eventb.core.seqprover.proofBuilderTests.ProofTreeShape.ctrHyp;
import static org.eventb.core.seqprover.proofBuilderTests.ProofTreeShape.hyp;
import static org.eventb.core.seqprover.proofBuilderTests.ProofTreeShape.open;
import static org.eventb.core.seqprover.proofBuilderTests.ProofTreeShape.splitGoal;
import static org.junit.Assert.assertEquals;

import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IProofTreeNode;
import org.junit.Test;

/**
 * Ensures that the proof tree shape mechanism used for tests works as expected.
 * 
 * @author Laurent Voisin
 */
public class ProofTreeShapeTests {

	private static void assertCreateCheck(IProofTreeNode node,
			ProofTreeShape shape) {
		shape.create(node);
		shape.check(node);
	}

	private static void assertDischarged(IProofTreeNode node) {
		assertEquals(IConfidence.DISCHARGED_MAX, node.getConfidence());
	}

	@Test
	public void openNode() throws Exception {
		final IProofTreeNode node = makeProofTreeNode(P);
		assertCreateCheck(node, open);
	}

	@Test
	public void ctrHypNode() throws Exception {
		final IProofTreeNode node = makeProofTreeNode(P, not(P), bfalse);
		assertCreateCheck(node, ctrHyp(P));
		assertDischarged(node);
	}

	@Test
	public void hypNode() throws Exception {
		final IProofTreeNode node = makeProofTreeNode(P, P);
		assertCreateCheck(node, hyp(P));
		assertDischarged(node);
	}

	@Test
	public void splitGoalNode() throws Exception {
		final IProofTreeNode node = makeProofTreeNode(land(P, Q));
		assertCreateCheck(node, splitGoal(open, open));
	}

	@Test
	public void splitGoalTree() throws Exception {
		final IProofTreeNode node = makeProofTreeNode(P, Q, land(P, Q));
		assertCreateCheck(node, splitGoal(hyp(P), hyp(Q)));
		assertDischarged(node);
	}

}
