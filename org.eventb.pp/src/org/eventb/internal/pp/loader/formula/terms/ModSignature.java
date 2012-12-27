/*******************************************************************************
 * Copyright (c) 2006, 2012 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.pp.loader.formula.terms;

import java.util.ArrayList;
import java.util.List;

import org.eventb.internal.pp.core.elements.terms.Mod;
import org.eventb.internal.pp.core.elements.terms.Term;
import org.eventb.internal.pp.loader.formula.ClauseContext;

public class ModSignature extends BinaryTermSignature {

	public ModSignature(TermSignature left, TermSignature right) {
		super(left, right);
	}
	
	protected ModSignature(List<TermSignature> terms) {
		super(terms);
		assert terms.size() == 2;
	}

	@Override
	public TermSignature deepCopy() {
		return new ModSignature(deepCopyHelper());
	}

	@Override
	public TermSignature getUnquantifiedTerm(int startOffset, int endOffset, List<TermSignature> termList) {
		if (isQuantified(startOffset,endOffset)) {
			return new ModSignature(super.getUnquantifiedTermHelper(startOffset, endOffset, termList));
		} else {
			addTermCopy(this, termList);
			return new VariableHolder(sort);
		}	
	}
	
	@Override
	public TermSignature getSimpleTerm(List<TermSignature> termList) {
		return new ModSignature(super.getSimpleTermHelper(termList));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof ModSignature) {
			ModSignature temp = (ModSignature) obj;
			return super.equals(temp);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + ("[O]").hashCode();
	}

	@Override
	public String toString() {
		return "mod " + super.toString();
	}
	
	@Override
	public void appendTermFromTermList(List<TermSignature> indexList, List<TermSignature> newList, int startOffset, int endOffset) {
		List<TermSignature> terms = new ArrayList<TermSignature>();
		super.appendTermFromTermList(indexList, terms, startOffset, endOffset);
		TermSignature term = new ModSignature(terms);
		newList.add(term);
	}
	
	@Override
	public Term getTerm(ClauseContext context) {
		return new Mod(super.getTermHelper(context));
	}
}
