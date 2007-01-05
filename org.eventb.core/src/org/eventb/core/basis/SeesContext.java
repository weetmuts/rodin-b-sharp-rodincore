/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.EventBPlugin;
import org.eventb.core.ISCContextFile;
import org.eventb.core.ISeesContext;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.InternalElement;

/**
 * Implementation of Event-B sees relationship as an extension of the Rodin database.
 * <p>
 * This class is intended to be subclassed by clients that want to extend this
 * internal element type.
 * </p>
 * <p>
 * This class should not be used in any other way than subclassing it
 * in a database extension. In particular, clients should not use it,
 * but rather use its associated interface <code>ISeesContext</code>.
 * </p>
 *
 * @author Stefan Hallerstede
 *
 */
public class SeesContext extends InternalElement implements ISeesContext {

	/**
	 *  Constructor used by the Rodin database. 
	 */
	public SeesContext(String name, IRodinElement parent) {
		super(name, parent);
	}

	@Override
	public IInternalElementType<ISeesContext> getElementType() {
		return ELEMENT_TYPE;
	}

	public ISCContextFile getSeenSCContext() throws RodinDBException {
		final String bareName = getSeenContextName();
		final String scName = EventBPlugin.getSCContextFileName(bareName);
		final IRodinProject project = getRodinProject();
		return (ISCContextFile) project.getRodinFile(scName);
	}

	public String getSeenContextName() throws RodinDBException {
		return getAttributeValue(EventBAttributes.TARGET_ATTRIBUTE);
	}

	public void setSeenContextName(String name, IProgressMonitor monitor) 
	throws RodinDBException {
		setAttributeValue(EventBAttributes.TARGET_ATTRIBUTE, name, monitor);
	}

	@Deprecated
	public void setSeenContextName(String name) throws RodinDBException {
		setSeenContextName(name, null);
	}

}
