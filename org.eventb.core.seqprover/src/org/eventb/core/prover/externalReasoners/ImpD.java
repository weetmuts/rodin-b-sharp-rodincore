package org.eventb.core.prover.externalReasoners;

import org.eventb.core.ast.Predicate;
import org.eventb.core.prover.IExtReasonerInput;
import org.eventb.core.prover.IExtReasonerOutput;
import org.eventb.core.prover.IExternalReasoner;
import org.eventb.core.prover.Lib;
import org.eventb.core.prover.SuccessfullExtReasonerOutput;
import org.eventb.core.prover.UnSuccessfulExtReasonerOutput;
import org.eventb.core.prover.proofs.Proof;
import org.eventb.core.prover.proofs.TrustedProof;
import org.eventb.core.prover.sequent.Hypothesis;
import org.eventb.core.prover.sequent.IProverSequent;
import org.eventb.core.prover.sequent.ISequent;
import org.eventb.core.prover.sequent.SimpleSequent;


public class ImpD implements IExternalReasoner{
	
	public String name(){
		return "remove implication";
	}

	public IExtReasonerOutput apply(IProverSequent S,IExtReasonerInput I) {
		if (! (I instanceof Input)) throw (new AssertionError(this));
		Input aI = (Input) I;
		
		Hypothesis impHyp = aI.impHyp;
		Predicate impHypPred = impHyp.getPredicate();
		if (! S.hypotheses().contains(impHyp))
			return new UnSuccessfulExtReasonerOutput(this,I,"Nonexistent hypothesis:"+impHyp.toString());
		if (! Lib.isImp(impHypPred))
			return new UnSuccessfulExtReasonerOutput(this,I,"Hypothesis not an implication:"+impHyp.toString());
	
		Predicate toShow;
		Predicate toAssume;
		// ITypeEnvironment te = S.typeEnvironment();
		if (aI.useContrapositive) 
		{
			toShow = Lib.makeNeg(Lib.impRight(impHypPred));
			toAssume = Lib.makeNeg(Lib.impLeft(impHypPred));
		}
		else 
		{ 
			toShow = Lib.impLeft(impHypPred);
			toAssume = Lib.impRight(impHypPred);
		}
		
		
		Predicate asmImpGoal = Lib.makeImp(toAssume,S.goal());
		Predicate shoImpAsmImpGoal = Lib.makeImp(toShow,asmImpGoal);
		Predicate newGoal = Lib.makeConj(toShow,shoImpAsmImpGoal);
		Predicate seqGoal = Lib.makeImp(newGoal,S.goal());
		assert seqGoal.isTypeChecked();
		assert seqGoal.isWellFormed();
		ISequent outputSequent = new SimpleSequent(S.typeEnvironment(),impHyp,seqGoal);
		Proof outputProof = new TrustedProof(outputSequent);
		return new SuccessfullExtReasonerOutput(this,I,outputProof,Lib.deselect(impHyp));
	}
	
	public static class Input implements IExtReasonerInput{

		public final Hypothesis impHyp;
		public final boolean useContrapositive;
		
		public Input(Hypothesis impHyp){
			assert Lib.isImp(impHyp.getPredicate());
			this.impHyp = impHyp;
			this.useContrapositive = false;
		}
		
		public Input(Hypothesis impHyp,boolean useContrapositive){
			assert Lib.isImp(impHyp.getPredicate());
			this.impHyp = impHyp;
			this.useContrapositive = useContrapositive;
		}
	}

}
