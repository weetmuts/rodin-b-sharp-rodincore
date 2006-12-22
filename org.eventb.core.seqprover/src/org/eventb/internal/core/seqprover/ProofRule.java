package org.eventb.internal.core.seqprover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IProofRule;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.IReasoner;
import org.eventb.core.seqprover.IReasonerInput;

public class ProofRule extends ReasonerOutput implements IProofRule{
	
	public static class Antecedent implements IAntecedent{
		
		private FreeIdentifier[] addedFreeIdentifiers;
		private Set <Predicate> addedHypotheses;
		private List <IHypAction> hypAction;
		private Predicate goal;
		
		public Antecedent(Predicate goal){
			addedFreeIdentifiers = new FreeIdentifier[0];
			addedHypotheses = new HashSet<Predicate>();
			hypAction = new ArrayList<IHypAction>();
			this.goal = goal;
		}
		
		public Antecedent(Predicate goal, Set<Predicate> addedHyps, FreeIdentifier[] addedFreeIdents, List<IHypAction> hypAction) {
			assert goal != null;
			this.goal = goal;
			this.addedHypotheses = addedHyps == null ? new HashSet<Predicate>() : addedHyps;
			this.addedFreeIdentifiers = addedFreeIdents == null ? new FreeIdentifier[0] : addedFreeIdents;
			this.hypAction = hypAction == null ? new ArrayList<IHypAction>() : hypAction;
		}
		
		/**
		 * @return Returns the addedFreeIdentifiers.
		 */
		public final FreeIdentifier[] getAddedFreeIdents() {
			return addedFreeIdentifiers;
		}

		/**
		 * @return Returns the hypAction.
		 */
		public final List<IHypAction> getHypAction() {
			return hypAction;
		}

		/**
		 * @return Returns the addedHypotheses.
		 */
		public final Set<Predicate> getAddedHyps() {
			return Collections.unmodifiableSet(addedHypotheses);
		}

		/**
		 * @return Returns the goal.
		 */
		public final Predicate getGoal() {
			return goal;
		}
		
		private IProverSequent genSequent(IProverSequent seq, Predicate goalInstantiation){
			ITypeEnvironment newTypeEnv;
			if (addedFreeIdentifiers.length == 0)
				newTypeEnv = seq.typeEnvironment();
			else
			{
				newTypeEnv = seq.typeEnvironment().clone();
				for (FreeIdentifier freeIdent : addedFreeIdentifiers) {
					// check for variable name clash
					if (newTypeEnv.contains(freeIdent.getName()))
					{
						// name clash
						return null;
					}
					newTypeEnv.addName(freeIdent.getName(),freeIdent.getType());
				}
			}
			
			Predicate newGoal;
			if (goal == null)
			{
				// Check for ill formed rule
				if (goalInstantiation == null) return null;
				newGoal = goalInstantiation;
			}
			else
			{
				newGoal = goal;
			}
			
			IInternalProverSequent result = ((IInternalProverSequent) seq).replaceGoal(newGoal,newTypeEnv);
			if (result == null) return null;
			result = result.addHyps(addedHypotheses,null);
			if (result == null) return null;
			result = result.selectHypotheses(addedHypotheses);
			result = ProofRule.performHypActions(hypAction,result);
			return result;
		}
		
	}
	
	private String display;
	private IAntecedent[] antecedents;
	private Set<Predicate> neededHypotheses;
	private Predicate goal;
	private int reasonerConfidence;
	
	public ProofRule(IReasoner generatedBy, IReasonerInput generatedUsing){
		super(generatedBy,generatedUsing);
		display = generatedBy.getReasonerID();
		antecedents = null;
		neededHypotheses = new HashSet<Predicate>();
		goal = null;
		reasonerConfidence = IConfidence.DISCHARGED_MAX;
	}
	
	public ProofRule(IReasoner generatedBy, IReasonerInput generatedUsing, Predicate goal, IAntecedent[] anticidents){
		super(generatedBy,generatedUsing);
		display = generatedBy.getReasonerID();
		this.antecedents = anticidents;
		neededHypotheses = new HashSet<Predicate>();
		this.goal = goal;
		reasonerConfidence = IConfidence.DISCHARGED_MAX;
	}

	public ProofRule(IReasoner generatedBy, IReasonerInput generatedUsing, Predicate goal, Set<Predicate> neededHyps, Integer confidence, String display, IAntecedent[] anticidents) {
		super(generatedBy,generatedUsing);
		
		assert goal != null;
		assert anticidents != null;
		
		this.goal = goal;
		this.antecedents = anticidents;
		this.neededHypotheses = neededHyps == null ? new HashSet<Predicate>() : neededHyps;
		this.reasonerConfidence = confidence == null ? IConfidence.DISCHARGED_MAX : confidence;
		this.display = display == null ? generatedBy.getReasonerID() : display;		
	}

	public String getDisplayName() {
		return display;
	}

	public String getRuleID() {
		return generatedBy.getReasonerID();
	}

	public int getConfidence() {
		return reasonerConfidence;
	}

	public IProverSequent[] apply(IProverSequent seq) {
		// Check if all the needed hyps are there
		if (! seq.containsHypotheses(neededHypotheses))
			return null;
		// Check if the goal null, or identical to the sequent.
		if ( goal!=null && ! goal.equals(seq.goal()) ) return null;
		
		// in case the goal is null, keep track of the sequent goal.
		Predicate goalInstantiation = null;
		if (goal == null)
			goalInstantiation = seq.goal();
		
		// Generate new antecedents
		IProverSequent[] anticidents 
			= new IProverSequent[antecedents.length];
		for (int i = 0; i < anticidents.length; i++) {
			anticidents[i] = ((Antecedent) antecedents[i]).genSequent(seq, goalInstantiation);
			if (anticidents[i] == null)
				// most probably a name clash occured
				// or the rule is ill formed
				// or an invalid type env.
				// add renaming/refactoring code here
				return null;
		}
		
		return anticidents;
	}

	public Set<Predicate> getNeededHyps() {
		return neededHypotheses;
	}

	public Predicate getGoal() {
		return goal;
	}

	public IAntecedent[] getAntecedents() {
		return antecedents;
	}

	
	public ProofDependenciesBuilder processDeps(ProofDependenciesBuilder[] subProofsDeps){
		assert antecedents.length == subProofsDeps.length;

		ProofDependenciesBuilder proofDeps = new ProofDependenciesBuilder();
		
		// the singular goal dependency
		Predicate depGoal = null;
		
		// process each antecedent
		for (int i = 0; i < antecedents.length; i++) {

			final IAntecedent antecedent = antecedents[i];
			final ProofDependenciesBuilder subProofDeps = subProofsDeps[i];
			
			// Process the antecedent
			processHypActionDeps(antecedent.getHypAction(), subProofDeps);
			
			subProofDeps.getUsedHypotheses().removeAll(antecedent.getAddedHyps());
			if (antecedent.getGoal()!=null)
				subProofDeps.getUsedFreeIdents().addAll(Arrays.asList(antecedent.getGoal().getFreeIdentifiers()));
			for (Predicate hyp : antecedent.getAddedHyps())
				subProofDeps.getUsedFreeIdents().addAll(Arrays.asList(hyp.getFreeIdentifiers()));
			for (FreeIdentifier freeIdent : antecedent.getAddedFreeIdents()){
				subProofDeps.getUsedFreeIdents().remove(freeIdent);
				subProofDeps.getIntroducedFreeIdents().add(freeIdent.getName());			
			}
						
			// Combine this information
			proofDeps.getUsedHypotheses().addAll(subProofDeps.getUsedHypotheses());
			proofDeps.getUsedFreeIdents().addAll(subProofDeps.getUsedFreeIdents());
			proofDeps.getIntroducedFreeIdents().addAll(subProofDeps.getIntroducedFreeIdents());
			
			// update depGoal
			if (antecedent.getGoal() == null){
				// Check for non-equal instantiations
				assert (depGoal == null || depGoal.equals(subProofDeps.getGoal()));
				depGoal = subProofDeps.getGoal();
			}

		}
		
		if (goal != null){	
			// goal is explicitly stated
			depGoal = goal;
		}
			
		proofDeps.setGoal(depGoal);
		proofDeps.getUsedHypotheses().addAll(neededHypotheses);	
		if (depGoal!=null) proofDeps.getUsedFreeIdents().addAll(Arrays.asList(depGoal.getFreeIdentifiers()));
		for (Predicate hyp : neededHypotheses)
			proofDeps.getUsedFreeIdents().addAll(Arrays.asList(hyp.getFreeIdentifiers()));
		
		return proofDeps;
	}
	
	
	private static IInternalProverSequent performHypActions(List<IHypAction> hypActions,IInternalProverSequent seq){
		if (hypActions == null) return seq;
		IInternalProverSequent result = seq;
		for(IHypAction action : hypActions){
			result = ((IInternalHypAction) action).perform(result);
		}
		return result;
	}
	
	private static void processHypActionDeps(List<IHypAction> hypActions,ProofDependenciesBuilder proofDeps){
		int length = hypActions.size();
		for (int i = length-1; i >= 0; i--) {
			((IInternalHypAction)hypActions.get(i)).processDependencies(proofDeps);
		}
	}
	

}
