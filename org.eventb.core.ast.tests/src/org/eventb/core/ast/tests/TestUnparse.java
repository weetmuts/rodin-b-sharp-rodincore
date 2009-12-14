/*******************************************************************************
 * Copyright (c) 2005, 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added abstract test class
 *     Systerel - mathematical language v2
 *     Systerel - added support for predicate variables
 *******************************************************************************/
package org.eventb.core.ast.tests;

import static org.eventb.core.ast.Formula.BCOMP;
import static org.eventb.core.ast.Formula.BINTER;
import static org.eventb.core.ast.Formula.BTRUE;
import static org.eventb.core.ast.Formula.BUNION;
import static org.eventb.core.ast.Formula.CPROD;
import static org.eventb.core.ast.Formula.CSET;
import static org.eventb.core.ast.Formula.DIV;
import static org.eventb.core.ast.Formula.DOMRES;
import static org.eventb.core.ast.Formula.EXPN;
import static org.eventb.core.ast.Formula.FCOMP;
import static org.eventb.core.ast.Formula.FORALL;
import static org.eventb.core.ast.Formula.LAND;
import static org.eventb.core.ast.Formula.LEQV;
import static org.eventb.core.ast.Formula.LOR;
import static org.eventb.core.ast.Formula.MAPSTO;
import static org.eventb.core.ast.Formula.MINUS;
import static org.eventb.core.ast.Formula.MOD;
import static org.eventb.core.ast.Formula.MUL;
import static org.eventb.core.ast.Formula.OVR;
import static org.eventb.core.ast.Formula.PLUS;
import static org.eventb.core.ast.Formula.QINTER;
import static org.eventb.core.ast.Formula.QUNION;
import static org.eventb.core.ast.Formula.RANRES;
import static org.eventb.core.ast.Formula.UNMINUS;
import static org.eventb.core.ast.QuantifiedExpression.Form.Explicit;
import static org.eventb.core.ast.QuantifiedExpression.Form.Implicit;
import static org.eventb.core.ast.QuantifiedExpression.Form.Lambda;
import static org.eventb.core.ast.tests.FastFactory.mAssociativeExpression;
import static org.eventb.core.ast.tests.FastFactory.mAssociativePredicate;
import static org.eventb.core.ast.tests.FastFactory.mBecomesEqualTo;
import static org.eventb.core.ast.tests.FastFactory.mBecomesMemberOf;
import static org.eventb.core.ast.tests.FastFactory.mBecomesSuchThat;
import static org.eventb.core.ast.tests.FastFactory.mBinaryExpression;
import static org.eventb.core.ast.tests.FastFactory.mBinaryPredicate;
import static org.eventb.core.ast.tests.FastFactory.mBoundIdentDecl;
import static org.eventb.core.ast.tests.FastFactory.mBoundIdentifier;
import static org.eventb.core.ast.tests.FastFactory.mFreeIdentifier;
import static org.eventb.core.ast.tests.FastFactory.mIntegerLiteral;
import static org.eventb.core.ast.tests.FastFactory.mList;
import static org.eventb.core.ast.tests.FastFactory.mLiteralPredicate;
import static org.eventb.core.ast.tests.FastFactory.mMaplet;
import static org.eventb.core.ast.tests.FastFactory.mMultiplePredicate;
import static org.eventb.core.ast.tests.FastFactory.mQuantifiedExpression;
import static org.eventb.core.ast.tests.FastFactory.mQuantifiedPredicate;
import static org.eventb.core.ast.tests.FastFactory.mRelationalPredicate;
import static org.eventb.core.ast.tests.FastFactory.mSetExtension;
import static org.eventb.core.ast.tests.FastFactory.mSimplePredicate;
import static org.eventb.core.ast.tests.FastFactory.mUnaryExpression;
import static org.eventb.core.ast.tests.FastFactory.mUnaryPredicate;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IFormulaRewriter;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.LanguageVersion;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.PredicateVariable;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.tests.Common.TagSupply;



/**
 * This class tests the unparser, which is the toString method. It is supposed
 * to return the formula with as less parentheses as possible. However, it is
 * not possible to do it without writing by hand all possible combinations of
 * formulae. This test takes an abstract syntax tree, unparses it, parses it
 * again and verifies that the tree obtained is the same as the one given. This
 * test tests all possible combinations (with regards to the operators) of
 * Expressions, and all combinations of Predicates.
 * 
 * @author fterrier
 */
public class TestUnparse extends AbstractTests {

	static FreeIdentifier id_x = ff.makeFreeIdentifier("x", null);
	private static FreeIdentifier id_y = ff.makeFreeIdentifier("y", null);
	private static FreeIdentifier id_z = ff.makeFreeIdentifier("z", null);
	private static FreeIdentifier id_t = ff.makeFreeIdentifier("t", null);
	private static FreeIdentifier id_u = ff.makeFreeIdentifier("u", null);
	private static FreeIdentifier id_v = ff.makeFreeIdentifier("v", null);
	private static FreeIdentifier id_S = ff.makeFreeIdentifier("S", null);
	private static FreeIdentifier id_f = ff.makeFreeIdentifier("f", null);
	private static FreeIdentifier id_A = ff.makeFreeIdentifier("A", null);
	private static FreeIdentifier id_g = ff.makeFreeIdentifier("g", null);
	
	private static BoundIdentDecl bd_x = ff.makeBoundIdentDecl("x", null);
	private static BoundIdentDecl bd_y = ff.makeBoundIdentDecl("y", null);
	private static BoundIdentDecl bd_z = ff.makeBoundIdentDecl("z", null);

	private static BoundIdentDecl bd_xp = ff.makeBoundIdentDecl("x'", null);
	private static BoundIdentDecl bd_yp = ff.makeBoundIdentDecl("y'", null);
	private static BoundIdentDecl bd_zp = ff.makeBoundIdentDecl("z'", null);

	private static BoundIdentifier b0 = ff.makeBoundIdentifier(0, null);
	private static BoundIdentifier b1 = ff.makeBoundIdentifier(1, null);
	private static BoundIdentifier b2 = ff.makeBoundIdentifier(2, null);

	private static PredicateVariable pv_P = ff.makePredicateVariable("$P", null);

	private static LiteralPredicate btrue = ff.makeLiteralPredicate(BTRUE, null);

	private static IntegerLiteral two = ff.makeIntegerLiteral(Common.TWO, null);
	
	static SourceLocationChecker slChecker = new SourceLocationChecker();

	private static abstract class TestPair {
		final String image;
		final LanguageVersion version;
		TestPair(String image) {
			this(image, LanguageVersion.V1);
		}
		TestPair(String image, LanguageVersion version) {
			this.image = image;
			this.version = version;
		}
		abstract Formula<?> getFormula();
		abstract Formula<?> parseAndCheck(String input);
		void verify(String input) {
			assertTrue(input, ParenChecker.check(input));
			Formula<?> actual = parseAndCheck(input);
			checkSourceLocations(input, actual);
		}
		void checkSourceLocations(String input, Formula<?> parsedFormula) {
			// Verify that source locations are properly nested
			parsedFormula.accept(slChecker);

			// also check that the source location reported corresponds to the
			// whole substring.
			final SourceLocation loc = parsedFormula.getSourceLocation();
			parseAndCheck(input.substring(loc.getStart(), loc.getEnd() + 1));
		}
	}
	
	private static class ExprTestPair extends TestPair {
		Expression formula;
		ExprTestPair(String image, Expression formula) {
			super(image);
			this.formula = formula;
		}
		ExprTestPair(String image, LanguageVersion version, Expression formula) {
			super(image, version);
			this.formula = formula;
		}
		@Override 
		Expression getFormula() {
			return formula;
		}
		@Override 
		Expression parseAndCheck(String input) {
			final Expression actual = parseExpression(input, version);
			assertEquals("Unexpected parser result", formula, actual);
			return actual;
		}
	}
	
	private static class PredTestPair extends TestPair {
		Predicate formula;
		PredTestPair(String image, Predicate formula) {
			super(image);
			this.formula = formula;
		}
		PredTestPair(String image, LanguageVersion version, Predicate formula) {
			super(image, version);
			this.formula = formula;
		}
		@Override 
		Predicate getFormula() {
			return formula;
		}
		@Override 
		Predicate parseAndCheck(String input) {
			final Predicate actual = parsePredicate(input, version);
			assertEquals("Unexpected parser result", formula, actual);
			return actual;
		}
	}

	private static class AssignTestPair extends TestPair {
		Assignment formula;
		AssignTestPair(String image, Assignment formula) {
			super(image);
			this.formula = formula;
		}
		@Override 
		Assignment getFormula() {
			return formula;
		}
		@Override 
		Assignment parseAndCheck(String input) {
			final Assignment actual = parseAssignment(input, version);
			assertEquals("Unexpected parser result", formula, actual);
			return actual;
		}
	}

	/*
	 * this verifies that each child of an associative expression is treated the
	 * same way by the parser/unparser
	 */
	private ExprTestPair[] associativeExpressionTestPairs = new ExprTestPair[] {
			// {BUNION, BINTER, BCOMP, FCOMP, OVR, PLUS, MUL}
			new ExprTestPair(
					"x\u2217y\u222ax\u2217y\u222ax\u2217y",
					buildExpression(MUL, BUNION)
			), new ExprTestPair(
					"(x\u222ay)\u2217(x\u222ay)\u2217(x\u222ay)",
					buildExpression(BUNION, MUL)
			), new ExprTestPair(
					"x+y\u222ax+y\u222ax+y",
					buildExpression(PLUS, BUNION)
			), new ExprTestPair(
					"(x\u222ay)+(x\u222ay)+(x\u222ay)",
					buildExpression(BUNION, PLUS)
			), new ExprTestPair(
					"(x\ue103y)\u222a(x\ue103y)\u222a(x\ue103y)",
					buildExpression(OVR, BUNION)
			), new ExprTestPair(
					"(x\u222ay)\ue103(x\u222ay)\ue103(x\u222ay)",
					buildExpression(BUNION, OVR)
			), new ExprTestPair(
					"(x;y)\u222a(x;y)\u222a(x;y)",
					buildExpression(FCOMP, BUNION)
			), new ExprTestPair(
					"(x\u222ay);(x\u222ay);(x\u222ay)",
					buildExpression(BUNION, FCOMP)
			), new ExprTestPair(
					"(x\u2218y)\u222a(x\u2218y)\u222a(x\u2218y)",
					buildExpression(BCOMP, BUNION)
			), new ExprTestPair(
					"(x\u222ay)\u2218(x\u222ay)\u2218(x\u222ay)",
					buildExpression(BUNION, BCOMP)
			), new ExprTestPair(
					"(x\u2229y)\u222a(x\u2229y)\u222a(x\u2229y)",
					buildExpression(BINTER, BUNION)
			), new ExprTestPair(
					"(x\u222ay)\u2229(x\u222ay)\u2229(x\u222ay)",
					buildExpression(BUNION, BINTER)
			), new ExprTestPair(
					"(x\u2218y)\u2229(x\u2218y)\u2229(x\u2218y)",
					buildExpression(BCOMP, BINTER)
			), new ExprTestPair(
					"(x\u2229y)\u2218(x\u2229y)\u2218(x\u2229y)",
					buildExpression(BINTER, BCOMP)
			), new ExprTestPair(
					"(x;y)\u2229(x;y)\u2229(x;y)",
					buildExpression(FCOMP, BINTER)
			), new ExprTestPair(
					"(x\u2229y);(x\u2229y);(x\u2229y)",
					buildExpression(BINTER, FCOMP)
			), new ExprTestPair(
					"(x\ue103y)\u2229(x\ue103y)\u2229(x\ue103y)",
					buildExpression(OVR, BINTER)
			), new ExprTestPair(
					"(x\u2229y)\ue103(x\u2229y)\ue103(x\u2229y)",
					buildExpression(BINTER, OVR)
			), new ExprTestPair(
					"x+y\u2229x+y\u2229x+y",
					buildExpression(PLUS, BINTER)
			), new ExprTestPair(
					"(x\u2229y)+(x\u2229y)+(x\u2229y)",
					buildExpression(BINTER, PLUS)
			), new ExprTestPair(
					"x\u2217y\u2229x\u2217y\u2229x\u2217y",
					buildExpression(MUL, BINTER)
			), new ExprTestPair(
					"(x\u2229y)\u2217(x\u2229y)\u2217(x\u2229y)",
					buildExpression(BINTER, MUL)
			), new ExprTestPair(
					"(x;y)\u2218(x;y)\u2218(x;y)",
					buildExpression(FCOMP, BCOMP)
			), new ExprTestPair(
					"(x\u2218y);(x\u2218y);(x\u2218y)",
					buildExpression(BCOMP, FCOMP)
			), new ExprTestPair(
					"(x\ue103y)\u2218(x\ue103y)\u2218(x\ue103y)",
					buildExpression(OVR, BCOMP)
			), new ExprTestPair(
					"(x\u2218y)\ue103(x\u2218y)\ue103(x\u2218y)",
					buildExpression(BCOMP, OVR)
			), new ExprTestPair(
					"x+y\u2218x+y\u2218x+y",
					buildExpression(PLUS, BCOMP)
			), new ExprTestPair(
					"(x\u2218y)+(x\u2218y)+(x\u2218y)",
					buildExpression(BCOMP, PLUS)
			), new ExprTestPair(
					"x\u2217y\u2218x\u2217y\u2218x\u2217y",
					buildExpression(MUL, BCOMP)
			), new ExprTestPair(
					"(x\u2218y)\u2217(x\u2218y)\u2217(x\u2218y)",
					buildExpression(BCOMP, MUL)
			), new ExprTestPair(
					"(x\ue103y);(x\ue103y);(x\ue103y)",
					buildExpression(OVR, FCOMP)
			), new ExprTestPair(
					"(x;y)\ue103(x;y)\ue103(x;y)",
					buildExpression(FCOMP, OVR)
			), new ExprTestPair(
					"x+y;x+y;x+y",
					buildExpression(PLUS, FCOMP)
			), new ExprTestPair(
					"(x;y)+(x;y)+(x;y)",
					buildExpression(FCOMP, PLUS)
			), new ExprTestPair(
					"x\u2217y;x\u2217y;x\u2217y",
					buildExpression(MUL, FCOMP)
			), new ExprTestPair(
					"(x;y)\u2217(x;y)\u2217(x;y)",
					buildExpression(FCOMP, MUL)
			), new ExprTestPair(
					"x+y\ue103x+y\ue103x+y",
					buildExpression(PLUS, OVR)
			), new ExprTestPair(
					"(x\ue103y)+(x\ue103y)+(x\ue103y)",
					buildExpression(OVR, PLUS)
			), new ExprTestPair(
					"x\u2217y\ue103x\u2217y\ue103x\u2217y",
					buildExpression(MUL, OVR)
			), new ExprTestPair(
					"(x\ue103y)\u2217(x\ue103y)\u2217(x\ue103y)",
					buildExpression(OVR, MUL)
			), new ExprTestPair(
					"x\u2217y+x\u2217y+x\u2217y",
					buildExpression(MUL, PLUS)
			), new ExprTestPair(
					"(x+y)\u2217(x+y)\u2217(x+y)",
					buildExpression(PLUS, MUL)
			),
	};

	private Predicate btrueEquivBtrue = mBinaryPredicate(LEQV, btrue, btrue);
	private Predicate btrueAndBtrue = mAssociativePredicate(LAND, btrue, btrue);
	private Predicate btrueOrBtrue = mAssociativePredicate(LOR, btrue, btrue);
	
	private PredTestPair[] associativePredicateTestPairs = new PredTestPair[] {
			new PredTestPair(
					"((\u22a4\u21d4\u22a4)\u21d4(\u22a4\u21d4\u22a4))\u21d4(\u22a4\u21d4\u22a4)",
					mBinaryPredicate(LEQV,
							mBinaryPredicate(LEQV, btrueEquivBtrue, btrueEquivBtrue),
							btrueEquivBtrue)
			), new PredTestPair(
					"(\u22a4\u21d4\u22a4)\u21d4((\u22a4\u21d4\u22a4)\u21d4(\u22a4\u21d4\u22a4))",
					mBinaryPredicate(LEQV,
							btrueEquivBtrue,
							mBinaryPredicate(LEQV, btrueEquivBtrue, btrueEquivBtrue))
			), new PredTestPair(
					"(\u22a4\u21d4\u22a4)\u2227(\u22a4\u21d4\u22a4)\u2227(\u22a4\u21d4\u22a4)",
					mAssociativePredicate(LAND,
							btrueEquivBtrue,
							btrueEquivBtrue, 
							btrueEquivBtrue)
			), new PredTestPair(
					"(\u22a4\u2227\u22a4\u21d4\u22a4\u2227\u22a4)\u21d4\u22a4\u2227\u22a4",
					mBinaryPredicate(LEQV,
							mBinaryPredicate(LEQV, btrueAndBtrue, btrueAndBtrue),
							btrueAndBtrue)
			), new PredTestPair(
					"\u22a4\u2227\u22a4\u21d4(\u22a4\u2227\u22a4\u21d4\u22a4\u2227\u22a4)",
					mBinaryPredicate(LEQV,
							btrueAndBtrue,
							mBinaryPredicate(LEQV, btrueAndBtrue, btrueAndBtrue))

			), new PredTestPair(
					"(\u22a4\u21d4\u22a4)\u2228(\u22a4\u21d4\u22a4)\u2228(\u22a4\u21d4\u22a4)",
					mAssociativePredicate(LOR,
							btrueEquivBtrue,
							btrueEquivBtrue, 
							btrueEquivBtrue)
			), new PredTestPair(
					"(\u22a4\u2228\u22a4\u21d4\u22a4\u2228\u22a4)\u21d4\u22a4\u2228\u22a4",
					mBinaryPredicate(LEQV,
							mBinaryPredicate(LEQV, btrueOrBtrue, btrueOrBtrue),
							btrueOrBtrue)
			), new PredTestPair(
					"\u22a4\u2228\u22a4\u21d4(\u22a4\u2228\u22a4\u21d4\u22a4\u2228\u22a4)",
					mBinaryPredicate(LEQV,
							btrueOrBtrue,
							mBinaryPredicate(LEQV, btrueOrBtrue, btrueOrBtrue))
			), new PredTestPair(
					"(\u22a4\u2227\u22a4)\u2227(\u22a4\u2227\u22a4)\u2227(\u22a4\u2227\u22a4)",
					buildPredicate(LAND, LAND)
			), new PredTestPair(
					"(\u22a4\u2227\u22a4)\u2228(\u22a4\u2227\u22a4)\u2228(\u22a4\u2227\u22a4)",
					buildPredicate(LAND, LOR)
			), new PredTestPair(
					"(\u22a4\u2228\u22a4)\u2227(\u22a4\u2228\u22a4)\u2227(\u22a4\u2228\u22a4)",
					buildPredicate(LOR, LAND)
			), new PredTestPair(
					"(\u22a4\u2228\u22a4)\u2228(\u22a4\u2228\u22a4)\u2228(\u22a4\u2228\u22a4)",
					buildPredicate(LOR, LOR)
			),
	};
			
	private PredTestPair[] predPatternTestPairs = new PredTestPair[] {
			new PredTestPair("$P", pv_P),
	};

	// Various special cases for expressions
	private ExprTestPair[] specialExprTestPairs = new ExprTestPair[] {
			new ExprTestPair(
					"{}",
					mSetExtension()
			), new ExprTestPair(
					"A ◁ f;g",
					mAssociativeExpression(FCOMP,
							mBinaryExpression(DOMRES, id_A, id_f),
							id_g 
					)
			), new ExprTestPair(
					"x × y",
					mBinaryExpression(CPROD, id_x, id_y)
			), new ExprTestPair(
					"f;g ▷ A",
					mBinaryExpression(RANRES,
							mAssociativeExpression(FCOMP, id_f, id_g),
							id_A 
					)
			), new ExprTestPair(
					"λx0·⊤ ∣ x+x0",
					mQuantifiedExpression(CSET, Lambda,
							mList(mBoundIdentDecl("x")),
							mLiteralPredicate(BTRUE),
							mMaplet(mBoundIdentifier(0),
									mAssociativeExpression(PLUS,
											id_x,
											mBoundIdentifier(0)
									)
							)
					)
			), new ExprTestPair(
					"\u2212(1)",
					mUnaryExpression(UNMINUS,
							mIntegerLiteral(1)
					)
			), new ExprTestPair(
					"\u22121",
					mIntegerLiteral(-1)
			), new ExprTestPair(
					"1 \u2212 (\u22121)",
					mBinaryExpression(MINUS,
							mIntegerLiteral(1),
							mIntegerLiteral(-1))
			), new ExprTestPair(
					"\u22121 \u2212 1",
					mBinaryExpression(MINUS,
							mIntegerLiteral(-1),
							mIntegerLiteral(1))
			), new ExprTestPair(
					"(\u22121)\u22171",
					mAssociativeExpression(MUL,
							mIntegerLiteral(-1),
							mIntegerLiteral(1))
			), new ExprTestPair(
					"\u22121\u22171",
					mUnaryExpression(UNMINUS,
							mAssociativeExpression(MUL,
									mIntegerLiteral(1),
									mIntegerLiteral(1)))
			), new ExprTestPair(
					"bool($P)",
					FastFactory.mBoolExpression(pv_P)
			),
	};
	
	private PredTestPair[] specialPredTestPairs = new PredTestPair[] {
			new PredTestPair(
					"∀x\u00b7∀y\u00b7∀z\u00b7finite(x∪y∪z∪t)",
					mQuantifiedPredicate(FORALL, mList(bd_x),
					mQuantifiedPredicate(FORALL, mList(bd_y),
					mQuantifiedPredicate(FORALL, mList(bd_z),
							mSimplePredicate(
									mAssociativeExpression(BUNION, b2, b1, b0, id_t)
							)
					)))
			), new PredTestPair(
					"∀x\u00b7∀x0\u00b7finite(x∪x0)",
					mQuantifiedPredicate(FORALL, mList(bd_x),
					mQuantifiedPredicate(FORALL, mList(bd_x),
							mSimplePredicate(
									mAssociativeExpression(BUNION, b1, b0)
							)
					))
			), new PredTestPair(
					"∀x\u00b7∀x0\u00b7∀x1\u00b7finite(x∪x0∪x1)",
					mQuantifiedPredicate(FORALL, mList(bd_x),
					mQuantifiedPredicate(FORALL, mList(bd_x),
					mQuantifiedPredicate(FORALL, mList(bd_x),
							mSimplePredicate(
									mAssociativeExpression(BUNION, b2, b1, b0)
							)
					)))
			), new PredTestPair(
					"∀x\u00b7∀y\u00b7∀y0\u00b7finite(x∪y∪y0)",
					mQuantifiedPredicate(FORALL, mList(bd_x),
					mQuantifiedPredicate(FORALL, mList(bd_y),
					mQuantifiedPredicate(FORALL, mList(bd_y),
							mSimplePredicate(
									mAssociativeExpression(BUNION, b2, b1, b0)
							)
					)))
			), new PredTestPair(
					"∀prj0\u00b7∀prj3\u00b7finite(prj∪prj0∪prj3)",
					mQuantifiedPredicate(FORALL, mList(mBoundIdentDecl("prj")),
					mQuantifiedPredicate(FORALL, mList(mBoundIdentDecl("prj")),
							mSimplePredicate(
									mAssociativeExpression(BUNION,
											mFreeIdentifier("prj"),
											b1, b0
									)
							)
					))
			), new PredTestPair(
					"$P",
					pv_P
			),
	};

	AssignTestPair[] assignmentTestPairs = new AssignTestPair[] {
			new AssignTestPair(
					"x ≔ y",
					mBecomesEqualTo(mList(id_x), mList(id_y))
			), new AssignTestPair(
					"x,y ≔ z, t",
					mBecomesEqualTo(mList(id_x, id_y), mList(id_z, id_t))
			), new AssignTestPair(
					"x,y,z ≔ t, u, v",
					mBecomesEqualTo(mList(id_x, id_y, id_z), mList(id_t, id_u, id_v))
			), new AssignTestPair(
					"x :∈ S",
					mBecomesMemberOf(id_x, id_S)
			), new AssignTestPair(
					"x :\u2223 x'=x",
					mBecomesSuchThat(mList(id_x), mList(bd_xp),
							mRelationalPredicate(Formula.EQUAL, b0, id_x)
					)
			), new AssignTestPair(
					"x,y :\u2223 x'=y∧y'=x",
					mBecomesSuchThat(mList(id_x, id_y), mList(bd_xp, bd_yp),
							mAssociativePredicate(Formula.LAND,
									mRelationalPredicate(Formula.EQUAL, b1, id_y),
									mRelationalPredicate(Formula.EQUAL, b0, id_x)
							))
			), new AssignTestPair(
					"x,y,z :\u2223 x'=y∧y'=z∧z'=x",
					mBecomesSuchThat(mList(id_x, id_y, id_z), mList(bd_xp, bd_yp, bd_zp),
							mAssociativePredicate(Formula.LAND,
									mRelationalPredicate(Formula.EQUAL, b2, id_y),
									mRelationalPredicate(Formula.EQUAL, b1, id_z),
									mRelationalPredicate(Formula.EQUAL, b0, id_x)
							))
			),
	};

	private Predicate buildPredicate(int firstTag, int secondTag) {
		return mAssociativePredicate(secondTag,
				mAssociativePredicate(firstTag, btrue, btrue),
				mAssociativePredicate(firstTag, btrue, btrue),
				mAssociativePredicate(firstTag, btrue, btrue)
		);
	}
	
	private Expression buildExpression(int firstTag, int secondTag) {
		return mAssociativeExpression(secondTag,
				mAssociativeExpression(firstTag, id_x, id_y),
				mAssociativeExpression(firstTag, id_x, id_y),
				mAssociativeExpression(firstTag, id_x, id_y)
		);
	}
	
	//--------------------------------------------------------------
	//
	//  SYSTEMATIC TESTS
	//
	//--------------------------------------------------------------

	private Expression[] constructBinaryBinaryTrees (TagSupply tagSupply) {
		// {MAPSTO,REL,TREL,SREL,STREL,PFUN,TFUN,PINJ,TINJ,PSUR,TSUR,TBIJ,SETMINUS,CPROD,DPROD,PPROD,DOMRES,DOMSUB,RANRES,RANSUB,UPTO,MINUS,DIV,MOD,EXPN}
		final Set<Integer> binaryExpressions = tagSupply.binaryExpressionTags;
		int length = binaryExpressions.size();
		Expression[]  formulae = new Expression[(length)*(length)*2];
		int idx = 0;
		for (int binTag1 : binaryExpressions) {
			for (int binTag2 : binaryExpressions) {
				formulae[idx ++] = mBinaryExpression(
						binTag2,
						mBinaryExpression(binTag1, id_x, id_y),
						id_z);
				formulae[idx ++] = mBinaryExpression(
						binTag2, 
						id_x, 
						mBinaryExpression(binTag1, id_y, id_z));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Formula<?>[] constructAssociativeAssociativeTrees(TagSupply tagSupply) {
		// {BUNION, BINTER, BCOMP, FCOMP, OVR, PLUS, MUL}
		final Set<Integer> associativeExpressions = tagSupply.associativeExpressionTags;
		int length = associativeExpressions.size();
		Expression[]  formulae = new Expression[3 * length * length];
		int idx = 0;
		for (int assocTag1 : associativeExpressions) {
			for (int assocTag2 : associativeExpressions) {
				formulae[idx ++] = mAssociativeExpression(
						assocTag2,
						mAssociativeExpression(assocTag1, id_y, id_z),
						id_x);
				formulae[idx ++] = mAssociativeExpression(
						assocTag2,
						id_x,
						mAssociativeExpression(assocTag1, id_y, id_z),
						id_t);
				formulae[idx ++] = mAssociativeExpression(
						assocTag2,
						id_x,
						mAssociativeExpression(assocTag1, id_y, id_z));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Expression[] constructAssociativeBinaryTrees(TagSupply tagSupply) {
		final Set<Integer> associativeExpressions = tagSupply.associativeExpressionTags;
		final int assocSize = associativeExpressions.size();
		final Set<Integer> binaryExpressions = tagSupply.binaryExpressionTags;
		final int binarySize = binaryExpressions.size();
		Expression[]  formulae = new Expression[5 * assocSize * binarySize];
		int idx = 0;
		for (int assocTag : associativeExpressions) {
			for (int binaryTag : binaryExpressions) {
				formulae[idx ++] = mAssociativeExpression(
						assocTag,
						id_x,
						mBinaryExpression(binaryTag, id_y, id_z));
				formulae[idx ++] = mAssociativeExpression(
						assocTag,
						id_x,
						mBinaryExpression(binaryTag, id_y, id_z),
						id_t);
				formulae[idx ++] = mAssociativeExpression(
						assocTag,
						mBinaryExpression(binaryTag, id_x, id_y),
						id_z);
				formulae[idx ++] = mBinaryExpression(
						binaryTag,
						mAssociativeExpression(assocTag, id_x, id_y),
						id_z);
				formulae[idx ++] = mBinaryExpression(
						binaryTag,
						id_x,
						mAssociativeExpression(assocTag, id_y, id_z));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Expression[] constructBinaryUnaryTrees(TagSupply tagSupply) {
		// {KCARD, POW, POW1, KUNION, KINTER, KDOM, KRAN, KPRJ1, KPRJ2, KID, KMIN, KMAX, CONVERSE}
		final Set<Integer> unaryExpressions = tagSupply.unaryExpressionTags;
		final int unarySize = unaryExpressions.size();
		final Set<Integer> binaryExpressions = tagSupply.binaryExpressionTags;
		final int binarySize = binaryExpressions.size();
		final int length = (3 * unarySize * binarySize)
				+ (2 * binarySize);
		final Expression[] formulae = new Expression[length];
		int idx = 0;
		for (int unaryTag:unaryExpressions) {
			for (int binaryTag: binaryExpressions) {
				formulae[idx ++] = mUnaryExpression(
						unaryTag, 
						mBinaryExpression(binaryTag, id_x, id_y));
				formulae[idx ++] = mBinaryExpression(
						binaryTag,
						mUnaryExpression(unaryTag, id_x),
						id_y);
				formulae[idx ++] = mBinaryExpression(
						binaryTag,
						id_x,
						mUnaryExpression(unaryTag, id_y));
			}
		}
		// Special case of negative integer literal
		for (int binaryTag: binaryExpressions) {
			formulae[idx ++] = mBinaryExpression(
					binaryTag,
					mIntegerLiteral(-1),
					id_y);
			formulae[idx ++] = mBinaryExpression(
					binaryTag,
					id_x,
					mIntegerLiteral(-1));
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Expression[] constructAssociativeUnaryTrees(TagSupply tagSupply) {
		// {KCARD, POW, POW1, KUNION, KINTER, KDOM, KRAN, KPRJ1, KPRJ2, KID, KMIN, KMAX, CONVERSE}
		final Set<Integer> unaryExpressions = tagSupply.unaryExpressionTags;
		final int unarySize = unaryExpressions.size();
		final Set<Integer> associativeExpressions = tagSupply.associativeExpressionTags;
		final int assocSize = associativeExpressions.size();
		final int length = (4 * unarySize * assocSize)
				+ (3 * assocSize);
		final Expression[] formulae = new Expression[length];
		int idx = 0;
		for (int unaryTag: unaryExpressions) {
			for (int assocTag: associativeExpressions) {
				formulae[idx ++] = mUnaryExpression(
						unaryTag,
						mAssociativeExpression(assocTag, id_x, id_y));
				formulae[idx ++] = mAssociativeExpression(
						assocTag,
						id_x,
						mUnaryExpression(unaryTag, id_y));
				formulae[idx ++] = mAssociativeExpression(
						assocTag,
						id_x,
						mUnaryExpression(unaryTag, id_y), 
						id_z);
				formulae[idx ++] = mAssociativeExpression(
						assocTag, 
						mUnaryExpression(unaryTag, id_x), 
						id_y);
			}
		}
		// Special case of negative integer literal
		for (int assocTag: associativeExpressions) {
			formulae[idx ++] = mAssociativeExpression(
					assocTag,
					id_x,
					mIntegerLiteral(-1));
			formulae[idx ++] = mAssociativeExpression(
					assocTag,
					id_x,
					mIntegerLiteral(-1), 
					id_z);
			formulae[idx ++] = mAssociativeExpression(
					assocTag, 
					mIntegerLiteral(-1), 
					id_y);
		}
		assert idx == length;
		return formulae;
	}
	
	private Expression[] constructUnaryUnaryTrees(TagSupply tagSupply) {
		final Set<Integer> unaryExpressions = tagSupply.unaryExpressionTags;
		final int unaryExpSize = unaryExpressions.size();
		
		final int length = (2 * unaryExpSize * unaryExpSize) + unaryExpSize;
		final Expression[] formulae = new Expression[length];
		int idx = 0;
		for (int tag1 : unaryExpressions) {
			for (int tag2 : unaryExpressions) {
				formulae[idx++] = mUnaryExpression(tag1, mUnaryExpression(tag2, id_x));
				formulae[idx++] = mUnaryExpression(tag2, mUnaryExpression(tag1, id_x));
			}
		}
		// Special case of negative integer literal
		for (int tag : unaryExpressions) {
			formulae[idx++] = mUnaryExpression(tag, mIntegerLiteral(-1));
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	// test that they are all handled the same way
	private Expression[] constructQuantifiedQuantifiedTrees() {
		return new Expression[] {
			makeQuantifiedExpression(CSET, CSET, Implicit, Lambda),
			makeQuantifiedExpression(CSET, CSET, Explicit, Explicit),
			makeQuantifiedExpression(CSET, CSET, Explicit, Lambda),
			makeQuantifiedExpression(CSET, CSET, Explicit, Implicit),
			makeQuantifiedExpression(CSET, CSET, Implicit, Explicit),

			makeQuantifiedExpression(CSET, QUNION, Explicit, Explicit),
			makeQuantifiedExpression(CSET, QUNION, Implicit, Explicit),
			makeQuantifiedExpression(CSET, QUNION, Explicit, Implicit),
			
			makeQuantifiedExpression(QUNION, CSET, Explicit, Explicit),
			makeQuantifiedExpression(QUNION, CSET, Explicit, Implicit),
			makeQuantifiedExpression(QUNION, CSET, Explicit, Lambda),
			makeQuantifiedExpression(QUNION, CSET, Implicit, Explicit),
			makeQuantifiedExpression(QUNION, CSET, Implicit, Lambda),
			
			makeQuantifiedExpression(CSET, QINTER, Explicit, Explicit),
			makeQuantifiedExpression(CSET, QINTER, Implicit, Explicit),
			makeQuantifiedExpression(CSET, QINTER, Explicit, Implicit),
			
			makeQuantifiedExpression(QINTER, CSET, Explicit, Explicit),
			makeQuantifiedExpression(QINTER, CSET, Explicit, Implicit),
			makeQuantifiedExpression(QINTER, CSET, Explicit, Lambda),
			makeQuantifiedExpression(QINTER, CSET, Implicit, Explicit),
			makeQuantifiedExpression(QINTER, CSET, Implicit, Lambda),
			
			makeQuantifiedExpression(QINTER, QUNION, Explicit, Explicit),
			makeQuantifiedExpression(QUNION, QINTER, Explicit, Explicit),
			makeQuantifiedExpression(QINTER, QINTER, Explicit, Explicit),
			makeQuantifiedExpression(QUNION, QUNION, Explicit, Explicit),
			makeQuantifiedExpression(QINTER, QUNION, Implicit, Explicit),
			makeQuantifiedExpression(QUNION, QINTER, Implicit, Explicit),
			makeQuantifiedExpression(QINTER, QINTER, Explicit, Implicit),
			makeQuantifiedExpression(QUNION, QUNION, Explicit, Implicit),
		};
	}
	
	private Expression makeQuantifiedExpression(int firstTag, int secondTag, QuantifiedExpression.Form firstForm, QuantifiedExpression.Form secondForm) {
		return mQuantifiedExpression(
				firstTag,
				firstForm,
				mList(bd_z),
				btrue,
				mQuantifiedExpression(
						secondTag,
						secondForm,
						(secondForm == Implicit ? mList(bd_y,bd_z) : mList(bd_y)),
						btrue,
						mBinaryExpression(MAPSTO,
								secondForm == Lambda ? b0 : b1,
								secondForm == Implicit || firstForm == Implicit
								? (secondForm == Lambda ? b1 : b0)
								: id_x)
				));
	}
	
	private Expression mQExpr(int qtag, QuantifiedExpression.Form form, BoundIdentDecl bid,
			Expression expr) {
		if (form == Lambda) {
			return mQuantifiedExpression(qtag, form, mList(bid), btrue, mMaplet(b0, expr));
		}
		return mQuantifiedExpression(qtag, form, mList(bid), btrue, mMaplet(b0, expr));
	}
	
	private Expression[] constructQuantifiedBinaryTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedExpressions = tagSupply.quantifiedExpressionTags;
		final int quantSize = quantifiedExpressions.size();
		final Set<Integer> binaryExpressions = tagSupply.binaryExpressionTags;
		final int binarySize = binaryExpressions.size();
		final int length = 3 * binarySize * (2 * quantSize + 1);
		Expression[]  formulae = new Expression[length];
		int idx = 0;
		for (int quantTag : quantifiedExpressions) {
			for (int binaryTag : binaryExpressions) {
				for (QuantifiedExpression.Form form: QuantifiedExpression.Form.values()) {
					if (form == Lambda && quantTag != CSET)
						continue;
					formulae[idx ++] = mQExpr(quantTag, form, 
							bd_x,
							mBinaryExpression(binaryTag, id_y, id_z));
					formulae[idx ++] = mBinaryExpression(binaryTag,
							id_x,
							mQExpr(quantTag, form, bd_y, id_z));
					formulae[idx ++] = mBinaryExpression(binaryTag,
							mQExpr(quantTag, form, bd_x, id_y),
							id_z);
				}
			}
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Expression[] constructQuantifiedAssociativeTree(TagSupply tagSupply) {
		final Set<Integer> quantifiedExpressions = tagSupply.quantifiedExpressionTags;
		final int quantSize = quantifiedExpressions.size();
		final Set<Integer> associativeExpressions = tagSupply.associativeExpressionTags;
		final int assocSize = associativeExpressions.size();
		final int length = 4 * assocSize * (2 * quantSize + 1);
		Expression[]  formulae = new Expression[length];
		int idx = 0;
		for (int quantTag:quantifiedExpressions) {
			for (int assocTag : associativeExpressions) {
				for (QuantifiedExpression.Form form: QuantifiedExpression.Form.values()) {
					if (form == Lambda &&  + quantTag != CSET)
						continue;
					formulae[idx ++] = mQExpr(quantTag, form, bd_x,
							mAssociativeExpression(assocTag, id_y, id_z));
					formulae[idx ++] = mAssociativeExpression(assocTag,
							id_x,
							mQExpr(quantTag, form, bd_y, id_z));
					formulae[idx ++] = mAssociativeExpression(assocTag,
							id_x,
							mQExpr(quantTag, form, bd_y, id_z),
							id_t);
					formulae[idx ++] = mAssociativeExpression(assocTag, 
							mQExpr(quantTag, form, bd_x, id_y),
							id_z);
				}
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Expression[] constructQuantifiedUnaryTree(TagSupply tagSupply) {
		final Set<Integer> quantifiedExpressions = tagSupply.quantifiedExpressionTags;
		final int quantSize = quantifiedExpressions.size();
		final Set<Integer> unaryExpressions = tagSupply.unaryExpressionTags;
		final int unarySize = unaryExpressions.size();
		final int length = (2 * unarySize + 1)
				* (2 * quantSize + 1);
		final Expression[]  formulae = new Expression[length];
		int idx = 0;
		for (int quantTag: quantifiedExpressions) {
			for (int unaryTag: unaryExpressions) {
				for (QuantifiedExpression.Form form: QuantifiedExpression.Form.values()) {
					if (form == Lambda && quantTag != CSET)
						continue;
					formulae[idx ++] = mQExpr(quantTag, form, bd_x, 
							mUnaryExpression(unaryTag, b0));
					formulae[idx ++] = mUnaryExpression(unaryTag,
							mQExpr(quantTag, form, bd_x, b0));
				}
			}
			// Special case of negative integer literal
			for (QuantifiedExpression.Form form: QuantifiedExpression.Form.values()) {
				if (form == Lambda && quantTag != CSET)
					continue;
				formulae[idx ++] = mQExpr(quantTag, form, bd_x, 
						mIntegerLiteral(-1));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructAssociativeAssociativePredicateTree(TagSupply tagSupply) {
		final Set<Integer> associativePredicates = tagSupply.associativePredicateTags;
		final int length = associativePredicates.size();
		Predicate[]  formulae = new Predicate[length * length * 3];
		int idx = 0;
		for (int assocTag1 : associativePredicates) {
			for (int assocTag2 : associativePredicates) {
				formulae[idx ++] = mAssociativePredicate(assocTag2,
						mAssociativePredicate(assocTag1, btrue, btrue),
						btrue);
				formulae[idx ++] = mAssociativePredicate(assocTag2,
						btrue,
						mAssociativePredicate(assocTag1, btrue, btrue),
						btrue);
				formulae[idx ++] = mAssociativePredicate(assocTag2,
						btrue,
						mAssociativePredicate(assocTag1, btrue, btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructAssociativePredicateVariableTrees(TagSupply tagSupply) {
		final Set<Integer> associativePredicates = tagSupply.associativePredicateTags;
		final int length = associativePredicates.size();
		Predicate[]  formulae = new Predicate[length * 1 * 3];
		int idx = 0;
		for (int assocTag : associativePredicates) {
			formulae[idx ++] = mAssociativePredicate(assocTag,
					pv_P,
					btrue);
			formulae[idx ++] = mAssociativePredicate(assocTag,
					btrue,
					pv_P,
					btrue);
			formulae[idx ++] = mAssociativePredicate(assocTag,
					btrue,
					pv_P);
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructBinaryBinaryPredicateTrees (TagSupply tagSupply) {
		final Set<Integer> binaryPredicates = tagSupply.binaryPredicateTags;
		final int binarySize = binaryPredicates.size();
		int length = binarySize;
		Predicate[]  formulae = new Predicate[(length)*(length)*2];
		int idx = 0;
		for (int binaryTag1 : binaryPredicates) {
			for (int binaryTag2 : binaryPredicates) {
				formulae[idx ++] = mBinaryPredicate(binaryTag2,
						mBinaryPredicate(binaryTag1, btrue, btrue),
						btrue);
				formulae[idx ++] = mBinaryPredicate(binaryTag2,
						btrue,
						mBinaryPredicate(binaryTag1, btrue, btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructBinaryPredicateVariableTrees (TagSupply tagSupply) {
		final Set<Integer> binaryPredicates = tagSupply.binaryPredicateTags;
		final int binarySize = binaryPredicates.size();
		int length = binarySize;
		Predicate[]  formulae = new Predicate[length * 1 * 2];
		int idx = 0;
		for (int binaryTag : binaryPredicates) {
			formulae[idx ++] = mBinaryPredicate(binaryTag,
					pv_P,
					btrue);
			formulae[idx ++] = mBinaryPredicate(binaryTag,
					btrue,
					pv_P);
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructAssociativeBinaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> associativePredicates = tagSupply.associativePredicateTags;
		final int assocSize = associativePredicates.size();
		final Set<Integer> binaryPredicates = tagSupply.binaryPredicateTags;
		final int binarySize = binaryPredicates.size();
		Predicate[]  formulae = new Predicate[(assocSize)*(binarySize)*5];
		int idx = 0;
		for (int assocTag : associativePredicates) {
			for (int binaryTag : binaryPredicates) {
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mBinaryPredicate(binaryTag, btrue, btrue));
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mBinaryPredicate(binaryTag, btrue, btrue),
						btrue);
				formulae[idx ++] = mAssociativePredicate(assocTag,
						mBinaryPredicate(binaryTag, btrue, btrue),
						btrue);
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						mAssociativePredicate(assocTag, btrue,btrue),
						btrue);
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						btrue,
						mAssociativePredicate(assocTag, btrue,btrue));
				
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructUnaryUnaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> unaryPredicates = tagSupply.unaryPredicateTags;
		final int unarySize = unaryPredicates.size();
		Predicate[]  formulae = new Predicate[unarySize * unarySize];
		int idx = 0;
		for (int unaryTag1 : unaryPredicates) {
			for (int unaryTag2 : unaryPredicates) {
				formulae[idx ++] = mUnaryPredicate(unaryTag1,
						mUnaryPredicate(unaryTag2, btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructUnaryPredicateVariableTrees(TagSupply tagSupply) {
		final Set<Integer> unaryPredicates = tagSupply.unaryPredicateTags;
		final int unarySize = unaryPredicates.size();
		Predicate[] formulae = new Predicate[unarySize * 1];
		int idx = 0;
		for (int unaryTag : unaryPredicates) {
			formulae[idx ++] = mUnaryPredicate(unaryTag,
					pv_P);
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructAssociativeUnaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> associativePredicates = tagSupply.associativePredicateTags;
		final int assocSize = associativePredicates.size();
		final Set<Integer> unaryPredicates = tagSupply.unaryPredicateTags;
		final int unarySize = unaryPredicates.size();
		Predicate[]  formulae = new Predicate[(unarySize)*(assocSize)*4];
		int idx = 0;
		for (int unaryTag : unaryPredicates) {
			for (int assocTag : associativePredicates) {
				formulae[idx ++] = mUnaryPredicate(unaryTag,
						mAssociativePredicate(assocTag, btrue,btrue));
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mUnaryPredicate(unaryTag, btrue));
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mUnaryPredicate(unaryTag, btrue),
						btrue);
				formulae[idx ++] = mAssociativePredicate(assocTag,
						mUnaryPredicate(unaryTag, btrue),
						btrue);
			}
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructBinaryUnaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> binaryPredicates = tagSupply.binaryPredicateTags;
		final int binarySize = binaryPredicates.size();
		final Set<Integer> unaryPredicates = tagSupply.unaryPredicateTags;
		final int unarySize = unaryPredicates.size();
		Predicate[]  formulae = new Predicate[(unarySize)*(binarySize)*3];
		int idx = 0;
		for (int unaryTag : unaryPredicates) {
			for (int binaryTag : binaryPredicates) {
				formulae[idx ++] = mUnaryPredicate(unaryTag,
						mBinaryPredicate(binaryTag, btrue, btrue));
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						mUnaryPredicate(unaryTag, btrue),
						btrue);
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						btrue,
						mUnaryPredicate(unaryTag, btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructQuantifiedQuantifiedPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedPredicates = tagSupply.quantifiedPredicateTags;
		final int quantSize = quantifiedPredicates.size();
		Predicate[]  formulae = new Predicate[quantSize * quantSize];
		int idx = 0;
		for (int quantTag1 : quantifiedPredicates) {
			for (int quantTag2 : quantifiedPredicates) {
				formulae[idx ++] = mQuantifiedPredicate(quantTag2,
						mList(bd_x),
						mQuantifiedPredicate(quantTag1, mList(bd_y), btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructQuantifiedBinaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedPredicates = tagSupply.quantifiedPredicateTags;
		final int quantSize = quantifiedPredicates.size();
		final Set<Integer> binaryPredicates = tagSupply.binaryPredicateTags;
		final int binarySize = binaryPredicates.size();
		Predicate[] formulae = new Predicate[quantSize*binarySize*3];
		int idx = 0;
		for (int quantTag : quantifiedPredicates) {
			for (int binaryTag : binaryPredicates) {
				formulae[idx ++] = mQuantifiedPredicate(quantTag,
						mList(bd_x),
						mBinaryPredicate(binaryTag, btrue, btrue));
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						mQuantifiedPredicate(quantTag, mList(bd_x), btrue),
						btrue);
				formulae[idx ++] = mBinaryPredicate(binaryTag,
						btrue,
						mQuantifiedPredicate(quantTag, mList(bd_x), btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructQuantifiedAssociativePredicateTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedPredicates = tagSupply.quantifiedPredicateTags;
		final int quantSize = quantifiedPredicates.size();
		final Set<Integer> associativePredicates = tagSupply.associativePredicateTags;
		final int assocSize = associativePredicates.size();
		Predicate[] formulae = new Predicate[quantSize*assocSize*4];
		int idx = 0;
		for (int quantTag : quantifiedPredicates) {
			for (int assocTag : associativePredicates) {
				formulae[idx ++] = mQuantifiedPredicate(quantTag,
						mList(bd_x),
						mAssociativePredicate(assocTag, btrue, btrue));
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mQuantifiedPredicate(quantTag, mList(bd_x), btrue));
				formulae[idx ++] = mAssociativePredicate(assocTag,
						btrue,
						mQuantifiedPredicate(quantTag, mList(bd_x), btrue),
						btrue);
				formulae[idx ++] = mAssociativePredicate(assocTag,
						mQuantifiedPredicate(quantTag, mList(bd_x), btrue),
						btrue);
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructQuantifiedUnaryPredicateTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedPredicates = tagSupply.quantifiedPredicateTags;
		final int quantSize = quantifiedPredicates.size();
		final Set<Integer> unaryPredicates = tagSupply.unaryPredicateTags;
		final int unarySize = unaryPredicates.size();
		Predicate[] formulae = new Predicate[quantSize*unarySize*2];
		int idx = 0;
		for (int quantTag : quantifiedPredicates) {
			for (int unaryTag : unaryPredicates) {
				formulae[idx ++] = mQuantifiedPredicate(quantTag,
						mList(bd_y),
						mUnaryPredicate(unaryTag, btrue));
				formulae[idx ++] = mUnaryPredicate(unaryTag,
						mQuantifiedPredicate(quantTag, mList(bd_y), btrue));
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructQuantifiedPredicateVariableTrees(TagSupply tagSupply) {
		final Set<Integer> quantifiedPredicates = tagSupply.quantifiedPredicateTags;
		final int quantSize = quantifiedPredicates.size();
		Predicate[] formulae = new Predicate[quantSize * 1];
		int idx = 0;
		for (int quantTag : quantifiedPredicates) {
			formulae[idx ++] = mQuantifiedPredicate(quantTag,
					mList(bd_y),
					pv_P);
		}
		assert idx == formulae.length;
		return formulae;
	}

	private Predicate[] constructMultiplePredicateTrees(TagSupply tagSupply) {
		final List<Expression> exprs = Common.constructExpressions(tagSupply);
		final int exprsSize = exprs.size();
		final Set<Integer> multiplePredicates = tagSupply.multiplePredicateTags;
		final int multiSize = multiplePredicates.size();
		Predicate[]  formulae = new Predicate[multiSize * exprsSize * exprsSize];
		int idx = 0;
		for (int multiTag : multiplePredicates) {
			for (Expression exp1 : exprs) {
				for (Expression exp2 : exprs) {
					formulae[idx ++] = mMultiplePredicate(multiTag, exp1, exp2);
				}
			}
		}
		assert idx == formulae.length;
		return formulae;
	}
	
	private Predicate[] constructRelop(TagSupply tagSupply) {
		List<Expression> exprs = Common.constructExpressions(tagSupply);
		final int exprsSize = exprs.size();
		final Set<Integer> relationalPredicates = tagSupply.relationalPredicateTags;
		final int relSize = relationalPredicates.size();
		Predicate[] formulae = new Predicate[2 * exprsSize * relSize];
		int idx = 0;
		for (Expression expr: exprs) {
			for (int relTag : relationalPredicates) {
				formulae[idx ++] = mRelationalPredicate(relTag, expr, id_t); 
				formulae[idx ++] = mRelationalPredicate(relTag, id_t, expr); 
			}
		}
		assert idx == formulae.length;
		return formulae; 
	}
	
	private Expression[] constructQuantifierWithPredicate(TagSupply tagSupply) {
		List<Predicate> preds = Common.constructPredicates(tagSupply);
		final Set<Integer> quantifiedExpressions = tagSupply.quantifiedExpressionTags;
		final int quantSize = quantifiedExpressions.size();
		Expression[] formulae = new Expression[2 * preds.size() * quantSize];
		int idx = 0;
		for (Predicate pred: preds) {
			for (int quantTag : quantifiedExpressions) {
				formulae[idx ++] = mQuantifiedExpression(quantTag, Explicit,
						mList(bd_x), pred, two); 
				formulae[idx ++] = mQuantifiedExpression(quantTag, Implicit,
						mList(bd_x), pred, b0); 
			}
		}
		assert idx == formulae.length;
		return formulae; 
	}
	
	// this test ensures that an associative expression treats all
	// its children the same way
	private void routineTestStringFormula(TestPair[] pairs) {
		for (TestPair pair: pairs) {
			String formula = pair.getFormula().toString();
			String formulaParenthesized = pair.getFormula().toStringFullyParenthesized();
			assertEquals("\nTest failed on original String: "+pair.image+"\nUnparser produced: "+formula+"\n",
					pair.image, formula);
			
			pair.verify(formula);
			pair.verify(formulaParenthesized);
		}
	}
	
	/**
	 * Test of hand-written formulae
	 */
	public void testStringFormula() {
		routineTestStringFormula(associativeExpressionTestPairs);
		routineTestStringFormula(associativePredicateTestPairs);
		routineTestStringFormula(predPatternTestPairs);
		routineTestStringFormula(specialExprTestPairs);
		routineTestStringFormula(specialPredTestPairs);
		routineTestStringFormula(assignmentTestPairs);
	}
	
	/**
	 * Test of automatically generated formulae
	 */
	public void testUnparse() {
		for (TagSupply tagSupply: TagSupply.getAllTagSupplies()) {
			routineUnparse(tagSupply);
		}
	}

	public void routineUnparse(TagSupply tagSupply) {
		final LanguageVersion ver = tagSupply.version;

		routineTest(constructAssociativeAssociativeTrees(tagSupply), ver);
		routineTest(constructBinaryBinaryTrees(tagSupply), ver);
		routineTest(constructUnaryUnaryTrees(tagSupply), ver);
		routineTest(constructQuantifiedQuantifiedTrees(), ver);
		routineTest(constructAssociativeBinaryTrees(tagSupply), ver);
		routineTest(constructAssociativeUnaryTrees(tagSupply), ver);
		routineTest(constructBinaryUnaryTrees(tagSupply), ver);
		routineTest(constructQuantifiedBinaryTrees(tagSupply), ver);
		routineTest(constructQuantifiedAssociativeTree(tagSupply), ver);
		routineTest(constructQuantifiedUnaryTree(tagSupply), ver);
		routineTest(constructAssociativeAssociativePredicateTree(tagSupply), ver);
		routineTest(constructAssociativePredicateVariableTrees(tagSupply), ver);
		routineTest(constructBinaryBinaryPredicateTrees(tagSupply), ver);
		routineTest(constructBinaryPredicateVariableTrees(tagSupply), ver);
		routineTest(constructUnaryUnaryPredicateTrees(tagSupply), ver);
		routineTest(constructUnaryPredicateVariableTrees(tagSupply), ver);
		routineTest(constructAssociativeBinaryPredicateTrees(tagSupply), ver);
		routineTest(constructAssociativeUnaryPredicateTrees(tagSupply), ver);
		routineTest(constructBinaryUnaryPredicateTrees(tagSupply), ver);
		routineTest(constructQuantifiedQuantifiedPredicateTrees(tagSupply), ver);
		routineTest(constructQuantifiedBinaryPredicateTrees(tagSupply), ver);
		routineTest(constructQuantifiedAssociativePredicateTrees(tagSupply), ver);
		routineTest(constructQuantifiedUnaryPredicateTrees(tagSupply), ver);
		routineTest(constructQuantifiedPredicateVariableTrees(tagSupply), ver);
		routineTest(constructMultiplePredicateTrees(tagSupply), ver);
		routineTest(constructRelop(tagSupply), ver);
		routineTest(constructQuantifierWithPredicate(tagSupply), ver);
	}
	
	
	private void routineTest(Formula<?>[] formulae, LanguageVersion version) {
		for (int i = 0; i < formulae.length; i++) {
			final TestPair pair;
			if (formulae[i] instanceof Expression) {
				pair = new ExprTestPair(null, version,
						(Expression) formulae[i]);
			} else {
				pair = new PredTestPair(null, version,
						(Predicate) formulae[i]);
			}
			
			final String formula = pair.getFormula().toString();
			final String formulaParenthesized = pair.getFormula().toStringFullyParenthesized();
			
			pair.verify(formula);
			pair.verify(formulaParenthesized);
		}
	}
	
	public void testNegativeIntegerLiteral() throws Exception {
		final ExprTestPair pairs[] = new ExprTestPair[] {
				// Alone
				new ExprTestPair("\u2212x", mUnaryExpression(UNMINUS, id_x)),
				// Unary minus
				new ExprTestPair("\u2212(\u2212x)", mUnaryExpression(UNMINUS,
						mUnaryExpression(UNMINUS, id_x))),
				// Addition
				new ExprTestPair("\u2212x+y", mAssociativeExpression(PLUS,
						mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y+(\u2212x)+z", mAssociativeExpression(PLUS,
						id_y, mUnaryExpression(UNMINUS, id_x), id_z)),
				new ExprTestPair("y+(\u2212x)", mAssociativeExpression(PLUS,
						id_y, mUnaryExpression(UNMINUS, id_x))),
				// Subtraction
				new ExprTestPair("\u2212x \u2212 y", mBinaryExpression(MINUS,
						mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y \u2212 (\u2212x)", mBinaryExpression(MINUS,
						id_y, mUnaryExpression(UNMINUS, id_x))),
				// Multiplication
				new ExprTestPair("(\u2212x)\u2217y", mAssociativeExpression(
						MUL, mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y\u2217(\u2212x)\u2217z",
						mAssociativeExpression(MUL, id_y, mUnaryExpression(
								UNMINUS, id_x), id_z)),
				new ExprTestPair("y\u2217(\u2212x)", mAssociativeExpression(
						MUL, id_y, mUnaryExpression(UNMINUS, id_x))),
				// Division
				new ExprTestPair("(\u2212x) \u00f7 y", mBinaryExpression(DIV,
						mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y \u00f7 (\u2212x)", mBinaryExpression(DIV,
						id_y, mUnaryExpression(UNMINUS, id_x))),
				// Modulo
				new ExprTestPair("(\u2212x) mod y", mBinaryExpression(MOD,
						mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y mod (\u2212x)", mBinaryExpression(MOD,
						id_y, mUnaryExpression(UNMINUS, id_x))),
				// Exponentiation
				new ExprTestPair("(\u2212x) ^ y", mBinaryExpression(EXPN,
						mUnaryExpression(UNMINUS, id_x), id_y)),
				new ExprTestPair("y ^ (\u2212x)", mBinaryExpression(EXPN, id_y,
						mUnaryExpression(UNMINUS, id_x))), };
		
		routineTestStringFormula(pairs);
		
		// Same test with a positive integer literal
		final int length = pairs.length;
		final ExprTestPair pairsPos[] = new ExprTestPair[length];
		final Pattern pattern = Pattern.compile(id_x.getName());
		final IntegerLiteral il_1 = mIntegerLiteral(1);
		final IFormulaRewriter rewriterPos = new DefaultRewriter(false, ff) {
			@Override
			public Expression rewrite(FreeIdentifier identifier) {
				return identifier == id_x ? il_1 : identifier;
			}
		};
		for (int i = 0; i < length; i++) {
			final String image = pairs[i].image;
			final Expression expr = pairs[i].formula;
			final String newImage = pattern.matcher(image).replaceAll("(1)");
			final Expression newExpr = expr.rewrite(rewriterPos);
			pairsPos[i] = new ExprTestPair(newImage, newExpr);
		}
		routineTestStringFormula(pairsPos);

		// Same test with a negative integer literal
		final ExprTestPair pairsNeg[] = new ExprTestPair[length];
		final IntegerLiteral il_m1 = mIntegerLiteral(-1);
		final IFormulaRewriter rewriterNeg = new DefaultRewriter(false, ff) {
			@Override
			public Expression rewrite(UnaryExpression expr) {
				return expr.getChild() == id_x ? il_m1 : expr;
			}
		};
		for (int i = 0; i < length; i++) {
			final String image = pairs[i].image;
			final Expression expr = pairs[i].formula;
			final String newImage = pattern.matcher(image).replaceAll("1");
			final Expression newExpr = expr.rewrite(rewriterNeg);
			pairsNeg[i] = new ExprTestPair(newImage, newExpr);
		}
		routineTestStringFormula(pairsNeg);
	}

}
