/*******************************************************************************
 * Copyright (c) 2008 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.sc.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.ISCEvent;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.IAbstractEventInfo;
import org.eventb.core.sc.state.IAbstractMachineInfo;
import org.eventb.core.sc.state.IConcreteEventInfo;
import org.eventb.core.sc.state.IEventLabelSymbolTable;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.sc.symbolTable.ILabelSymbolInfo;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Stefan Hallerstede
 * 
 */
public abstract class MachineEventCopyLabeledElementsModule extends
		SCProcessorModule {

	private IAbstractMachineInfo abstractMachineInfo;
	private IConcreteEventInfo concreteEventInfo;
	private IEventLabelSymbolTable labelSymbolTable;

	public void process(IRodinElement element, IInternalParent target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {

		if (concreteEventInfo.isInitialisation())
			return;

		ILabelSymbolInfo symbolInfo = concreteEventInfo.getSymbolInfo();

		if (symbolInfo.hasAttribute(EventBAttributes.EXTENDED_ATTRIBUTE)
				&& symbolInfo
						.getAttributeValue(EventBAttributes.EXTENDED_ATTRIBUTE)
				&& concreteEventInfo.getAbstractEventInfos().size() > 0) {

			IAbstractEventInfo abstractEventInfo = concreteEventInfo
					.getAbstractEventInfos().get(0);
			ISCEvent scEvent = abstractEventInfo.getEvent();

			ILabeledElement[] scElements = getSCElements(scEvent);

			IRefinesEvent refinesEvent = concreteEventInfo.getRefinesClauses()
					.get(0);
			String abstractMachineName = abstractMachineInfo
					.getAbstractMachine().getComponentName();

			for (ILabeledElement scElement : scElements) {
				String label = scElement.getLabel();
				ILabelSymbolInfo newSymbolInfo = makeLabelSymbolInfo(label,
						refinesEvent, abstractMachineName);
				labelSymbolTable.putSymbolInfo(newSymbolInfo);
			}

			if (target == null)
				return;

			for (ILabeledElement scElement : scElements) {
				scElement.copy(target, null, null, false, monitor);
			}
		}
	}

	protected abstract ILabelSymbolInfo makeLabelSymbolInfo(String label,
			IRefinesEvent refinesEvent, String component);

	protected abstract ILabeledElement[] getSCElements(ISCEvent scEvent)
			throws RodinDBException;

	@Override
	public void initModule(IRodinElement element,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		super.initModule(element, repository, monitor);
		concreteEventInfo = (IConcreteEventInfo) repository
				.getState(IConcreteEventInfo.STATE_TYPE);
		labelSymbolTable = (IEventLabelSymbolTable) repository
				.getState(IEventLabelSymbolTable.STATE_TYPE);
		abstractMachineInfo = (IAbstractMachineInfo) repository
				.getState(IAbstractMachineInfo.STATE_TYPE);
	}

	@Override
	public void endModule(IRodinElement element, ISCStateRepository repository,
			IProgressMonitor monitor) throws CoreException {
		concreteEventInfo = null;
		labelSymbolTable = null;
		abstractMachineInfo = null;
		super.endModule(element, repository, monitor);
	}

}
