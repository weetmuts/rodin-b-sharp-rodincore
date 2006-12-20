/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.ast;

/**
 * Common protocol for formula filters. A formula filter tells whether a given
 * formula satisifies some criterion.
 * <p>
 * This interface contains one method for each of the sub-classes of
 * <code>Formula</code>, except assignments which are not covered by
 * sub-formula positions.
 * </p>
 * 
 * @author Laurent Voisin
 * @see Formula#getPositions(IFormulaFilter)
 */
public interface IFormulaFilter {

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(AssociativeExpression expression);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(AssociativePredicate predicate);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(AtomicExpression expression);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(BinaryExpression expression);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(BinaryPredicate predicate);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(BoolExpression expression);

	/**
	 * Tells whether the given bound identifier declaration passes this filter
	 * criterion.
	 * 
	 * @param decl
	 *            declaration to test
	 * @return <code>true</code> iff the given declaration passes the
	 *         criterion
	 */
	boolean select(BoundIdentDecl decl);

	/**
	 * Tells whether the given identifier passes this filter criterion.
	 * 
	 * @param identifier
	 *            identifier to test
	 * @return <code>true</code> iff the given identifier passes the criterion
	 */
	boolean select(BoundIdentifier identifier);

	/**
	 * Tells whether the given identifier passes this filter criterion.
	 * 
	 * @param identifier
	 *            identifier to test
	 * @return <code>true</code> iff the given identifier passes the criterion
	 */
	boolean select(FreeIdentifier identifier);

	/**
	 * Tells whether the given literal passes this filter criterion.
	 * 
	 * @param literal
	 *            literal to test
	 * @return <code>true</code> iff the given literal passes the criterion
	 */
	boolean select(IntegerLiteral literal);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(LiteralPredicate predicate);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(QuantifiedExpression expression);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(QuantifiedPredicate predicate);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(RelationalPredicate predicate);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(SetExtension expression);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(SimplePredicate predicate);

	/**
	 * Tells whether the given expression passes this filter criterion.
	 * 
	 * @param expression
	 *            expression to test
	 * @return <code>true</code> iff the given expression passes the criterion
	 */
	boolean select(UnaryExpression expression);

	/**
	 * Tells whether the given predicate passes this filter criterion.
	 * 
	 * @param predicate
	 *            predicate to test
	 * @return <code>true</code> iff the given predicate passes the criterion
	 */
	boolean select(UnaryPredicate predicate);

}
