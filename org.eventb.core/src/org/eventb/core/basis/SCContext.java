/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eventb.core.IAxiom;
import org.eventb.core.ISCAxiomSet;
import org.eventb.core.ISCContext;
import org.eventb.core.ISCTheoremSet;
import org.eventb.core.ITheorem;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author halstefa
 *
 */
public class SCContext extends Context implements ISCContext {

	/**
	 *  Constructor used by the Rodin database. 
	 */
	public SCContext(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.RodinElement#getElementType()
	 */
	@Override
	public String getElementType() {
		return ISCContext.ELEMENT_TYPE;
	}

	public IAxiom[] getOldAxioms() throws RodinDBException {
		ArrayList<IRodinElement> axiomSetList = getChildrenOfType(ISCAxiomSet.ELEMENT_TYPE);
		
		assert axiomSetList.size() == 1;
		
		ISCAxiomSet axiomSet = (ISCAxiomSet) axiomSetList.get(0);
		return axiomSet.getAxioms();
	}

	public ITheorem[] getOldTheorems() throws RodinDBException {
		ArrayList<IRodinElement> theoremSetList = getChildrenOfType(ISCTheoremSet.ELEMENT_TYPE);
		
		assert theoremSetList.size() == 1;
		
		ISCTheoremSet theoremSet = (ISCTheoremSet) theoremSetList.get(0);
		return theoremSet.getTheorems();
	}

}
