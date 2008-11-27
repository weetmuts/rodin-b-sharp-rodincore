/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
  *******************************************************************************/


package fr.systerel.explorer.model;

import org.eventb.core.IPSStatus;
import org.eventb.core.ITheorem;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;

/**
 * Represents a Theorem in the Model
 *
 */
public class ModelTheorem extends ModelPOContainer {
	public ModelTheorem(ITheorem theorem, IModelElement parent){
		internalTheorem = theorem;
		this.parent =  parent;
	}

	private ITheorem internalTheorem;
	
	
	public ITheorem getInternalTheorem() {
		return internalTheorem;
	}

	public IRodinElement getInternalElement() {
		return internalTheorem;
	}

	public Object getParent(boolean complex) {
		if (parent instanceof ModelContext ) {
			return ((ModelContext) parent).theorem_node;
		}
		if (parent instanceof ModelMachine ) {
			return ((ModelMachine) parent).theorem_node;
		}
		return parent;
	}
	
	public Object[] getChildren(IInternalElementType<?> type, boolean complex) {
		if (type != IPSStatus.ELEMENT_TYPE) {
			return new Object[0];
		}
		return getIPSStatuses();
	}

	
}
