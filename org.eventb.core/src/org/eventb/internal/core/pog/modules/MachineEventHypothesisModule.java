/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.pog.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPOFile;
import org.eventb.core.IPOPredicateSet;
import org.eventb.core.ISCAction;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCVariable;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.pog.state.IAbstractEventActionTable;
import org.eventb.core.pog.state.IAbstractEventGuardList;
import org.eventb.core.pog.state.IAbstractEventGuardTable;
import org.eventb.core.pog.state.IConcreteEventActionTable;
import org.eventb.core.pog.state.IConcreteEventGuardTable;
import org.eventb.core.pog.state.IMachineHypothesisManager;
import org.eventb.core.pog.state.IMachineVariableTable;
import org.eventb.core.pog.state.IState;
import org.eventb.core.pog.state.IStateRepository;
import org.eventb.core.pog.state.IWitnessTable;
import org.eventb.core.tool.state.IToolStateRepository;
import org.eventb.internal.core.pog.AbstractEventActionTable;
import org.eventb.internal.core.pog.AbstractEventGuardList;
import org.eventb.internal.core.pog.AbstractEventGuardTable;
import org.eventb.internal.core.pog.ConcreteEventActionTable;
import org.eventb.internal.core.pog.ConcreteEventGuardTable;
import org.eventb.internal.core.pog.EventHypothesisManager;
import org.eventb.internal.core.pog.WitnessTable;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Stefan Hallerstede
 *
 */
public class MachineEventHypothesisModule extends UtilityModule {

	EventHypothesisManager eventHypothesisManager;
	ITypeEnvironment eventTypeEnvironment;
	IAbstractEventGuardList abstractEventGuardList;
	
	/* (non-Javadoc)
	 * @see org.eventb.core.pog.IProcessorModule#process(org.rodinp.core.IRodinElement, org.eventb.core.IPOFile, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void process(
			IRodinElement element, 
			IStateRepository repository,
			IProgressMonitor monitor)
			throws CoreException {
		// all processing is done in the initModule() method

	}
	
	private void fetchActionsAndVariables(
			ISCEvent concreteEvent, 
			IToolStateRepository<IState> repository) throws CoreException {
		
		IMachineVariableTable machineVariableTable =
			(IMachineVariableTable) repository.getState(IMachineVariableTable.STATE_TYPE);
		
		IConcreteEventActionTable concreteEventActionTable =
			new ConcreteEventActionTable(
					concreteEvent.getSCActions(), 
					eventTypeEnvironment, 
					machineVariableTable, 
					factory);
		repository.setState(concreteEventActionTable);
		
		IAbstractEventActionTable abstractEventActionTable = 
			fetchAbstractActions(
					concreteEventActionTable, 
					machineVariableTable, 
					repository);
		
		fetchPostValueVariables(concreteEventActionTable.getAssignedVariables());
		fetchPostValueVariables(abstractEventActionTable.getAssignedVariables());
	}

	private IAbstractEventActionTable fetchAbstractActions(
			IConcreteEventActionTable concreteEventActionTable, 
			IMachineVariableTable machineVariableTable, 
			IToolStateRepository<IState> repository) throws RodinDBException, CoreException {
		ISCEvent abstractEvent = abstractEventGuardList.getFirstAbstractEvent();
		
		if (abstractEvent != null)
			fetchVariables(abstractEvent.getSCVariables());
		
		IAbstractEventActionTable abstractEventActionTable = 
			new AbstractEventActionTable(
					getAbstractSCActions(abstractEvent), 
					eventTypeEnvironment, 
					machineVariableTable,
					concreteEventActionTable,
					factory);
		repository.setState(abstractEventActionTable);
		return abstractEventActionTable;
	}

	private ISCAction[] getAbstractSCActions(ISCEvent abstractEvent) throws RodinDBException {
		return (abstractEvent == null ? new ISCAction[0] : abstractEvent.getSCActions());
	}

	private void setEventHypothesisManager(
			IMachineHypothesisManager machineHypothesisManager, 
			ISCEvent event, ISCGuard[] guards, 
			IStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		
		IPOFile target = repository.getTarget();
		
		IPOPredicateSet fullHypothesis = (event.getLabel().equals("INITIALISATION")) ?
				machineHypothesisManager.getContextHypothesis() :
				machineHypothesisManager.getFullHypothesis();
		eventHypothesisManager = 
			new EventHypothesisManager(event, target, guards, fullHypothesis.getElementName());
		
		repository.setState(eventHypothesisManager);
	}

	private void fetchGuards(
			ISCEvent concreteEvent,
			ISCGuard[] concreteGuards, 
			IToolStateRepository<IState> repository) throws CoreException {
		
		IConcreteEventGuardTable concreteEventGuardTable = 
			fetchConcreteGuards(concreteGuards, repository);
		
		ISCEvent[] abstractEvents = concreteEvent.getAbstractSCEvents();
		
		List<IAbstractEventGuardTable> abstractGuardTables = 
			new ArrayList<IAbstractEventGuardTable>(abstractEvents.length);
		
		for (ISCEvent abstractEvent : abstractEvents) {
		
			IAbstractEventGuardTable abstractEventGuardTable = 
				new AbstractEventGuardTable(
						abstractEvent.getSCGuards(),
						eventTypeEnvironment, 
						concreteEventGuardTable,
						factory);
			
			abstractGuardTables.add(abstractEventGuardTable);
		}
		
		abstractEventGuardList =
			new AbstractEventGuardList(abstractEvents, abstractGuardTables);
		
		repository.setState(abstractEventGuardList);
	}

	private IConcreteEventGuardTable fetchConcreteGuards(ISCGuard[] concreteGuards, IToolStateRepository<IState> repository) throws RodinDBException, CoreException {
		IConcreteEventGuardTable concreteEventGuardTable = 
			new ConcreteEventGuardTable(
					concreteGuards, 
					eventTypeEnvironment, 
					factory);
		repository.setState(concreteEventGuardTable);
		return concreteEventGuardTable;
	}

	private void fetchPostValueVariables(Set<FreeIdentifier> identifiers) throws CoreException {
		for (FreeIdentifier identifier : identifiers) {
			FreeIdentifier primedIdentifier = identifier.withPrime(factory);
			if (eventTypeEnvironment.contains(primedIdentifier.getName()))
				continue;
			eventHypothesisManager.addIdentifier(primedIdentifier);
			eventTypeEnvironment.addName(primedIdentifier.getName(), primedIdentifier.getType());
		}
	}

	private void fetchVariables(ISCVariable[] variables) throws CoreException {
		for (ISCVariable variable : variables) {
			FreeIdentifier identifier = variable.getIdentifier(factory);
			eventTypeEnvironment.add(identifier);
			eventHypothesisManager.addIdentifier(identifier);
		}
	}

	private void fetchWitnesses(
			ISCEvent concreteEvent, 
			IToolStateRepository<IState> repository, 
			IProgressMonitor monitor) throws CoreException, RodinDBException {
		IWitnessTable witnessTable = 
			new WitnessTable(concreteEvent.getSCWitnesses(), eventTypeEnvironment, factory, monitor);
		repository.setState(witnessTable);
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.pog.ProcessorModule#initModule(org.rodinp.core.IRodinElement, org.eventb.core.IPOFile, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initModule(
			IRodinElement element, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		super.initModule(element, repository, monitor);
		
		factory = repository.getFormulaFactory();
		eventTypeEnvironment = repository.getTypeEnvironment();
		
		IMachineHypothesisManager machineHypothesisManager =
			(IMachineHypothesisManager) repository.getState(IMachineHypothesisManager.STATE_TYPE);
		
		ISCEvent concreteEvent = (ISCEvent) element;
		
		ISCGuard[] guards = concreteEvent.getSCGuards();
		
		setEventHypothesisManager(
				machineHypothesisManager, concreteEvent, guards, repository, monitor);
		
		fetchVariables(concreteEvent.getSCVariables());
		
		fetchGuards(
				concreteEvent,
				guards, 
				repository);
		
		fetchActionsAndVariables(concreteEvent, repository);
		
		fetchWitnesses(concreteEvent, repository, monitor);
	
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.pog.ProcessorModule#endModule(org.rodinp.core.IRodinElement, org.eventb.core.IPOFile, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void endModule(
			IRodinElement element, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		
		eventHypothesisManager.createHypotheses(monitor);

		eventHypothesisManager = null;
		abstractEventGuardList = null;
		eventTypeEnvironment = null;
		factory = null;
		
		super.endModule(element, repository, monitor);
	}

}
