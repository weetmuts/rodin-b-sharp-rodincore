/*******************************************************************************
 * Copyright (c) 2006, 2014 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.rewriters;

import org.eventb.core.ast.Formula;
import org.eventb.core.ast.IFormulaRewriter;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.seqprover.SequentProver;

public class SetEqlRewrites extends AbstractManualRewrites {

	public static final String REASONER_ID = SequentProver.PLUGIN_ID
			+ ".setEqlRewrites";

	@Override
	public String getReasonerID() {
		return REASONER_ID;
	}

	@Override
	protected String getDisplayName(Predicate pred, IPosition position) {
		if (pred == null)
			return "rewrites set equality in goal";
		return "rewrites set equality in hyp (" + pred.getSubFormula(position) + ")";
	}

	@Override
	public Predicate rewrite(Predicate pred, IPosition position) {
		IFormulaRewriter rewriter = new SetEqlRewriterImpl();
		Formula<?> subFormula = pred.getSubFormula(position);
		
		Formula<?> newSubFormula = null;
		if (subFormula instanceof RelationalPredicate) {
			newSubFormula = rewriter
				.rewrite((RelationalPredicate) subFormula);
		}
		if (newSubFormula == null)
			return null;
		
		if (newSubFormula == subFormula) // No rewrite occurs
			return null;

		return pred.rewriteSubFormula(position, newSubFormula);
	}

}
