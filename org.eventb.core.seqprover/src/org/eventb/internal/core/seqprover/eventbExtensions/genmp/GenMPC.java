/*******************************************************************************
 * Copyright (c) 2011, 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.genmp;

import static java.util.Collections.singleton;
import static org.eventb.core.seqprover.eventbExtensions.DLib.makeNeg;
import static org.eventb.core.seqprover.eventbExtensions.Lib.disjuncts;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isDisj;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isNeg;
import static org.eventb.internal.core.seqprover.eventbExtensions.genmp.Substitute.makeSubstitutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.DefaultInspector;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.IAccumulator;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.MultiplePredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.PredicateVariable;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.UnaryPredicate;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.internal.core.seqprover.eventbExtensions.genmp.GeneralizedModusPonens.Level;

/**
 * @author Emmanuel Billaud
 */
public class GenMPC {

	// The level of the calling GenMP reasoner
	private final Level level;

	// The input sequent
	private final IProverSequent seq;

	// The proof monitor (for cancellation)
	private final IProofMonitor pm;

	// The goal of the input sequent
	private final Predicate goal;

	// All possible substitutions
	// (hypothesis or goal ↦ substitute (<code>⊤</code> or <code>⊥</code>))
	private final Map<Predicate, Substitute> substitutes;

	// Output from this class
	private Predicate rewrittenGoal;
	private final Set<Predicate> neededHyps;

	private boolean isGoalDependent;
	private final List<IHypAction> hypActions;

	public GenMPC(Level level, IProofMonitor pm, IProverSequent seq) {
		super();
		this.level = level;
		this.seq = seq;
		this.pm = pm;
		this.goal = seq.goal();
		this.substitutes = new HashMap<Predicate, Substitute>();
		this.rewrittenGoal = null;
		this.neededHyps = new HashSet<Predicate>();
		this.isGoalDependent = false;
		this.hypActions = new ArrayList<IHypAction>();
	}

	public Predicate goal() {
		return goal;
	}

	public Set<Predicate> neededHyps() {
		return neededHyps;
	}

	public Predicate rewrittenGoal() {
		return rewrittenGoal;
	}

	public boolean isGoalDependent() {
		return isGoalDependent;
	}

	public List<IHypAction> hypActions() {
		return hypActions;
	}

	/**
	 * Runs generalized Modus Ponens reasoner that generate the re-written goal
	 * and hypotheses and all the hypotheses needed to achieve it and all
	 * IHypActions computed.
	 * 
	 * We first compute possible substitutes from hypotheses and goal. Then, we
	 * traverse each predicate to get the list of possible applications of
	 * substitutes and finally apply them.
	 */
	public boolean runGenMP() {
		extractSubstitutes();

		List<SubstAppli> applis = analyzePred(goal, true);
		rewriteGoal(applis);

		for (Predicate hyp : seq.visibleHypIterable()) {
			if (pm != null && pm.isCanceled()) {
				return false;
			}
			applis = analyzePred(hyp, false);
			rewriteHyp(hyp, applis);

		}
		return true;
	}

	/**
	 * Extracts the substitutes from all hypotheses and the goal.
	 */
	private void extractSubstitutes() {
		for (Predicate hyp : seq.hypIterable()) {
			addSubstitute(hyp, false, hyp);
		}
		if (!level.from(Level.L1)) {
			// Level 0 ignores all predicates in the goal
			return;
		}
		if (isDisj(goal)) {
			for (final Predicate child : disjuncts(goal)) {
				addSubstitute(goal, true, child);
			}
			return;
		}
		addSubstitute(goal, true, goal);
	}

	// Substitutes coming from hypotheses must be added BEFORE those coming from
	// the goal.
	private void addSubstitute(Predicate origin, boolean fromGoal,
			Predicate source) {
		final List<Substitute> substs = makeSubstitutes(origin, fromGoal,
				source);
		for (final Substitute subst : substs) {
			final Predicate toReplace = subst.toReplace();
			if (substitutes.containsKey(toReplace)) {
				return;
			}
			substitutes.put(toReplace, subst);
		}
	}

	/**
	 * Returns a list of applications of substitutes to the given predicate. The
	 * returned applications are guaranteed to not overlap.
	 * 
	 * @param origin
	 *            the predicate to analyze (should be either a hypothesis or the
	 *            goal)
	 * @param isGoal
	 *            <code>true</code> if <code>origin</code> is the goal
	 * @return a list of all possible substitution applications
	 */
	private List<SubstAppli> analyzePred(final Predicate origin,
			final boolean isGoal) {

		return origin.inspect(new DefaultInspector<SubstAppli>() {

			private void addPredToMap(Predicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				final Substitute subst = substitutes.get(predicate);
				if (subst == null) {
					return;
				}
				// TODO in next level, fix these tests which are wrong.
				if (isGoal && subst.fromGoal()) {
					// Do not rewrite the goal with itself
					return;
				}
				if (predicate == computePred(origin)) {
					return;
				}
				final IPosition pos = accumulator.getCurrentPosition();
				accumulator.add(new SubstAppli(subst, pos));
				accumulator.skipChildren();
			}

			// Retained for backward compatibility, do not change.
			private Predicate computePred(Predicate pred) {
				if (isNeg(pred)) {
					return makeNeg(pred);
				}
				return pred;
			}

			@Override
			public void inspect(AssociativePredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(BinaryPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(ExtendedPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(LiteralPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(MultiplePredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(PredicateVariable predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(QuantifiedPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(RelationalPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(SimplePredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

			@Override
			public void inspect(UnaryPredicate predicate,
					IAccumulator<SubstAppli> accumulator) {
				addPredToMap(predicate, accumulator);
			}

		});
	}

	/**
	 * Rewrites the goal using the given substitute applications, updating the
	 * result in fields.
	 * 
	 * @param applis
	 *            list of substitute applications to the goal
	 */
	private void rewriteGoal(List<SubstAppli> applis) {
		Predicate newGoal = goal;
		for (final SubstAppli appli : applis) {
			neededHyps.add(appli.origin());
			newGoal = appli.apply(newGoal);
		}
		if (newGoal != goal) {
			rewrittenGoal = newGoal;
		}
	}

	/**
	 * Rewrites the given hypothesis with the given application.
	 * 
	 * @param hyp
	 *            the hypothesis
	 * @param applis
	 *            list of substitute applications to the hypothesis
	 */
	private void rewriteHyp(Predicate hyp, List<SubstAppli> applis) {
		final Set<Predicate> sourceHyps = new LinkedHashSet<Predicate>();
		Predicate rewrittenHyp = hyp;
		for (final SubstAppli appli : applis) {
			if (appli.fromGoal()) {
				isGoalDependent = true;
			} else {
				sourceHyps.add(appli.origin());
			}
			rewrittenHyp = appli.apply(rewrittenHyp);
		}
		if (rewrittenHyp != hyp) {
			sourceHyps.add(hyp);
			hypActions.add(ProverFactory.makeForwardInfHypAction(sourceHyps,
					singleton(rewrittenHyp)));
			hypActions.add(ProverFactory.makeHideHypAction(singleton(hyp)));
		}
	}

}