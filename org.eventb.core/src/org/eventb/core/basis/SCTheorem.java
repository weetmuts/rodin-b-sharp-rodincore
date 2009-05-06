/*******************************************************************************
 * Copyright (c) 2005, 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core.basis;

import org.eventb.core.ISCTheorem;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;

/**
 * Implementation of Event-B SC theorems as an extension of the Rodin database.
 * <p>
 * This class is intended to be subclassed by clients that want to extend this
 * internal element type.
 * </p>
 * <p>
 * This class should not be used in any other way than subclassing it in a
 * database extension. In particular, clients should not use it, but rather use
 * its associated interface <code>ISCTheorem</code>.
 * </p>
 * 
 * @author Stefan Hallerstede
 *
 */
@Deprecated
public class SCTheorem extends SCPredicateElement implements ISCTheorem {

	/**
	 *  Constructor used by the Rodin database. 
	 */
	public SCTheorem(String name, IRodinElement parent) {
		super(name, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.IRodinElement#getElementType()
	 */
	@Override
	public IInternalElementType<ISCTheorem> getElementType() {
		return ELEMENT_TYPE;
	}

}
