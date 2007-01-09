/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.sc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IEventBFile;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.eventb.core.sc.state.IStateRepository;
import org.eventb.core.sc.util.IMarkerDisplay;
import org.eventb.core.tool.IToolModule;
import org.eventb.internal.core.sc.StaticChecker;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProblem;
import org.rodinp.core.RodinDBException;

/**
 * 
 * Default implementation for modules.
 * 
 * @see org.eventb.core.tool.IToolModule
 * 
 * @author Stefan Hallerstede
 *
 */
public abstract class Module implements IToolModule, IMarkerDisplay {
	
	private void traceMarker(IRodinElement element, String message) {
		
		String name = element.getElementName();
		
		try {
			if (element instanceof ILabeledElement)
				name = ((ILabeledElement) element).getLabel();
			else if (element instanceof IIdentifierElement)
				name = ((IIdentifierElement) element).getIdentifierString();
			else if (element instanceof IEventBFile)
				name = ((IEventBFile) element).getBareName();
		} catch (RodinDBException e) {
			// ignore
		} finally {
		
			System.out.println("SC MARKER: " + name + ": " + message);
		}
	}
	
	public void createProblemMarker(
			IRodinElement element, 
			IRodinProblem problem, 
			Object... args)
		throws RodinDBException {
		if (StaticChecker.DEBUG_MARKERS)
			traceMarker(element, problem.getLocalizedMessage(args));

		element.createProblemMarker(problem, args);
	}
	
	public void createProblemMarker(IInternalElement element,
			IAttributeType attributeType, IRodinProblem problem,
			Object... args) throws RodinDBException {
		if (StaticChecker.DEBUG_MARKERS)
			traceMarker(element, problem.getLocalizedMessage(args));

		element.createProblemMarker(attributeType, problem, args);
	}

	public void createProblemMarker(IInternalElement element,
			IAttributeType.String attributeType, int charStart, int charEnd,
			IRodinProblem problem, Object... args) throws RodinDBException {
		if (StaticChecker.DEBUG_MARKERS)
			traceMarker(element, problem.getLocalizedMessage(args));

		element.createProblemMarker(attributeType, charStart, charEnd+1, problem,
				args);
	}
	
	protected void initFilterModules(
			IFilterModule[] modules,
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IFilterModule module : modules) {
			module.initModule(repository, monitor);
		}
	}
	
	protected void initProcessorModules(
			IRodinElement element,
			IProcessorModule[] modules,
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IProcessorModule module : modules) {
			module.initModule(element, repository, monitor);
		}
	}
	
	protected boolean filterModules(
			IFilterModule[] modules, 
			IRodinElement element, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IFilterModule module : modules) {
			IFilterModule acceptorModule = module;
			if (acceptorModule.accept(element, repository, monitor))
				continue;
			return false;
		}
		return true;
	}
	
	protected void processModules(
			IProcessorModule[] modules, 
			IRodinElement element, 
			IInternalParent target,
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IProcessorModule module : modules) {
			module.process(element, target, repository, monitor);
		}
	}
	
	protected void endFilterModules(
			IFilterModule[] modules, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IFilterModule module : modules) {
			module.endModule(repository, monitor);
		}
	}

	protected void endProcessorModules(
			IRodinElement element,
			IProcessorModule[] modules, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IProcessorModule module : modules) {
			module.endModule(element, repository, monitor);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
