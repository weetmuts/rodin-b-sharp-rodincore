/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.basis;

import org.eventb.core.IPRReasonerInput;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.basis.InternalElement;
import org.rodinp.core.basis.UnnamedInternalElement;

/**
 * @author Farhad Mehta
 *
 */
public class PRReasonerInput extends InternalElement implements IPRReasonerInput {

	public PRReasonerInput(String name, IRodinElement parent) {
		super(name, parent);
	}

	@Override
	public String getElementType() {
		return ELEMENT_TYPE;
	}
}
