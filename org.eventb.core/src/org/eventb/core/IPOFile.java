/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.core;

import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

/**
 * Common protocol for Event-B Proof Obligation (PO) files.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @author Stefan Hallerstede
 *
 */
public interface IPOFile extends IRodinFile {

	public String ELEMENT_TYPE = EventBPlugin.PLUGIN_ID + ".poFile"; //$NON-NLS-1$
	
	/**
	 * Returns a handle to the checked version of the context for which this
	 * proof obligation file has been generated.
	 * <p>
	 * This is a handle-only operation.
	 * </p>
	 * 
	 * @return a handle to the checked version of the corresponding context
	 */
	public ISCContextFile getSCContext();

	/**
	 * Returns a handle to the checked version of the machine for which this
	 * proof obligation file has been generated.
	 * <p>
	 * This is a handle-only operation.
	 * </p>
	 * 
	 * @return a handle to the checked version of the corresponding machine
	 */
	public ISCMachineFile getSCMachine();

	/**
	 * Returns a handle to the file containing proofs for this component.
	 * <p>
	 * This is a handle-only operation.
	 * </p>
	 * 
	 * @return a handle to the proof file of this component
	 */
	public IPRFile getPRFile();

	/**
	 * Returns a handle to the predicate set with the given name.
	 * 
	 * @param name
	 *            the name of the predicate set
	 * @return a handle to the predicate set or <code>null</code> there is no
	 *         predicate set witrh given name
	 * @throws RodinDBException if there was a problem accessing the database
	 */
	public IPOPredicateSet getPredicateSet(String name) throws RodinDBException;
	
	@Deprecated
	public IPOIdentifier[] getIdentifiers() throws RodinDBException;
	
	/**
	 * Returns handles to the proof obligations of this component. 
	 * 
	 * @return the array of handles to the proof obligations
	 * @throws RodinDBException if there was a problem accessing the database
	 */
	public IPOSequent[] getSequents() throws RodinDBException;
	
	// TODO : a method that returns a sequent with a particular name
	// 			or null if no suc sequent exists
	// public IPOSequent getSequent(String name) throws RodinDBException;
}
