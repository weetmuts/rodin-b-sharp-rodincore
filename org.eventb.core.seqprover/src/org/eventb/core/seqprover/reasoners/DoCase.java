package org.eventb.core.seqprover.reasoners;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.Lib;
import org.eventb.core.seqprover.ReasonerOutput;
import org.eventb.core.seqprover.ReasonerOutputFail;
import org.eventb.core.seqprover.ProofRule;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.ProofRule.Anticident;
import org.eventb.core.seqprover.reasonerInputs.SinglePredInput;
import org.eventb.core.seqprover.reasonerInputs.SinglePredInputReasoner;
import org.eventb.core.seqprover.sequent.IProverSequent;

public class DoCase extends SinglePredInputReasoner{
	
	public static String REASONER_ID = SequentProver.PLUGIN_ID + ".doCase";
	
	public String getReasonerID() {
		return REASONER_ID;
	}
	
	public ReasonerOutput apply(IProverSequent seq,IReasonerInput reasonerInput, IProgressMonitor progressMonitor){
		
		// Organize Input
		SinglePredInput input = (SinglePredInput) reasonerInput;
		
		if (input.hasError())
		{
			return new ReasonerOutputFail(this,input,input.getError());
		}

		Predicate trueCase = input.getPredicate();
		// This check may be redone for replay since the type environment
		// may have shrunk, making the previous predicate with dangling free vars.
		
		// This check now done when constructing the sequent.. 
		// so the reasoner is successful, but the rule fails.
		
		//		if (! Lib.typeCheckClosed(trueCase,seq.typeEnvironment()))
		//			return new ReasonerOutputFail(this,input,
		//					"Type check failed for predicate: "+trueCase);
		
		
		// We can now assume that the true case has been properly parsed and typed.
		
		// Generate the well definedness condition for the true case
		Predicate trueCaseWD = Lib.WD(trueCase);
		
		// Generate the successful reasoner output
		ProofRule reasonerOutput = new ProofRule(this,input);
		reasonerOutput.display = "dc ("+trueCase.toString()+")";
		reasonerOutput.goal = seq.goal();

		// Generate the anticidents
		reasonerOutput.anticidents = new Anticident[3];
		
		// Well definedness condition
		reasonerOutput.anticidents[0] = new ProofRule.Anticident();
		reasonerOutput.anticidents[0].subGoal = trueCaseWD;
		
		// The goal with the true case
		reasonerOutput.anticidents[1] = new ProofRule.Anticident();
		reasonerOutput.anticidents[1].addConjunctsToAddedHyps(trueCase);
		reasonerOutput.anticidents[1].subGoal = seq.goal();
		
		// The goal with the false case
		reasonerOutput.anticidents[2] = new ProofRule.Anticident();
		reasonerOutput.anticidents[2].addToAddedHyps(Lib.makeNeg(trueCase));
		reasonerOutput.anticidents[2].subGoal = seq.goal();	
				
		return reasonerOutput;
	}

}
