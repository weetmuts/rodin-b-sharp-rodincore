/*******************************************************************************
 * Copyright (c) 2005, 2010 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added abstract test class
 *     Systerel - mathematical language v2
 *     Systerel - added additional acceptance tests + improved result
 *******************************************************************************/
package org.eventb.core.ast.tests;

import static java.util.Arrays.asList;
import static org.eventb.core.ast.tests.FastFactory.mTypeEnvironment;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BooleanType;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.IntegerType;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.ParametricType;
import org.eventb.core.ast.PowerSetType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.extension.ICompatibilityMediator;
import org.eventb.core.ast.extension.IExpressionExtension;
import org.eventb.core.ast.extension.IExtendedFormula;
import org.eventb.core.ast.extension.IExtensionKind;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.ast.extension.IPriorityMediator;
import org.eventb.core.ast.extension.ITypeCheckMediator;
import org.eventb.core.ast.extension.ITypeMediator;
import org.eventb.core.ast.extension.IWDMediator;
import org.eventb.core.ast.extension.datatype.IArgument;
import org.eventb.core.ast.extension.datatype.IArgumentType;
import org.eventb.core.ast.extension.datatype.IConstructorMediator;
import org.eventb.core.ast.extension.datatype.IDatatype;
import org.eventb.core.ast.extension.datatype.IDatatypeExtension;
import org.eventb.core.ast.extension.datatype.ITypeConstructorMediator;
import org.eventb.core.ast.extension.datatype.ITypeParameter;

/**
 * Unit and acceptance tests for the computation of WD lemmas.
 * 
 * @author Stefan Hallerstede
 */
public class TestWD extends AbstractTests {

	private static IntegerType INTEGER = ff.makeIntegerType();
	private static BooleanType BOOL = ff.makeBooleanType();
	private static GivenType S = ff.makeGivenType("S");

	static ITypeEnvironment defaultTEnv = mTypeEnvironment(//
			"x", INTEGER,//
			"y", INTEGER,//
			"A", POW(INTEGER),//
			"B", POW(INTEGER),//
			"f", POW(CPROD(INTEGER, INTEGER)),//
			"Y", POW(BOOL),//
			"S", POW(S)//
	);

	// static final List<TestFormula<?>> tests = new
	// ArrayList<TestFormula<?>>();

	private static abstract class TestFormula<T extends Formula<T>> {

		final T input;
		final Predicate originalPredicate;
		final Predicate simplifiedPredicate;
		protected final FormulaFactory factory;

		TestFormula(ITypeEnvironment env, String in, String exp, String imp) {
			this.factory = env.getFormulaFactory();
			this.input = parse(in);
			this.originalPredicate = parsePredicate(exp, factory).flatten(factory);
			this.simplifiedPredicate = parsePredicate(imp, factory).flatten(factory);
			typeCheck(input, env);
			typeCheck(originalPredicate, env);
			typeCheck(simplifiedPredicate, env);
			// tests.add(this);
		}

		// private static final WDComputer computer = new WDComputer(ff);

		public void test() {
			// final Predicate actual = computer.getWDLemma(input);
			final Predicate actual = input.getWDPredicate(factory);
			assertTrue("Ill-formed WD predicate", actual.isWellFormed());
			assertTrue("Untyped WD predicate", actual.isTypeChecked());
			assertEquals(simplifiedPredicate, actual);

			// final Predicate impAct = new WDImprover(ff).improve(actual);
			// assertTrue("Ill-formed WD predicate", impAct.isWellFormed());
			// assertTrue("Untyped WD predicate", impAct.isTypeChecked());
			// assertEquals(improvedExpected, impAct);
		}

		public abstract T parse(String image);
	}

	private static class TestPredicate extends TestFormula<Predicate> {

		TestPredicate(ITypeEnvironment env, String in, String exp, String imp) {
			super(env, in, exp, imp);
		}

		@Override
		public Predicate parse(String image) {
			return parsePredicate(image, factory);
		}

	}

	private static class TestAssignment extends TestFormula<Assignment> {

		TestAssignment(String in, String exp, String imp, FormulaFactory factory) {
			super(defaultTEnv, in, exp, imp);
		}

		@Override
		public Assignment parse(String image) {
			return parseAssignment(image, factory);
		}

	}

	private static void assertWDLemma(String in, String expected) {
		assertWDLemma(defaultTEnv, in, expected);
	}

	private static void assertWDLemma(String in, String expected,
			String improvedExpected) {
		assertWDLemma(defaultTEnv, in, expected, improvedExpected);
	}

	private static void assertWDLemma(ITypeEnvironment env, String in,
			String expected) {
		assertWDLemma(env, in, expected, expected);
	}

	private static void assertWDLemma(ITypeEnvironment env, String in,
			String expected, String improvedExpected) {
		final TestPredicate test = new TestPredicate(env, in, expected,
				improvedExpected);
		test.test();
	}

	private static void assertWDLemmaAssignment(String in, String expected) {
		assertWDLemmaAssignment(in, expected, expected);
	}

	private static void assertWDLemmaAssignment(String in, String expected,
			String improvedExpected) {
		final TestAssignment test = new TestAssignment(in, expected,
				improvedExpected, ff);
		test.test();
	}

	public void testWD() {
		assertWDLemma("x≠y ∧ y=1", "⊤");
		assertWDLemma("x+y+x+1=0 ⇒ y<x", "⊤");
		assertWDLemma("x+1=0 ∨ x<y", "⊤");
		assertWDLemma("(∃x \u00b7 0<x ⇒ (∀y \u00b7 y+x=0))", "⊤");
		assertWDLemma("(B×Y)(x) ∈ Y", "x∈dom(B × Y) ∧ B × Y ∈ ℤ ⇸ BOOL");
		assertWDLemma(
				"x=f(f(y))", //
				"((y∈dom(f) ∧ f ∈ ℤ ⇸ ℤ) ∧ f(y)∈dom(f)) ∧ f ∈ ℤ ⇸ ℤ",
				"y∈dom(f) ∧ f ∈ ℤ ⇸ ℤ ∧ f(y)∈dom(f)");
		assertWDLemma("(x÷y=y) ⇔ (y mod x=0)", "y≠0 ∧ 0 ≤ y ∧ 0 < x");
		assertWDLemma("∀z \u00b7 x^z>y", "∀z \u00b7 0≤x ∧ 0≤z");
		assertWDLemma("card(A)>x", "finite(A)");
		assertWDLemma("inter({A,B}) ⊆ A∩B", "{A,B}≠∅");
		assertWDLemma("(λ m↦n \u00b7 m>n \u2223 y)(1↦x) = y",
				"1 ↦ x∈dom(λm ↦ n\u00b7m>n ∣ y) "
						+ "∧ (λm ↦ n\u00b7m>n ∣ y) ∈ (ℤ×ℤ) ⇸ ℤ");
		assertWDLemma("{m,n \u00b7 m=f(n) \u2223 m↦n}[A] ⊂ B",
				"∀n \u00b7 n∈dom(f) ∧ f ∈ ℤ ⇸ ℤ");
		assertWDLemma("{f(n)↦m \u2223 x=n ∧ y+x=m ∧ f ∈ ℤ→A} = A×B",
				"∀f,n,m \u00b7 x=n ∧ y+x=m ∧ f ∈ ℤ→A ⇒ n∈dom(f) ∧ f ∈ ℤ ⇸ ℤ");
		assertWDLemma("{1, 2, x, x+y, 4, 6} = B", "⊤");
		assertWDLemma("(⋂ m,n \u00b7 m∈A ∧ n∈B \u2223 {m÷n}) = B",
				"(∀m,n \u00b7 (m∈A ∧ n∈B) ⇒ n≠0) ∧ (∃m,n \u00b7 (m∈A ∧ n∈B))");
		assertWDLemma("(⋂{m+n} \u2223 m+n∈A)=B", "∃m,n\u00b7m+n∈A");
		assertWDLemma("bool(⊤)=bool(⊥)", "⊤");
		assertWDLemma("x+y+(x mod y)\u2217x+1=0 ⇒ y<x", "0 ≤ x ∧ 0 < y");
		assertWDLemmaAssignment("x≔y", "⊤");
		assertWDLemmaAssignment("x :\u2223 x'>x", "⊤");
		assertWDLemmaAssignment("x :∈ {x,y}", "⊤");
		assertWDLemmaAssignment("x :∈ {x÷y, y}", "y≠0");
		assertWDLemmaAssignment("f(x)≔f(x)", "x∈dom(f)∧f ∈ ℤ ⇸ ℤ");
		assertWDLemmaAssignment("x :\u2223 x'=card(A∪{x'})",
				"∀x' \u00b7 finite(A∪{x'})");
		assertWDLemma("a = {x∣x≤card(A)}", "finite(A)");
		assertWDLemma("a = min(A)", "A ≠ ∅ ∧ (∃b·∀x·x∈A ⇒ b≤x)");
		assertWDLemma("a = max(A)", "A ≠ ∅ ∧ (∃b·∀x·x∈A ⇒ b≥x)");
		assertWDLemma("a = max(A)", "A ≠ ∅ ∧ (∃b·∀x·x∈A ⇒ b≥x)");

		assertWDLemma("T ⊆ S ∧ g ∈ ℤ → T ⇒ (∃S·g(S) ∈ T)",
				"T ⊆ S ∧ g ∈ ℤ → T ⇒ (∀S0·S0 ∈ dom(g) ∧ g ∈ ℤ ⇸ S)");

		assertWDLemma("∀f,y·f∈ℤ → ℤ ⇒ (∃x·x = f(y))",
				"∀f,y·f∈ℤ → ℤ ⇒ y∈dom(f) ∧ f∈ℤ ⇸ ℤ");

		assertWDLemma("∀y·∃x·x = f(y)", "∀y·y∈dom(f) ∧ f∈ℤ ⇸ ℤ");

		assertWDLemma("f(x)=f(y)", "x∈dom(f) ∧ f∈ℤ ⇸ ℤ ∧ y∈dom(f) ∧ f∈ℤ ⇸ ℤ",
				"x∈dom(f) ∧ f∈ℤ ⇸ ℤ ∧ y∈dom(f)");

		// Ensure that a type name doesn't get captured
		// when computing a WD lemma
		assertWDLemma("T ⊆ S ∧ g ∈ ℤ → T ⇒ (∃S·g(S) ∈ T)",
				"T ⊆ S ∧ g ∈ ℤ → T ⇒ (∀S0·S0 ∈ dom(g) ∧ g ∈ ℤ ⇸ S)");

		// Example from the Mobile model
		assertWDLemma("a ∈ S ↔ S ∧ b ∈ S ↔ (ℤ ↔ S) ∧"
				+ "(∀s·s ∈ dom(a) ⇒ a(s) = b(s)(max(dom(b(s)))))"//
		,//
				"a∈S ↔ S ∧ b∈S ↔ (ℤ ↔ S) ⇒"
						+ "  (∀s·s∈dom(a) ⇒ s∈dom(a) ∧ a∈S ⇸ S ∧"
						+ "   s∈dom(b) ∧ b∈S ⇸ ℙ(ℤ × S) ∧"
						+ "   s∈dom(b) ∧ b∈S ⇸ ℙ(ℤ × S) ∧"
						+ "   dom(b(s))≠∅ ∧ (∃b0·∀x·x∈dom(b(s))⇒b0≥x) ∧"
						+ "   max(dom(b(s)))∈dom(b(s)) ∧ b(s)∈ℤ ⇸ S)"//
				,//
				"a∈S ↔ S ∧ b∈S ↔ (ℤ ↔ S) ⇒" + "  (∀s·s∈dom(a) ⇒ a∈S ⇸ S ∧"
						+ "   s∈dom(b) ∧ b∈S ⇸ ℙ(ℤ × S) ∧"
						+ "   dom(b(s))≠∅ ∧ (∃b0·∀x·x∈dom(b(s))⇒b0≥x) ∧"
						+ "   max(dom(b(s)))∈dom(b(s)) ∧ b(s)∈ℤ ⇸ S)"//
		);

		// Reduced example extracted from the preceding one
		assertWDLemma("∀s·max(s) ∈ s", "∀s·s≠∅ ∧ (∃b·∀x·x ∈ s  ⇒ b ≥ x)");

		// Case where a bound variable disappears
		assertWDLemma("∀y·∃x·x = f(y)", "∀y·y∈dom(f) ∧ f∈ℤ ⇸ ℤ");
		assertWDLemma("∀f,y·f∈ℤ → ℤ ⇒ (∃x·x = f(y))",
				"∀f,y·f∈ℤ → ℤ ⇒ y∈dom(f) ∧ f∈ℤ ⇸ ℤ");

		// Disjunctions
		assertWDLemma("a÷1=b ∨ a÷2=b",//
				"1≠0 ∧ (a÷1=b ∨ 2≠0)");
		assertWDLemma("a÷1=b ∨ a÷2=b ∨ a÷3=b",
				"1≠0 ∧ (a÷1=b ∨ (2≠0 ∧ (a÷2=b ∨ 3≠0)))");
		assertWDLemma("a÷1=b ∨ a=b ∨ a÷3=b",//
				"1≠0 ∧ (a÷1=b ∨ a=b ∨ 3≠0)");
	}

	public void testRedundant() {
		assertWDLemma("3÷P = 4÷P", "P≠0 ∧ P≠0", "P≠0");
		assertWDLemma("∃x·x=a÷b", "∀x·b≠0");
		assertWDLemma("3÷P=0 ∧ 2=5 ∧ 6÷P=0", "P≠0 ∧ (3÷P=0 ∧ 2=5 ⇒ P≠0)", "P≠0");
		assertWDLemma("f(x)=f(y)", "x∈dom(f) ∧ f∈ℤ⇸ℤ ∧ y∈dom(f) ∧ f∈ℤ⇸ℤ",
				"x∈dom(f) ∧ f∈ℤ⇸ℤ ∧ y∈dom(f)");
	}

	/**
	 * Tests coming from model "routing_new" from Jean-Raymond Abrial.
	 */
	public void testRouting() {
		final ITypeEnvironment env = mTypeEnvironment(//
				"N", "ℙ(N)", //
				"age", "L ↔ ℤ", //
				"l_net", "ℤ ↔ L",//
				"parity", "ℤ ↔ ℤ");

		// inv11/WD in rm_3
		assertWDLemma(env, //
				"∀n,l· n∈N ∧ l∈L ⇒ (n↦l∈m_net_up ⇔ n↦l↦age(l)∈n_net"
						+ " ∧ parity(age(l))=1)", //
				"∀ n, l · n∈N ∧ l∈L" //
						+ "  ⇒"//
						+ "    l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "(      n ↦ l ↦ age(l)∈n_net" //
						+ "    ⇒"//
						+ "      l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "      age(l)∈dom(parity) ∧" //
						+ "      parity∈ℤ ⇸ ℤ)",

				"∀ n, l · n∈N ∧ l∈L" //
						+ "  ⇒"
						+ "    l∈dom(age) ∧ age∈L ⇸ ℤ ∧"
						+ "(      n ↦ l ↦ age(l)∈n_net" //
						+ "    ⇒" + "      age(l)∈dom(parity) ∧" //
						+ "      parity∈ℤ ⇸ ℤ)");

		// inv3/WD in rm_3
		assertWDLemma(env, "∀l·l∈L ⇒ (l∈net ⇔ parity(age(l))=1)",//
				"∀ l · l∈L" //
						+ "⇒"//
						+ "l∈dom(age) ∧ age∈L ⇸ ℤ ∧"
						+ "age(l)∈dom(parity) ∧"
						+ "parity∈ℤ ⇸ ℤ");

		// change_link2/ln/WD
		assertWDLemma(env, "(parity(x)=0 ⇒ ln = l_net∖{n↦l}) ∧"//
				+ "(parity(x)=1 ⇒ ln = l_net∪{n↦l})",
				"x∈dom(parity) ∧ parity∈ℤ ⇸ ℤ ∧"//
						+ "((parity(x)=0 ⇒ ln=l_net ∖ {n ↦ l})"//
						+ "⇒"//
						+ "x∈dom(parity) ∧ parity∈ℤ ⇸ ℤ)",
				"x∈dom(parity) ∧ parity∈ℤ ⇸ ℤ");

		// inv5/WD in rm_3
		assertWDLemma(env, "∀l·l∈L ⇒ n_net[N×{l}] ⊆ 0‥age(l)",
				"∀l·l∈L ⇒ l∈dom(age) ∧ age∈L ⇸ ℤ");

		// inv7/WD in rm_7
		assertWDLemma(env,
				"∀n,l·n∈N ∧ l∈L ⇒ age(l)=l_age(n↦l) ∨ n↦l↦age(l)∈n_net ",
				"∀ n, l · "//
						+ "    n∈N ∧ l∈L"//
						+ "  ⇒"//
						+ "    l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "    n ↦ l∈dom(l_age) ∧"//
						+ "    l_age∈N × L ⇸ ℤ ∧"//
						+ "    (age(l)=l_age(n ↦ l) ∨"//
						+ "     (l∈dom(age) ∧ age∈L ⇸ ℤ))");//

		// inv 10 WD in rm_3
		assertWDLemma(
				env,
				"∀n,l· n∈N ∧ l∈L ⇒ (n↦l∈m_net_up ⇔ n↦l↦age(l)∈n_net ∧ parity(age(l))=1)",
				"∀ n, l ·"//
						+ "    n∈N ∧ l∈L"//
						+ "  ⇒"//
						+ "    l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "    (n ↦ l ↦ age(l)∈n_net"//
						+ "    ⇒"//
						+ "      l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "      age(l)∈dom(parity) ∧"//
						+ "      parity∈ℤ ⇸ ℤ)",//
				"∀ n, l ·"//
						+ "    n∈N ∧ l∈L"//
						+ "  ⇒"//
						+ "    l∈dom(age) ∧ age∈L ⇸ ℤ ∧"//
						+ "    (n ↦ l ↦ age(l)∈n_net"//
						+ "    ⇒"//
						+ "      age(l)∈dom(parity) ∧"//
						+ "      parity∈ℤ ⇸ ℤ)");//
	}

	/**
	 * Tests coming from model "DIR41.4 "
	 */
	public void testDIR() {
		final ITypeEnvironment env = mTypeEnvironment(//
				"T", "ℙ(T)", //
				"C", "ℙ(C)",//
				"SI", "ℙ(SI)",//
				"CH", "ℙ(CH)",//
				"CO", "ℙ(CO)",//
				"pcoc", "CO ↔ C",//
				"p_at", "CO ↔ CO",//
				"p_c_a", "CO ↔ CO",//
				"p_c_inv", "CO ↔ CO",//
				"c_chemin_signal", "CH ↔ SI",//
				"c_chemin_cellule_accès", "CH ↔ CO",//
				"c_signal_cellule_arrêt", "SI ↔ CO",//
				"closure1", "(CO ↔ CO) ↔ (CO ↔ CO)"//
		);

		// thm1/WD in Atteignable
		assertWDLemma(env, "∀x,y · x↦y ∈ p_at ⇒ p_c_inv(y)↦p_c_inv(x) ∈ p_at",
				"∀ x, y · "//
						+ "    x ↦ y∈p_at"//
						+ "  ⇒"//
						+ "    y∈dom(p_c_inv) ∧"//
						+ "    p_c_inv∈ CO ⇸ CO ∧"//
						+ "    x∈dom(p_c_inv) ∧"//
						+ "    p_c_inv∈ CO ⇸ CO",//
				"∀ x, y · "//
						+ "    x ↦ y∈p_at"//
						+ "  ⇒"//
						+ "    y∈dom(p_c_inv) ∧"//
						+ "    p_c_inv∈ CO ⇸ CO ∧"//
						+ "    x∈dom(p_c_inv)");

		// thm2/WD in C_SIGNAUX
		assertWDLemma(
				env,
				" ∀ ch, s ·"//
						+ "		ch ∈ dom(c_chemin_signal) ∧"//
						+ "		c_chemin_cellule_accès(ch)=c_signal_cellule_arrêt(s)"//
						+ "		 ⇒"//
						+ "		 	c_chemin_signal(ch) = s"//
				,//
				"∀ ch, s ·"//
						+ "(    ch∈dom(c_chemin_signal)"//
						+ "  ⇒"//
						+ "    ch∈dom(c_chemin_cellule_accès)    ∧"//
						+ "    c_chemin_cellule_accès∈CH ⇸ CO    ∧"//
						+ "    s∈dom(c_signal_cellule_arrêt)    ∧"//
						+ "    c_signal_cellule_arrêt∈SI ⇸ CO)  ∧"//
						+ "    (ch∈dom(c_chemin_signal) ∧"//
						+ "     c_chemin_cellule_accès(ch)=c_signal_cellule_arrêt(s)"//
						+ "     ⇒"//
						+ "     ch∈dom(c_chemin_signal) ∧"
						+ "     c_chemin_signal∈CH ⇸ SI)",//
				"∀ ch, s ·"//
						+ "(    ch∈dom(c_chemin_signal)"//
						+ "  ⇒"//
						+ "    ch∈dom(c_chemin_cellule_accès)    ∧"//
						+ "    c_chemin_cellule_accès∈CH ⇸ CO    ∧"//
						+ "    s∈dom(c_signal_cellule_arrêt)    ∧"//
						+ "    c_signal_cellule_arrêt∈SI ⇸ CO)  ∧"//
						+ "    (ch∈dom(c_chemin_signal) ∧"//
						+ "     c_chemin_cellule_accès(ch)=c_signal_cellule_arrêt(s)"//
						+ "     ⇒"//
						+ "     c_chemin_signal∈CH ⇸ SI)");

		// axm2/WD in Closure1
		assertWDLemma(env, "∀R · closure1(R);R ⊆ closure1(R)",
				"∀R·R∈dom(closure1)∧closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)∧"
						+ "R∈dom(closure1)∧closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)",
				"∀R·R∈dom(closure1)∧closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)");

		// thm2/WD in Closure1
		assertWDLemma(env,
				"∀R1,R2 · R1⊆R2 ⇒ closure1(R1) ⊆ closure1(R2)",//
				"∀ R1, R2 · R1⊆R2 ⇒"//
						+ "  R1∈dom(closure1) ∧ "
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  R2∈dom(closure1) ∧ "
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)",
				"∀ R1, R2 · R1⊆R2 ⇒"//
						+ "  R1∈dom(closure1) ∧ "
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  R2∈dom(closure1)");

		// thm7/WD in Closure1
		assertWDLemma(env, "∀R · closure1(R);closure1(R) ⊆ closure1(R)	",//
				"∀ R ·" //
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)",//
				"∀ R ·" //
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)");

		// thm7/WD in closure 1 modified
		assertWDLemma(env, "∀R · closure1(R) ⊆ closure1(R)",//
				"∀ R ·" //
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)",//
				"∀ R ·" //
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)");

		// thm18/WD in Closure1
		assertWDLemma(
				env,
				"∀R · R∈ CO ⇸ CO ⇒ closure1(R) ⊆ {x↦y ∣ x↦y ∈ closure1(R) ∧"
						+ "(∀z · "
						+ "  x↦z ∈ closure1(R) ∧ y≠z ∧ z↦y ∉ closure1(R)"//
						+ "  ⇒"//
						+ "  y↦z ∈ closure1(R))}",//
				"∀ R · R∈CO ⇸ CO ⇒"//
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "  (∀ x, y ·"
						+ "    R∈dom(closure1) ∧"
						+ "    closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "    (x ↦ y∈closure1(R)"
						+ "     ⇒"
						+ "     (∀ z · "
						+ "      R∈dom(closure1) ∧"
						+ "      closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "      (x ↦ z∈closure1(R) ∧ y≠z"
						+ "       ⇒"
						+ "       R∈dom(closure1) ∧"
						+ "       closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)) ∧"
						+ "       (x ↦ z∈closure1(R) ∧ y≠z ∧"
						+ "        z ↦ y∉closure1(R)"
						+ "        ⇒"
						+ "        R∈dom(closure1) ∧"
						+ "        closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)))))",
				"∀ R · R∈CO ⇸ CO ⇒"//
						+ "  R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)");

		// thm13/WD in Closure1
		assertWDLemma(env,//
				"∀R,x,y ·"//
						+ "R ∈ CO ⇸ CO ∧ y ∈ closure1(R)[{x}] ⇒"
						+ "((closure1(R)[{x}])∖(closure1(R)[{y}])) ⊆"
						+ "((closure1(R))∼)[{y}] ∪ {y}",//
				"∀R,x,y·"
						+ "(R∈CO ⇸ CO"//
						+ " ⇒ R∈dom(closure1) ∧"
						+ "   closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)) ∧"
						+ "(R∈CO ⇸ CO ∧ y∈(closure1(R))[{x}]"
						+ " ⇒ R∈dom(closure1) ∧ "
						+ "   closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "   R∈dom(closure1) ∧"
						+ "   closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO) ∧"
						+ "   R∈dom(closure1) ∧"
						+ "   closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO))",//
				"∀R,x,y·R∈CO ⇸ CO" + "⇒ R∈dom(closure1) ∧"
						+ "  closure1∈ℙ(CO × CO) ⇸ ℙ(CO × CO)");
	}

	/**
	 * Ensures that WD simplification does not mess up bound identifiers
	 */
	public void testQuantifiers() {
		final ITypeEnvironment env = mTypeEnvironment("f", REL(S, S));
		assertWDLemma(env,//
				"∀x·x ∈ dom(f) ⇒ (∃y · f(x) = f(y)) ",//
				"∀x·x∈dom(f) ⇒" //
						+ "(∀y·x∈dom(f) ∧ f∈S ⇸ S ∧ y∈dom(f) ∧ f∈S ⇸ S)",//
				"∀x·x∈dom(f) ⇒ (∀y·f∈S ⇸ S∧y∈dom(f))");
	}

	/**
	 * Ensures that WD simplification does not mess up bound identifiers, even
	 * in the presence of multiple quantifiers.
	 */
	public void testQuantifiedMany() {
		final ITypeEnvironment env = mTypeEnvironment(//
				"S", POW(S),//
				"f", REL(S, S)//
		);
		assertWDLemma(env,//
				"∀x·x∈dom(f) ⇒ (∃y,z·f(y) = f(z)) ",//
				"∀x·x∈dom(f) ⇒"//
						+ "(∀y,z·y∈dom(f) ∧ f∈S⇸S ∧ z∈dom(f) ∧ f∈S⇸S)",//
				"∀x·x∈dom(f) ⇒"//
						+ "(∀y,z·y∈dom(f) ∧ f∈S⇸S ∧ z∈dom(f))");

		assertWDLemma(env,//
				"(∃x·x⊆S) ⇒ (⋂x∣x⊆S) = S",//
				"(∃x·x⊆S) ⇒ (∃x·x⊆S)",//
				"⊤");
		assertWDLemma(env,//
				"(∃x·x⊆S) ⇒ (∀y·y=0 ⇒ (⋂x∣x⊆S) = S)",//
				"(∃x·x⊆S) ⇒ (∀y·y=0 ⇒ (∃x·x⊆S))",//
				"⊤");
		assertWDLemma(env,//
				"(∀y·y=0 ∧ (∃x·x⊆S)) ⇒ (⋂x∣x⊆S) = S",//
				"(∀y·y=0 ∧ (∃x·x⊆S)) ⇒ (∃x·x⊆S)",//
				"⊤");
	}

	/**
	 * Ensures that WD simplification does not mess up bound identifiers, even
	 * after a quantifier.
	 */
	public void testQuantifiedAfter() {
		final ITypeEnvironment env = mTypeEnvironment("f", REL(S, S));
		assertWDLemma(env,//
				"∀x·x∈dom(f) ⇒ (∃y·f(x)=f(y)) ∧ f(x)=f(x)",//
				"∀x·x∈dom(f) ⇒"//
						+ "(∀y·x∈dom(f) ∧ f∈S⇸S ∧ y∈dom(f) ∧ f∈S⇸S) ∧"//
						+ "((∃y·f(x)=f(y)) ⇒"//
						+ "   x∈dom(f) ∧ f∈S⇸S ∧"//
						+ "   x∈dom(f) ∧ f∈S⇸S)",//
				"∀x·x∈dom(f) ⇒ (∀y·f∈S⇸S ∧ y∈dom(f))");
	}

	/**
	 * Ensures that WD simplification does not mess up bound identifiers, even
	 * in the presence of deep nesting.
	 */
	public void testQuantifiedDeep() {
		assertWDLemma(
				mTypeEnvironment("S", POW(S)),//
				"∀f·f∈S ⇸ S ⇒ (∀x·f(x) = f(x) ⇒ (∃y·f(x) = f(y)))", //
				"∀f·f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧ f∈S⇸S ∧ x∈dom(f) ∧ f∈S⇸S ∧"//
						+ "    (f(x)=f(x) ⇒"
						+ "      (∀y·x∈dom(f) ∧ f∈S⇸S ∧ y∈dom(f) ∧ f∈S⇸S)))",
				"∀f·f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧"//
						+ "    (f(x)=f(x) ⇒ (∀y·y∈dom(f))))");
	}

	/**
	 * Ensures that WD conditions produced by two copies of the same predicate
	 * are properly simplified. Also checks, that if the duplication is not
	 * exact, only sound simplifications are carried.
	 */
	public void testQuantifierDeepDuplicate() {
		final ITypeEnvironment env = mTypeEnvironment("f", REL(S, S));
		assertWDLemma(
				env,//
				"f∈S ⇸ S ⇒ (∀x·f(x) = f(x) ⇒"//
						+ "   (∃y·f(x) = f(y)) ∧ (∃z·f(x) = f(z)))", //
				"f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧ f∈S⇸S ∧ x∈dom(f) ∧ f∈S⇸S ∧"//
						+ "    (f(x)=f(x) ⇒"
						+ "      (∀y·x∈dom(f) ∧ f∈S⇸S ∧ y∈dom(f) ∧ f∈S⇸S) ∧"
						+ "      ((∃y·f(x) = f(y)) ⇒"
						+ "        (∀z·x∈dom(f) ∧ f∈S⇸S ∧ z∈dom(f) ∧ f∈S⇸S))))",
				"f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧"//
						+ "    (f(x)=f(x) ⇒ (∀y·y∈dom(f))))");

		// With differing condition for nested quantified identifier "y"
		assertWDLemma(
				env,//
				"f∈S ⇸ S ⇒ (∀x·f(x) = f(x) ⇒"//
						+ "   (∃y·y∈dom(f) ⇒ f(x) = f(y)) ∧"
						+ "   (∃z·f(x) = f(z)))", //
				"f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧ f∈S⇸S ∧ x∈dom(f) ∧ f∈S⇸S ∧"//
						+ "    (f(x)=f(x) ⇒"
						+ "      (∀y·y∈dom(f) ⇒ x∈dom(f) ∧ f∈S⇸S ∧ y∈dom(f) ∧ f∈S⇸S) ∧"
						+ "      ((∃y·y∈dom(f) ⇒ f(x) = f(y)) ⇒"
						+ "        (∀z·x∈dom(f) ∧ f∈S⇸S ∧ z∈dom(f) ∧ f∈S⇸S))))",
				"f∈S ⇸ S ⇒ "//
						+ "  (∀x·x∈dom(f) ∧"//
						+ "    (f(x)=f(x) ∧ (∃y·y∈dom(f) ⇒ f(x) = f(y)) ⇒"
						+ "      (∀z·z∈dom(f))))");
	}

	/**
	 * Acceptance test for CDIS model
	 */
	public void testCDIS() {
		final ITypeEnvironment env = mTypeEnvironment(//
				"Attr_Id", "ℙ(Attr_id)",//
				"Attrs", "ℙ(Attrs)",//
				"Attr_value", "ℙ(Attr_value)", //
				"value", "Attrs ↔ Attr_value", //
				"db0", "Attr_id ↔ Attrs",//
				"contents", "Page ↔ Page_contents",//
				"private_pages", "Page_number ↔ Page",//
				"previous_pages", "Page_number ↔ Page",//
				"last_update", "Attrs ↔ Date_time",//
				"creation_date", "Page ↔ Date_time",//
				"release_date", "Page ↔ Date_time",//
				"leq", "Date_time ↔ Date_time",//
				"dp_time", "Disp_params ↔ Date_time",//
				"conform", "Attr_id ↔ Attr_value");

		assertWDLemma(env,//
				"∀ai·ai∈Attr_id ⇒ ai ↦ value(db0(ai)) ∈ conform", //
				"∀ ai·ai∈Attr_id ⇒"//
						+ "ai∈dom(db0) ∧"
						+ "db0∈Attr_id ⇸ Attrs ∧"
						+ "db0(ai)∈dom(value) ∧" + "value∈Attrs ⇸ Attr_value");
		assertWDLemma(env,//
				"value(a)=av", //
				"a∈dom(value) ∧ value∈Attrs ⇸ Attr_value");
		assertWDLemma(env,//
				"contents(p) = pc", //
				"p∈dom(contents) ∧ contents∈Page ⇸ Page_contents");

		assertWDLemma(env,//
				"last_update(a) = time_now", //
				"a∈dom(last_update) ∧ last_update∈Attrs ⇸ Date_time");

		assertWDLemma(env,//
				"creation_date(p) = time_now", //
				"p∈dom(creation_date) ∧ creation_date∈Page ⇸ Date_time");
		assertWDLemma(
				env,//
				"pp = {rp ∣ rp∈Rel_Page ∧ (release_date(rp) ↦ time_now) ∈ leq}",
				"∀rp·rp∈Rel_Page ⇒"//
						+ "rp∈dom(release_date) ∧"
						+ "release_date∈Page ⇸ Date_time");
		assertWDLemma(env,//
				"(time_now ↦ release_date(p)) ∈ leq", //
				"p∈dom(release_date) ∧ release_date∈Page ⇸ Date_time");
		assertWDLemma(env,//
				"dp_time(dp) = time_now", //
				"dp∈dom(dp_time) ∧ dp_time∈Disp_params ⇸ Date_time");
	}

	/**
	 * Ensures that the predicates which are not part of an implication are not
	 * simplified by predicates which belong to an implication. Also checks,
	 * that all the isolated predicates are used to simplify implications.
	 */
	public void testTraversal() {
		final ITypeEnvironment env = mTypeEnvironment(//
				"f", REL(S, POW(S)),//
				"g", REL(S, POW(S)));
		assertWDLemma(
				env,//
				"g(x)∪{y ∣ (b<a⇒a=0) ∧ f(x)=∅ ∧ g(x)=∅}=f(x)",
				"x∈dom(g) ∧ g∈S ⇸ ℙ(S) ∧"
						+ "((b<a ⇒ a=0) ⇒ x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"//
						+ "  (f(x)=∅ ⇒ x∈dom(g) ∧ g∈S ⇸ ℙ(S))) ∧"
						+ "x∈dom(f) ∧ f∈S ⇸ ℙ(S)",
				"x∈dom(g) ∧ g∈S ⇸ ℙ(S) ∧ x∈dom(f) ∧ f∈S ⇸ ℙ(S)");
		assertWDLemma(
				env,//
				"f(x)∪{y ∣ (b<a⇒a=0) ∧ f(x)=∅ ∧ g(x)=∅}=g(x)",
				"x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"
						+ "((b<a ⇒ a=0) ⇒ x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"
						+ "  (f(x)=∅ ⇒ x∈dom(g) ∧ g∈S ⇸ ℙ(S))) ∧"
						+ "x∈dom(g) ∧ g∈S ⇸ ℙ(S)",
				"x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧ x∈dom(g) ∧ g∈S ⇸ ℙ(S)");
		assertWDLemma(
				env,//
				"(b<c⇒c=0) ⇒ (f(x)∪{y ∣ (b<a⇒a=0) ∧ f(x)=∅ ∧ g(x)=∅}=g(x))",
				"(b<c⇒c=0) ⇒"//
						+ "x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"
						+ "((b<a⇒a=0) ⇒ x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"
						+ "  (f(x)=∅ ⇒ x∈dom(g) ∧ g∈S ⇸ ℙ(S))) ∧"
						+ "x∈dom(g) ∧ g∈S ⇸ ℙ(S)",
				"(b<c⇒c=0) ⇒ x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧ x∈dom(g) ∧ g∈S ⇸ ℙ(S)");
		assertWDLemma(
				env,//
				"(b<c⇒c=0) ⇒ (g(x)∪{y ∣ (b<a⇒a=0) ∧ f(x)=∅ ∧ g(x)=∅}=f(x))",
				"(b<c⇒c=0) ⇒"//
						+ "x∈dom(g) ∧ g∈S ⇸ ℙ(S) ∧"
						+ "((b<a⇒a=0) ⇒ x∈dom(f) ∧ f∈S ⇸ ℙ(S) ∧"
						+ "  (f(x)=∅ ⇒ x∈dom(g) ∧ g∈S ⇸ ℙ(S))) ∧"
						+ "x∈dom(f) ∧ f∈S ⇸ ℙ(S)",
				"(b<c⇒c=0) ⇒ x∈dom(g) ∧ g∈S ⇸ ℙ(S) ∧ x∈dom(f) ∧ f∈S ⇸ ℙ(S)");
	}

	// public void testPerformance() throws Exception {
	// final int LOOP = 10000;
	// for (int i = 0; i < LOOP; ++ i) {
	// for (TestFormula<?> t: tests) {
	// t.test();
	// }
	// }
	// }

	private static final Set<Predicate> NO_PREDICATE = Collections.emptySet();
	private static final LiteralPredicate LIT_BFALSE = ff.makeLiteralPredicate(
			Formula.BFALSE, null);
	private static final PowerSetType POW_S_TYPE = ff.makePowerSetType(S);
	private static final FreeIdentifier FRID_S = ff.makeFreeIdentifier("S", null, POW_S_TYPE);
	private static final UnaryExpression CARD_S = ff.makeUnaryExpression(Formula.KCARD, FRID_S, null);
	private static final SimplePredicate FINITE_S = ff.makeSimplePredicate(Formula.KFINITE, FRID_S, null);
	protected static final IntegerLiteral ZERO = ff.makeIntegerLiteral(BigInteger.ZERO, null);
	private static final FreeIdentifier FRID_B = ff.makeFreeIdentifier("B", null, INTEGER);
	private static final FreeIdentifier FRID_A = ff.makeFreeIdentifier("A", null, INTEGER);
	
	private static class Emax implements IExpressionExtension {
		private static final String SYNTAX_SYMBOL = "emax";
		private static final String OPERATOR_ID = "Extension Maximum";
		private final boolean conjoinChildrenWD;

		public Emax(boolean conjoinChildrenWD) {
			this.conjoinChildrenWD = conjoinChildrenWD;
		}

		@Override
		public Type synthesizeType(Expression[] childExprs,
				Predicate[] childPreds, ITypeMediator mediator) {
			return childExprs[0].getType();
		}

		@Override
		public boolean verifyType(Type proposedType,
				Expression[] childExprs, Predicate[] childPreds) {
			for (Expression child : childExprs) {
				final Type childType = child.getType();
				if (!(childType instanceof IntegerType)) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public Type typeCheck(ExtendedExpression expression,
				ITypeCheckMediator tcMediator) {
			final Expression[] children = expression.getChildExpressions();
			final Type resultType = tcMediator.makeIntegerType();
			for (int i = 0; i < children.length; i++) {
				tcMediator.sameType(children[i].getType(), resultType);
			}
			return resultType;
		}

		@Override
		public void addCompatibilities(ICompatibilityMediator mediator) {
			mediator.addCompatibility(getId(), getId());
		}

		@Override
		public void addPriorities(IPriorityMediator mediator) {
			// no priority to add
		}

		@Override
		public String getGroupId() {
			return "Arithmetic";
		}

		@Override
		public String getId() {
			return OPERATOR_ID;
		}

		@Override
		public IExtensionKind getKind() {
			return PARENTHESIZED_BINARY_EXPRESSION;
		}

		@Override
		public String getSyntaxSymbol() {
			return SYNTAX_SYMBOL;
		}

		// BTRUE if the first child is an integer literal
		// else BFALSE 
		@Override
		public Predicate getWDPredicate(IExtendedFormula formula,
				IWDMediator wdMediator) {
			final Expression firstChild = formula.getChildExpressions()[0];
			
			final FormulaFactory factory = wdMediator.getFormulaFactory();
			if (firstChild.getTag() == Formula.INTLIT) {
				return factory.makeLiteralPredicate(Formula.BTRUE, null);
			} else {
				return factory.makeLiteralPredicate(Formula.BFALSE, null);
			}
		}

		@Override
		public boolean conjoinChildrenWD() {
			return conjoinChildrenWD;
		}

		@Override
		public boolean isATypeConstructor() {
			return false;
		}

		@Override
		public Object getOrigin() {
			return null;
		}

	}

	private static final IExpressionExtension EMAX = new Emax(true);
	
	public void testSimpleWD() throws Exception {
		final Predicate expectedWD = LIT_BFALSE;

		final FormulaFactory extFac = FormulaFactory.getInstance(Collections
				.<IFormulaExtension> singleton(EMAX));
		final Expression emax = extFac.makeExtendedExpression(EMAX, Arrays
				.<Expression> asList(
						// first child is an identifier => WD = false
						FRID_A,
						FRID_B),
				NO_PREDICATE, null);

		final Predicate actualWD = emax.getWDPredicate(extFac);
		assertEquals("unexpected WD predicate", expectedWD, actualWD);
	}

	public void testWithChildWD() throws Exception {
		final Predicate expectedWD = ff.makeAssociativePredicate(Formula.LAND,
				Arrays.asList(LIT_BFALSE, FINITE_S), null);

		final FormulaFactory extFac = FormulaFactory.getInstance(Collections
				.<IFormulaExtension> singleton(EMAX));
		final Expression emax = extFac.makeExtendedExpression(EMAX, Arrays
				.<Expression> asList(
						// first child is an identifier => WD = false
						FRID_A,
						CARD_S),
				NO_PREDICATE, null);

		final Predicate actualWD = emax.getWDPredicate(extFac);
		assertEquals("unexpected WD predicate", expectedWD, actualWD);
	}
	
	public void testWithChildWDAndSimplification() throws Exception {
		final Predicate expectedWD = FINITE_S;

		final FormulaFactory extFac = FormulaFactory.getInstance(Collections
				.<IFormulaExtension> singleton(EMAX));
		final Expression emax = extFac.makeExtendedExpression(EMAX, Arrays
				.<Expression> asList(
						// first child is an integer literal => WD = true
						ZERO,
						CARD_S),
				NO_PREDICATE, null);

		final Predicate actualWD = emax.getWDPredicate(extFac);
		assertEquals("unexpected WD predicate", expectedWD, actualWD);
	}
	
	private static final IExpressionExtension EMAX_NO_CONJ = new Emax(false);

	public void testNoConjChildrenWD() throws Exception {
		final FormulaFactory extFac = FormulaFactory.getInstance(Collections
				.<IFormulaExtension> singleton(EMAX_NO_CONJ));
		final Expression emax = extFac.makeExtendedExpression(EMAX_NO_CONJ, Arrays
				.<Expression> asList(
						// first child is an identifier => WD = false
						FRID_A,
						CARD_S),
				NO_PREDICATE, null);

		final Predicate actualWD = emax.getWDPredicate(extFac);
		// conjoin children WD is disabled => no finite(S)
		assertEquals("unexpected WD predicate", LIT_BFALSE, actualWD);
	}

	private static final IDatatypeExtension LIST_TYPE = new IDatatypeExtension() {

		private static final String TYPE_NAME = "List";
		private static final String TYPE_IDENTIFIER = "List Id";
		
		
		@Override
		public String getTypeName() {
			return TYPE_NAME;
		}

		@Override
		public String getId() {
			return TYPE_IDENTIFIER;
		}
		
		@Override
		public void addTypeParameters(ITypeConstructorMediator mediator) {
			mediator.addTypeParam("S");			
		}

		@Override
		public void addConstructors(IConstructorMediator mediator) {
			mediator.addConstructor("nil", "NIL");
			final ITypeParameter typeS = mediator.getTypeParameter("S");
			
			final IArgumentType refS = mediator.newArgumentType(typeS);
			final IArgument head = mediator.newArgument("head", refS);
			final IArgumentType listS = mediator.newArgumentTypeConstr(asList(refS));
			final IArgument tail = mediator.newArgument("tail", listS);
			
			mediator.addConstructor("cons", "CONS", Arrays.asList(head, tail));
		}

	};

	private static final IDatatype LIST_DT = ff.makeDatatype(LIST_TYPE);
	private static final FormulaFactory LIST_FAC = FormulaFactory
			.getInstance(LIST_DT.getExtensions());
	private static final IExpressionExtension EXT_LIST = LIST_DT
			.getTypeConstructor();
	private static final ParametricType LIST_INT_TYPE = LIST_FAC
			.makeParametricType(Collections.<Type> singletonList(LIST_FAC.makeIntegerType()),
					EXT_LIST);
	
	public void testDestructorWD() throws Exception {
		final ITypeEnvironment listEnv = LIST_FAC.makeTypeEnvironment();
		listEnv.addName("x", INTEGER);
		listEnv.addName("l", LIST_INT_TYPE);
		
		assertWDLemma(listEnv, "x = head(l)",
				"∃ head0⦂ℤ, tail1⦂List(ℤ)· l = cons(head0, tail1)");

		assertWDLemma(listEnv, "∀l1⦂List(BOOL),l2⦂List(BOOL)· l1=l2 ⇒ tail(l1) = l2",
				"∀l1⦂List(BOOL),l2⦂List(BOOL)· l1=l2 ⇒ (∃head0⦂BOOL, tail1⦂List(BOOL)· l1 = cons(head0, tail1))");

	}
}
