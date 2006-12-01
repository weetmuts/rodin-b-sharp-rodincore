package org.eventb.internal.core.seqprover.eventbExtensions;

import java.util.Collections;

import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.Hypothesis;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.IProofRule;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.IReasoner;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.IReasonerInputSerializer;
import org.eventb.core.seqprover.IReasonerOutput;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.ProverLib;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.IReasonerInputSerializer.SerializeException;
import org.eventb.core.seqprover.reasonerInputs.CombiInput;
import org.eventb.core.seqprover.reasonerInputs.SinglePredInput;
import org.eventb.core.seqprover.reasonerInputs.SingleStringInput;
import org.eventb.internal.core.seqprover.eventbExtensions.rewriters.Rewriter;
import org.eventb.internal.core.seqprover.eventbExtensions.rewriters.RewriterRegistry;

public class RewriteHyp implements IReasoner{
	
	public static String REASONER_ID = SequentProver.PLUGIN_ID + ".rewriteHyp";
	
	public String getReasonerID() {
		return REASONER_ID;
	}
	
	public IReasonerInput deserializeInput(IReasonerInputSerializer reasonerInputSerializer) throws SerializeException {
		return new CombiInput(
				new SinglePredInput(reasonerInputSerializer),
				new SingleStringInput(reasonerInputSerializer)
		);
	}
	
	public IReasonerOutput apply(IProverSequent seq,IReasonerInput reasonerInput, IProofMonitor pm){
		
		CombiInput input = (CombiInput)reasonerInput;
		
		if (input.hasError())
			ProverFactory.reasonerFailure(this,input,input.getError());
		
		Hypothesis hyp = new Hypothesis(((SinglePredInput)input.getReasonerInputs()[0]).getPredicate());
		Rewriter rewriter = RewriterRegistry.getRewriter(((SingleStringInput)input.getReasonerInputs()[1]).getString());
		
		if (rewriter == null) 
			return ProverFactory.reasonerFailure(this,input,
					"Uninstalled rewriter");
		
		if (! seq.hypotheses().contains(hyp))
			return ProverFactory.reasonerFailure(this,input,
					"Nonexistent hypothesis:"+hyp);
		
		Predicate newHyp = rewriter.apply(hyp.getPredicate());
		if (newHyp == null)
			return ProverFactory.reasonerFailure(this,input,
					"Rewriter " + rewriter +" inapplicable for hypothesis "+ hyp);

		IAntecedent[] anticidents = new IAntecedent[1];
		
		anticidents[0] = ProverFactory.makeAntecedent(
				seq.goal(),
				Collections.singleton(newHyp),
				ProverLib.deselect(hyp));
		
		IProofRule reasonerOutput = ProverFactory.makeProofRule(
				this,input,
				seq.goal(),
				hyp,
				" hyp ("+hyp.toString()+")",
				anticidents);
		
//		ProofRule reasonerOutput = new ProofRule(this,input);
//		reasonerOutput.goal = seq.goal();
//		reasonerOutput.display = rewriter.getName()+" hyp ("+hyp.toString()+")";
//		reasonerOutput.neededHypotheses.add(hyp);
//		reasonerOutput.anticidents = new Antecedent[1];
//		
//		reasonerOutput.anticidents[0] = new ProofRule.Antecedent(seq.goal());
//		reasonerOutput.anticidents[0].addToAddedHyps(newHyp);
//		reasonerOutput.anticidents[0].hypAction.add(Lib.deselect(hyp));
				
		return reasonerOutput;
	}
	
//	public static class Input implements ReasonerInput{
//
//		public final Rewriter rewriter;
//		public final Hypothesis hyp;
//		
//		public Input(Rewriter rewriter,Hypothesis hyp){
//			this.rewriter = rewriter;
//			this.hyp = hyp;
//		}
//
//		public Input(SerializableReasonerInput serializableReasonerInput) {
//			this.rewriter = RewriterRegistry.getRewriter(serializableReasonerInput.getString("rewriterID"));
//			assert this.rewriter != null;
//			this.hyp = new Hypothesis(serializableReasonerInput.getPredicate("hyp"));
//		}
//		
//		public SerializableReasonerInput genSerializable() {
//			SerializableReasonerInput serializableReasonerInput 
//			= new SerializableReasonerInput();
//			serializableReasonerInput.putString("rewriterID",rewriter.getRewriterID());
//			serializableReasonerInput.putPredicate("hyp",hyp.getPredicate());
//			return serializableReasonerInput;
//		}
//	}

}
