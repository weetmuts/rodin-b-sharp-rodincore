/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.prover.tactics;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.ui.prover.IPositionApplication;
import org.eventb.ui.prover.ITacticApplication;
import org.eventb.ui.prover.ITacticProvider2;
import org.eventb.ui.prover.TacticProviderUtils;

public class FunImgSimp implements ITacticProvider2 {

	private static final String GOAL_TACTIC_ID = "org.eventb.ui.funImgSimpGoal";
	private static final String HYP_TACTIC_ID = "org.eventb.ui.funImgSimpHyp";
	
	private static final List<ITacticApplication> EMPTY_LIST = Collections
			.emptyList();

	public List<ITacticApplication> getPossibleApplications(
			IProofTreeNode node, final Predicate hyp, String globalInput) {
		final List<IPosition> positions = Tactics.funImgSimpGetPositions(hyp,
				node.getSequent());
		if (positions.isEmpty()) {
			return EMPTY_LIST;
		}
		final List<ITacticApplication> tactics = new LinkedList<ITacticApplication>();
		for (final IPosition p : positions) {
			tactics.add(new IPositionApplication() {

				public String getHyperlinkLabel() {
					return null;
				}

				public Point getHyperlinkBounds(String parsedString,
						Predicate parsedPredicate) {
					return TacticProviderUtils.getOperatorPosition(
							parsedPredicate, parsedString, p.getFirstChild());
				}

				public String getTacticID() {
					if (hyp == null) {
						return GOAL_TACTIC_ID;
					} else
						return HYP_TACTIC_ID;
				}

				public ITactic getTactic(String[] inputs, String gInput) {
					return Tactics.funImgSimplifies(hyp, p);
				}
			});
		}
		return tactics;
	}

}
