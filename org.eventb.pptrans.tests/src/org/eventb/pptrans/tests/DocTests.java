/*******************************************************************************
 * Copyright (c) 2006, 2011 ETH Zurich and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added test2648946
 *     Systerel - added test2962503
 *     Systerel - added test for SMT solvers
 *******************************************************************************/
package org.eventb.pptrans.tests;

import static org.eventb.core.ast.tests.FastFactory.mList;
import static org.eventb.core.ast.tests.FastFactory.mTypeEnvironment;

import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.pptrans.Translator;

public class DocTests extends AbstractTranslationTests {
	
	private static void doTransTest(String input, String expected, boolean transformExpected, ITypeEnvironment te) {
		Predicate pinput = parse(input, te);
		Predicate pexpected = parse(expected, te);
		if(transformExpected) {
			pexpected = Translator.simplifyPredicate(pexpected, ff);
			pexpected = Translator.reduceToPredicateCalulus(pexpected, ff);
		}
		doTransTest(pinput, pexpected);
	}
	
	private static void doTransTest(Predicate input, Predicate expected) {
		assertTypeChecked(input);
		assertTypeChecked(expected);

		Predicate actual = Translator.reduceToPredicateCalulus(input, ff);
		actual = Translator.simplifyPredicate(actual, ff);

		assertTypeChecked(actual);
		assertTrue("Result not in goal: " + actual, Translator.isInGoal(actual));
		assertEquals("Unexpected result of translation", expected, actual);
	}
	
	@SuppressWarnings("deprecation")
	private void doDecompTest(String inputString, String expectedString, ITypeEnvironment te) {
		final Predicate input = parse(inputString, te);
		final Predicate expected = parse(expectedString, te);
		final Predicate actual = Translator.decomposeIdentifiers(input, ff);
		assertTypeChecked(actual);
		assertEquals("Wrong identifier decomposition", expected, actual);
	}

	
	public void testDoc1() {
		
		doDecompTest( 	"∀x·10↦(20↦30)=x",
						"∀x,x0,x1·10↦(20↦30)=x↦(x0↦x1)",
						mTypeEnvironment());
	}

	public void testDoc2() {
		
		doDecompTest(	"a=b ∧ a ∈ S",
						"∀x0,x1,x2,x3·(a=x0↦x1 ∧ b = x2 ↦x3)⇒(x0↦x1=x2↦x3 ∧ x0↦x1 ∈ S)",
						mTypeEnvironment(
								mList("a", "b", "S"),
								mList(CPROD(INT, INT), CPROD(INT, INT), REL(INT, INT))));
	}
	
	public void testDoc3() {
		
		doTransTest(	"p⊆S ∧ q⊆S ⇒ (p⊆q ⇔ S∖q ⊆ S∖p)",
						"(∀x·x∈p⇒x∈S)∧(∀y·y∈q⇒y∈S)⇒((∀z·z∈p⇒z∈q)⇔(∀t·t∈S∧¬t∈q⇒t∈S∧¬t∈p))",
						false,
						mTypeEnvironment(
								mList("p", "S", "q"),
								mList(INT_SET, INT_SET, INT_SET)));
	}
	
	public void testDoc4() {
		
		doTransTest(	"u ≠ ∅ ⇒ (∀t·t∈u ⇒ inter(u) ⊆ t)",
						"¬(∀x·¬x∈u) ⇒ (∀t·t∈u ⇒ (∀x·(∀s·s∈u ⇒ x∈s)⇒x∈t))",
						false,
						mTypeEnvironment(
								mList("u", "t"),
								mList(POW(INT_SET), INT_SET)));
	}

	public void testDoc5() {
		
		doTransTest(	"(S ◁ r)∼  =r∼  ▷ S",
						"(∀x,y·y↦x ∈ r ∧ y∈S  ⇔ y↦x ∈ r ∧ y∈S)",
						true,
						mTypeEnvironment(
								mList("S", "r"),
								mList(INT_SET, REL(INT, BOOL))));
	}
	
	public void testDoc6() {
		
		doTransTest(	"a ⊆ b ⇒ r[a] ⊆ r[b]",
						"(∀x·x∈a ⇒ x∈b) ⇒ (∀y·(∃z·z∈a ∧ z↦y∈r) ⇒ (∃t·t∈b∧t↦y∈r))",
						false,
						mTypeEnvironment(
								mList("a", "b", "r"),
								mList(INT_SET, INT_SET, REL(INT, BOOL))));
	}

	public void testUseCase1() {

		final ITypeEnvironment te = ff.makeTypeEnvironment();
		te.addGivenSet("GS");
		te.addGivenSet("GT");
		te.addName("S", POW(mGivenSet("GS")));
		te.addName("T", POW(mGivenSet("GT")));

		doTransTest("r ∈ S↔T", "∀x,y·x↦y∈r ⇒ x∈S ∧ y∈T", false, te);
	}

	public void testUseCase2() {

		final ITypeEnvironment te = mTypeEnvironment(
		"r", "GS ↔ GU", "s", "GU ↔ GT");

		doTransTest("r;s ∈ S↔T",
				"∀x,y·(∃z·x↦z∈r ∧ z↦y∈s) ⇒ x∈S ∧ y∈T",
				false,
				te);
	}

	public void testIR34_full() {
		final ITypeEnvironment te = mTypeEnvironment("r", "S↔T");
		doTransTest("e↦f ∈ rs",
				"(e↦f ∈ r ∧ ¬(∃x·e↦x ∈ s)) ∨ e↦f ∈ s",
				false, 
				te);
	}

	public void testIR34_full2() {
		final ITypeEnvironment te = mTypeEnvironment("r", "S↔T");
		doTransTest("rs ∈ A↔B",
				"∀x,y·(x↦y ∈ r ∧ ¬(∃z·x↦z ∈ s)) ∨ x↦y ∈ s ⇒ x ∈ A ∧ y ∈ B",
				false, 
				te);
	}

	public void testIR34_full3() {
		final ITypeEnvironment te = mTypeEnvironment("r", "S↔T");
		doTransTest("r{a ↦ b} ∈ A↔B",
				"∀x,y·(x↦y ∈ r ∧ ¬(∃z·x = a ∧ z = b)) ∨ (x = a ∧ y = b)" +
				"     ⇒ x ∈ A ∧ y ∈ B",
				false, 
				te);
	}

	public void testBool_01() {
		final ITypeEnvironment te = mTypeEnvironment();
		doTransTest("bool(bool(x = 5) = TRUE) = TRUE",
				"x = 5",
				false, 
				te);
	}

	public void testBool_02() {
		final ITypeEnvironment te = mTypeEnvironment();
		doTransTest("bool(bool(x = 5) = FALSE) = TRUE",
				"¬(x = 5)",
				false, 
				te);
	}

	public void testBool_03() {
		final ITypeEnvironment te = mTypeEnvironment();
		doTransTest("bool(x = 5) = f(x)",
				"∃y·(y = TRUE ⇔ x = 5) ∧ x ↦ y ∈ f",
				false, 
				te);
	}

	public void testBool_04() {
		final ITypeEnvironment te = mTypeEnvironment();
		doTransTest("bool(x = 5) ∈ S",
				"∃y·(y = TRUE ⇔ x = 5) ∧ y ∈ S",
				false, 
				te);
	}

	public void testBool_05() {
		final ITypeEnvironment te = mTypeEnvironment("f", "ℙ(BOOL×S)");
		doTransTest("f(bool(x = 5)) = a",
				"∃y·(y = TRUE ⇔ x = 5) ∧ y ↦ a ∈ f",
				false, 
				te);
	}

	public void testBool_06() {
		final ITypeEnvironment te = mTypeEnvironment("f", "ℙ(S×BOOL)");
		doTransTest("f(a) = bool(x = 5)",
				"∃y·(y = TRUE ⇔ x = 5) ∧ a ↦ y ∈ f",
				false, 
				te);
	}

	public void testBool_07() {
		final ITypeEnvironment te = mTypeEnvironment("f", "ℙ(BOOL×BOOL×BOOL×S)");
		doTransTest("f(bool(x = 5) ↦ bool(x = 6) ↦ bool(x = 7)) = a",
				"∃y,z,t·(y = TRUE ⇔ x = 5)" +
				"     ∧ (z = TRUE ⇔ x = 6)" +
				"     ∧ (t = TRUE ⇔ x = 7)" +
				"     ∧ y ↦ z ↦ t ↦ a ∈ f",
				false, 
				te);
	}

	public void testBool_08() {
		final ITypeEnvironment te = mTypeEnvironment("f", "ℙ(BOOL×S×ℤ)");
		doTransTest("f(bool(x = 5) ↦ a)∈ℕ",
				"∀y·(∃z·(z = TRUE ⇔ x = 5) ∧ z ↦ a ↦ y ∈ f) ⇒ 0 ≤ y",
				false, 
				te);
	}

	public void test2648946() {
		final ITypeEnvironment te = mTypeEnvironment("A", "ℙ(A)", "B", "ℙ(B)");
		doTransTest("G ⊆ A ∧ H ⊆ A ∧ f ∈ ℙ(A) → ℙ(B) ⇒ G ∪ H ∈ dom(f)",//
				"  (∀x,y,z· x↦y∈f ∧ x↦z∈f ⇒ y=z)" +
				"∧ (∀x·∃y·x↦y∈f)" +
				"⇒ (∃x·∃S·(∀x·x∈S ⇔ x∈G ∨ x∈H) ∧ S↦x∈f)",//
				false, te);
	}

	public void test2962503() {
		final ITypeEnvironment te = mTypeEnvironment("i", "ℤ", "j", "ℤ");
		doTransTest("i≥0 ∧ j≥0 ⇒" +
				" j∈dom(succ) ∧ succ∈ℤ ⇸ ℤ ∧" +
				" 0≤i ∧ 0≤succ(j) ∧ 0≤i ∧ 0≤j",//
				"0 ≤ i ∧ 0 ≤ j ⇒" +
				" (∃x·x = j + 1) ∧ (∀x,y,z·y=x+1 ∧ z=x+1 ⇒ y=z) ∧" +
				" 0 ≤ i ∧ (∀x·x=j+1 ⇒ 0≤x) ∧ 0 ≤ i ∧ 0 ≤ j",//
				false, te);
	}

	/**
	 * Unit test coming from usage in translation to SMT solvers. Shows that
	 * predefined set types <code>BOOL</code> and <code>ℤ</code> are retained in
	 * the left-hand side of a membership predicate.
	 */
	public void testSMT1() {
		final ITypeEnvironment te = mTypeEnvironment("a", "S");
		doTransTest("a↦BOOL↦ℤ ∈ A", "a↦BOOL↦ℤ ∈ A", false, te);
	}

	/**
	 * Other unit tests used for verifying assumptions about the translation
	 * of booleans.
	 */
	public void testSMT2() throws Exception {
		final ITypeEnvironment te = mTypeEnvironment("f", "BOOL ↔ S");
		doTransTest("f(TRUE) = a", "∃x·x=TRUE ∧ x↦a ∈ f", false, te);
		doTransTest("f(FALSE) = a", "∃x·¬x=TRUE ∧ x↦a ∈ f", false, te);
		doTransTest("b = bool(c=TRUE ∧ FALSE=d)",
				"b=TRUE ⇔ (c=TRUE ∧ ¬d=TRUE)", false, te);
		doTransTest("A=BOOL", "∀x· x ∈ A", false, te);
	}
}
