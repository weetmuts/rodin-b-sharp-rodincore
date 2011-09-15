/*******************************************************************************
 * Copyright (c) 2011 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.mbGoal;

import static org.eventb.core.ast.Formula.EQUAL;
import static org.eventb.core.ast.Formula.IN;
import static org.eventb.core.ast.Formula.SUBSET;
import static org.eventb.core.ast.Formula.SUBSETEQ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;

public class MembershipGoalImpl {

	// Formula factory to use everywhere
	final FormulaFactory ff;

	// Rule factory to use everywhere
	private final MembershipGoalRules rf;

	// Available hypotheses
	private final Set<Predicate> hyps;

	// Subset of hypotheses that take a membership form
	private final Set<Predicate> msHyps;

	// Subset of hypotheses that take an inclusionform
	private final List<Inclusion> inclHyps;

	// Initial goal
	private final Goal goal;

	// Goals already tried in this search thread
	// Used to prevent loops
	private final Set<Goal> tried;

	private static final Set<Predicate> extractMemberships(Set<Predicate> hyps) {
		final Set<Predicate> result = new HashSet<Predicate>();
		for (final Predicate hyp : hyps) {
			if (hyp.getTag() == IN) {
				result.add(hyp);
			}
		}
		return result;
	}

	private static final List<Inclusion> extractInclusions(Set<Predicate> hyps,
			final MembershipGoalImpl impl) {
		final List<Inclusion> result = new ArrayList<Inclusion>();
		for (final Predicate hyp : hyps) {
			switch (hyp.getTag()) {
			case SUBSET:
			case SUBSETEQ:
				result.add(new Inclusion(hyp) {
					@Override
					public Rule<?> makeRule() {
						return impl.hypothesis(predicate());
					}
				});
				break;
			case EQUAL:
				// TODO implement double inclusion
				break;
			case IN:
				// TODO implement membership in relation set
				break;
			default:
				// Ignore
				break;
			}
		}
		return result;
	}

	public MembershipGoalImpl(Predicate goal, Set<Predicate> hyps,
			FormulaFactory ff) {
		this.goal = new Goal(goal) {
			@Override
			public Rule<?> makeRule(Rule<?> rule) {
				return rule;
			}
		};
		this.ff = ff;
		this.rf = new MembershipGoalRules(ff);
		this.tried = new HashSet<Goal>();
		this.hyps = hyps;
		this.msHyps = extractMemberships(hyps);
		this.inclHyps = extractInclusions(hyps, this);
	}

	/**
	 * Tells whether there is a proof for the goal from hypotheses.
	 */
	public Rule<?> search() {
		final Rule<?> rule = search(goal);
		if (rule != null) {
			assert rule.consequent.equals(goal.predicate());
		}
		return rule;
	}

	public Rule<?> search(Goal goal) {
		return searchNoLoop(goal);
	}

	/**
	 * Tells whether there is a proof for the goal from hypotheses. The member
	 * variable <code>tried</code> is used to prevent looping.
	 * 
	 * @param goal
	 *            membership to discharge
	 * @return a justification for the given membership
	 */
	public Rule<?> searchNoLoop(Goal goal) {
		if (tried.contains(goal)) {
			// Don't loop
			return null;
		}
		tried.add(goal);
		final Rule<?> result = doSearch(goal);
		tried.remove(goal);
		return result;
	}

	// Must be called only by searchNoLoop
	private Rule<?> doSearch(Goal goal) {
		final Predicate predicate = goal.predicate();
		if (msHyps.contains(predicate)) {
			return hypothesis(predicate);
		}
		for (final Inclusion hyp : inclHyps) {
			for (final Goal subGoal : hyp.generate(goal, this)) {
				final Rule<?> rule = searchNoLoop(subGoal);
				if (rule != null) {
					return subGoal.makeRule(rule);
				}
			}
		}
		return null;
	}

	public Rule<?> hypothesis(Predicate predicate) {
		assert hyps.contains(predicate);
		return rf.hypothesis(predicate);
	}

	public Rule<?> compose(Rule<?> rule, Rule<?> rule2) {
		return rf.compose(rule, rule2);
	}

}
