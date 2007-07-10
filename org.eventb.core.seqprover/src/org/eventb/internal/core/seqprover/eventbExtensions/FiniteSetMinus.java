package org.eventb.internal.core.seqprover.eventbExtensions;

import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.IReasonerOutput;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.reasonerInputs.EmptyInputReasoner;

public class FiniteSetMinus extends EmptyInputReasoner {

	public static String REASONER_ID = SequentProver.PLUGIN_ID + ".finiteSetMinus";

	private static FormulaFactory ff = FormulaFactory.getDefault();
	
	public String getReasonerID() {
		return REASONER_ID;
	}

	protected IAntecedent[] getAntecedents(IProverSequent seq) {
		Predicate goal = seq.goal();

		// goal should have the form finite(S \ T)
		if (!Lib.isFinite(goal))
			return null;
		SimplePredicate sPred = (SimplePredicate) goal;
		if (!Lib.isSetMinus(sPred.getExpression()))
			return null;
		
		// There will be 1 antecidents
		IAntecedent[] antecidents = new IAntecedent[1];
		
		BinaryExpression aExp = (BinaryExpression) sPred
				.getExpression();
		
		Expression S = aExp.getLeft();
		Predicate newGoal = ff.makeSimplePredicate(Predicate.KFINITE, S, null);

		antecidents[0] = ProverFactory.makeAntecedent(newGoal);
		return antecidents;
	}

	protected String getDisplayName() {
		return "finite of ∖";
	}

	public IReasonerOutput apply(IProverSequent seq, IReasonerInput input,
			IProofMonitor pm) {
		IAntecedent[] antecidents = getAntecedents(seq);
		if (antecidents == null)
			return ProverFactory.reasonerFailure(this, input,
					"Inference " + getReasonerID()
							+ " is not applicable");

		// Generate the successful reasoner output
		return ProverFactory.makeProofRule(this, input, seq.goal(),
				getDisplayName(), antecidents);
	}

}
