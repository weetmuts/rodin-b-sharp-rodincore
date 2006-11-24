/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B convergence properties of an event.
 * <p>
 * A convergence is represented by an integer constant. Only the specified values
 * {@link Convergence#ORDINARY}, {@link Convergence#CONVERGENT}, and
 * {@link Convergence#ANTICIPATED} are permitted.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @author Stefan Hallerstede
 *
 */
public interface IConvergenceElement extends IInternalElement {

	enum Convergence {
		ORDINARY,
		CONVERGENT,
		ANTICIPATED
	}
	
	/**
	 * Sets the convergence to one of the values {@link Convergence#ORDINARY}, 
	 * {@link Convergence#CONVERGENT}, or {@link Convergence#ANTICIPATED}.
	 * @param value the convergence to be set
	 * @param monitor 
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @throws RodinDBException if there was a problem accessing the database
	 */
	void setConvergence(Convergence value, IProgressMonitor monitor) throws RodinDBException;
	
	/**
	 * Returns the convergence stored in this attribute.
	 * 
	 * @return the convergence; one of  {@link Convergence#ORDINARY}, 
	 * {@link Convergence#CONVERGENT}, {@link Convergence#ANTICIPATED}
	 * @throws RodinDBException if there was a problem accessing the database
	 */
	Convergence getConvergence() throws RodinDBException;
}
