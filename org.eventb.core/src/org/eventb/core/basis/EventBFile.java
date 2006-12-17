/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import org.eclipse.core.resources.IFile;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IContextFile;
import org.eventb.core.IEventBFile;
import org.eventb.core.IMachineFile;
import org.eventb.core.IPOFile;
import org.eventb.core.IPRFile;
import org.eventb.core.IPSFile;
import org.eventb.core.ISCContextFile;
import org.eventb.core.ISCMachineFile;
import org.rodinp.core.IElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.RodinFile;

/**
 * Common implementation for event-B files.
 * 
 * @author Stefan Hallerstede
 * @author Laurent Voisin
 */
public abstract class EventBFile extends RodinFile implements IEventBFile {

	protected EventBFile(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	public final String getComponentName() {
		return EventBPlugin.getComponentName(getElementName());
	}

	public final IContextFile getContextFile() {
		final String name = EventBPlugin.getContextFileName(getComponentName());
		return (IContextFile) getRodinProject().getRodinFile(name);
	}

	public final IMachineFile getMachineFile() {
		final String name = EventBPlugin.getMachineFileName(getComponentName());
		return (IMachineFile) getRodinProject().getRodinFile(name);
	}

	public final IPRFile getPRFile() {
		final String name = EventBPlugin.getPRFileName(getComponentName());
		return (IPRFile) getRodinProject().getRodinFile(name);
	}

	public final ISCContextFile getSCContextFile() {
		final String name = EventBPlugin.getSCContextFileName(getComponentName());
		return (ISCContextFile) getRodinProject().getRodinFile(name);
	}

	public final ISCMachineFile getSCMachineFile() {
		final String name = EventBPlugin.getSCMachineFileName(getComponentName());
		return (ISCMachineFile) getRodinProject().getRodinFile(name);
	}

	public final IPOFile getPOFile() {
		final String name = EventBPlugin.getPOFileName(getComponentName());
		return (IPOFile) getRodinProject().getRodinFile(name);
	}

	public final IPSFile getPSFile() {
		final String name = EventBPlugin.getPSFileName(getComponentName());
		return (IPSFile) getRodinProject().getRodinFile(name);
	}

	@Deprecated
	protected final IRodinElement getSingletonChild(IElementType elementType,
			String message) throws RodinDBException {

		return EventBUtil.getSingletonChild(this, elementType, message);
	}

}
