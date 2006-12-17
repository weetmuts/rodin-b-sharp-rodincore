/*******************************************************************************
 * Copyright (c) 2005-2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import org.eclipse.core.resources.IFile;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCCarrierSet;
import org.eventb.core.ISCConstant;
import org.eventb.core.ISCContextFile;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ISCTheorem;
import org.rodinp.core.IFileElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * Implementation of Event-B statically checked contexts as an extension of the
 * Rodin database.
 * <p>
 * This class should not be used directly by any client except the Rodin
 * database. In particular, clients should not use it, but rather use its
 * associated interface <code>IContextFile</code>.
 * </p>
 * 
 * @author Stefan Hallerstede
 */
public class SCContextFile extends EventBFile implements ISCContextFile {

	/**
	 *  Constructor used by the Rodin database. 
	 */
	public SCContextFile(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	@Override
	public IFileElementType getElementType() {
		return ELEMENT_TYPE;
	}

	public ISCCarrierSet[] getSCCarrierSets() 
	throws RodinDBException {
		IRodinElement[] elements = getChildrenOfType(ISCCarrierSet.ELEMENT_TYPE);
		return (ISCCarrierSet[]) elements; 
	}
	
	public ISCConstant[] getSCConstants() throws RodinDBException {
		IRodinElement[] elements = getChildrenOfType(ISCConstant.ELEMENT_TYPE);
		return (ISCConstant[]) elements; 
	}

	public ISCAxiom[] getSCAxioms() throws RodinDBException {
		IRodinElement[] elements = getChildrenOfType(ISCAxiom.ELEMENT_TYPE);
		return (ISCAxiom[]) elements; 
	}

	public ISCTheorem[] getSCTheorems() throws RodinDBException {
		IRodinElement[] elements = getChildrenOfType(ISCTheorem.ELEMENT_TYPE);
		return (ISCTheorem[]) elements; 
	}

	public ISCInternalContext[] getAbstractSCContexts() throws RodinDBException {
		IRodinElement[] elements = getChildrenOfType(ISCInternalContext.ELEMENT_TYPE);
		return (ISCInternalContext[]) elements; 
	}
	
	public ISCInternalContext getSCInternalContext(String elementName) {
		return (ISCInternalContext) getInternalElement(ISCInternalContext.ELEMENT_TYPE, elementName);
	}

	public ISCAxiom getSCAxiom(String elementName) {
		return (ISCAxiom) getInternalElement(ISCAxiom.ELEMENT_TYPE, elementName);
	}

	public ISCCarrierSet getSCCarrierSet(String elementName) {
		return (ISCCarrierSet) getInternalElement(ISCCarrierSet.ELEMENT_TYPE, elementName);
	}

	public ISCConstant getSCConstant(String elementName) {
		return (ISCConstant) getInternalElement(ISCConstant.ELEMENT_TYPE, elementName);
	}

	public ISCTheorem getSCTheorem(String elementName) {
		return (ISCTheorem) getInternalElement(ISCTheorem.ELEMENT_TYPE, elementName);
	}

}
