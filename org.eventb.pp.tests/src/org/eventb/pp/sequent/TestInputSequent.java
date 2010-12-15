/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.pp.sequent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.eventb.core.ast.FormulaFactory.getInstance;

import java.util.List;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.extension.datatype.IConstructorMediator;
import org.eventb.core.ast.extension.datatype.IDatatype;
import org.eventb.core.ast.extension.datatype.IDatatypeExtension;
import org.eventb.core.ast.extension.datatype.ITypeConstructorMediator;
import org.eventb.internal.pp.CancellationChecker;
import org.eventb.internal.pp.sequent.InputSequent;
import org.eventb.pp.AbstractRodinTest;
import org.eventb.pp.TestSequent;
import org.junit.Test;

/**
 * Unit tests for class {@link InputSequent}.
 * 
 * @author Laurent Voisin
 */
public class TestInputSequent extends AbstractRodinTest {

	private static final List<String> NO_TE = emptyList();
	private static final CancellationChecker NO_CHECKER = CancellationChecker
			.newChecker(null);
	
	private static final IDatatypeExtension DT_TYPE = new IDatatypeExtension() {
		
		@Override
		public String getTypeName() {
			return "DT";
		}
		
		@Override
		public String getId() {
			return "DT.id";
		}
		
		@Override
		public void addTypeParameters(ITypeConstructorMediator mediator) {
			// none
		}
		
		@Override
		public void addConstructors(IConstructorMediator mediator) {
			mediator.addConstructor("dt", "dt.id");
		}
	};
	
	private static final IDatatype DT = ff.makeDatatype(DT_TYPE);
	
	private static final FormulaFactory DT_FF = getInstance(DT.getExtensions());

	private static InputSequent makeInputSequent(TestSequent s) {
		return new InputSequent(s.hypotheses(), s.goal(), ff, NO_CHECKER);
	}

	private static InputSequent makeInputSequent(List<String> typenv,
			List<String> hyps, String goal, FormulaFactory factory) {
		return makeInputSequent(new TestSequent(typenv, hyps, goal, factory));
	}

	private static InputSequent makeInputSequent(List<String> typenv,
			List<String> hyps, String goal) {
		return makeInputSequent(typenv, hyps, goal, ff);
	}

	private static TestSequent makeTestSequent(List<String> typenv,
	List<String> hyps, final String goal, FormulaFactory factory) {
		return new TestSequent(typenv, hyps, goal, factory);
	}
	
	private static TestSequent makeTestSequent(List<String> typenv,
			List<String> hyps, final String goal) {
				return makeTestSequent(typenv, hyps, goal, ff);
			}
			
	/**
	 * Ensures that a closed sequent is properly normalized and translated.
	 */
	@Test
	public void closedSequent() throws Exception {
		final TestSequent sequent = makeTestSequent(NO_TE, asList("⊤"), "⊤");
		final InputSequent is = makeInputSequent(sequent);
		is.translate();
		sequent.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that a sequent containing only scalar variables is properly
	 * normalized and translated.
	 */
	@Test
	public void simpleSequent() throws Exception {
		final InputSequent is = makeInputSequent(NO_TE, asList("a = 0"),
				"b = 1");
		is.translate();

		final TestSequent expected = makeTestSequent(
				asList("a", "ℤ", "b", "ℤ"), asList("a = 0"), "b = 1");
		expected.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that a sequent is properly normalized before translation.
	 */
	@Test
	public void normalization() throws Exception {
		final InputSequent is = makeInputSequent(NO_TE, asList("p = 0 ↦ TRUE"),
				"q = FALSE ↦ 1");
		is.translate();
		final TestSequent expected = makeTestSequent(asList("p_1", "ℤ", "p_2",
				"BOOL", "q_1", "BOOL", "q_2", "ℤ"),
				asList("p_1 = 0 ∧ p_2 = TRUE"), "¬ q_1 = TRUE ∧ q_2 = 1");
		expected.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that two occurrences of the same variable are normalized in the
	 * same way before translation.
	 */
	@Test
	public void normalizationDouble() throws Exception {
		final InputSequent is = makeInputSequent(NO_TE,
				asList("p = 0 ↦ 1  ↦ (2 ↦ 3)"), "p = q ↦ r");
		is.translate();
		final TestSequent expected = makeTestSequent(NO_TE,
				asList("p_1 = 0 ∧ p_2 = 1 ∧ p_3 = 2 ∧ p_4 = 3"),
				"p_1 = q_1 ∧ p_2 = q_2 ∧ p_3 = r_1 ∧ p_4 = r_2");
		expected.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that normalization takes care of name conflicts.
	 */
	@Test
	public void normalizationConflict() throws Exception {
		final InputSequent is = makeInputSequent(NO_TE, asList("p = 0 ↦ 1",
				"p_1 = 2"), "p_2 = TRUE");
		is.translate();
		final TestSequent expected = makeTestSequent(NO_TE, asList(
				"p_3 = 0 ∧ p_4 = 1", "p_1 = 2"), "p_2 = TRUE");
		expected.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that normalization removes hypotheses with extended expressions
	 * while keeping those with identifiers bearing a parametric type.
	 */
	@Test
	public void testExtensionsInHyps() throws Exception {
		final List<String> tEnv = asList("p", "DT", "a", "ℤ", "x", "DT");
		final List<String> inHyps = asList("p = dt", "a = 0", "x ∈ DT",
				"∀y⦂DT,z⦂DT·y=z");
		final List<String> transHyps = asList("⊤", "a = 0", "⊤",
				"∀y⦂DT,z⦂DT·y=z");
		final String goal = "a < a";
		final InputSequent is = makeInputSequent(tEnv, inHyps, goal, DT_FF);
		final TestSequent exp = makeTestSequent(tEnv, transHyps, goal, DT_FF);
		is.translate();
		exp.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that normalization makes a false goal if it contains extended
	 * expressions.
	 */
	@Test
	public void testExtensionsInGoal() throws Exception {
		final List<String> tEnv = asList("p", "DT", "a", "ℤ");
		final List<String> hyp = asList("a = 0");
		final InputSequent is = makeInputSequent(tEnv, hyp, "p = dt", DT_FF);
		final TestSequent expected = makeTestSequent(tEnv, hyp, "⊥", DT_FF);
		is.translate();
		expected.assertTranslatedSequentOf(is);
	}

	/**
	 * Ensures that normalization succeeds if goal contains identifiers bearing
	 * a parametric type.
	 */
	@Test
	public void testExtensionsInTypeEnv() throws Exception {
		final List<String> tEnv = asList("a", "ℤ", "x", "DT");
		final List<String> inHyps = asList("a = 0", "x ∈ DT");
		final List<String> transHyps = asList("a = 0", "⊤");
		final String goal = "∀y⦂DT·y=x";
		final InputSequent is = makeInputSequent(tEnv, inHyps, goal, DT_FF);
		final TestSequent exp = makeTestSequent(tEnv, transHyps, goal, DT_FF);
		is.translate();
		exp.assertTranslatedSequentOf(is);
	}

}
