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
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachine;
import org.eventb.core.ISees;
import org.eventb.core.ITheorem;
import org.eventb.core.IVariable;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.RodinFile;

/**
 * Implementation of Event-B (unchecked) machines as an extension of the Rodin database.
 * <p>
 * This class is intended to be implemented by clients that want to extend this
 * file element type.
 * </p>
 * <p>
 * This class should not be used in any other way than subclassing it
 * in a database extension. In particular, clients should not use it,
 * but rather use its associated interface <code>IMachine</code>.
 * </p>
 *
 * @author Laurent Voisin
 */
public class Machine extends RodinFile implements IMachine {
	
	/**
	 *  Constructor used by the Rodin database. 
	 */
	public Machine(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	@Override
	public String getElementType() {
		return ELEMENT_TYPE;
	}
	
	public IVariable[] getVariables() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(IVariable.ELEMENT_TYPE);
		Variable[] variables = new Variable[list.size()];
		list.toArray(variables);
		return variables; 
	}
	public ITheorem[] getTheorems() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(ITheorem.ELEMENT_TYPE);
		Theorem[] theorems = new Theorem[list.size()];
		list.toArray(theorems);
		return theorems; 
	}
	
	public IInvariant[] getInvariants() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(IInvariant.ELEMENT_TYPE);
		Invariant[] invariants = new Invariant[list.size()];
		list.toArray(invariants);
		return invariants; 
	}
	
	public IEvent[] getEvents() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(IEvent.ELEMENT_TYPE);
		Event[] events = new Event[list.size()];
		list.toArray(events);
		return events; 
	}
	
	public ISees[] getSees() throws RodinDBException {
		ArrayList<IRodinElement> list = getChildrenOfType(ISees.ELEMENT_TYPE);
		
		// for now:
		assert list.size() <= 1;
		
		Sees[] sees = new Sees[list.size()];
		list.toArray(sees);
		return sees; 
	}
}
