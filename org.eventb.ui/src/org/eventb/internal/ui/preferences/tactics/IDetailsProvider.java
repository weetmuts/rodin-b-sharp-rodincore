/*******************************************************************************
 * Copyright (c) 2010, 2012 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.preferences.tactics;

import org.eclipse.swt.widgets.Composite;

/**
 * Provides details concerning an selected element. The details are a list of
 * strings.
 * 
 * @see DetailedList#setDetailsProvider(IDetailsProvider)
 */
public interface IDetailsProvider {

	/**
	 * Sets the parent composite in which to put the details.
	 * 
	 * @param parent
	 *            the parent details composite
	 */
	void setParentComposite(Composite parent);

	/**
	 * Puts the details of the given element to be displayed in the given parent
	 * composite.
	 * 
	 * @param element
	 *            the element to retrieve details from
	 */
	void putDetails(String element);

	/**
	 * Clears all details
	 */
	void clear();
	
	/**
	 * Returns whether current details are different from saved ones.
	 * 
	 * @return <code>true</code> iff details are different from saved ones
	 */
	public boolean hasChanges();
	
	/**
	 * If details changed, display a popup that proposes to save changes.
	 */
	void save();
}
