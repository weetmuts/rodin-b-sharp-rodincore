package org.eventb.internal.core.seqprover.eventbExtensions;

import java.util.Collections;

import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.seqprover.Hypothesis;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.ProverLib;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.HypothesesManagement.Action;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.reasonerInputs.HypothesisReasoner;

public class ExE extends HypothesisReasoner{
	
	public static String REASONER_ID = SequentProver.PLUGIN_ID + ".exE";
	
	public String getReasonerID() {
		return REASONER_ID;
	}

	@Override
	protected IAntecedent[] getAntecedents(IProverSequent sequent,
			Predicate pred) throws IllegalArgumentException {

		if (pred == null) {
			throw new IllegalArgumentException("Null hypothesis");
		}
		if (!Lib.isExQuant(pred)) {
			throw new IllegalArgumentException(
					"Hypothesis is not existentially quantified: " + pred);
		}

		final QuantifiedPredicate ExQ = (QuantifiedPredicate) pred;
		final BoundIdentDecl[] boundIdentDecls = Lib.getBoundIdents(ExQ);
		
		// The type environment is cloned since makeFresh.. adds directly to the
		// given type environment
		// TODO : Change implementation
		final ITypeEnvironment newTypenv = Lib.ff.makeTypeEnvironment();
		newTypenv.addAll(sequent.typeEnvironment());
		final FreeIdentifier[] freeIdents = 
			Lib.ff.makeFreshIdentifiers(boundIdentDecls, newTypenv);
		
		Predicate instantiatedEx = ExQ.instantiate(freeIdents, Lib.ff);
		assert instantiatedEx.isTypeChecked();
		
		final Action action = ProverLib.deselect(new Hypothesis(pred));
		return new IAntecedent[] {
				ProverFactory.makeAntecedent(
				sequent.goal(),
				Lib.breakPossibleConjunct(instantiatedEx),
				freeIdents,
				Collections.singletonList(action))
		};
	}

	@Override
	protected String getDisplay(Predicate pred) {
		return "∃ hyp";
	}

}
