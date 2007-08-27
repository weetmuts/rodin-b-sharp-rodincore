/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.core.tests.version.db.basis;

import org.eclipse.core.resources.IFile;
import org.rodinp.core.IFileElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.basis.RodinFile;
import org.rodinp.core.tests.version.db.IVersionFileB;

/**
 * @author Stefan Hallerstede
 *
 */
public class VersionFileB extends RodinFile implements IVersionFileB {

	public VersionFileB(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.basis.RodinFile#getElementType()
	 */
	@Override
	public IFileElementType<? extends IRodinFile> getElementType() {
		// TODO Auto-generated method stub
		return ELEMENT_TYPE;
	}

}
