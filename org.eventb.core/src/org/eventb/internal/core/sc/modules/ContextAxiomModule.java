/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.sc.modules;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IAxiom;
import org.eventb.core.IContextFile;
import org.eventb.core.ILabeledElement;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCContextFile;
import org.eventb.core.sc.IFilterModule;
import org.eventb.core.sc.IModuleManager;
import org.eventb.core.sc.state.IContextLabelSymbolTable;
import org.eventb.core.sc.state.ILabelSymbolTable;
import org.eventb.core.sc.state.IStateSC;
import org.eventb.core.sc.symbolTable.ILabelSymbolInfo;
import org.eventb.core.state.IStateRepository;
import org.eventb.internal.core.sc.Messages;
import org.eventb.internal.core.sc.ModuleManager;
import org.eventb.internal.core.sc.symbolTable.AxiomSymbolInfo;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Stefan Hallerstede
 *
 */
public class ContextAxiomModule extends PredicateWithTypingModule<IAxiom> {

	public static final String CONTEXT_AXIOM_FILTER = 
		EventBPlugin.PLUGIN_ID + ".contextAxiomFilter";

	private final IFilterModule[] filterModules;

	public ContextAxiomModule() {
		IModuleManager manager = ModuleManager.getModuleManager();
		filterModules = 
			manager.getFilterModules(CONTEXT_AXIOM_FILTER);
	}

	private static String AXIOM_NAME_PREFIX = "AXM";

	/* (non-Javadoc)
	 * @see org.eventb.core.sc.IProcessorModule#process(org.rodinp.core.IRodinElement, org.rodinp.core.IInternalParent, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void process(
			IRodinElement element, 
			IInternalParent target,
			IStateRepository<IStateSC> repository,
			IProgressMonitor monitor)
			throws CoreException {
		
		monitor.subTask(Messages.bind(Messages.progress_ContextAxioms));
		
		if (formulaElements.size() == 0)
			return;
		
		checkAndType(
				target, 
				filterModules,
				element.getElementName(),
				repository,
				monitor);
		
		saveAxioms((ISCContextFile) target, null);
		
	}
	
	private void saveAxioms(
			ISCContextFile target, 
			IProgressMonitor monitor) throws RodinDBException {
		
		int index = 0;
		
		for (int i=0; i<formulaElements.size(); i++) {
			if (formulas.get(i) == null)
				continue;
			ISCAxiom scAxiom = target.getSCAxiom(AXIOM_NAME_PREFIX + index++);
			scAxiom.create(null, monitor);
			scAxiom.setLabel(formulaElements.get(i).getLabel(), monitor);
			scAxiom.setPredicate(formulas.get(i), null);
			scAxiom.setSource(formulaElements.get(i), monitor);
		}
	}

	@Override
	protected void makeProgress(IProgressMonitor monitor) {
		monitor.worked(1);
	}
	/* (non-Javadoc)
	 * @see org.eventb.internal.core.sc.modules.LabeledElementModule#getLabelSymbolTableFromRepository(org.eventb.core.sc.IStateRepository)
	 */
	@Override
	protected ILabelSymbolTable getLabelSymbolTableFromRepository(
			IStateRepository repository) throws CoreException {
		return (ILabelSymbolTable) repository.getState(IContextLabelSymbolTable.STATE_TYPE);
	}

	@Override
	protected ILabelSymbolInfo createLabelSymbolInfo(
			String symbol, ILabeledElement element, String component) throws CoreException {
		return new AxiomSymbolInfo(symbol, element, EventBAttributes.LABEL_ATTRIBUTE, component);
	}

	@Override
	protected List<IAxiom> getFormulaElements(IRodinElement element) throws CoreException {
		IContextFile contextFile = (IContextFile) element;
		IAxiom[] axioms = contextFile.getAxioms();
		return Arrays.asList(axioms);
	}

}
