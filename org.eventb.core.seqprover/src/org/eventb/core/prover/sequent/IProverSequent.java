package org.eventb.core.prover.sequent;

import java.util.Collection;
import java.util.Set;

import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;

public interface IProverSequent{
	
	ITypeEnvironment typeEnvironment();
	Set<Hypothesis> hypotheses();
	Predicate goal();
	
	Set<Hypothesis> selectedHypotheses();
	Set<Hypothesis> visibleHypotheses();
	Set<Hypothesis> hiddenHypotheses();
	
	// ProverSequent InitialSequent(ITypeEnvironment typeEnvironment,Set<Predicate> hyps,Predicate goal);
	IProverSequent addHyps(Set<Hypothesis> hyps,ITypeEnvironment typeEnvironment);
	IProverSequent addHyp(Hypothesis hyp,ITypeEnvironment typeEnvironment);
	IProverSequent replaceGoal(Predicate goal,ITypeEnvironment typeEnvironment);
	// IProverSequent buildOn(ITypeEnvironment addedTypeEnvironment,Set<Predicate> addedHyps,Predicate newGoal);
	
	
	IProverSequent hideHypotheses(Set<Hypothesis> toHide);
	IProverSequent showHypotheses(Set<Hypothesis> toShow);
	IProverSequent selectHypotheses(Collection<Hypothesis> toSelect);
	IProverSequent deselectHypotheses(Set<Hypothesis> toDeselect);
	
}
