/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.protosc;

import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author halstefa
 *
 */
public interface IMachineRule {

	public boolean verify(IInternalElement element, MachineCache cache, ISCProblemList problemList) throws RodinDBException;
	
}
