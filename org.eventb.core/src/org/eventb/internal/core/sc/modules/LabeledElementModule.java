/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.sc.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ILabeledElement;
import org.eventb.core.sc.ProcessorModule;
import org.eventb.core.sc.state.ILabelSymbolTable;
import org.eventb.core.sc.state.IStateSC;
import org.eventb.core.sc.symbolTable.ILabelSymbolInfo;
import org.eventb.core.state.IStateRepository;
import org.eventb.internal.core.sc.symbolTable.SymbolInfoFactory;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;

/**
 * @author Stefan Hallerstede
 *
 */
public abstract class LabeledElementModule extends ProcessorModule {

	ILabelSymbolTable labelSymbolTable;
	
	@Override
	public void initModule(
			IRodinElement element, 
			IStateRepository<IStateSC> repository, 
			IProgressMonitor monitor) throws CoreException {
		labelSymbolTable = getLabelSymbolTableFromRepository(repository);
	}
	
	@Override
	public void endModule(
			IRodinElement element, 
			IStateRepository<IStateSC> repository, 
			IProgressMonitor monitor) throws CoreException {
		labelSymbolTable = null;
	}

	protected abstract ILabelSymbolTable getLabelSymbolTableFromRepository(
			IStateRepository<IStateSC> repository) throws CoreException;

	/**
	 * Adds a new label symbol to the label symbol table.
	 * Returns the new symbol info created if the label is not already in use,
	 * and <code>null</code> otherwise.
	 * 
	 * @param internalElement the labeled element
	 * @return the new label symbol
	 * @throws CoreException if there was a problem with the database or the symbol table
	 */
	protected ILabelSymbolInfo fetchLabel(
			IInternalElement internalElement, 
			String component,
			IProgressMonitor monitor) throws CoreException {
		
		ILabeledElement labeledElement = (ILabeledElement) internalElement;
		
		String label = labeledElement.getLabel(monitor);
		
		ILabelSymbolInfo newSymbolInfo = 
			SymbolInfoFactory.createLabelSymbolInfo(label, labeledElement, component);
		
		try {
			
			labelSymbolTable.putSymbolInfo(newSymbolInfo);
			
		} catch (CoreException e) {
			
			ILabelSymbolInfo symbolInfo = 
				(ILabelSymbolInfo) labelSymbolTable.getSymbolInfo(label);
			
			newSymbolInfo.createConflictMarker(this);
			
			if(symbolInfo.hasError())
				return null; // do not produce too many error messages
			
			symbolInfo.createConflictMarker(this);
			
			if (symbolInfo.isMutable())
				symbolInfo.setError();
			
			return null;
	
		}
	
		return newSymbolInfo;
	}

}
