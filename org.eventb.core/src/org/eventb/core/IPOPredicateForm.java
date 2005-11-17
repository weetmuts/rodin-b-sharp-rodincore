/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core;

import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author halstefa
 *
 */
public interface IPOPredicateForm extends IUnnamedInternalElement {
	String ELEMENT_TYPE = EventBPlugin.PLUGIN_ID + ".poPredicateForm";
	
	public String getSubstitution() throws RodinDBException;
	public IPOAnyPredicate getPredicate() throws RodinDBException;
}
