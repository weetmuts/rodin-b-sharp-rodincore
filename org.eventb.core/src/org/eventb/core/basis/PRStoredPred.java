/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPRIdentifier;
import org.eventb.core.IPRStoredPred;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Farhad Mehta
 *
 */

public class PRStoredPred extends SCPredicateElement implements IPRStoredPred {

	public PRStoredPred(String name, IRodinElement parent) {
		super(name, parent);
	}

	@Override
	public IInternalElementType<IPRStoredPred> getElementType() {
		return ELEMENT_TYPE;
	}
	
	public FreeIdentifier[] getFreeIdents(FormulaFactory factory) throws RodinDBException {
		IRodinElement[] children = getChildrenOfType(IPRIdentifier.ELEMENT_TYPE);
		FreeIdentifier[] freeIdents = new FreeIdentifier[children.length];
		for (int i = 0; i < freeIdents.length; i++) {
			freeIdents[i] = ((IPRIdentifier)children[i]).getIdentifier(factory);			
		}
		return freeIdents;
	}
	
	public void setFreeIdents(FreeIdentifier[] freeIdents, IProgressMonitor monitor) throws RodinDBException {
		
		for (int i = 0; i < freeIdents.length; i++) {
			IPRIdentifier prIdent = getInternalElement(
					IPRIdentifier.ELEMENT_TYPE, freeIdents[i].getName());
			prIdent.create(null, monitor);
			prIdent.setType(freeIdents[i].getType(), monitor);
		}
	}


}
