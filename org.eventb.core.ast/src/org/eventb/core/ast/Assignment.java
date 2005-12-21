/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eventb.internal.core.ast.Replacement;
import org.eventb.internal.core.typecheck.TypeUnifier;

/**
 * Common implementation for event-B assignments.
 * <p>
 * There are various kinds of assignments which are implemented in sub-classes
 * of this class. The commonality between these assignments is that they are
 * formed of two parts: a left-hand side and a right hand-side. The left-hand side,
 * that is a list of free identifiers, is implemented in this class, while the
 * right-hand side is implemented in subclasses.
 * </p>
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @author Laurent Voisin
 */
public abstract class Assignment extends Formula<Assignment> {

	protected final FreeIdentifier[] assignedIdents;
	
	// True iff this formula has been type-checked
	private boolean typeChecked;
	
	/**
	 * Creates a new assignment with the given arguments.
	 * 
	 * @param tag node tag of this expression
	 * @param location source location of this expression
	 * @param hashCode combined hash code for children
	 * @param assignedIdent free identifier that constitute the left-hand side
	 */
	protected Assignment(int tag, SourceLocation location, int hashCode, 
			FreeIdentifier assignedIdent) {
		
		super(tag, location, combineHashCodes(assignedIdent.hashCode(), hashCode));
		this.assignedIdents = new FreeIdentifier[] {assignedIdent};
	}

	/**
	 * Creates a new assignment with the given arguments.
	 * 
	 * @param tag node tag of this expression
	 * @param location source location of this expression
	 * @param hashCode combined hash code for children
	 * @param assignedIdents array of free identifiers that constitute the left-hand side
	 */
	protected Assignment(int tag, SourceLocation location, int hashCode, 
			FreeIdentifier[] assignedIdents) {
		
		super(tag, location, combineHashCodes(combineHashCodes(assignedIdents), hashCode));
		this.assignedIdents = new FreeIdentifier[assignedIdents.length];
		System.arraycopy(assignedIdents, 0, this.assignedIdents, 0, assignedIdents.length);
	}

	/**
	 * Creates a new assignment with the given arguments.
	 * 
	 * @param tag node tag of this expression
	 * @param location source location of this expression
	 * @param hashCode combined hash code for children
	 * @param assignedIdents array of free identifiers that constitute the left-hand side
	 */
	protected Assignment(int tag, SourceLocation location, int hashCode,
			List<FreeIdentifier> assignedIdents) {
		
		super(tag, location, combineHashCodes(combineHashCodes(assignedIdents), hashCode));
		this.assignedIdents = assignedIdents.toArray(new FreeIdentifier[assignedIdents.size()]);
	}

	
	protected final void appendAssignedIdents(StringBuilder result) {
		boolean comma = false;
		for (FreeIdentifier ident : assignedIdents) {
			if (comma)
				result.append(',');
			comma = true;
			result.append(ident.getName());
		}
	}
	
	@Override
	protected final Assignment bindTheseIdents(Map<String, Integer> binding, int offset,
			FormulaFactory factory) {
		// Should never happen
		assert false;
		return this;
	}

	protected final boolean finalizeTypeCheck(boolean childrenOK, TypeUnifier unifier) {
		assert typeChecked == false || typeChecked == childrenOK;
		typeChecked = childrenOK;
		for (FreeIdentifier ident: assignedIdents) {
			typeChecked &= ident.solveType(unifier);
		}
		return typeChecked;
	}

	/**
	 * Return the left-hand side of this assignment.
	 * 
	 * @return an array of the free identifiers that make up the left-hand side
	 *         of this assignment
	 */
	public FreeIdentifier[] getAssignedIdentifiers() {
		FreeIdentifier[] result = new FreeIdentifier[this.assignedIdents.length];
		System.arraycopy(assignedIdents, 0, result, 0, assignedIdents.length);
		return result;
	}
	
	protected final String getSyntaxTreeLHS(String[] boundNames, String tabs) {
		StringBuilder builder = new StringBuilder();
		for (FreeIdentifier ident: assignedIdents) {
			builder.append(ident.getSyntaxTree(boundNames, tabs));
		}
		return builder.toString();
	}
		
	@Override
	protected final Assignment getTypedThis() {
		return this;
	}

	protected final boolean hasSameAssignedIdentifiers(Assignment other) {
		return Arrays.equals(assignedIdents, other.assignedIdents);
	}

	@Override
	public final boolean isTypeChecked() {
		return typeChecked;
	}

	@Override
	protected final Assignment substituteAll(int noOfBoundVars, Replacement replacement,
			FormulaFactory formulaFactory) {
		// Should never happen
		assert false;
		return null;
	}

}
