/*******************************************************************************
 * Copyright (c) 2006, 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.rewriters;

import java.util.Arrays;

import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IFormulaRewriter;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IVersionedReasoner;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.SequentProver;

public class ArithRewrites extends AbstractManualRewrites implements IVersionedReasoner {

	private static final IFormulaRewriter rewriter = new ArithRewriterImpl();

	public static final String REASONER_ID = SequentProver.PLUGIN_ID
			+ ".arithRewrites";

	public String getReasonerID() {
		return REASONER_ID;
	}

	@Override
	protected String getDisplayName(Predicate pred, IPosition position) {
		if (pred == null) {
			return "arithmetic simplification in goal";
		}
		return "arithmetic simplification in hyp (" + pred.getSubFormula(position) + ")";
	}

	@Override
	protected IHypAction getHypAction(Predicate pred, IPosition position) {
		if (pred == null) {
			return null;
		}
		return ProverFactory.makeHideHypAction(Arrays.asList(pred));
	}

	@Override
	protected Predicate rewrite(Predicate pred, IPosition position) {
		Formula<?> subFormula = pred.getSubFormula(position);
		final Formula<?> newSubFormula;
		if (subFormula instanceof BinaryExpression) {
			newSubFormula = rewriter.rewrite((BinaryExpression) subFormula);
		} else if (subFormula instanceof AssociativeExpression) {
			newSubFormula = rewriter.rewrite((AssociativeExpression) subFormula);
		} else if (subFormula instanceof RelationalPredicate) {
			newSubFormula = rewriter.rewrite((RelationalPredicate) subFormula);
		} else {
			newSubFormula = subFormula;
		}
		if (newSubFormula == subFormula) // No rewrite occurs
			return null;
		return pred.rewriteSubFormula(position, newSubFormula, FormulaFactory
				.getDefault());
	}

	public int getVersion() {
		return 0;
	}

}