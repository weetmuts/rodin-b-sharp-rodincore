/*******************************************************************************
 * Copyright (c) 2007, 2009 ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 ******************************************************************************/
package org.eventb.internal.ui.prover.tactics;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.ui.prover.DefaultTacticProvider;

/**
 * Simplifies predicates using pre-defined arithmetic simplification rewritings.
 * 
 * @author htson
 */
public class Arith extends DefaultTacticProvider {

	@Override
	public List<IPosition> getApplicablePositions(IProofTreeNode node,
			Predicate hyp, String input) {
		final Predicate predicate;
		if (hyp == null) {
			predicate = node.getSequent().goal();
		} else {
			predicate = hyp;
		}
		final List<IPosition> positions = Tactics.arithGetPositions(predicate);
		if (positions.size() == 0) {
			return null;
		}
		return positions;
	}

	@Override
	public ITactic getTactic(IProofTreeNode node, Predicate hyp,
			IPosition position, String[] inputs, String globalInput) {
		return Tactics.arithRewrites(hyp, position);
	}
}