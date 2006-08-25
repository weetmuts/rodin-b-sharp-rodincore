/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core;

import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ITypeEnvironment;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B SC elements that contain a predicate.
 * <p>
 * As this element has been statically checked, the contained predicate parses
 * and type-checks. Thus, it can be manipulated directly as an AST.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @author Stefan Hallerstede
 */
public interface ISCPredicateElement extends IInternalElement {

	/**
	 * Returns the predicate string contained in this element.
	 * 
	 * @return the string representation of the predicate of this element
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	String getPredicateString()
			throws RodinDBException;

	/**
	 * Returns the untyped predicate contained in this element.
	 * 
	 * @param factory
	 *            the formula factory to use for building the result
	 * 
	 * @return the predicate of this element
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	Predicate getPredicate(FormulaFactory factory)
			throws RodinDBException;

	/**
	 * Returns the typed predicate contained in this element.
	 * 
	 * @param factory
	 *            the formula factory to use for building the result
	 * 
	 * @param typenv
	 *            the type environment to use for building the result
	 * 
	 * @return the predicate of this element
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	Predicate getPredicate(FormulaFactory factory, ITypeEnvironment typenv)
			throws RodinDBException;

	/**
	 * Sets the predicate contained in this element.
	 * 
	 * @param predicate
	 *            the predicate to set (must be type-checked)
	 * @throws RodinDBException
	 *             if there was a problem accessing the database
	 */
	
	void setPredicate(Predicate predicate) throws RodinDBException;

}
