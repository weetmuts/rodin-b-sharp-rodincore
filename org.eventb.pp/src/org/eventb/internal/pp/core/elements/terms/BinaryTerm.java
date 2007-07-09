/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.internal.pp.core.elements.terms;

import java.util.Arrays;
import java.util.List;


public abstract class BinaryTerm extends AssociativeTerm {
	
	public BinaryTerm(Term left, Term right, int priority) {
		super(Arrays.asList(new Term[]{left, right}), priority);
		
		assert left != null && right != null;
		
	}
	
	protected BinaryTerm(List<Term> terms, int priority) {
		super(terms, priority);
	}
	
	public Term getLeft() {
		return children.get(0);
	}
	
	public Term getRight() {
		return children.get(1);
	}

}
