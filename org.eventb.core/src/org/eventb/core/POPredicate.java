/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core;

import org.rodinp.core.RodinElement;

/**
 * @author halstefa
 *
 * A predicate has a name associated as its attribute 
 * and the "predicate value" in the contents. 
 */
public class POPredicate extends POAnyPredicate {

	public POPredicate(RodinElement parent) {
		super(ELEMENT_TYPE, parent);
		// TODO Auto-generated constructor stub
	}

	public static final String ELEMENT_TYPE = EventBPlugin.PLUGIN_ID + ".poPredicate";
	
	/* (non-Javadoc)
	 * @see org.rodinp.core.RodinElement#getElementType()
	 */
	@Override
	public String getElementType() {
		return ELEMENT_TYPE;
	}
	
	public String getName() {
		return null;
	}

}
