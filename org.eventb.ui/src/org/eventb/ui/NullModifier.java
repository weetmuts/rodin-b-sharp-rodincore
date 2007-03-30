/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.ui;

import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class implements <code>org.eventb.ui.IElementModifier</code>
 *         as a dummy element modifier which doing nothing.
 */
public class NullModifier implements IElementModifier {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.ui.IElementModifier#modify(org.rodinp.core.IRodinElement,
	 *      java.lang.String)
	 */
	public void modify(IRodinElement element, String text)
			throws RodinDBException {
		// Do nothing
	}

}
