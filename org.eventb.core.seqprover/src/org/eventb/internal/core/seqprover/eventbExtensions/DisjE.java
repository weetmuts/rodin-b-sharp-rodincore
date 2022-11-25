/*******************************************************************************
 * Copyright (c) 2006, 2022 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Université de Lorraine - extended for case on unions and set extension
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static org.eventb.core.ast.Formula.EQUAL;
import static org.eventb.core.ast.Formula.IN;
import static org.eventb.core.seqprover.ProverFactory.makeAntecedent;
import static org.eventb.core.seqprover.ProverFactory.makeDeselectHypAction;

import java.util.stream.Stream;

import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.seqprover.IHypAction;
import org.eventb.core.seqprover.IProofRule.IAntecedent;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ProverRule;
import org.eventb.core.seqprover.SequentProver;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.reasonerInputs.HypothesisReasoner;


/**
 * Generates a proof rule to perform a case distinction on a hypothesis.
 *
 * It eliminates:
 * <ul>
 * <li>a disjunctive hypothesis by case distinction on its disjuncts</li>
 * <li>a set membership to an union by case distinction on the sets in the
 * union</li>
 * <li>a set membership to an extension set by case distinction on the elements
 * of the set</li>
 * </ul>
 * 
 * <p>
 * Proof rules generated by this reasoner are not goal dependent.
 * </p>
 * 
 * @author Farhad Mehta
 *
 */
public class DisjE extends HypothesisReasoner {
	
	public static final String REASONER_ID = SequentProver.PLUGIN_ID + ".disjE";
	
	@Override
	public String getReasonerID() {
		return REASONER_ID;
	}
	
	@ProverRule("CASE")
	@Override
	protected IAntecedent[] getAntecedents(IProverSequent sequent,
			Predicate pred) throws IllegalArgumentException {

		if (pred == null) {
			throw new IllegalArgumentException("Null hypothesis");
		}

		if (Lib.isDisj(pred)) {
			final IHypAction action = makeDeselectHypAction(asList(pred));
			return stream(Lib.disjuncts(pred)).map(e -> makeAntecedent(null, Lib.breakPossibleConjunct(e), action))
					.toArray(IAntecedent[]::new);
		}

		if (Lib.isInclusion(pred)) {
			final FormulaFactory ff = sequent.getFormulaFactory();
			final var rel = (RelationalPredicate) pred;
			final Expression element = rel.getLeft();
			final Expression set = rel.getRight();
			Stream<Predicate> newPredicates = null;
			if (Lib.isUnion(set)) {
				Expression[] values = ((AssociativeExpression) set).getChildren();
				newPredicates = stream(values).map(e -> ff.makeRelationalPredicate(IN, element, e, null));
			} else if (Lib.isSetExtension(set) && !Lib.isSingletonSet(set)) {
				Expression[] values = ((SetExtension) set).getMembers();
				newPredicates = stream(values).map(e -> ff.makeRelationalPredicate(EQUAL, element, e, null));
			}
			if (newPredicates != null) {
				final IHypAction hypAction = makeDeselectHypAction(asList(pred));
				return newPredicates.map(e -> makeAntecedent(null, singleton(e), hypAction))
						.toArray(IAntecedent[]::new);
			}
		}

		throw new IllegalArgumentException("Case analysis not possible on hypothesis: " + pred);
	}

	@Override
	protected String getDisplay(Predicate pred) {
		return "case distinction (" + pred + ")";
	}
	
	@Override
	protected boolean isGoalDependent(IProverSequent sequent, Predicate pred) {
		return false;
	}

}
