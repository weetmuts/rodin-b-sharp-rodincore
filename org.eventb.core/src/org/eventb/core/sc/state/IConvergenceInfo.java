/*******************************************************************************
 * Copyright (c) 2006, 2013 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.core.sc.state;

import org.eventb.core.IConvergenceElement;


/**
 * A convergence info provides information about the convergence of an event.
 * 
 * @see IAbstractEventInfo
 * @author Stefan Hallerstede
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConvergenceInfo extends ISCState {
	
	/**
	 * Returns the convergence of the event that is represented by this convergence info.
	 * 
	 * @return the convergence of the event that is represented by this convergence info
	 */
	IConvergenceElement.Convergence getConvergence();
	
}
