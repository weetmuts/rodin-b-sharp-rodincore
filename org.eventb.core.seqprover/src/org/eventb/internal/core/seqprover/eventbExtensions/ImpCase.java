/*******************************************************************************
 * Copyright (c) 2011 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions;

import static java.util.Collections.singleton;
import static org.eventb.core.seqprover.ProverFactory.makeAntecedent;

import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.ProverRule;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.eventbExtensions.DLib;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.reasonerInputs.ImpHypothesisReasoner;

/**
 * Generates a proof rule for Case distinction for a given implicative hypothesis.
 * <p>
 * Proof rules generated by this reasoner are not goal dependent.
 * </p>
 * <p>
 * Implementation is similar to the one in {@link ImpE}.
 * </p>
 * 
 * @author Emmanuel Billaud
 */
public class ImpCase extends ImpHypothesisReasoner {

	public static final String REASONER_ID = SequentProver.PLUGIN_ID
			+ ".impCase";

	public String getReasonerID() {
		return REASONER_ID;
	}

	@ProverRule("IMP_CASE")
	@Override
	protected IAntecedent[] getAntecedents(Predicate left, Predicate right,
			DLib lib, IHypAction hideHypAction) {
		final Predicate notLeft = lib.makeNeg(left);
		return new IAntecedent[] {
				makeAntecedent(null, singleton(notLeft), hideHypAction),
				makeAntecedent(null, Lib.breakPossibleConjunct(right), hideHypAction) };
	}

	@Override
	protected String getDisplay(Predicate pred) {
		return "⇒ hyp case (" + pred + ")";
	}

}
