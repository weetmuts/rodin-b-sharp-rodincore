/*******************************************************************************
 * Copyright (c) 2005, 2012 ETH Zurich and others.
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
 *     Systerel - added mathematical extensions
 *     Systerel - added specialization
 *******************************************************************************/
package org.eventb.core.ast.tests;

import static java.util.Collections.singletonList;
import static org.eventb.core.ast.Formula.BTRUE;
import static org.eventb.core.ast.Formula.EQUAL;
import static org.eventb.core.ast.Formula.FORALL;
import static org.eventb.core.ast.Formula.KFINITE;
import static org.eventb.core.ast.Formula.KID_GEN;
import static org.eventb.core.ast.Formula.KPARTITION;
import static org.eventb.core.ast.Formula.KPRJ1_GEN;
import static org.eventb.core.ast.Formula.KPRJ2_GEN;
import static org.eventb.core.ast.Formula.LAND;
import static org.eventb.core.ast.Formula.LIMP;
import static org.eventb.core.ast.Formula.MAPSTO;
import static org.eventb.core.ast.Formula.MINUS;
import static org.eventb.core.ast.Formula.NOT;
import static org.eventb.core.ast.Formula.PLUS;
import static org.eventb.core.ast.Formula.POW;
import static org.eventb.core.ast.Formula.QUNION;
import static org.eventb.core.ast.Formula.TRUE;
import static org.eventb.core.ast.LanguageVersion.LATEST;
import static org.eventb.core.ast.QuantifiedExpression.Form.Explicit;
import static org.eventb.core.ast.tests.AbstractTests.LIST_DT;
import static org.eventb.core.ast.tests.AbstractTests.parseType;
import static org.eventb.core.ast.tests.TestGenParser.EXT_PRIME;
import static org.eventb.core.ast.tests.TestGenParser.MONEY;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.AtomicExpression;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.BoolExpression;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ISpecialization;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.MultiplePredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.PredicateVariable;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.UnaryPredicate;
import org.eventb.core.ast.extension.IFormulaExtension;

/**
 * Provides simplistic methods for creating new formulae without too much
 * typing.
 * <p>
 * The methods provided are essentially geared towards unit test development,
 * making it less painful to build test formulae with a keyboard.
 * </p>
 * 
 * @author Laurent Voisin
 */
public class FastFactory {

	public static final Predicate[] NO_PREDICATE = new Predicate[0];
	public static final Expression[] NO_EXPRESSION = new Expression[0];
	
	private static final Set<IFormulaExtension> EXTNS = new HashSet<IFormulaExtension>(Arrays.asList(EXT_PRIME, MONEY));
	static {
		EXTNS.addAll(LIST_DT.getExtensions());
	}
	public static FormulaFactory ff = FormulaFactory.getInstance(EXTNS);

	public static AssociativeExpression mAssociativeExpression(
			Expression... children) {
		return mAssociativeExpression(PLUS, children);
	}

	public static AssociativeExpression mAssociativeExpression(
			int tag, Expression... children) {
		return ff.makeAssociativeExpression(tag, children, null);
	}

	public static AssociativePredicate mAssociativePredicate(int tag,
			Predicate... children) {
		return ff.makeAssociativePredicate(tag, children, null);
	}

	public static AssociativePredicate mAssociativePredicate(
			Predicate... children) {
		return mAssociativePredicate(LAND, children);
	}

	public static AtomicExpression mAtomicExpression() {
		return mAtomicExpression(TRUE);
	}

	public static AtomicExpression mAtomicExpression(int tag) {
		return ff.makeAtomicExpression(tag, null);
	}

	public static AtomicExpression mEmptySet(Type type) {
		return ff.makeEmptySet(type, null);
	}

	public static AtomicExpression mPrj1(Type type) {
		return ff.makeAtomicExpression(KPRJ1_GEN, null, type);
	}

	public static AtomicExpression mPrj2(Type type) {
		return ff.makeAtomicExpression(KPRJ2_GEN, null, type);
	}

	public static AtomicExpression mId(Type type) {
		return ff.makeAtomicExpression(KID_GEN, null, type);
	}

	public static BecomesEqualTo mBecomesEqualTo(FreeIdentifier ident, Expression value) {
		return ff.makeBecomesEqualTo(ident, value, null);
	}
	
	public static BecomesEqualTo mBecomesEqualTo(FreeIdentifier[] lhs, Expression[] rhs) {
		return ff.makeBecomesEqualTo(lhs, rhs, null);
	}

	public static BecomesMemberOf mBecomesMemberOf(FreeIdentifier lhs, Expression rhs) {
		return ff.makeBecomesMemberOf(lhs, rhs, null);
	}
	
	public static BecomesSuchThat mBecomesSuchThat(FreeIdentifier[] lhs,
			BoundIdentDecl[] primed, Predicate rhs) {
		return ff.makeBecomesSuchThat(lhs, primed, rhs, null);
	}
	
	public static BecomesSuchThat mBecomesSuchThat(FreeIdentifier[] lhs, Predicate rhs) {
		BoundIdentDecl[] primed = new BoundIdentDecl[lhs.length];
		for (int i = 0; i < lhs.length; i++) {
			primed[i] = lhs[i].asDecl(ff);
		}
		return mBecomesSuchThat(lhs, primed, rhs);
	}
	
	public static BinaryExpression mBinaryExpression(Expression left,
			Expression right) {
		return mBinaryExpression(MINUS, left, right);
	}

	public static BinaryExpression mBinaryExpression(int tag, Expression left,
			Expression right) {
		return ff.makeBinaryExpression(tag, left, right, null);
	}

	public static BinaryPredicate mBinaryPredicate(int tag, Predicate left,
			Predicate right) {
		return ff.makeBinaryPredicate(tag, left, right, null);
	}

	public static BinaryPredicate mBinaryPredicate(Predicate left,
			Predicate right) {
		return mBinaryPredicate(LIMP, left, right);
	}

	public static BoolExpression mBoolExpression(Predicate pred) {
		return ff.makeBoolExpression(pred, null);
	}

	public static BoundIdentDecl mBoundIdentDecl(String name) {
		return mBoundIdentDecl(name, null);
	}

	public static BoundIdentDecl mBoundIdentDecl(String name, Type type) {
		return ff.makeBoundIdentDecl(name, null, type);
	}

	public static BoundIdentifier mBoundIdentifier(int index) {
		return mBoundIdentifier(index, null);
	}

	public static BoundIdentifier mBoundIdentifier(int index, Type type) {
		return ff.makeBoundIdentifier(index, null, type);
	}

	public static FreeIdentifier mFreeIdentifier(String name) {
		return mFreeIdentifier(name, null);
	}

	public static FreeIdentifier mFreeIdentifier(String name, Type type) {
		return ff.makeFreeIdentifier(name, null, type);
	}

	public static IntegerLiteral mIntegerLiteral() {
		return ff.makeIntegerLiteral(BigInteger.ZERO, null);
	}

	public static IntegerLiteral mIntegerLiteral(long value) {
		return ff.makeIntegerLiteral(BigInteger.valueOf(value), null);
	}

	public static <T> T[] mList(T... objs) {
		return objs;
	}
	
	public static LiteralPredicate mLiteralPredicate(int tag) {
		return ff.makeLiteralPredicate(tag, null);
	}

	public static LiteralPredicate mLiteralPredicate() {
		return mLiteralPredicate(BTRUE);
	}

	// Builds left-associative maplet chains
	public static BinaryExpression mMaplet(Expression left, Expression right,
			Expression... others) {
		BinaryExpression maplet;
		maplet = mBinaryExpression(MAPSTO, left, right);
		for (final Expression other: others) {
			maplet = mBinaryExpression(MAPSTO, maplet, other);
		}
		return maplet;
	}

	public static QuantifiedExpression mQuantifiedExpression(
			BoundIdentDecl[] boundIdents, Predicate pred, Expression expr) {
		return mQuantifiedExpression(QUNION, Explicit, boundIdents, pred, expr);
	}

	public static QuantifiedExpression mQuantifiedExpression(int tag,
			QuantifiedExpression.Form form, BoundIdentDecl[] boundIdents,
			Predicate pred, Expression expr) {
		return ff.makeQuantifiedExpression(tag, boundIdents, pred, expr, null,
				form);
	}

	public static QuantifiedPredicate mQuantifiedPredicate(
			BoundIdentDecl[] boundIdents, Predicate pred) {
		return mQuantifiedPredicate(FORALL, boundIdents, pred);
	}

	public static QuantifiedPredicate mQuantifiedPredicate(int tag,
			BoundIdentDecl[] boundIdents, Predicate pred) {
		return ff.makeQuantifiedPredicate(tag, boundIdents, pred, null);
	}

	public static RelationalPredicate mRelationalPredicate(Expression left,
			Expression right) {
		return mRelationalPredicate(EQUAL, left, right);
	}

	public static RelationalPredicate mRelationalPredicate(int tag,
			Expression left, Expression right) {
		return ff.makeRelationalPredicate(tag, left, right, null);
	}

	public static SetExtension mSetExtension(Expression... members) {
		return ff.makeSetExtension(members, null);
	}

	public static SimplePredicate mSimplePredicate(Expression expr) {
		return ff.makeSimplePredicate(KFINITE, expr, null);
	}

	public static ITypeEnvironment mTypeEnvironment() {
		return ff.makeTypeEnvironment();
	}

	public static ITypeEnvironment mTypeEnvironment(String[] names, Type[] types) {
		assert names.length == types.length;
		ITypeEnvironment result = ff.makeTypeEnvironment();
		for (int i = 0; i < names.length; i++) {
			result.addName(names[i], types[i]);
		}
		return result;
	}

	public static ITypeEnvironment mTypeEnvironment(String... strs) {
		assert (strs.length & 1) == 0;
		ITypeEnvironment te = ff.makeTypeEnvironment();
		for (int i = 0; i < strs.length; i += 2) {
			final String name = strs[i];
			final Type type = parseType(strs[i + 1]);
			te.addName(name, type);
		}
		return te;
	}

	public static ITypeEnvironment mTypeEnvironment(Object... objs) {
		assert (objs.length & 1) == 0;
		ITypeEnvironment result = ff.makeTypeEnvironment();
		for (int i = 0; i < objs.length; i += 2) {
			result.addName((String) objs[i], (Type) objs[i+1]);
		}
		return result;
	}
	
	public static ITypeEnvironment addToTypeEnvironment(
			ITypeEnvironment typeEnv, String... strs) {
		assert (strs.length & 1) == 0;
		for (int i = 0; i < strs.length; i += 2) {
			typeEnv.addName(strs[i],
					typeEnv.getFormulaFactory().parseType(strs[i + 1], LATEST)
							.getParsedType());
		}
		return typeEnv;
	}
	
	public static ISpecialization mTypeSpecialization(ITypeEnvironment te,
			String typeSpecialization) {
		final SpecializationBuilder builder = new SpecializationBuilder(te);
		builder.addTypeSpecializations(typeSpecialization);
		return builder.getResult();
	}

	public static ISpecialization mSpecialization(ITypeEnvironment te,
			String specialization) {
		final SpecializationBuilder builder = new SpecializationBuilder(te);
		builder.addSpecialization(specialization);
		return builder.getResult();
	}

	public static UnaryExpression mUnaryExpression(Expression child) {
		return mUnaryExpression(POW, child);
	}

	public static UnaryExpression mUnaryExpression(int tag, Expression child) {
		return ff.makeUnaryExpression(tag, child, null);
	}

	public static UnaryPredicate mUnaryPredicate(int tag, Predicate child) {
		return ff.makeUnaryPredicate(tag, child, null);
	}
	
	public static UnaryPredicate mUnaryPredicate(Predicate child) {
		return mUnaryPredicate(NOT, child);
	}

	public static MultiplePredicate mMultiplePredicate(int tag, Expression... expressions) {
		return ff.makeMultiplePredicate(tag, expressions, null);
	}

	public static MultiplePredicate mMultiplePredicate(Expression... expressions) {
		return mMultiplePredicate(KPARTITION, expressions);
	}

	public static PredicateVariable mPredicateVariable(String name) {
		return ff.makePredicateVariable(name, null);
	}
	
	public static ExtendedPredicate mExtendedPredicate(Expression e) {
		return ff.makeExtendedPredicate(EXT_PRIME, Collections.singleton(e),
				Collections.<Predicate> emptySet(), null);
	}
	
	public static ExtendedExpression mExtendedExpression(
			Expression... expressions) {
		return ff
				.makeExtendedExpression(MONEY, expressions, NO_PREDICATE, null);
	}

	public static ExtendedExpression mListCons(Expression... expressions) {
		final Type eType;
		if (expressions.length == 0) {
			eType = null;
		} else {
			eType = expressions[0].getType();
		}
		return mListCons(eType, expressions);
	}

	public static ExtendedExpression mListCons(Type eType,
			Expression... expressions) {
		final Type listType;
		if (eType == null) {
			listType = null;
		} else {
			listType = ff.makeParametricType(singletonList(eType),
					LIST_DT.getTypeConstructor());
		}
		ExtendedExpression result = ff.makeExtendedExpression(
				LIST_DT.getConstructor("NIL"), NO_EXPRESSION, NO_PREDICATE,
				null, listType);
		for (int i = expressions.length - 1; i >= 0; i--) {
			final Expression[] exprs = new Expression[] { expressions[i],
					result };
			result = ff.makeExtendedExpression(LIST_DT.getConstructor("CONS"),
					exprs, NO_PREDICATE, null, listType);
		}
		return result;
	}
}
