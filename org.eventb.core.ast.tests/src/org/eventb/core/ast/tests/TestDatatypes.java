/*******************************************************************************
 * Copyright (c) 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.ast.tests;

import static java.util.Arrays.asList;
import static org.eventb.core.ast.Formula.FORALL;
import static org.eventb.core.ast.Formula.IN;
import static org.eventb.core.ast.Formula.LAND;
import static org.eventb.core.ast.Formula.LIMP;
import static org.eventb.core.ast.Formula.MAPSTO;
import static org.eventb.core.ast.Formula.TRUE;
import static org.eventb.core.ast.Formula.UPTO;
import static org.eventb.core.ast.ProblemKind.InvalidGenericType;
import static org.eventb.core.ast.ProblemKind.InvalidTypeExpression;
import static org.eventb.core.ast.ProblemSeverities.Error;
import static org.eventb.core.ast.tests.FastFactory.ff_extns;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eventb.core.ast.ASTProblem;
import org.eventb.core.ast.AtomicExpression;
import org.eventb.core.ast.BooleanType;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.ITypeCheckResult;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.ParametricType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.ProblemKind;
import org.eventb.core.ast.ProblemSeverities;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.IExpressionExtension;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.ast.extension.datatype2.IConstructorBuilder;
import org.eventb.core.ast.extension.datatype2.IDatatype2;
import org.eventb.core.ast.extension.datatype2.IDatatypeBuilder;
import org.eventb.core.ast.extension.datatype2.ITypeConstructorExtension;
import org.eventb.internal.core.ast.extension.datatype2.DatatypeBuilder;
import org.eventb.internal.core.ast.extension.datatype2.ExtensionHarvester;
import org.junit.Test;

public class TestDatatypes extends AbstractTests {

	private static final Predicate[] NO_PRED = new Predicate[0];
	private static final Expression[] NO_EXPR = new Expression[0];

	protected static final AtomicExpression INT_ffLIST = LIST_FAC
			.makeAtomicExpression(Formula.INTEGER, null);

	protected static final BooleanType BOOL_TYPE_ffLIST = LIST_FAC
			.makeBooleanType();

	protected static final IntegerLiteral ONE_ffLIST = LIST_FAC
			.makeIntegerLiteral(BigInteger.ONE, null);
	protected static final IntegerLiteral ZERO_ffLIST = LIST_FAC
			.makeIntegerLiteral(BigInteger.ZERO, null);
	protected static final FreeIdentifier FRID_x_ffLIST = LIST_FAC
			.makeFreeIdentifier("x", null);

	protected static final SourceLocationChecker slChecker = new SourceLocationChecker();

	private static void assertFailure(IParseResult result,
			ASTProblem... expected) {
		assertTrue("expected parsing to fail", result.hasProblem());
		final List<ASTProblem> problems = result.getProblems();
		assertEquals("wrong problem", asList(expected), problems);
	}

	private static IParseResult parseTypeRes(String image,
			FormulaFactory factory) {
		return factory.parseType(image);
	}

	private static Type doTypeTest(String formula, Type expected,
			FormulaFactory factory) {
		final IParseResult result = parseTypeRes(formula, factory);
		assertFalse("unexpected problems " + result.getProblems(),
				result.hasProblem());
		final Type actual = result.getParsedType();
		assertEquals(expected, actual);
		return actual;
	}

	private static void checkSourceLocation(Formula<?> formula, int length) {
		for (int i = 0; i < length; i++) {
			for (int j = i; j < length; j++) {
				final SourceLocation sloc = new SourceLocation(i, j);
				final IPosition pos = formula.getPosition(sloc);
				if (!formula.contains(sloc)) {
					assertNull(pos);
					break;
				}
				assertNotNull("null position for location " + sloc
						+ " in formula " + formula + " with location: "
						+ formula.getSourceLocation(), pos);
				final Formula<?> actual = formula.getSubFormula(pos);
				assertTrue(actual.getSourceLocation().contains(sloc));
			}
		}
	}

	private static <T extends Formula<T>> void checkParsedFormula(
			String formula, T expected, T actual) {
		assertEquals(expected, actual);

		actual.accept(slChecker);
		checkSourceLocation(actual, formula.length());
	}

	private static Expression parseExpr(String formula, FormulaFactory factory) {
		final IParseResult result = factory.parseExpression(formula, null);
		assertFalse("unexpected problem(s): " + result.getProblems(),
				result.hasProblem());
		final Expression actual = result.getParsedExpression();
		return actual;
	}

	private static Expression parseAndCheck(String formula,
			Expression expected, FormulaFactory factory) {
		final Expression actual = parseExpr(formula, factory);
		checkParsedFormula(formula, expected, actual);

		return actual;
	}

	private static Expression doExpressionTest(String formula,
			Expression expected, FormulaFactory factory) {
		return parseAndCheck(formula, expected, factory);
	}

	private static Expression doExpressionTest(String formula,
			Expression expected, Type expectedType, FormulaFactory factory,
			boolean typeCheck) {
		final Expression actual = doExpressionTest(formula, expected, factory);
		if (typeCheck) {
			final ITypeCheckResult result = actual.typeCheck(factory
					.makeTypeEnvironment());
			assertFalse(
					"unexpected type check problems " + result.getProblems(),
					result.hasProblem());
		}
		assertEquals(expectedType, actual.getType());
		return actual;
	}

	private static Predicate doPredicateTest(String formula,
			Predicate expected, FormulaFactory factory) {
		final IParseResult result = factory.parsePredicate(formula, null);
		assertFalse("unexpected problem(s): " + result.getProblems(),
				result.hasProblem());
		final Predicate actual = result.getParsedPredicate();
		checkParsedFormula(formula, expected, actual);
		return actual;
	}

	@Test
	public void testDatatypeType() throws Exception {

		final ExtendedExpression list = LIST_FAC.makeExtendedExpression(
				EXT_LIST, Collections.<Expression> singleton(INT_ffLIST),
				Collections.<Predicate> emptyList(), null);

		final Expression expr = doExpressionTest("List(ℤ)", list,
				POW_LIST_INT_TYPE, LIST_FAC, false);

		assertTrue("expected a type expression", expr.isATypeExpression());
		assertEquals("unexpected toType", LIST_INT_TYPE, expr.toType());

		doTypeTest("List(ℤ)", LIST_INT_TYPE, LIST_FAC);

		final ParametricType listBoolType = LIST_FAC.makeParametricType(
				Collections.<Type> singletonList(BOOL_TYPE_ffLIST), EXT_LIST);
		assertFalse(listBoolType.equals(LIST_INT_TYPE));
	}

	@Test
	public void testDatatypeExpr() throws Exception {
		final Expression upTo = LIST_FAC.makeBinaryExpression(UPTO,
				LIST_FAC.makeIntegerLiteral(BigInteger.ZERO, null),
				LIST_FAC.makeIntegerLiteral(BigInteger.ONE, null), null);

		final ExtendedExpression list0upTo1 = LIST_FAC.makeExtendedExpression(
				EXT_LIST, Collections.<Expression> singleton(upTo),
				Collections.<Predicate> emptyList(), null);

		final Expression expr = doExpressionTest("List(0‥1)", list0upTo1,
				POW_LIST_INT_TYPE, LIST_FAC, false);
		assertFalse("unexpected type expression", expr.isATypeExpression());
		final IParseResult result = parseTypeRes("List(0‥1)", LIST_FAC);
		assertFailure(result, new ASTProblem(new SourceLocation(0, 8),
				InvalidTypeExpression, ProblemSeverities.Error));
	}

	@Test
	public void testDatatypeNil() throws Exception {

		final ExtendedExpression nil = LIST_FAC.makeExtendedExpression(EXT_NIL,
				Collections.<Expression> emptyList(),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("nil", nil, LIST_FAC);

		final ExtendedExpression nilInt = LIST_FAC.makeExtendedExpression(
				EXT_NIL, NO_EXPR, NO_PRED, null, LIST_INT_TYPE);

		doExpressionTest("(nil ⦂ List(ℤ))", nilInt, LIST_INT_TYPE, LIST_FAC,
				false);

		final ParametricType listBoolBoolType = LIST_FAC.makeParametricType(
				Collections.<Type> singletonList(LIST_FAC.makeProductType(
						BOOL_TYPE_ffLIST, BOOL_TYPE_ffLIST)), EXT_LIST);
		final ExtendedExpression nilBoolBool = LIST_FAC.makeExtendedExpression(
				EXT_NIL, NO_EXPR, NO_PRED, null, listBoolBoolType);

		doExpressionTest("(nil ⦂ List(BOOL×BOOL))", nilBoolBool,
				listBoolBoolType, LIST_FAC, false);

		assertFalse(nil.equals(nilInt));
		assertFalse(nil.equals(nilBoolBool));
		assertFalse(nilBoolBool.equals(nilInt));
	}

	@Test
	public void testDatatypeNilInvalidType() throws Exception {
		final IParseResult result = LIST_FAC.parseExpression("(nil ⦂ ℤ)", null);
		assertFailure(result, new ASTProblem(new SourceLocation(1, 7),
				InvalidGenericType, Error, "[see operator definition]"));
	}

	@Test
	public void testDatatypeConstructor() throws Exception {

		final ExtendedExpression nil = LIST_FAC.makeExtendedExpression(EXT_NIL,
				NO_EXPR, NO_PRED, null);

		final ExtendedExpression list1 = LIST_FAC.makeExtendedExpression(
				EXT_CONS, Arrays.asList(ONE_ffLIST, nil),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("cons(1, nil)", list1, LIST_INT_TYPE, LIST_FAC, true);

		final ExtendedExpression list01 = LIST_FAC.makeExtendedExpression(
				EXT_CONS,
				Arrays.asList(
						ZERO_ffLIST,
						LIST_FAC.makeExtendedExpression(EXT_CONS,
								Arrays.asList(ONE_ffLIST, nil),
								Collections.<Predicate> emptyList(), null)),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("cons(0, cons(1, nil))", list01, LIST_INT_TYPE,
				LIST_FAC, true);

		assertFalse(list1.equals(list01));
	}

	@Test
	public void testDatatypeDestructors() throws Exception {
		assertNotNull("head destructor not found", EXT_HEAD);

		assertNotNull("tail destructor not found", EXT_TAIL);

		final ExtendedExpression head = LIST_FAC.makeExtendedExpression(
				EXT_HEAD, Arrays.<Expression> asList(FRID_x_ffLIST),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("head(x)", head, LIST_FAC);

		final ExtendedExpression tail = LIST_FAC.makeExtendedExpression(
				EXT_TAIL, Arrays.<Expression> asList(FRID_x_ffLIST),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("tail(x)", tail, LIST_FAC);
	}

	@Test
	public void testTypeConstrTypeCheck() throws Exception {
		final Expression listIntExpr = LIST_FAC.makeExtendedExpression(
				EXT_LIST, Collections.<Expression> singleton(INT_ffLIST),
				Collections.<Predicate> emptySet(), null);
		final Predicate expected = LIST_FAC.makeRelationalPredicate(IN,
				LIST_FAC.makeFreeIdentifier("x", null), listIntExpr, null);

		final Predicate pred = doPredicateTest("x ∈ List(ℤ)", expected,
				LIST_FAC);
		final ITypeCheckResult tcResult = pred.typeCheck(LIST_FAC
				.makeTypeEnvironment());
		assertFalse(tcResult.hasProblem());
		assertTrue(pred.isTypeChecked());
		final FreeIdentifier[] freeIdentifiers = pred.getFreeIdentifiers();
		assertEquals(1, freeIdentifiers.length);
		final FreeIdentifier x = freeIdentifiers[0];
		assertEquals(LIST_INT_TYPE, x.getType());
	}

	@Test
	public void testTypeCheckError() throws Exception {
		// problem raised by Issam, produced a StackOverflowError
		final Expression A_Id = LIST_FAC.makeFreeIdentifier("A", null);

		final Expression List_A = LIST_FAC.makeExtendedExpression(EXT_LIST,
				asList(A_Id), Collections.<Predicate> emptySet(), null);
		final Expression List_List_A = LIST_FAC.makeExtendedExpression(
				EXT_LIST, asList(List_A), Collections.<Predicate> emptySet(),
				null);

		final BoundIdentDecl bid_x = LIST_FAC.makeBoundIdentDecl("x", null);
		final BoundIdentDecl bid_y = LIST_FAC.makeBoundIdentDecl("y", null);
		final BoundIdentifier bi_x = LIST_FAC.makeBoundIdentifier(1, null);
		final BoundIdentifier bi_y = LIST_FAC.makeBoundIdentifier(0, null);

		final Predicate x_In_A = LIST_FAC.makeRelationalPredicate(IN, bi_x,
				A_Id, null);

		final Predicate y_In_ListListA = LIST_FAC.makeRelationalPredicate(IN,
				bi_y, List_List_A, null);

		final ExtendedExpression cons_x_y = LIST_FAC.makeExtendedExpression(
				EXT_CONS, new Expression[] { bi_x, bi_y }, NO_PRED, null);
		final Predicate cons_In_ListA = LIST_FAC.makeRelationalPredicate(IN,
				cons_x_y, List_A, null);

		final Predicate expected = LIST_FAC.makeQuantifiedPredicate(FORALL,
				asList(bid_x, bid_y), LIST_FAC.makeBinaryPredicate(
						LIMP,
						LIST_FAC.makeAssociativePredicate(LAND,
								asList(x_In_A, y_In_ListListA), null),
						cons_In_ListA, null), null);
		final Predicate pred = doPredicateTest(
				"∀ x,y· (x ∈A ∧ y ∈List(List(A))) ⇒ cons(x,y)∈ List(A)",
				expected, LIST_FAC);
		final ITypeCheckResult tcRes = pred.typeCheck(LIST_FAC
				.makeTypeEnvironment());
		assertTrue(tcRes.hasProblem());

		final List<ASTProblem> problems = tcRes.getProblems();
		for (ASTProblem problem : problems) {
			assertEquals(ProblemKind.Circularity, problem.getMessage());
		}
	}

	@Test
	public void testDatatypeDestructorsTyping() throws Exception {

		final ExtendedExpression nil = LIST_FAC.makeExtendedExpression(EXT_NIL,
				Collections.<Expression> emptyList(),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression list1 = LIST_FAC.makeExtendedExpression(
				EXT_CONS, Arrays.asList(ONE_ffLIST, nil),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression headList1 = LIST_FAC.makeExtendedExpression(
				EXT_HEAD, Arrays.<Expression> asList(list1),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("head(cons(1, nil))", headList1, INT_TYPE, LIST_FAC,
				true);

		final ExtendedExpression tail = LIST_FAC.makeExtendedExpression(
				EXT_TAIL, Arrays.<Expression> asList(list1),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("tail(cons(1, nil))", tail, LIST_INT_TYPE, LIST_FAC,
				true);
	}

	@Test
	public void testListOfLists() throws Exception {
		final ExtendedExpression nil = LIST_FAC.makeExtendedExpression(EXT_NIL,
				Collections.<Expression> emptyList(),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression nilInt = LIST_FAC.makeExtendedExpression(
				EXT_NIL, NO_EXPR, NO_PRED, null, LIST_INT_TYPE);

		final ExtendedExpression listNilNil = LIST_FAC.makeExtendedExpression(
				EXT_CONS, Arrays.<Expression> asList(nilInt, nil),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression headListNil = LIST_FAC.makeExtendedExpression(
				EXT_HEAD, Arrays.<Expression> asList(listNilNil),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("head(cons((nil ⦂ List(ℤ)), nil))", headListNil,
				LIST_INT_TYPE, LIST_FAC, true);

		final ExtendedExpression cons1 = LIST_FAC.makeExtendedExpression(
				EXT_CONS, Arrays.asList(ONE_ffLIST, nil),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression consCons1 = LIST_FAC.makeExtendedExpression(
				EXT_CONS, Arrays.<Expression> asList(cons1, nil),
				Collections.<Predicate> emptyList(), null);

		final ExtendedExpression tailConsCons1 = LIST_FAC
				.makeExtendedExpression(EXT_TAIL,
						Arrays.<Expression> asList(consCons1),
						Collections.<Predicate> emptyList(), null);

		final ParametricType NLIST_LIST_INT_TYPE = LIST_FAC.makeParametricType(
				Arrays.<Type> asList(LIST_INT_TYPE), EXT_LIST);

		doExpressionTest("tail(cons(cons(1, nil), nil))", tailConsCons1,
				NLIST_LIST_INT_TYPE, LIST_FAC, true);
	}

	@Test
	public void testDatatypeOrigins() throws Exception {
		for (IFormulaExtension extension : LIST_DT.getExtensions()) {
			final Object origin = extension.getOrigin();
			assertSame("wrong origin for " + extension.getId(), LIST_DT, origin);
		}
	}


	public static final IDatatype2 MOULT_DT;
	public static final FormulaFactory MOULT_FAC;

	static {
		final GivenType tyS = ff.makeGivenType("S");
		final GivenType tyT = ff.makeGivenType("T");
		final IDatatypeBuilder bldr = ff.makeDatatypeBuilder("Moult", tyS, tyT);
		final IConstructorBuilder cons = bldr.addConstructor("makeMoult");
		cons.addArgument(tyS);
		cons.addArgument(tyT);
		MOULT_DT = bldr.finalizeDatatype();
		MOULT_FAC = FormulaFactory.getInstance(MOULT_DT.getExtensions());
	}

	public static final IExpressionExtension EXT_MOULT = MOULT_DT
			.getTypeConstructor();
	private static final ParametricType MOULT_INT_BOOL_TYPE = MOULT_FAC
			.makeParametricType(
					Arrays.<Type> asList(MOULT_FAC.makeIntegerType(),
							MOULT_FAC.makeBooleanType()), EXT_MOULT);
	private static final IExpressionExtension EXT_MAKE_MOULT = MOULT_DT
			.getConstructor("makeMoult");

	private static final IntegerLiteral ONE_MOULT = MOULT_FAC
			.makeIntegerLiteral(BigInteger.ONE, null);
	private static final AtomicExpression ATOM_TRUE_MOULT = MOULT_FAC
			.makeAtomicExpression(TRUE, null);

	@Test
	public void testMoult() throws Exception {

		doTypeTest("Moult(ℤ, BOOL)", MOULT_INT_BOOL_TYPE, MOULT_FAC);

		final ExtendedExpression moult1True = MOULT_FAC.makeExtendedExpression(
				EXT_MAKE_MOULT, Arrays.asList(ONE_MOULT, ATOM_TRUE_MOULT),
				Collections.<Predicate> emptyList(), null);

		doExpressionTest("makeMoult(1, TRUE)", moult1True, MOULT_INT_BOOL_TYPE,
				MOULT_FAC, true);
	}

	public static final GivenType[] noInducTypeParams = {
			ff.makeGivenType("S"), ff.makeGivenType("T") };

	private static final IDatatypeBuilder NO_INDUC_BUILDER = ff
			.makeDatatypeBuilder("NoInduc", noInducTypeParams);

	static {
		IConstructorBuilder cons1 = NO_INDUC_BUILDER.addConstructor("cons1");
		cons1.addArgument(NO_INDUC_BUILDER.parseType("S").getParsedType());
		cons1.addArgument(NO_INDUC_BUILDER.parseType("ℙ(ℤ)").getParsedType());
		cons1.addArgument(NO_INDUC_BUILDER.parseType("T").getParsedType());
		IConstructorBuilder cons2 = NO_INDUC_BUILDER.addConstructor("cons2");
		cons2.addArgument(NO_INDUC_BUILDER.parseType("ℙ(S)").getParsedType());
		cons2.addArgument(NO_INDUC_BUILDER.parseType("ℙ(ℤ)×T").getParsedType());
		IConstructorBuilder cons3 = NO_INDUC_BUILDER.addConstructor("cons3");
		cons3.addArgument(NO_INDUC_BUILDER.parseType("S↔T").getParsedType());
	}

	private static final IDatatype2 NO_INDUC_EXTNS = NO_INDUC_BUILDER
			.finalizeDatatype();

	private static final FormulaFactory NO_INDUC_FAC = FormulaFactory
			.getInstance(NO_INDUC_EXTNS.getExtensions());
	private static final IExpressionExtension EXT_NO_INDUC = NO_INDUC_EXTNS
			.getTypeConstructor();
	private static final ParametricType NO_INDUC_INT_BOOL_TYPE = NO_INDUC_FAC
			.makeParametricType(Arrays.<Type> asList(
					NO_INDUC_FAC.makeIntegerType(),
					NO_INDUC_FAC.makeBooleanType()), EXT_NO_INDUC);
	private static final IntegerLiteral ONE_ffNO_INDUC = NO_INDUC_FAC
			.makeIntegerLiteral(BigInteger.ONE, null);
	private static final IntegerLiteral ZERO_ffNO_INDUC = NO_INDUC_FAC
			.makeIntegerLiteral(BigInteger.ZERO, null);
	private static final AtomicExpression ATOM_TRUE_ffNO_INDUC = NO_INDUC_FAC
			.makeAtomicExpression(TRUE, null);
	private static final IntegerLiteral ONE_NO_INDUC = NO_INDUC_FAC
			.makeIntegerLiteral(BigInteger.ONE, null);
	private static final IntegerLiteral ZERO_NO_INDUC = NO_INDUC_FAC
			.makeIntegerLiteral(BigInteger.ZERO, null);
	private static final AtomicExpression ATOM_TRUE_NO_INDUC = NO_INDUC_FAC
			.makeAtomicExpression(TRUE, null);

	@Test
	public void testNoInducType() throws Exception {
		doTypeTest("NoInduc(ℤ, BOOL)", NO_INDUC_INT_BOOL_TYPE, NO_INDUC_FAC);
	}

	@Test
	public void testArgSimpleType() throws Exception {
		final IExpressionExtension extCons1 = NO_INDUC_EXTNS
				.getConstructor("cons1");

		final ExtendedExpression c1Sing0True = NO_INDUC_FAC
				.makeExtendedExpression(extCons1, Arrays.asList(ONE_NO_INDUC,
						NO_INDUC_FAC.makeSetExtension(ZERO_NO_INDUC, null),
						ATOM_TRUE_NO_INDUC), Collections
						.<Predicate> emptyList(), null);

		doExpressionTest("cons1(1, {0}, TRUE)", c1Sing0True,
				NO_INDUC_INT_BOOL_TYPE, NO_INDUC_FAC, true);
	}

	@Test
	public void testArgPowerSetType() throws Exception {
		final IExpressionExtension extCons2 = NO_INDUC_EXTNS
				.getConstructor("cons2");

		final ExtendedExpression c2Sing2MapSing0True = NO_INDUC_FAC
				.makeExtendedExpression(extCons2, Arrays.asList(NO_INDUC_FAC
						.makeSetExtension(ONE_ffNO_INDUC, null), NO_INDUC_FAC
						.makeBinaryExpression(MAPSTO, NO_INDUC_FAC
								.makeSetExtension(ZERO_ffNO_INDUC, null),
								ATOM_TRUE_ffNO_INDUC, null)), Collections
						.<Predicate> emptyList(), null);

		doExpressionTest("cons2({1}, {0} ↦ TRUE)", c2Sing2MapSing0True,
				NO_INDUC_INT_BOOL_TYPE, NO_INDUC_FAC, true);
	}

	@Test
	public void testArgRelationalType() throws Exception {
		final IExpressionExtension extCons3 = NO_INDUC_EXTNS
				.getConstructor("cons3");

		final ExtendedExpression c3SingMaps0True = NO_INDUC_FAC
				.makeExtendedExpression(extCons3, Arrays
						.<Expression> asList(NO_INDUC_FAC.makeSetExtension(
								Arrays.<Expression> asList(NO_INDUC_FAC
										.makeBinaryExpression(MAPSTO,
												ZERO_ffNO_INDUC,
												ATOM_TRUE_ffNO_INDUC, null)),
								null)), Collections.<Predicate> emptyList(),
						null);

		doExpressionTest("cons3({0 ↦ TRUE})", c3SingMaps0True,
				NO_INDUC_INT_BOOL_TYPE, NO_INDUC_FAC, true);
	}

	@Test
	public void testDatatypeSameExtensions() throws Exception {
		final IDatatype2 extns1 = NO_INDUC_BUILDER.finalizeDatatype();
		final IDatatype2 extns2 = NO_INDUC_BUILDER.finalizeDatatype();
		final IExpressionExtension typeExt1 = extns1.getTypeConstructor();
		final IExpressionExtension typeExt2 = extns2.getTypeConstructor();
		assertSame("expected same extensions", typeExt1, typeExt2);

		final IExpressionExtension cons1Ext1 = extns1.getConstructor("cons1");
		final IExpressionExtension cons1Ext2 = extns2.getConstructor("cons1");
		assertSame("expected same extensions", cons1Ext1, cons1Ext2);
	}

	// Specific tests for new datatypes:

	// Test FormulaFactory builder error cases

	@Test(expected = NullPointerException.class)
	public void testNullDatatypeName() {
		ff.makeDatatypeBuilder(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDatatypeName() {
		ff.makeDatatypeBuilder("partition");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncompatibleDatatypeAndTypeName() {
		ff.makeDatatypeBuilder("List", ff.makeGivenType("List"));
	}

	@Test(expected = NullPointerException.class)
	public void testNullTypeParameterArray() {
		ff.makeDatatypeBuilder("List", (GivenType[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullTypeaParameterList() {
		ff.makeDatatypeBuilder("List", (List<GivenType>) null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullType() {
		ff.makeDatatypeBuilder("List", ff.makeGivenType("S"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncompatibleTypeParametersNames() {
		ff.makeDatatypeBuilder("List", ff.makeGivenType("S"),
				ff.makeGivenType("S"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncompatibleTypeParametersFactory() {
		ff.makeDatatypeBuilder("List", ff.makeGivenType("S"),
				ff_extns.makeGivenType("T"));
	}

	// Tests on datatype builder addConstructor()

	@Test(expected = NullPointerException.class)
	public void testAddNullConstructor() {
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT");
		builder.addConstructor(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConstructorNotIdentifierName() {
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT");
		builder.addConstructor("123");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddConstructorSameNameAsDatatype() {
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT");
		builder.addConstructor("DT");
	}
	
	public void testAddConstructorSameNameAsTypeParameter() {
		final GivenType tyS = ff.makeGivenType("S");
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT", tyS);
		builder.addConstructor("S");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddConstructorSameNameAsOtherConstructor() {
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT");
		builder.addConstructor("cons");
		builder.addConstructor("cons");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testAddConstructorSameNameAsDestructor() {
		final IDatatypeBuilder builder = ff.makeDatatypeBuilder("DT");
		final IConstructorBuilder cons = builder.addConstructor("cons");
		cons.addArgument(INT_TYPE, "dest");
		builder.addConstructor("dest");
	}

	@Test
	public void testDatatypeBuilder() {
		assertNotNull(makeList3(ff));
	}

	private IDatatype2 makeList3(final FormulaFactory fac) {
		final GivenType tyList3 = fac.makeGivenType("List3");
		final GivenType tyS = fac.makeGivenType("S");
		final GivenType tyT = fac.makeGivenType("T");
		final GivenType tyU = fac.makeGivenType("U");
		final IDatatypeBuilder dtBuilder = fac.makeDatatypeBuilder("List3", tyS,
				tyT, tyU);
		dtBuilder.addConstructor("nil3");
		final IConstructorBuilder cons = dtBuilder.addConstructor("cons3");
		cons.addArgument(tyS, "head1");
		cons.addArgument(tyT, "head2");
		cons.addArgument(tyU, "head3");
		cons.addArgument(tyList3, "tail3");
		final IDatatype2 datatype = dtBuilder.finalizeDatatype();
		return datatype;
	}

	@Test
	public void testHasBasicConstructor() {
		final GivenType tyDT = ff.makeGivenType("DT");
		final GivenType tyT = ff.makeGivenType("T");
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		assertFalse("A datatype without constructor has not basic constructor",
				dtBuilder.hasBasicConstructor());
		final IConstructorBuilder cons = dtBuilder.addConstructor("dt");
		cons.addArgument(tyDT);
		cons.addArgument(tyT);
		assertFalse(
				"A datatype with a constructor using the datatype type is not a basic constructor",
				dtBuilder.hasBasicConstructor());
		final IConstructorBuilder cons2 = dtBuilder.addConstructor("dt2");
		cons2.addArgument(tyT);
		assertTrue(
				"A datatype with a constructor which do not use the datatype type has a basic constructor",
				dtBuilder.hasBasicConstructor());
	}

	@Test(expected = IllegalStateException.class)
	public void testFinalizeWithoutConstructor() {
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		dtBuilder.finalizeDatatype();
	}

	@Test(expected = IllegalStateException.class)
	public void testFinalizeWithoutBasicConstructor() {
		final GivenType tyDT = ff.makeGivenType("DT");
		final GivenType tyT = ff.makeGivenType("T");
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		final IConstructorBuilder cons = dtBuilder.addConstructor("dt");
		cons.addArgument(tyDT);
		cons.addArgument(tyT);
		dtBuilder.finalizeDatatype();
	}

	@Test
	public void testFinalize() {
		final GivenType tyDT = ff.makeGivenType("DT");
		final GivenType tyT = ff.makeGivenType("T");
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		final IConstructorBuilder cons = dtBuilder.addConstructor("dt");
		cons.addArgument(tyDT);
		cons.addArgument(tyT);
		final IConstructorBuilder cons2 = dtBuilder.addConstructor("dt2");
		cons2.addArgument(tyT);
		dtBuilder.finalizeDatatype();
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalAddConstructor() {
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		dtBuilder.addConstructor("dt");
		dtBuilder.finalizeDatatype();
		dtBuilder.addConstructor("void");
	}

	// Tests on datatype constructor builder

	@Test
	public void testIsBasicConstructor() {
		final GivenType tyDT = ff.makeGivenType("DT");
		final GivenType tyT = ff.makeGivenType("T");
		final IDatatypeBuilder dtBuilder = ff.makeDatatypeBuilder("DT");
		final IConstructorBuilder cons = dtBuilder.addConstructor("dt");
		assertTrue("A constructor without argument is a basic constructor",
				cons.isBasic());
		cons.addArgument(tyT);
		assertTrue(
				"A constructor which do not use the datatype is a basic constructor",
				cons.isBasic());
		cons.addArgument(tyDT);
		assertFalse(
				"A datatype which use the datatype is not a basic constructor",
				dtBuilder.hasBasicConstructor());
	}

	/**
	 * Ensures that the base factory of a datatype is indeed minimal.
	 */
	@Test
	public void testBaseFactoryMinimal() {
		final DatatypeBuilder builder = (DatatypeBuilder) LIST_FAC
				.makeDatatypeBuilder("foo");
		assertSame(ff, builder.getBaseFactory());
		final IConstructorBuilder cons = builder.addConstructor("cons2");
		cons.addArgument(LIST_INT_TYPE);
		assertSame(LIST_FAC, builder.getBaseFactory());
	}

	/**
	 * Ensure uniqueness of datatypes.
	 */
	@Test
	public void testDatatypeUniqueness() {
		final IDatatype2 dt1 = makeList3(ff);
		final IDatatype2 dt2 = makeList3(LIST_FAC);
		final IDatatype2 dt3 = makeList3(MOULT_FAC);
		assertSame(dt1, dt2);
		assertSame(dt2, dt3);
	}
	
	@Test
	public void testDatatypeDifferentTypeConstructors() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("E");
		b2.addConstructor("f");
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeMissingConstructor() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f");
		b1.addConstructor("g");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("f");
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeDifferentConstructors() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("g");
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeMissingDestructor() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f").addArgument(INT_TYPE);
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("f");
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeDifferentDestructorTypes() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f").addArgument(INT_TYPE, "d");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("f").addArgument(BOOL_TYPE);
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeUnnamedDestructor() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f").addArgument(INT_TYPE, "d");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("f").addArgument(INT_TYPE);
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	@Test
	public void testDatatypeDifferentDestructorNames() {
		final IDatatypeBuilder b1 = ff.makeDatatypeBuilder("D");
		b1.addConstructor("f").addArgument(INT_TYPE, "d");
		final IDatatypeBuilder b2 = ff.makeDatatypeBuilder("D");
		b2.addConstructor("f").addArgument(INT_TYPE, "e");
		assertNotSame(b1.finalizeDatatype(), b2.finalizeDatatype());
	}
	
	// Partial tests on extensions since these are already tested with old
	// datatypes tests

	@Test
	public void testTypeConstructorExtension() {
		final IDatatype2 dt = makeList3(ff);
		final ITypeConstructorExtension tconsExt = dt.getTypeConstructor();
		final String[] actuals = tconsExt.getFormalNames();
		final String[] expecteds = { "S", "T", "U" };
		assertArrayEquals("Type parameters names: " + expecteds
				+ " were expected instead of " + actuals, expecteds, actuals);
		assertSame(
				"ITypeConstructorExtension origin must be the datatype object instead of: "
						+ tconsExt.getOrigin(), dt, tconsExt.getOrigin());
	}

	/*
	 * Unit tests for extension harvester
	 */
	private static final FormulaFactory LIST_MOULT_FAC = LIST_FAC
			.withExtensions(MOULT_DT.getExtensions());

	@Test
	public void testExtensionHarvester() {
		assertExtensions("BOOL");
		assertExtensions("ℤ");
		assertExtensions("S");
		assertExtensions("List(S)", EXT_LIST);
		assertExtensions("Moult(List(S), T)", EXT_LIST, EXT_MOULT);
		assertExtensions("Moult(S, List(T))", EXT_LIST, EXT_MOULT);
		assertExtensions("ℙ(List(S))", EXT_LIST);
		assertExtensions("List(S) × T", EXT_LIST);
		assertExtensions("S × List(T)", EXT_LIST);
	}

	private void assertExtensions(String typeImage,
			IFormulaExtension... expecteds) {
		final Type type = parseType(typeImage, LIST_MOULT_FAC);
		final Set<IFormulaExtension> expectedSet = new HashSet<IFormulaExtension>(
				asList(expecteds));
		final ExtensionHarvester harvester = new ExtensionHarvester();
		harvester.harvest(type);
		assertEquals(expectedSet, harvester.getResult());
	}

}