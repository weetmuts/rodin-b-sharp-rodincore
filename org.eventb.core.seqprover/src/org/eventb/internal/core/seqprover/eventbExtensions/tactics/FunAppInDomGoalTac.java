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
package org.eventb.internal.core.seqprover.eventbExtensions.tactics;

import static org.eventb.internal.core.seqprover.Util.getNullProofMonitor;

import java.util.HashSet;
import java.util.Set;

import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.tactics.BasicTactics;
import org.eventb.internal.core.seqprover.eventbExtensions.FunImageGoal;
import org.eventb.internal.core.seqprover.eventbExtensions.rewriters.TotalDomRewrites;

/**
 * If the goal is such as <code>f(x)∈dom(g)</code> :
 * <ul>
 * <li>First, we try to add hypothesis such as <code>f(x)∈E</code> using the
 * <code>FunImageGoal</code> reasoner.</li>
 * <li>If it succeeds, we try to re-write dom(g) using
 * <code>TotalDomRewrites</code>.</li>
 * <li>Finally, if it succeeds, we try to discharge the goal using
 * <code>MembershipGoal</code>. If the goal is discharged, then the tactics
 * are applied.</li>
 * </ul>
 * These tests are done for every possible case of re-writing
 * <code>dom(g)</code> and every possible added hypothesis.
 * 
 * @author Emmanuel Billaud
 */
public class FunAppInDomGoalTac implements ITactic {
	private final IPosition funAppPos = IPosition.ROOT.getFirstChild();
	private final IPosition domPos = funAppPos.getNextSibling();

	@Override
	public Object apply(IProofTreeNode ptNode, IProofMonitor pm) {
		try {
			if (pm == null) {
				pm = getNullProofMonitor();
			}
			Set<Predicate> set_f_AopB = new HashSet<Predicate>();
			Set<Expression> setDomain_g = new HashSet<Expression>();
			final Object resultPreCompute = preCompute(ptNode, set_f_AopB,
					setDomain_g, pm);
			if (resultPreCompute != null) {
				return resultPreCompute;
			}

			for (Predicate f_AopB : set_f_AopB) {
				final IProofTreeNode childPtNode = applyFunImageGoal(f_AopB,
						ptNode, pm);
				if (childPtNode == null) {
					continue;
				}
				for (Expression domain_g : setDomain_g) {
					final IProofTreeNode grandChildPtNode = applyTotalDomRewrites(
							domain_g, childPtNode, pm);
					if (pm.isCanceled()) {
						return "The tatic has been cancelled";
					}
					if (grandChildPtNode == null) {
						continue;
					}
					final Object dischargeResult = new MembershipGoalTac()
							.apply(grandChildPtNode, pm);
					if (dischargeResult != null) {
						childPtNode.pruneChildren();
						continue;
					}
					return dischargeResult;
				}
				ptNode.pruneChildren();
			}
			return "The goal cannot be re-written";
		} finally {
			if (!finalCondition(ptNode)) {
				ptNode.pruneChildren();
			}
		}
	}

	/**
	 * If the predicate <code>hypothesis</code> matches <code>f∈A op B</code>
	 * (with "op" an function or a relation, and f is the given expression),
	 * then the predicate is added to the set <code>set_f_AopB</code>.
	 * <p>
	 * Else, if the predicate <code>hypothesis</code> matches
	 * <code>g∈A op B</code> (with "op" a total function or a total relation,
	 * and g is the given expression), then the expression <code>A</code> is
	 * added to the set <code>setDomain_g</code>.
	 * <p>
	 * Else, nothing happens.
	 * 
	 * @param f
	 *            the function or relation extracted from the goal matching
	 *            <code>f(x) ∈ dom(g)</code>
	 * @param set_f_AopB
	 *            the set containing all hypotheses matching
	 *            <code>f∈ A op B</code> ("f" is given).
	 * @param g
	 *            the total function or total relation extracted from the goal
	 *            matching <code>f(x) ∈ dom(g)</code>
	 * @param setDomain_g
	 *            the set containing all expressions "A" extracted from
	 *            hypotheses matching <code>g ∈ A op B</code>.
	 * @param hypothesis
	 *            the tested predicate (should be a hypothesis of the sequent).
	 */
	private void createSets(final Expression f, Set<Predicate> set_f_AopB,
			final Expression g, Set<Expression> setDomain_g,
			Predicate hypothesis) {
		if (!Lib.isInclusion(hypothesis)) {
			return;
		}
		final Expression left = ((RelationalPredicate) hypothesis).getLeft();
		final Expression right = ((RelationalPredicate) hypothesis).getRight();
		if (left.equals(f)) {
			if (!isFunOrRel(right)) {
				return;
			}
			set_f_AopB.add(hypothesis);
		} else if (left.equals(g)) {
			if (!isTFunOrTRel(right)) {
				return;
			}
			setDomain_g.add(Lib.getLeft(right));
		}
	}

	/**
	 * Return <code>true</code> iff the expression <code>expression</code> is a
	 * function or a relation
	 * 
	 * @param expression
	 *            the considered expression
	 * @return <code>true</code> iff the expression <code>expression</code> is a
	 *         function or a relation
	 */
	private boolean isFunOrRel(final Expression expression) {
		return Lib.isFun(expression) || Lib.isRel(expression);
	}

	/**
	 * Return <code>true</code> iff the expression <code>expression</code> is a
	 * total function or a total relation
	 * 
	 * @param expression
	 *            the considered expression
	 * @return <code>true</code> iff the expression <code>expression</code> is a
	 *         total function or a total relation
	 */
	private boolean isTFunOrTRel(final Expression expression) {
		switch (expression.getTag()) {
		case (Formula.TREL):
		case (Formula.STREL):
		case (Formula.TFUN):
		case (Formula.TINJ):
		case (Formula.TSUR):
		case (Formula.TBIJ):
			return true;
		default:
			return false;
		}
	}

	/**
	 * Try to apply the reasoner <code>FunImageGoal</code> on the given
	 * IProofTreeNode <code>ptNode</code> with the IProofManager <code>pm</code>
	 * . The predicate <code>f_AopB</code> (should be a hypothesis matching f∈ A
	 * op B ("op" either a relation or a function)) is used in the input of the
	 * reasoner.
	 * <p>
	 * It returns <code>null</code> if the reasoner failed or if it created more
	 * than one sub-goal, it returns the proof tree node child else.
	 * 
	 * @param f_AopB
	 *            the predicate used for the input of the reasoner
	 * @param ptNode
	 *            the considered IProofTreeNode
	 * @param pm
	 *            the considered IProofMonitor
	 * @return <code>null</code> if the reasoner failed or if it created more
	 *         than one sub-goal, else it returns the resulting proof tree node.
	 */
	private IProofTreeNode applyFunImageGoal(Predicate f_AopB,
			IProofTreeNode ptNode, IProofMonitor pm) {
		final Object funImgGoalTac = BasicTactics.reasonerTac(
				new FunImageGoal(), new FunImageGoal.Input(f_AopB, funAppPos))
				.apply(ptNode, pm);
		if (funImgGoalTac != null) {
			return null;
		}
		final IProofTreeNode[] childNodes = ptNode.getChildNodes();
		if (childNodes.length != 1) {
			ptNode.pruneChildren();
			return null;
		}
		return childNodes[0];
	}

	/**
	 * Try to apply the reasoner <code>TotalDomRewrites</code> on the given
	 * IProofTreeNode <code>ptNode</code> with the IProofManager <code>pm</code>
	 * . The expression <code>domain_g</code> (should be a set "A" matching a
	 * hypothesis <code>g∈ A op B</code> (with "g" given)) is used in the input
	 * of the reasoner.
	 * 
	 * @param domain_g
	 *            the expression (should be a set) used for the input of the
	 *            reasoner
	 * @param ptNode
	 *            the considered IProofTreeNode
	 * @param pm
	 *            the considered IProofMonitor
	 * @return <code>null</code> if the reasoner failed or if it created more
	 *         than one sub-goal, else it returns the resulting proof tree node.
	 */
	private IProofTreeNode applyTotalDomRewrites(Expression domain_g,
			IProofTreeNode ptNode, IProofMonitor pm) {
		final Object totalDomTac = BasicTactics.reasonerTac(
				new TotalDomRewrites(),
				new TotalDomRewrites.Input(null, domPos, domain_g)).apply(
				ptNode, pm);
		if (totalDomTac != null) {
			return null;
		}
		final IProofTreeNode[] childNodes = ptNode.getChildNodes();
		if (childNodes.length != 1) {
			ptNode.pruneChildren();
			return null;
		}
		return childNodes[0];
	}

	/**
	 * Test if the sequent's goal of the given IProofTreeNode
	 * <code>ptNode</code> matches <code>f(x)∈dom(g)</code>. If so, in the set
	 * <code>set_f_AopB</code> will be added every hypotheses matching
	 * <code>f ∈ A op B</code> (with "op" etiher a function or a relation); and
	 * in the set <code>setDomain_g</code> will be added every expression "A"
	 * matching <code>g ∈ A op B</code> (with "op" either a total relation or a
	 * total function). If these two sets are non-empty, then it return
	 * <code>null</code>.
	 * 
	 * 
	 * @param ptNode
	 *            the considered IProofTreeNode
	 * @param set_f_AopB
	 *            a set of every hypothesis matching <code>f ∈ A op B</code>
	 *            (with "op" either a relation or a function) (should be empty)
	 * @param setDomain_g
	 *            a set of every expression "A" matching <code>g ∈ A op B</code>
	 *            (with "op" either a total relation or a total function)
	 *            (should be empty)
	 * @param pm
	 *            support cancellation (must not be <code>null</code>
	 * @return <code>null</code> iff the sequent's goal of the given
	 *         IProofTreeNode <code>ptNode</code> matches
	 *         <code>f(x)∈dom(g)</code> and <code>set_f_AopB</code> and
	 *         <code>setDomain_g</code> are non empty in the end.
	 */
	private Object preCompute(IProofTreeNode ptNode, Set<Predicate> set_f_AopB,
			Set<Expression> setDomain_g, IProofMonitor pm) {
		final IProverSequent sequent = ptNode.getSequent();
		final Predicate goal = sequent.goal();

		if (!Lib.isInclusion(goal)) {
			return "Goal is not an Inclusion";
		}
		final Expression funApp = ((RelationalPredicate) goal).getLeft();
		if (!Lib.isFunApp(funApp)) {
			return "Left member is not a function application";
		}
		final Expression dom = ((RelationalPredicate) goal).getRight();
		if (!Lib.isDom(dom)) {
			return "Right member is not a domain";
		}
		final Expression f = ((BinaryExpression) funApp).getLeft();
		final Expression g = ((UnaryExpression) dom).getChild();

		for (Predicate hyp : sequent.hypIterable()) {
			createSets(f, set_f_AopB, g, setDomain_g, hyp);
			if (pm.isCanceled()) {
				return "Tactic has been cancelled.";
			}
		}
		if (set_f_AopB.isEmpty()) {
			return "Cannot find set for the function application";
		}
		if (setDomain_g.isEmpty()) {
			return "Cannot find set for the domain";
		}
		return null;
	}

	/**
	 * Returns whether the given node has any pending descendant (i.e., is
	 * either open or closed).
	 * 
	 * @param ptNode
	 *            the considered proof tree node
	 * @return <code>true</code> iff the given node has no pending descendant
	 */
	private boolean finalCondition(IProofTreeNode ptNode) {
		return ptNode.isOpen() || ptNode.isClosed();
	}

}