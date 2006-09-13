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
import org.eventb.core.EventBPlugin;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineFile;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ISCMachineFile;
import org.eventb.core.ast.Predicate;
import org.eventb.core.sc.IAbstractEventTable;
import org.eventb.core.sc.IAcceptorModule;
import org.eventb.core.sc.IModuleManager;
import org.eventb.core.sc.IStateRepository;
import org.eventb.internal.core.sc.ModuleManager;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Stefan Hallerstede
 *
 */
public class MachineInvariantModule extends PredicateWithTypingModule {

	public static final String MACHINE_INVARIANT_ACCEPTOR = 
		EventBPlugin.PLUGIN_ID + ".machineInvariantAcceptor";

	private IAcceptorModule[] modules;

	public MachineInvariantModule() {
		IModuleManager manager = ModuleManager.getModuleManager();
		modules = manager.getAcceptorModules(MACHINE_INVARIANT_ACCEPTOR);
	}

	private static String INVARIANT_NAME_PREFIX = "INV";
	
	public void process(
			IRodinElement element, 
			IInternalParent target,
			IStateRepository repository,
			IProgressMonitor monitor)
			throws CoreException {

		IMachineFile machineFile = (IMachineFile) element;
		
		IInvariant[] invariants = machineFile.getInvariants();
		
		IAbstractEventTable abstractEventTable =
			(IAbstractEventTable) repository.getState(IAbstractEventTable.STATE_TYPE);
		
		ISCMachineFile scMachineFile = abstractEventTable.getMachineFile();
		
		int offset = 0;
		
		if (scMachineFile != null) {
			ISCInvariant[] scInvariants = scMachineFile.getSCInvariants();
			offset = scInvariants.length;
			copySCPredicates(scInvariants, target, monitor);
		}
		
		if (invariants.length == 0)
			return;
		
		Predicate[] predicates = new Predicate[invariants.length];
	
		checkAndType(
				invariants, 
				target,
				predicates,
				modules,
				machineFile.getElementName(),
				repository,
				monitor);
		
		saveInvariants(target, offset, invariants, predicates, monitor);

	}
	
	private void saveInvariants(
			IInternalParent parent, 
			int offset,
			IInvariant[] invariants, 
			Predicate[] predicates,
			IProgressMonitor monitor) throws RodinDBException {
		
		final String bag = parent.getElementName();
		
		int index = offset;
		
		for (int i=0; i<invariants.length; i++) {
			if (predicates[i] == null)
				continue;
			ISCInvariant scInvariant = 
				(ISCInvariant) parent.createInternalElement(
						ISCInvariant.ELEMENT_TYPE, 
						INVARIANT_NAME_PREFIX + index++, 
						null, 
						monitor);
			scInvariant.setLabel(invariants[i].getLabel(monitor), monitor);
			scInvariant.setPredicate(predicates[i]);
			scInvariant.setSource(invariants[i], monitor);
			scInvariant.setBag(bag, monitor);
		}
	}

	@Override
	protected void makeProgress(IProgressMonitor monitor) {
		monitor.worked(1);
	}

}
