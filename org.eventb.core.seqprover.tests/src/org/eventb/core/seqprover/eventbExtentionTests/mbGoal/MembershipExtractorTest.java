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
package org.eventb.core.seqprover.eventbExtentionTests.mbGoal;

import static org.eventb.core.ast.Formula.IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.seqprover.tests.TestLib;
import org.eventb.internal.core.seqprover.eventbExtensions.mbGoal.MembershipExtractor;
import org.eventb.internal.core.seqprover.eventbExtensions.mbGoal.Rationale;
import org.eventb.internal.core.seqprover.eventbExtensions.mbGoal.Rule;
import org.junit.Test;

/**
 * Unit tests for the membership extractor.
 * 
 * @author Laurent Voisin
 */
public class MembershipExtractorTest extends AbstractMbGoalTests {

	private static class TestItem extends AbstractMbGoalTests.TestItem {

		private final Expression member;
		private final MembershipExtractor extractor;

		TestItem(String typenvImage, String memberImage, String... hypImages) {
			super(typenvImage, hypImages);
			this.member = TestLib.genExpr(typenv, memberImage);
			this.extractor = new MembershipExtractor(rf, member, hyps);
		}

		public void assertExtraction(Rule<?>... expecteds) {
			final List<Rationale> actuals = extractor.extract();
			assertEquals(expecteds.length, actuals.size());
			int count = 0;
			for (Rationale actual : actuals) {
				// Ensure actual is well-formed
				final Predicate pred = actual.predicate();
				assertEquals(IN, pred.getTag());
				assertEquals(member, ((RelationalPredicate) pred).getLeft());
				final Rule<?> rule = actual.makeRule();
				assertEquals(pred, rule.getConsequent());
				assertTrue(hyps.containsAll(rule.getHypotheses()));

				// Ensure expected has been obtained
				assertEquals(expecteds[count++], rule);
			}
		}

	}

	/**
	 * Ensures that the extractor accepts empty input.
	 */
	@Test
	public void none() {
		final TestItem it = new TestItem("x=ℤ", "x");
		it.assertExtraction();
	}

	/**
	 * Ensures that the extractor rejects a wrong membership.
	 */
	@Test
	public void reject() {
		final TestItem it = new TestItem("x=ℤ, y=ℤ", "x", "y ∈ A");
		it.assertExtraction();
	}

	/**
	 * Ensures that the extractor extracts a single matching predicate.
	 */
	@Test
	public void single() {
		final TestItem it = new TestItem("x=ℤ", "x", "x ∈ A");
		it.assertExtraction(it.hyp("x ∈ A"));
	}

	/**
	 * Ensures that the extractor extracts several matching predicates.
	 */
	@Test
	public void multi() {
		final TestItem it = new TestItem("x=ℤ", "x", "x ∈ A", "x ∈ B");
		it.assertExtraction(it.hyp("x ∈ A"), it.hyp("x ∈ B"));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side, taking
	 * the left component.
	 */
	@Test
	public void mapletLeft() {
		final TestItem it = new TestItem("x=ℤ, y=ℤ", "x", "x↦y ∈ A×B");
		it.assertExtraction(rf.domPrj(it.hyp("x↦y ∈ A×B")));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side, taking
	 * the right component.
	 */
	@Test
	public void mapletRight() {
		final TestItem it = new TestItem("x=ℤ, y=ℤ", "y", "x↦y ∈ A×B");
		it.assertExtraction(rf.ranPrj(it.hyp("x↦y ∈ A×B")));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side, taking
	 * both component.
	 */
	@Test
	public void mapletBoth() {
		final String hyp = "x↦x ∈ A×B";
		final TestItem it = new TestItem("x=ℤ", "x", hyp);
		it.assertExtraction(rf.domPrj(it.hyp(hyp)), rf.ranPrj(it.hyp(hyp)));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side deeply.
	 */
	@Test
	public void mapletDeep() {
		final String hyp = "a↦(b↦c)↦d ∈ A×(B×C)×D";
		final TestItem it = new TestItem("a=ℤ, b=ℤ, c=ℤ, d=ℤ", "c", hyp);
		it.assertExtraction(rf.ranPrj(rf.ranPrj(rf.domPrj(it.hyp(hyp)))));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side deeply
	 * and return several predicates.
	 */
	@Test
	public void mapletDeepSeveral() {
		final String hyp = "a↦(b↦a)↦a ∈ A×(B×C)×D";
		final TestItem it = new TestItem("a=ℤ, b=ℤ", "a", hyp);
		it.assertExtraction(rf.domPrj(rf.domPrj(it.hyp(hyp))),
				rf.ranPrj(rf.ranPrj(rf.domPrj(it.hyp(hyp)))),
				rf.ranPrj(it.hyp(hyp)));
	}

	/**
	 * Ensures that the extractor can split maplet on the left-hand side even
	 * when the right-hand side is not a Cartesian product.
	 */
	@Test
	public void mapletNoCprod() {
		final String hyp = "a↦b↦a ∈ f";
		final TestItem it = new TestItem("a=ℤ, b=ℤ", "a", hyp);
		it.assertExtraction(rf.domPrj(rf.domPrj(it.hyp(hyp))),
				rf.ranPrj(it.hyp(hyp)));
	}

}