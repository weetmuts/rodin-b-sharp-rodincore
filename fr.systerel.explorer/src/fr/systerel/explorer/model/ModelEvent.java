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

import org.eventb.core.IEvent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * Represents an Event in the Model
 *
 */
public class ModelEvent extends ModelPOContainer {
	public ModelEvent(IEvent event, IModelElement parent){
		internalEvent = event;
		this.parent = parent;
	}

	private IEvent internalEvent;
	
	
	public IEvent getInternalEvent() {
		return internalEvent;
	}

	@Override
	public String getLabel() {
		try {
			return "Event " +internalEvent.getLabel();
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Eventt ??";
	}

	public IRodinElement getInternalElement() {
		return internalEvent;
	}
	
}
