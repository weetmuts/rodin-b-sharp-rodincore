/*******************************************************************************
 * Copyright (c) 2005, 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.basis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ISCAxiom;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * Implementation of Event-B SC axioms as an extension of the Rodin database.
 * <p>
 * This class is intended to be subclassed by clients that want to extend this
 * internal element type.
 * </p>
 * <p>
 * This class should not be used in any other way than subclassing it in a
 * database extension. In particular, clients should not use it, but rather use
 * its associated interface <code>ISCAxiom</code>.
 * </p>
 * 
 * @author Stefan Hallerstede
 */
public class SCAxiom extends SCPredicateElement implements ISCAxiom {

	/**
	 *  Constructor used by the Rodin database. 
	 */
	public SCAxiom(String name, IRodinElement parent) {
		super(name, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.IRodinElement#getElementType()
	 */
	@Override
	public String getElementType() {
		return ELEMENT_TYPE;
	}

	public void setBag(String bag, IProgressMonitor monitor) throws RodinDBException {
		CommonAttributesUtil.setBag(this, bag, monitor);
	}

	public String getBag(IProgressMonitor monitor) throws RodinDBException {
		return CommonAttributesUtil.getBag(this, monitor);
	}

}
