/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.indexer;

import static org.eventb.core.EventBAttributes.*;
import static org.eventb.core.indexer.EventBIndexUtil.REFERENCE;
import static org.rodinp.core.index.RodinIndexer.getRodinLocation;

import java.util.Map;

import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IParameter;
import org.eventb.core.IPredicateElement;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IVariable;
import org.eventb.core.IWitness;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IAttributedElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.index.IDeclaration;
import org.rodinp.core.index.IIndexingToolkit;

/**
 * @author Nicolas Beauger
 * 
 */
public class EventIndexer extends Cancellable{

	private final IEvent event;
	private final Map<IEvent, SymbolTable> absParamTables;
	private final SymbolTable eventST;
	private final SymbolTable declImportST;
	private final IIndexingToolkit index;

	/**
	 * Constructor.
	 * 
	 * @param event
	 *            the event to index
	 * @param absParamTables
	 * @param eventST
	 * @param declImportST
	 *            a SymbolTable containing, by decreasing order of priority:
	 *            <ul>
	 *            <li>local declarations</li>
	 *            <li>imported declarations</li>
	 *            </ul>
	 * @param index 
	 */
	public EventIndexer(IEvent event, Map<IEvent, SymbolTable> absParamTables,
			SymbolTable eventST, SymbolTable declImportST,
			IIndexingToolkit index) {
		this.declImportST = declImportST;
		this.event = event;
		this.absParamTables = absParamTables;
		this.eventST = eventST;
		this.index = index;
	}

	public void process() throws RodinDBException {
		checkCancel();
		processEventLabel();
		
		checkCancel();
		final SymbolTable absPrmDeclImpST = new SymbolTable(declImportST);
		processRefines(event.getRefinesClauses(), absPrmDeclImpST);

		checkCancel();
		final SymbolTable totalST = new SymbolTable(absPrmDeclImpST);
		processParameters(event.getParameters(), totalST);

		checkCancel();
		processPredicateElements(event.getGuards(), totalST);
		checkCancel();
		processActions(event.getActions(), totalST);

		checkCancel();
		processWitnesses(event.getWitnesses(), totalST);
	}

	/**
	 * @param refinesEvents
	 * @throws RodinDBException
	 */
	private void processRefines(IRefinesEvent[] refinesEvents,
			SymbolTable absParamDeclImportST) throws RodinDBException {
		for (IRefinesEvent refinesEvent : refinesEvents) {
			final String absEventLabel = refinesEvent.getAbstractEventLabel();

			final IDeclaration declAbsEvent = eventST.lookup(absEventLabel);
			if (declAbsEvent != null) {
				final IInternalElement element = declAbsEvent.getElement();
				if (element instanceof IEvent) {
					addRefAttribute(declAbsEvent, refinesEvent,
							TARGET_ATTRIBUTE);

					addAbstractParams((IEvent) element, absParamDeclImportST);
				}
			}
			checkCancel();
		}

	}

	/**
	 * @param declaration
	 * @param element
	 * @param attribute
	 * @param index
	 */
	private void addRefAttribute(final IDeclaration declaration,
			IAttributedElement element, IAttributeType.String attribute) {
		index.addOccurrence(declaration, REFERENCE, getRodinLocation(element,
				attribute));
	}

	/**
	 * @param declaration
	 * @param absParamDeclImportST
	 */
	private void addAbstractParams(IEvent abstractEvent,
			SymbolTable absParamDeclImportST) {
		final SymbolTable absParamST = absParamTables.get(abstractEvent);
		if (absParamST != null) {
			absParamDeclImportST.putAll(absParamST);
		}
	}

	/**
	 * @param index
	 * @throws RodinDBException
	 */
	private void processEventLabel() throws RodinDBException {
		final String eventLabel = event.getLabel();
		final IDeclaration declaration = index.declare(event, eventLabel);
		index.export(declaration);
	}

	/**
	 * @param witnesses
	 * @param totalST
	 * @param index
	 * @throws RodinDBException
	 */
	private void processWitnesses(IWitness[] witnesses, SymbolTable totalST)
			throws RodinDBException {

		processWitnessLabels(witnesses, totalST);
		processPredicateElements(witnesses, totalST);
	}

	/**
	 * @param witnesses
	 * @param totalST
	 * @param index
	 * @param ff
	 * @throws RodinDBException
	 */
	private void processWitnessLabels(ILabeledElement[] witnesses,
			SymbolTable totalST) throws RodinDBException {

		for (ILabeledElement label : witnesses) {
			final String name = getNameNoPrime(label);

			final IDeclaration declAbs = totalST.lookUpper(name);

			if (declAbs != null) {
				final IInternalElement element = declAbs.getElement();
				if (element instanceof IParameter
						|| element instanceof IVariable) {
					// could be a namesake
					addRefAttribute(declAbs, label, LABEL_ATTRIBUTE);
				}
			}
			checkCancel();
		}
	}

	/**
	 * @param labelElem
	 * @return
	 * @throws RodinDBException
	 */
	private static String getNameNoPrime(ILabeledElement labelElem)
			throws RodinDBException {
		final FormulaFactory ff = FormulaFactory.getDefault();

		final String label = labelElem.getLabel();
		final FreeIdentifier ident = ff.makeFreeIdentifier(label, null);
		final String name;
		if (ident.isPrimed()) {
			name = ident.withoutPrime(ff).getName();
		} else {
			name = ident.getName();
		}
		return name;
	}

	/**
	 * @param parameters
	 * @param table
	 * @param index
	 * @throws RodinDBException
	 */
	private void processParameters(final IParameter[] parameters,
			SymbolTable totalST) throws RodinDBException {
		for (IParameter parameter : parameters) {
			final String ident = parameter.getIdentifierString();

			IDeclaration declaration = index.declare(parameter, ident);
			totalST.put(declaration);
			index.export(declaration);

			refAnyAbstractParam(ident, parameter, totalST);
		}
	}

	/**
	 * @param ident
	 * @param parameter
	 * @param totalST
	 * @param index
	 */
	private void refAnyAbstractParam(final String ident, IParameter parameter,
			SymbolTable totalST) {
		final IDeclaration declAbsParam = totalST.lookUpper(ident);
		if (declAbsParam != null) {
			if (declAbsParam.getElement() instanceof IParameter) {
				// could be a namesake
				addRefAttribute(declAbsParam, parameter, IDENTIFIER_ATTRIBUTE);
			}
		}
	}

	private void processActions(IAction[] actions, SymbolTable eventTable)
			throws RodinDBException {
		for (IAction action : actions) {
			final AssignmentIndexer assignIndexer = new AssignmentIndexer(
					action, eventTable, index);
			assignIndexer.process();
			
			checkCancel();
		}
	}

	private void processPredicateElements(IPredicateElement[] preds,
			SymbolTable symbolTable) throws RodinDBException {
		for (IPredicateElement elem : preds) {
			final PredicateIndexer predIndexer = new PredicateIndexer(elem,
					symbolTable, index);
			predIndexer.process();

			checkCancel();
		}
	}
	
	protected void checkCancel() {
		checkCancel(index);
	}

}
