package org.eventb.core.prover.reasoners;

import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.prover.Lib;
import org.eventb.core.prover.Reasoner;
import org.eventb.core.prover.ReasonerInput;
import org.eventb.core.prover.ReasonerOutput;
import org.eventb.core.prover.ReasonerOutputFail;
import org.eventb.core.prover.ReasonerOutputSucc;
import org.eventb.core.prover.ReasonerOutputSucc.Anticident;
import org.eventb.core.prover.sequent.IProverSequent;

public class AllI implements Reasoner{
	
	public String getReasonerID() {
		return "allI";
	}
	
	public ReasonerOutput apply(IProverSequent seq,ReasonerInput input){
		
		if (! Lib.isUnivQuant(seq.goal()))
		{
			ReasonerOutputFail reasonerOutput = new ReasonerOutputFail(this,input);
			reasonerOutput.error = "Goal is not universally quantified";
			return reasonerOutput;
		}
		
		ReasonerOutputSucc reasonerOutput = new ReasonerOutputSucc(this,input);
		reasonerOutput.goal = seq.goal();
		reasonerOutput.anticidents = new Anticident[1];
		
		reasonerOutput.anticidents[0] = new ReasonerOutputSucc.Anticident();
		
		QuantifiedPredicate UnivQ = (QuantifiedPredicate)seq.goal();
		BoundIdentDecl[] boundIdentDecls = Lib.getBoundIdents(UnivQ);
		
		// The type environment is cloned since makeFresh.. adds directly to the
		// given type environment
		// TODO : Change implementation
		ITypeEnvironment newITypeEnvironment = seq.typeEnvironment().clone();
		FreeIdentifier[] freeIdents = (Lib.ff).makeFreshIdentifiers(boundIdentDecls,newITypeEnvironment);		
		assert boundIdentDecls.length == freeIdents.length;
		reasonerOutput.display = "∀ intro (frees "+displayFreeIdents(freeIdents)+")";
		
		reasonerOutput.anticidents[0].subGoal = UnivQ.instantiate(freeIdents,Lib.ff);
		reasonerOutput.anticidents[0].addedFreeIdentifiers = freeIdents;
		assert reasonerOutput.anticidents[0].subGoal.isTypeChecked();
				
		return reasonerOutput;
	}
	
	private String displayFreeIdents(FreeIdentifier[] freeIdents) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < freeIdents.length; i++) {
				str.append(freeIdents[i].toString());
			if (i != freeIdents.length-1) str.append(",");
		}
		return str.toString();
	}

}
