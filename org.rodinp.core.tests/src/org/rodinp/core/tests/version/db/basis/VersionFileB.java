/*******************************************************************************
 * Copyright (c) 2006, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - separation of file and root element
 *******************************************************************************/
package org.rodinp.core.tests.version.db.basis;

import org.eclipse.core.resources.IFile;
import org.rodinp.core.IFileElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.basis.RodinFile;
import org.rodinp.core.tests.version.db.IVersionFileB;

/**
 * @author Stefan Hallerstede
 *
 */
@Deprecated
public class VersionFileB extends RodinFile implements IVersionFileB {

	public VersionFileB(IFile file, IRodinElement parent) {
		super(file, parent);
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.basis.RodinFile#getElementType()
	 */
	@Override
	public IFileElementType getElementType() {
		// TODO Auto-generated method stub
		return ELEMENT_TYPE;
	}

}
