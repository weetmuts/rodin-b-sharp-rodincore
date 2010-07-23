/*******************************************************************************
 * Copyright (c) 2006, 2010 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.seqprover.eventbExtensions.rewriters;

import static org.eventb.core.seqprover.eventbExtensions.DLib.mDLib;
import static org.eventb.core.seqprover.eventbExtensions.Lib.eqLeft;
import static org.eventb.core.seqprover.eventbExtensions.Lib.eqRight;
import static org.eventb.core.seqprover.eventbExtensions.Lib.getSet;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isEmptySet;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isEq;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isInclusion;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isNotEq;
import static org.eventb.core.seqprover.eventbExtensions.Lib.isNotInclusion;
import static org.eventb.core.seqprover.eventbExtensions.Lib.notEqLeft;
import static org.eventb.core.seqprover.eventbExtensions.Lib.notEqRight;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.DLib;

/**
 * @author fmehta
 * @deprecated this should be done in {@link AutoRewrites}
 */
@Deprecated
public class TypePredRewriter implements Rewriter{

	public String getRewriterID() {
		return "typePredRewriter";
	}
	
	public String getName() {
		return "type predicate";
	}
	
	public boolean isApplicable(Predicate p) {
		if (isNotEq(p)) {
			if (isEmptySet(notEqRight(p)) &&
					notEqLeft(p).isATypeExpression())
				return true;
			if (isEmptySet(notEqLeft(p)) &&
					notEqRight(p).isATypeExpression())
				return true;
		}
		
		if (isEq(p)) {
			if (isEmptySet(eqRight(p)) &&
					eqLeft(p).isATypeExpression())
				return true;
			if (isEmptySet(eqLeft(p)) &&
					eqRight(p).isATypeExpression())
				return true;
		}
			
		if (isInclusion(p) && getSet(p).isATypeExpression())
			return true;
		
		if (isNotInclusion(p) && getSet(p).isATypeExpression())
			return true;
		
		return false;
	}

	public Predicate apply(Predicate p, FormulaFactory ff) {
		final DLib lib = mDLib(ff);
		// Ty is a type expression (NAT, BOOL, carrierset, Pow(Ty), etc)
		// t is an expression of type Ty
		
		//  Ty/={} <OR> {}/=Ty  ==  T
		if (isNotEq(p)) {
			if (isEmptySet(notEqRight(p)) &&
					notEqLeft(p).isATypeExpression())
				return lib.True();
			if (isEmptySet(notEqLeft(p)) &&
					notEqRight(p).isATypeExpression())
				return lib.True();
		}
		
		//	  Ty={} <OR> {}=Ty  ==  F
		if (isEq(p)) {
			if (isEmptySet(eqRight(p)) &&
					eqLeft(p).isATypeExpression())
				return lib.False();
			if (isEmptySet(eqLeft(p)) &&
					eqRight(p).isATypeExpression())
				return lib.False();
		}
		
		// t : Ty  == T
		if (isInclusion(p) && getSet(p).isATypeExpression())
			return lib.True();
		
		// t /: Ty  == F
		if (isNotInclusion(p) && getSet(p).isATypeExpression())
			return lib.False();
		
		
		return null;
	}

}
