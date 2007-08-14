/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.internal.pp.core.elements;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import org.eventb.internal.pp.core.elements.terms.SimpleTerm;

public abstract class PredicateLiteral extends Literal<PredicateLiteral,SimpleTerm> {

	final protected PredicateLiteralDescriptor descriptor;
	final protected boolean isPositive;
	
	public PredicateLiteral(PredicateLiteralDescriptor descriptor, boolean isPositive, List<SimpleTerm> terms) {
		super(terms, 37*descriptor.hashCode()+(isPositive?0:1));
		
		this.descriptor = descriptor;
		this.isPositive = isPositive;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PredicateLiteral) {
			PredicateLiteral temp = (PredicateLiteral) obj;
			return descriptor.equals(temp.descriptor) && isPositive == temp.isPositive && super.equals(obj);
		}
		return false;
	}
	
	@Override
	public boolean equalsWithDifferentVariables(PredicateLiteral literal, HashMap<SimpleTerm, SimpleTerm> map) {
		return descriptor.equals(literal.descriptor) && isPositive == literal.isPositive && super.equalsWithDifferentVariables(literal, map);
	}
	
	public boolean isPositive() {
		return isPositive;
	}

	public void setBit(BitSet set) {
		set.set(descriptor.getIndex());
	}

	public PredicateLiteralDescriptor getDescriptor() {
		return descriptor;
	}
	
}