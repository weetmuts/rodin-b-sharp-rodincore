/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.basis;

import java.util.ArrayList;

import org.eventb.core.ISCTheoremSet;
import org.eventb.core.ITheorem;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.UnnamedInternalElement;

/**
 * @author halstefa
 *
 */
public class SCTheoremSet extends UnnamedInternalElement implements ISCTheoremSet {

	public SCTheoremSet(String type, IRodinElement parent) {
		super(type, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.RodinElement#getElementType()
	 */
	@Override
	public String getElementType() {
		return ELEMENT_TYPE;
	}
	
	public Theorem[] getTheorems() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(ITheorem.ELEMENT_TYPE);
		Theorem[] theorems = new Theorem[list.size()];
		list.toArray(theorems);
		return theorems;
	}

}
