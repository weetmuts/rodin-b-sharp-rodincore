/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.ast;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides some static method which are useful when manipulating
 * quantified formulas.
 * 
 * @author Laurent Voisin
 */
public abstract class QuantifiedUtil {

	private static class StructuredName {
		String prefix;
		int suffix;
		
		static Pattern suffixExtractor = Pattern.compile("^(.*)(\\d+)$", Pattern.DOTALL);		
		
		StructuredName(String name) {
			Matcher matcher = suffixExtractor.matcher(name);
			if (matcher.matches()) {
				prefix = matcher.group(1);
				suffix = Integer.valueOf(matcher.group(2));
			}
			else {
				prefix = name;
				suffix = -1;
			}
		}
		
		@Override 
		public String toString() {
			if (suffix < 0) {
				return prefix;
			}
			return prefix + suffix;
		}
	}

	/**
	 * Concatenates the two given arrays of bound identifier declarations into one.
	 * 
	 * @param bound1
	 *            first array to concatenate
	 * @param bound2
	 *            second array to concatenate
	 * @return the result of concatenating the second array after the first one
	 */
	public static BoundIdentDecl[] catenateBoundIdentLists(BoundIdentDecl[] bound1, BoundIdentDecl[] bound2) {
		BoundIdentDecl[] newBoundIdents = new BoundIdentDecl[bound1.length + bound2.length];
		System.arraycopy(bound1, 0, newBoundIdents, 0, bound1.length);
		System.arraycopy(bound2, 0, newBoundIdents, bound1.length, bound2.length);
		return newBoundIdents;
	}

	/**
	 * Concatenates the two given arrays into one array of identifier names.
	 * 
	 * @param boundNames
	 *            array of identifier names
	 * @param quantifiedIdents
	 *            array of quantifier identifier declarations
	 * @return the result of concatenating the names in the second array after the first one
	 */
	public static String[] catenateBoundIdentLists(String[] boundNames, BoundIdentDecl[] quantifiedIdents) {
		String[] newBoundNames = new String[boundNames.length + quantifiedIdents.length];
		System.arraycopy(boundNames, 0, newBoundNames, 0, boundNames.length);
		int idx = boundNames.length;
		for (BoundIdentDecl ident : quantifiedIdents) {
			newBoundNames[idx ++] = ident.getName();
		}
		return newBoundNames;
	}

	/**
	 * Concatenates the two given arrays into one.
	 * 
	 * @param bound1
	 *            first array of names
	 * @param bound2
	 *            second array of names
	 * @return the result of concatenating the second array after the first one
	 */
	public static String[] catenateBoundIdentLists(String[] bound1, String[] bound2) {
		String[] newBoundIdents = new String[bound1.length + bound2.length];
		System.arraycopy(bound1, 0, newBoundIdents, 0, bound1.length);
		System.arraycopy(bound2, 0, newBoundIdents, bound1.length, bound2.length);
		return newBoundIdents;
	}

	/**
	 * Find new names for the given quantified identifiers so that they don't
	 * conflict with the given names.
	 * 
	 * @param boundHere
	 *            array of bound identifier declarations to make free.
	 * @param usedNames
	 *            array of names that are reserved (usually occurring already
	 *            free in the formula)
	 * @return a list of new names that are distinct from each other and do not
	 *         occur in the list of used names
	 */
	public static String[] resolveIdents(BoundIdentDecl[] boundHere, final HashSet<String> usedNames) {
		final int length = boundHere.length;
		String[] result = new String[length];
		
		// Create the new identifiers.
		for (int i = 0; i < length; i++) {
			result[i] = solve(boundHere[i].getName(), usedNames);
			usedNames.add(result[i]);
		}
		
		return result;
	}

	private static String solve(String name, Set<String> usedNames) {
		if (! usedNames.contains(name)) {
			// Not used, this name is OK.
			return name;
		}
		
		// We have a name conflict, so we try with another name
		QuantifiedUtil.StructuredName sname = new QuantifiedUtil.StructuredName(name);
		String newName;
		do {
			++ sname.suffix;
			newName = sname.toString();
		} while (usedNames.contains(newName));
		
		return newName;
	}

	// resolve (locally) quantified names so that they do not conflict with the
	// given type environment.
	//
	// @see FormulaFactory#makeFreshIdentifiers(BoundIdentDecl[], ITypeEnvironment)
	//
	protected static FreeIdentifier[] resolveIdents(BoundIdentDecl[] boundHere,
			final ITypeEnvironment environment, FormulaFactory factory) {
		
		final int length = boundHere.length;
		FreeIdentifier[] result = new FreeIdentifier[length];
		
		// Create the new identifiers.
		for (int i = 0; i < length; i++) {
			assert boundHere[i].getType() != null;
			
			String name = solve(boundHere[i].getName(), environment.getNames());
			result[i] = factory.makeFreeIdentifier(name, boundHere[i].getSourceLocation());
			result[i].setType(boundHere[i].getType(), null);
			environment.addName(name, result[i].getType());
		}
		
		return result;
	}

}
