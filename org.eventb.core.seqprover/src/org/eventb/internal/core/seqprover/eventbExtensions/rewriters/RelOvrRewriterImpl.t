/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.rewriters;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.AtomicExpression;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.BoolExpression;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Identifier;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.MultiplePredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.UnaryPredicate;
import org.eventb.core.seqprover.eventbExtensions.Lib;

/**
 * Basic automated rewriter for the Event-B sequent prover.
 */
@SuppressWarnings("unused")
public class RelOvrRewriterImpl extends DefaultRewriter {

	private Expression subExp;

	public RelOvrRewriterImpl(Expression subExp) {
		super(true, FormulaFactory.getDefault());
		this.subExp = subExp;
	}
		
	%include {Formula.tom}
	
	@Override
	public Expression rewrite(AssociativeExpression expression) {
	    %match (Expression expression) {

			/**
	    	 * Set Theory : p  ...  q  r  ...  s == 
	    	 *              ((dom(r  ...  s)) ⩤ (p  ...  q)) ∪ (r  ...  s)
	    	 */
			Ovr(children) -> {
				Collection<Expression> pToQ = new ArrayList<Expression>();
				Collection<Expression> rToS = new ArrayList<Expression>();
				FormulaFactory ff = FormulaFactory.getDefault();
				boolean found = false;				
				for (Expression child : `children) {
					if (found)
						rToS.add(child);
					else if (child == subExp) {
						found = true;
						rToS.add(child);
					}
					else {
						pToQ.add(child);
					}
				}
				
				if (pToQ.size() == 0 || rToS.size() == 0)
					return expression;
				
				Expression pToQOvr = makeOvrIfNeccessary(pToQ);
				Expression rToSOvr = makeOvrIfNeccessary(rToS);
				
				Expression dom = ff.makeUnaryExpression(Expression.KDOM, rToSOvr,
						null);
				Expression domSub = ff.makeBinaryExpression(Expression.DOMSUB,
						dom, pToQOvr, null);
						
				return ff.makeAssociativeExpression(Expression.BUNION,
						new Expression[] { domSub, rToSOvr }, null);
			}
			
	    }
	    return expression;
	}

	private Expression makeOvrIfNeccessary(Collection<Expression> children) {
		if (children.size() == 1)
			return children.iterator().next();
		else
			return ff.makeAssociativeExpression(Expression.OVR, children, null);	
	}
}
