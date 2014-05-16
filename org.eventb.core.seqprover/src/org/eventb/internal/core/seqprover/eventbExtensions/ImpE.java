/*******************************************************************************
 * Copyright (c) 2005, 2014 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - corrected: hid original hyp, added impLeft hyp (V0)
 *     Systerel - visibility: deselected impLeft hyp (V1)
 *     Systerel - back to original rule, but hiding the original predicate (V2)
 *     Systerel - factored out code common with ModusTollens
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions;

import java.util.Set;

import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.IVersionedReasoner;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.ProverRule;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.eventbExtensions.Lib;

/**
 * Generates a proof rule for modus ponens for a given implicative hypothesis.
 * <p>
 * Proof rules generated by this reasoner are not goal dependent.
 * </p>
 * 
 * @author Farhad Mehta
 */
public class ImpE extends ImpHypothesisReasoner implements IVersionedReasoner {
	
	public static final String REASONER_ID = SequentProver.PLUGIN_ID + ".impE";
	private static final int VERSION = 2;
	
	@Override
	public String getReasonerID() {
		return REASONER_ID;
	}

	@ProverRule("MH")
	@Override
	protected IAntecedent[] getAntecedents(Predicate left, Predicate right,
			IHypAction hideHypAction) {
		final Set<Predicate> addedHyps = Lib.breakPossibleConjunct(right);
		return new IAntecedent[] {
				ProverFactory.makeAntecedent(left, null, hideHypAction),
				ProverFactory.makeAntecedent(null, addedHyps, hideHypAction) };
	}

	@Override
	protected String getDisplay(Predicate pred) {
		return "⇒ hyp mp (" + pred + ")";
	}

	@Override
	public int getVersion() {
		return VERSION;
	}

}
