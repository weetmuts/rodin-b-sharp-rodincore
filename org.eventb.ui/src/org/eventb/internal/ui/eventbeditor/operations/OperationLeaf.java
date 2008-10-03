/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.operations;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.rodinp.core.IInternalElement;

abstract class OperationLeaf extends AbstractOperation implements OperationTree {

	private ArrayList<IInternalElement> createdElements;

	public OperationLeaf(String label) {
		super(label);
		createdElements = new ArrayList<IInternalElement>();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Empty Collection
	 */
	public Collection<IInternalElement> getCreatedElements() {
		return createdElements;
	}

	/**
	 * @return null
	 */
	public IInternalElement getCreatedElement() {
		return null;
	}
}
