package org.eventb.core.seqprover;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IHypAction.ISelectionHypAction;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.internal.core.seqprover.ForwardInfHypAction;
import org.eventb.internal.core.seqprover.ProofRule;
import org.eventb.internal.core.seqprover.ProofTree;
import org.eventb.internal.core.seqprover.ProverSequent;
import org.eventb.internal.core.seqprover.ReasonerFailure;
import org.eventb.internal.core.seqprover.SelectionHypAction;
import org.eventb.internal.core.seqprover.ProofRule.Antecedent;

/**
 * Static class with factory methods required to construct various data structures
 * used in the sequent prover.
 * 
 * @author Farhad Mehta
 *
 */
public final class ProverFactory {

	/**
	 * Non-instantiable class
	 */
	private ProverFactory() {
	}
	
	/**
	 * Returns a new reasoner failure object with the given reason.
	 * 
	 * @param generatedBy
	 * 		Reasoner used
	 * @param generatedUsing
	 * 		Reasoner Input used
	 * @param reason
	 * 		Reason for reasoner failure
	 * @return
	 * 		A reasoner failure object with the given reason.
	 */
	public static IReasonerFailure reasonerFailure(
			IReasoner generatedBy,
			IReasonerInput generatedUsing,
			String reason){
		return new ReasonerFailure(generatedBy,generatedUsing,reason);
	}
	
	/**
	 * Returns a new proof rule with the given information
	 * 
	 * <p>
	 * This is the most general factory method to construct proof rules. In case the
	 * rule to be constructed is more specific, use of a more specific factory method
	 * is encouraged.
	 * </p>
	 * 
	 * @param generatedBy
	 * 		The reasoner used.
	 * @param generatedUsing
	 * 		The reasoner input used.
	 * @param goal
	 * 		The goal of the proof rule, or <code>null</code> iff the 
	 * 		proof rule is applicable to a sequent with any goal. In the latter case
	 * 		it is permitted that the antecedents may also contain a <code>null</code> goal.
	 * @param neededHyps
	 * 		The hypotheses needed for the proof rule to be applicable, or <code>null</code>
	 * 		iff no hypotheses are needed.
	 * @param confidence
	 * 		The confidence level of the proof rule, or <code>null</code> iff the greatest confidence level
	 * 		(i.e. <code>IConfidence.DISCHARGED_MAX</code>) is to be used.
	 * @param display
	 * 		The display string for the proof rule, or <code>null</code> iff the reasoner id is to be used.
	 * @param antecedents
	 * 		The antecedents of the proof rule, or <code>null</code> iff this rule has no antecedents.
	 * @return
	 * 		A new proof rule with the given information.
	 */
	public static IProofRule makeProofRule (
			IReasoner generatedBy,
			IReasonerInput generatedUsing,
			Predicate goal,
			Set<Predicate> neededHyps,
			Integer confidence,
			String display,
			IAntecedent... antecedents) {
			
		return new ProofRule(
				generatedBy,generatedUsing,
				goal,neededHyps,
				confidence,display,
				antecedents);
	}
	
	/**
	 * Returns a new proof rule with at most one needed hypothesis and the maximum
	 * confidence
	 * 
	 * @param generatedBy
	 * 		The reasoner used.
	 * @param generatedUsing
	 * 		The reasoner input used.
	 * @param goal
	 * 		The goal of the proof rule, or <code>null</code> iff the 
	 * 		proof rule is applicable to a sequent with any goal.
	 * @param neededHyp
	 * 		The hypothesis needed for the proof rule to be applicable, or <code>null</code>
	 * 		iff no hypotheses are needed.
	 * @param display
	 * 		The display string for the proof rule, or <code>null</code> iff the reasoner id is to be used.
	 * @param antecedents
	 * 		The antecedents of the proof rule, or <code>null</code> iff this rule has no antecedents.
	 * @return
	 * 		A new proof rule with the given information.
	 */
	public static IProofRule makeProofRule (
			IReasoner generatedBy,
			IReasonerInput generatedUsing,
			Predicate goal,
			Predicate neededHyp,
			String display,
			IAntecedent... anticidents) {
		
		final Set<Predicate>  neededHyps;
		if (neededHyp == null) {
			neededHyps = null;
		} else {
			neededHyps = Collections.singleton(neededHyp);
		}
		return makeProofRule(
				generatedBy, generatedUsing,
				goal, neededHyps,
				null, display,
				anticidents);
	}
	
	/**
	 * Returns a new proof rule with no needed hypothesis and the maximum
	 * confidence
	 * 
	 * @param generatedBy
	 * 		The reasoner used.
	 * @param generatedUsing
	 * 		The reasoner input used.
	 * @param goal
	 * 		The goal of the proof rule, or <code>null</code> iff the 
	 * 		proof rule is applicable to a sequent with any goal.
	 * @param display
	 * 		The display string for the proof rule, or <code>null</code> iff 
	 * 		the reasoner id is to be used.
	 * @param antecedents
	 * 		The antecedents of the proof rule, or <code>null</code> iff this rule
	 * 		 has no antecedents.
	 * @return
	 * 		A new proof rule with the given information.
	 */
	public static IProofRule makeProofRule (
			IReasoner generatedBy,
			IReasonerInput generatedUsing,
			Predicate goal,
			String display,
			IAntecedent... anticidents) {
		return makeProofRule(generatedBy,generatedUsing,goal,null,null,display,anticidents);
	}
	
	/**
	 * Returns a new proof rule that only contains hypothesis actions.
	 * 
	 * <p>
	 * This factory method returns a goal independent rule with one antecedent containing
	 * the given hypothesis actions.
	 * </p>
	 * 
	 * @param generatedBy
	 * 		The reasoner used.
	 * @param generatedUsing
	 * 		The reasoner input used.
	 * @param hypActions
	 * 		The hypothesis actions contained in the rule.
	 * @return
	 * 		A new proof rule with the given information.
	 */
	public static IProofRule makeProofRule (
			IReasoner generatedBy,
			IReasonerInput generatedUsing,
			String display,
			List<IHypAction> hypActions) {
		
		IAntecedent antecedent = makeAntecedent(null, null, null, hypActions);
		return makeProofRule(
				generatedBy,generatedUsing,
				null,null,null,
				display,new IAntecedent[]{antecedent});
	}
	
	
	/**
	 * Returns a new antecedent with the given inputs. The constructed antecedent
	 * can then be used to construct a proof rule.
	 * 
	 * <p>
	 * This is the most general factory method to construct antecedents. In case the
	 * antecedent to be constructed is more specific, use of a more specific factory method
	 * is encouraged.
	 * </p>
	 * 
	 * @param goal
	 * 		The goal of the antecedent, or <code>null</code> iff the rule is intended
	 * 		to be goal independent.
	 * @param addedHyps
	 * 		The added hypotheses, or <code>null</code> iff there are no added 
	 * 		hypotheses.
	 * @param addedFreeIdents
	 * 		The added free identifiers, or <code>null</code> iff there are no added
	 * 		free identifiers.
	 * @param hypActions
	 * 		The hypothesis actions, or <code>null</code> iff there are no hypothesis
	 * 		actions.
	 * @return
	 * 		A new antecedent with the given information.
	 */
	public static IAntecedent makeAntecedent(
			Predicate goal,
			Set<Predicate> addedHyps,
			FreeIdentifier[] addedFreeIdents,
			List<IHypAction> hypActions){
		
		return new Antecedent(goal, addedHyps, addedFreeIdents, hypActions);
	}

	/**
	 * Returns a new antecedent with no added free identifiers, and at most one hypothesis
	 * action.
	 * 
	 * @param goal
	 * 		The goal of the antecedent, or <code>null</code> iff the rule is intended
	 * 		to be goal independent.
	 * @param addedHyps
	 * 		The added hypotheses, or <code>null</code> iff there are no added 
	 * 		hypotheses.
	 * @param hypAction
	 * 		The hypothesis action, or <code>null</code> iff there are no hypothesis
	 * 		actions.
	 * @return
	 * 		A new antecedent with the given information.
	 */
	public static IAntecedent makeAntecedent(
			Predicate goal,
			Set<Predicate> addedHyps,
			IHypAction hypAction) {
		
		List<IHypAction> hypActions = null;
		if (hypAction != null){
			hypActions = Collections.singletonList(hypAction);
		}
		return makeAntecedent(goal,addedHyps,null,hypActions);
	}
	
	/**
	 * Returns a new antecedent that may only specify a goal (i.e. with no added 
	 * hypotheses, no added free identifiers and no hypothesis actions.)
	 * 
	 * @param goal
	 * 		The goal of the antecedent, or <code>null</code> iff the rule is intended
	 * 		to be goal independent.
	 * @return
	 * 		A new antecedent with the given information.
	 */
	public static IAntecedent makeAntecedent(Predicate goal) {
		return makeAntecedent(goal,null,null,null);
	}

	
	/**
	 * Returns a new sequent.
	 * 
	 * @param typeEnv
	 * 		The type environment for the sequent, or <code>null</code> iff the empty
	 * 		type environment is to be used.
	 * 		It should be ensured that all predicates can be type checked using this
	 * 		type environment. 
	 * @param hyps
	 * 		The set of hypotheses, or <code>null</code> iff this set is intended to
	 * 		be empty.
	 * @param goal
	 * 		The goal. This parameter must not be <code>null</code>.
	 * @return
	 * 		A new sequent with the given information, or <code>null</code> if a valid
	 * 		sequent could not be constructed
	 */
	public static IProverSequent makeSequent(ITypeEnvironment typeEnv,Collection<Predicate> hyps,Predicate goal){
		return new ProverSequent(typeEnv,hyps,goal);
	}
	
	/**
	 * Returns a new sequent with some selected hypotheses.
	 * 
	 * @param typeEnv
	 * 		The type environment for the sequent, or <code>null</code> iff the empty
	 * 		type environment is to be used.
	 * 		It should be ensured that all predicates can be type checked using this
	 * 		type environment. 
	 * @param hyps
	 * 		The set of hypotheses, or <code>null</code> iff this set is intended to
	 * 		be empty.
	 * @param selHyps
	 * 		The set of hypotheses to select. The set of hypotheses to select should be
	 * 		contained in the set of hypotheses
	 * @param goal
	 * 		The goal. This parameter must not be <code>null</code>.
	 * @return
	 * 		A new sequent with the given information, or <code>null</code> if a valid
	 * 		sequent could not be constructed
	 */
	public static IProverSequent makeSequent(ITypeEnvironment typeEnv,Collection<Predicate> hyps,Collection<Predicate> selHyps,Predicate goal){
		return new ProverSequent(typeEnv,hyps,selHyps,goal);
	}

	/**
	 * Returns a new proof tree with the given sequent at the root.
	 * 
	 * @param sequent
	 *            the sequent of the root node
	 * @param origin
	 *            an object describing the origin of the sequent, might be
	 *            <code>null</code>
	 * @return a new proof tree for the given sequent
	 */
	public static IProofTree makeProofTree(IProverSequent sequent, Object origin) {
		return new ProofTree(sequent, origin);
	}

	
	/**
	 * Returns a new select hypotheses action
	 * 
	 * @param toSelect
	 * 		Hypotheses to select
	 * @return
	 * 		A new select hypotheses action
	 */
	public static IHypAction makeSelectHypAction(Collection<Predicate> toSelect){
		return new SelectionHypAction(ISelectionHypAction.SELECT_ACTION_TYPE,toSelect);
	}

	/**
	 * Returns a new deselect hypotheses action
	 * 
	 * @param toDeselect
	 * 		Hypotheses to deselect
	 * @return
	 * 		A new deselect hypotheses action
	 */
	public static IHypAction makeDeselectHypAction(Collection<Predicate> toDeselect){
		return new SelectionHypAction(ISelectionHypAction.DESELECT_ACTION_TYPE,toDeselect);
	}

	/**
	 * Returns a new hide hypotheses action
	 * 
	 * @param toHide
	 * 		Hypotheses to hide
	 * @return
	 * 		A new hide hypotheses action
	 */
	public static IHypAction makeHideHypAction(Collection<Predicate> toHide){
		return new SelectionHypAction(ISelectionHypAction.HIDE_ACTION_TYPE,toHide);
	}

	/**
	 * Returns a new show hypotheses action
	 * 
	 * @param toShow
	 * 		Hypotheses to show
	 * @return
	 * 		A new show hypotheses action
	 */
	public static IHypAction makeShowHypAction(Collection<Predicate> toShow){
		return new SelectionHypAction(ISelectionHypAction.SHOW_ACTION_TYPE,toShow);
	}
	
	/**
	 * Returns a new forward inference hypothesis action
	 * 
	 * <p>
	 * This is the most general factory method for this construction. In case the
	 * construction is more specific, the use of a more specific factory method
	 * is encouraged.
	 * </p>
	 * 
	 * @param hyps
	 * 		The hypotheses needed for the forward inference
	 * @param addedFreeIdents
	 * 		Fresh free identifiers added by the forward inference
	 * @param inferredHyps
	 * 		The inferred hypotheses
	 * @return
	 * 		A new forward inference hypothesis action
	 */
	public static IHypAction makeForwardInfHypAction(Collection<Predicate> hyps, FreeIdentifier[] addedFreeIdents, Collection<Predicate> inferredHyps){
		return new ForwardInfHypAction(hyps,addedFreeIdents,inferredHyps);
	}
	
	private final static FreeIdentifier[] NO_FREE_IDENTS = new FreeIdentifier[0];
	
	/**
	 * Returns a new forward inference hypothesis action that does not introduce
	 * free identifiers
	 * 
	 * @param hyps
	 * 		The hypotheses needed for the forward inference
	 * @param inferredHyps
	 * 		The inferred hypotheses
	 * @return
	 * 		A new forward inference hypothesis action
	 */
	public static IHypAction makeForwardInfHypAction(Collection<Predicate> hyps, Collection<Predicate> inferredHyps){
		return new ForwardInfHypAction(hyps,NO_FREE_IDENTS,inferredHyps);
	}

	/**
	 * Constructs an instance of {@link IProofDependencies} from the values given as
	 * parameters. 
	 * 
	 * This is a convenience method. Clients must independently check that the data 
	 * provided conforms to the constraints in {@link IProofDependencies}.
	 * 
	 * @param hasDeps
	 * @param goal
	 * @param usedHypotheses
	 * @param usedFreeIdents
	 * @param introducedFreeIdents
	 * @return An instance of {@link IProofDependencies} with the values given as 
	 * 	input parameters
	 */
	public static IProofDependencies makeProofDependencies(
			final boolean hasDeps,
			final Predicate goal,
			final Set<Predicate> usedHypotheses,
			final ITypeEnvironment usedFreeIdents,
			final Set<String> introducedFreeIdents){
	
		return new IProofDependencies(){
	
			public Predicate getGoal() {
				return goal;
			}
	
			public Set<String> getIntroducedFreeIdents() {
				return introducedFreeIdents;
			}
	
			public ITypeEnvironment getUsedFreeIdents() {
				return usedFreeIdents;
			}
	
			public Set<Predicate> getUsedHypotheses() {
				return usedHypotheses;
			}
	
			public boolean hasDeps() {
				return hasDeps;
			}
			
		};
	}
	
}
