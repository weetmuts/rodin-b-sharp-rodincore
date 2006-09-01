/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.internal.core.sc;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IContextFile;
import org.eventb.core.IExtendsContext;
import org.eventb.core.ISCContextFile;
import org.eventb.core.sc.IModuleManager;
import org.eventb.core.sc.IProcessorModule;
import org.eventb.core.sc.IStateRepository;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.builder.IGraph;

/**
 * @author Stefan Hallerstede
 *
 */
public class ContextStaticChecker extends StaticChecker {
	
	public static final String CONTEXT_SC_TOOL_ID = EventBPlugin.PLUGIN_ID + ".contextSC"; //$NON-NLS-1$
	public static final String CONTEXT_SC_EXTENDS_ID = EventBPlugin.PLUGIN_ID + ".contextSCExtends"; //$NON-NLS-1$

	public static final String CONTEXT_PROCESSOR = EventBPlugin.PLUGIN_ID + ".contextProcessor"; //$NON-NLS-1$
	
	private final IModuleManager manager;
	
	private IProcessorModule[] contextModules = null;
	
	public ContextStaticChecker() {
		manager = ModuleManager.getModuleManager();
	}
	
	/* (non-Javadoc)
	 * @see org.rodinp.core.builder.IAutomaticTool#run(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean run(IFile file, IProgressMonitor monitor)
			throws CoreException {
		
		if (contextModules == null) {
			
			contextModules = manager.getProcessorModules(CONTEXT_PROCESSOR);
			
		}
		
		ISCContextFile scContextFile = (ISCContextFile) RodinCore.create(file).getMutableCopy();
		IContextFile contextFile = (IContextFile) scContextFile.getContextFile().getSnapshot();
		
		IRodinProject project = (IRodinProject) scContextFile.getParent();
		project.createRodinFile(scContextFile.getElementName(), true, null);

		IStateRepository repository = createRepository(contextFile, monitor);
		
		runProcessorModules(
				contextFile, 
				scContextFile,
				contextModules, 
				repository,
				monitor);
		
		scContextFile.save(monitor, true);
		
		// TODO delta checking
		// return repository.targetHasChanged();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.rodinp.core.builder.IAutomaticTool#clean(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void clean(IFile file, IProgressMonitor monitor)
			throws CoreException {
		file.delete(true, monitor);
	}
	
	public void extract(IFile file, IGraph graph) throws CoreException {
		
		IContextFile source = (IContextFile) RodinCore.create(file);
		ISCContextFile target = source.getSCContextFile();
		
		IPath sourcePath = source.getPath();
		IPath targetPath = target.getPath();
		
		graph.addNode(targetPath, CONTEXT_SC_TOOL_ID);
		graph.putToolDependency(sourcePath, targetPath, CONTEXT_SC_TOOL_ID, true);
		
		IExtendsContext[] extendsContexts = source.getExtendsClauses();
		for(IExtendsContext extendsContext : extendsContexts) {
			graph.putUserDependency(
					source.getPath(), 
					extendsContext.getAbstractSCContext().getPath(), 
					target.getPath(), 
					CONTEXT_SC_EXTENDS_ID, 
					false);
		}
		
		graph.updateGraph();

	}

}
