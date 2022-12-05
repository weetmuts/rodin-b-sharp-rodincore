/*******************************************************************************
 * Copyright (c) 2007, 2022 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.prover.tactics;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eventb.core.seqprover.eventbExtensions.Tactics.finiteRanGetPositions;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.ui.prover.DefaultTacticProvider.DefaultPositionApplication;
import org.eventb.ui.prover.ITacticApplication;
import org.eventb.ui.prover.ITacticProvider;

/**
 * Provider for the "finite of range of a relation" tactic.
 * <ul>
 * <li>Provider ID : <code>org.eventb.ui.finiteRanGoal</code></li>
 * <li>Target : goal</li>
 * <ul>
 */
public class FiniteRanGoal implements ITacticProvider{

	public static class FiniteRanGoalApplication extends DefaultPositionApplication {
		
		private static final String TACTIC_ID = "org.eventb.ui.finiteRanGoal";

		public FiniteRanGoalApplication() {
			super(null, IPosition.ROOT);
		}

		@Override
		public ITactic getTactic(String[] inputs, String globalInput) {
			return Tactics.finiteRan();
		}

		@Override
		public String getTacticID() {
			return TACTIC_ID;
		}
		
	}

	private static final List<ITacticApplication> NO_APPLICATIONS = emptyList();

	private static final List<ITacticApplication> GOAL_APPLICATION = singletonList(new FiniteRanGoalApplication());

	@Override
	public List<ITacticApplication> getPossibleApplications(
			IProofTreeNode node, Predicate hyp, String globalInput) {
		if (node == null)
			return NO_APPLICATIONS;

		final Predicate goal = node.getSequent().goal();
		return finiteRanGetPositions(goal).isEmpty() ? NO_APPLICATIONS : GOAL_APPLICATION;
	}

}
