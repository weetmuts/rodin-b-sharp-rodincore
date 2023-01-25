/*******************************************************************************
 * Copyright (c) 2010, 2023 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *     Université de Lorraine - additional hypotheses for set membership
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions;

import static java.util.Arrays.stream;
import static org.eventb.internal.core.seqprover.eventbExtensions.utils.FreshInstantiation.genFreshFreeIdent;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.ParametricType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.datatype.IConstructorArgument;
import org.eventb.core.ast.datatype.IDatatype;
import org.eventb.core.ast.datatype.ISetInstantiation;
import org.eventb.core.ast.datatype.ITypeInstantiation;
import org.eventb.core.ast.extension.IExpressionExtension;

/**
 * Helper class for reasoners about datatypes.
 * 
 * @author Nicolas Beauger
 * 
 */
public class DTReasonerHelper {

	public static final Predicate[] NO_PRED = new Predicate[0];

	public static boolean isDatatypeType(Type type) {
		if (!(type instanceof ParametricType)) {
			return false;
		}
		final ParametricType prmType = (ParametricType) type;
		final IExpressionExtension ext = prmType.getExprExtension();
		return ext.getOrigin() instanceof IDatatype;
	}

	public static Expression predIsExtSetMembership(Predicate pred, IPosition position, IExpressionExtension ext) {
		/*
		 * This matches a predicate x ∈ E, where x is pointed at by position and E is an
		 * extended expression that is made with ext and is not a type expression.
		 */
		if (position.isFirstChild() && position.getParent().isRoot() && pred.getTag() == Formula.IN) {
			RelationalPredicate rel = (RelationalPredicate) pred;
			Expression right = rel.getRight();
			if (right instanceof ExtendedExpression) {
				ExtendedExpression rightExt = (ExtendedExpression) right;
				if (rightExt.getExtension().equals(ext) && !rightExt.isATypeExpression()) {
					return rightExt;
				}
			}
		}
		return null;
	}

	public static Predicate makeIdentEqualsConstr(FreeIdentifier ident,
			IExpressionExtension constr, ParametricType type,
			FreeIdentifier[] params, FormulaFactory ff) {
		final Expression constInst = ff.makeExtendedExpression(constr, params,
				NO_PRED, null, type);
		final Predicate newHyp = ff.makeRelationalPredicate(Formula.EQUAL,
				ident, constInst, null);
		return newHyp;
	}

	public static Expression[] makeParamSets(IConstructorArgument[] arguments, ISetInstantiation instSet) {
		if (instSet == null) {
			return null;
		}
		return stream(arguments).map(arg -> arg.getSet(instSet)).toArray(Expression[]::new);
	}

	public static FreeIdentifier[] makeFreshIdents(
			IConstructorArgument[] arguments, ITypeInstantiation inst,
			FormulaFactory ff, ITypeEnvironment env) {
		final int size = arguments.length;
		final FreeIdentifier[] idents = new FreeIdentifier[size];
		for (int i = 0; i < size; i++) {
			final IConstructorArgument arg = arguments[i];
			final String argName = makeArgName(arg, i);
			final Type argType = arguments[i].getType(inst);
			// proposed argName changes each time => no need to add to env
			idents[i] = genFreshFreeIdent(env, argName, argType);
		}
		return idents;
	}

	public static String makeArgName(IConstructorArgument arg, int i) {
		final String prefix = "p_";
		final String suffix;
		if (arg.isDestructor()) {
			suffix = arg.asDestructor().getName();
		} else {
			suffix = Integer.toString(i);
		}
		return prefix + suffix;
	}

}