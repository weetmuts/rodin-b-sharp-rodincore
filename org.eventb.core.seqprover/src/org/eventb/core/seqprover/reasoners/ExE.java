package org.eventb.core.seqprover.reasoners;

import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.seqprover.IProofRule;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.IReasonerOutput;
import org.eventb.core.seqprover.Lib;
import org.eventb.core.seqprover.RuleFactory;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.IProofRule.IAnticident;
import org.eventb.core.seqprover.reasonerInputs.SinglePredInput;
import org.eventb.core.seqprover.reasonerInputs.SinglePredInputReasoner;
import org.eventb.core.seqprover.sequent.Hypothesis;
import org.eventb.core.seqprover.sequent.IProverSequent;

public class ExE extends SinglePredInputReasoner{
	
	public static String REASONER_ID = SequentProver.PLUGIN_ID + ".exE";
	
	public String getReasonerID() {
		return REASONER_ID;
	}
	
	public IReasonerOutput apply(IProverSequent seq,IReasonerInput reasonerInput, IProgressMonitor progressMonitor){
		
		SinglePredInput input = (SinglePredInput) reasonerInput;

		Predicate exHypPred = input.getPredicate();
		Hypothesis exHyp = new Hypothesis(exHypPred);
		
		
		if (! seq.hypotheses().contains(exHyp))
			return RuleFactory.reasonerFailure(this,input,
					"Nonexistent hypothesis:"+exHyp);
		if (! Lib.isExQuant(exHypPred))
			return RuleFactory.reasonerFailure(this,input,
					"Hypothesis is not existentially quantified:"+exHyp);
		
		QuantifiedPredicate ExQ = (QuantifiedPredicate)exHypPred;
		BoundIdentDecl[] boundIdentDecls = Lib.getBoundIdents(ExQ);
		
		// The type environment is cloned since makeFresh.. adds directly to the
		// given type environment
		// TODO : Change implementation
		ITypeEnvironment newITypeEnvironment = seq.typeEnvironment().clone();
		FreeIdentifier[] freeIdents = (Lib.ff).makeFreshIdentifiers(boundIdentDecls,newITypeEnvironment);
		
//		for (FreeIdentifier identifier : freeIdents) {
//			reasonerOutput.anticidents[0].addedFreeIdentifiers.addName(identifier.getName(),identifier.getType());
//		}
		
		assert boundIdentDecls.length == freeIdents.length;
		Predicate instantiatedEx = ExQ.instantiate(freeIdents,Lib.ff);
		assert instantiatedEx.isTypeChecked();
		
		IAnticident[] anticidents = new IAnticident[1];
		anticidents[0] = RuleFactory.makeAnticident(
				seq.goal(),
				Lib.breakPossibleConjunct(instantiatedEx),
				freeIdents,
				Collections.singletonList(Lib.deselect(exHyp)));
		
		IProofRule reasonerOutput = RuleFactory.makeProofRule(
				this,input,
				seq.goal(),
				"∃ hyp (frees "+displayFreeIdents(freeIdents)+")",
				anticidents);
		
//		ProofRule reasonerOutput = new ProofRule(this,input);
//		reasonerOutput.goal = seq.goal();
//		reasonerOutput.anticidents = new Anticident[1];
//		
//		reasonerOutput.anticidents[0] = new ProofRule.Anticident();
//		
//		reasonerOutput.display = "∃ hyp (frees "+displayFreeIdents(freeIdents)+")";
//		reasonerOutput.anticidents[0].addConjunctsToAddedHyps(instantiatedEx);
//		reasonerOutput.anticidents[0].addedFreeIdentifiers = freeIdents;
//		reasonerOutput.anticidents[0].hypAction.add(Lib.deselect(exHyp));
//		reasonerOutput.anticidents[0].goal = seq.goal();
				
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

//	public static class Input implements ReasonerInput{
//		
//		Hypothesis exHyp;
//		
//		public Input(Hypothesis exHyp){
//			this.exHyp = exHyp;
//		}
//		
//		public Input(SerializableReasonerInput serializableReasonerInput) {
//			this.exHyp = new Hypothesis(serializableReasonerInput.getPredicate("exHyp"));
//		}
//		
//		public SerializableReasonerInput genSerializable(){
//			SerializableReasonerInput serializableReasonerInput 
//			= new SerializableReasonerInput();
//			serializableReasonerInput.putPredicate("exHyp",exHyp.getPredicate());
//			return serializableReasonerInput;
//		}
//		
//	}

}
