/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.ast;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eventb.internal.core.ast.LegibilityResult;
import org.eventb.internal.core.typecheck.TypeCheckResult;
import org.eventb.internal.core.typecheck.TypeUnifier;

/**
 * Implements the deterministic assignment, where an expression is given for
 * each assigned identifier.
 * 
 * @author Laurent Voisin
 */
public class BecomesEqualTo extends Assignment {

	private final Expression[] values;
	
	public BecomesEqualTo(FreeIdentifier assignedIdent, Expression value,
			SourceLocation location) {
		super(BECOMES_EQUAL_TO, location, value.hashCode(), assignedIdent);
		this.values = new Expression[] {value};
		checkPreconditions();
	}

	public BecomesEqualTo(FreeIdentifier[] assignedIdents, Expression[] values,
			SourceLocation location) {
		super(BECOMES_EQUAL_TO, location, combineHashCodes(values), assignedIdents);
		this.values = new Expression[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
		checkPreconditions();
	}

	public BecomesEqualTo(List<FreeIdentifier> assignedIdents, List<Expression> values,
			SourceLocation location) {
		super(BECOMES_EQUAL_TO, location, combineHashCodes(values), assignedIdents);
		this.values = values.toArray(new Expression[values.size()]);
		checkPreconditions();
	}


	private void checkPreconditions() {
		assert assignedIdents.length == values.length;
	}
	
	/**
	 * Returns the expressions that occur in the right-hand side of this
	 * assignment.
	 * 
	 * @return an array containing the expressions on the right-hand side of
	 *         this assignment
	 */
	public Expression[] getExpressions() {
		Expression[] result = new Expression[values.length];
		System.arraycopy(values, 0, result, 0, values.length);
		return result;
	}
	
	@Override
	public Assignment flatten(FormulaFactory factory) {
		final Expression[] newValues = new Expression[values.length];
		boolean changed = false;
		for (int i = 0; i < values.length; i++) {
			newValues[i] = values[i].flatten(factory);
			changed |= newValues[i] != values[i];
		}
		if (! changed)
			return this;
		return factory.makeBecomesEqualTo(assignedIdents, values, getSourceLocation());
	}

	@Override
	protected void collectFreeIdentifiers(LinkedHashSet<FreeIdentifier> freeIdents) {
		for (FreeIdentifier ident: assignedIdents) {
			ident.collectFreeIdentifiers(freeIdents);
		}
		for (Expression value: values) {
			value.collectFreeIdentifiers(freeIdents);
		}
	}

	@Override
	protected void collectNamesAbove(Set<String> names, String[] boundNames,
			int offset) {
		
		for (FreeIdentifier ident: assignedIdents) {
			ident.collectNamesAbove(names, boundNames, offset);
		}
		for (Expression value: values) {
			value.collectNamesAbove(names, boundNames, offset);
		}
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.ast.Formula#getSyntaxTree(java.lang.String[], java.lang.String)
	 */
	@Override
	protected String getSyntaxTree(String[] boundNames, String tabs) {
		final String childTabs = tabs + '\t';
		
		final StringBuilder result = new StringBuilder();
		result.append(tabs);
		result.append(this.getClass().getSimpleName());
		result.append(" [:=]\n");
		for (FreeIdentifier ident: assignedIdents) {
			result.append(ident.getSyntaxTree(boundNames, childTabs));
		}
		for (Expression value: values) {
			result.append(value.getSyntaxTree(boundNames, childTabs));
		}
		return result.toString();
	}

	@Override
	protected boolean isWellFormed(int noOfBoundVars) {
		for (Expression value: values) {
			if (! value.isWellFormed(noOfBoundVars))
				return false;
		}
		return true;
	}

	@Override
	protected boolean equals(Formula otherFormula, boolean withAlphaConversion) {
		BecomesEqualTo other = (BecomesEqualTo) otherFormula;
		if (! this.hasSameAssignedIdentifiers(other))
			return false;
		for (int i = 0; i < values.length; i++) {
			if (! values[i].equals(other.values[i], withAlphaConversion))
				return false;
		}
		return true;
	}

	@Override
	protected void typeCheck(TypeCheckResult result, BoundIdentDecl[] boundAbove) {
		final SourceLocation loc = getSourceLocation();
		for (int i = 0; i < values.length; i++) {
			assignedIdents[i].typeCheck(result, boundAbove);
			values[i].typeCheck(result, boundAbove);
			result.unify(assignedIdents[i].getType(), values[i].getType(), loc);
		}
	}

	@Override
	protected void isLegible(LegibilityResult result, BoundIdentDecl[] quantifiedIdents) {
		for (FreeIdentifier ident: assignedIdents) {
			ident.isLegible(result, quantifiedIdents);
			if (! result.isSuccess())
				return;
		}
		for (Expression value: values) {
			value.isLegible(result, quantifiedIdents);
			if (! result.isSuccess())
				return;
		}
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.ast.Formula#getWDPredicateRaw(org.eventb.core.ast.FormulaFactory)
	 */
	@Override
	protected Predicate getWDPredicateRaw(FormulaFactory formulaFactory) {
		return getWDConjunction(formulaFactory, values);
	}

	@Override
	protected boolean solveType(TypeUnifier unifier) {
		boolean result = true;
		for (Expression value: values) {
			result &= value.solveType(unifier);
		}
		return finalizeTypeCheck(result, unifier);
	}

	@Override
	protected String toString(boolean isRightChild, int parentTag,
			String[] boundNames) {
		
		StringBuilder result = new StringBuilder();
		appendAssignedIdents(result);
		result.append(" ≔ ");
		boolean comma = false;
		for (Expression value: values) {
			if (comma) result.append(", ");
			result.append(value.toString(false, STARTTAG, boundNames));
			comma = true;
		}
		return result.toString();
	}

	@Override
	protected String toStringFullyParenthesized(String[] boundNames) {
		StringBuilder result = new StringBuilder();
		appendAssignedIdents(result);
		result.append(" ≔ ");
		boolean comma = false;
		for (Expression value: values) {
			if (comma) result.append(", ");
			result.append('(');
			result.append(value.toStringFullyParenthesized(boundNames));
			result.append(')');
			comma = true;
		}
		return result.toString();
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.ast.Formula#accept(org.eventb.core.ast.IVisitor)
	 */
	@Override
	public boolean accept(IVisitor visitor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Predicate getFISPredicateRaw(FormulaFactory formulaFactory) {
		return formulaFactory.makeLiteralPredicate(BTRUE, getSourceLocation());
	}

	@Override
	protected Predicate getBAPredicateRaw(FormulaFactory formulaFactory) {
		Predicate[] predicates = new Predicate[assignedIdents.length];
		for(int i=0; i<assignedIdents.length; i++) {
			FreeIdentifier primedIdentifier = formulaFactory.makePrimedFreeIdentifier(assignedIdents[i]);
			predicates[i] = formulaFactory.makeRelationalPredicate(EQUAL, primedIdentifier, values[i], null);
		}
		if(predicates.length > 1)
			return formulaFactory.makeAssociativePredicate(LAND, predicates, getSourceLocation());
		else
			return predicates[0];
	}

}
