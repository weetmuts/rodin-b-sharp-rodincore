/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.pog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ISCAction;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.pog.state.IAbstractEventActionTable;
import org.eventb.core.pog.state.IConcreteEventActionTable;
import org.eventb.core.pog.state.IMachineVariableTable;

/**
 * @author Stefan Hallerstede
 *
 */
public class AbstractEventActionTable extends EventActionTable implements
		IAbstractEventActionTable {
	
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		disappearingWitnesses = Collections.unmodifiableList(disappearingWitnesses);
		simAssignments = Collections.unmodifiableList(simAssignments);
		simActions = Collections.unmodifiableList(simActions);
	}

	private List<BecomesEqualTo> disappearingWitnesses;
	private List<Assignment> simAssignments;
	private List<ISCAction> simActions;
	
	private final Correspondence<Assignment> correspondence;

	public AbstractEventActionTable(
			ISCAction[] abstractActions, 
			ITypeEnvironment typeEnvironment, 
			IMachineVariableTable variables,
			IConcreteEventActionTable concreteTable,
			FormulaFactory factory) throws CoreException {
		super(abstractActions, typeEnvironment, factory);
		
		correspondence = new Correspondence<Assignment>(concreteTable.getAssignments(), assignments);
		
		disappearingWitnesses = new ArrayList<BecomesEqualTo>(abstractActions.length);
		simAssignments = new ArrayList<Assignment>(abstractActions.length);
		simActions = new ArrayList<ISCAction>(abstractActions.length);
		
		for (int i=0; i<assignments.size(); i++) {
			Assignment assignment = assignments.get(i);
			if (isDisappearing(assignment, variables) 
					&& assignment instanceof BecomesEqualTo) {
				disappearingWitnesses.add((BecomesEqualTo) assignment);
			} else {
				simAssignments.add(assignment);
				simActions.add(abstractActions[i]);
			}
					
		}

	}
	
	private boolean isDisappearing(Assignment assignment, IMachineVariableTable variables) {
		boolean found = false;
		for (FreeIdentifier identifier : assignment.getAssignedIdentifiers())
			if (variables.contains(identifier)) {
				found = true;
			}
		return !found;
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.sc.IState#getStateType()
	 */
	public String getStateType() {
		return STATE_TYPE;
	}

	public List<BecomesEqualTo> getDisappearingWitnesses() {
		return disappearingWitnesses;
	}

	public List<Assignment> getSimAssignments() {
		return simAssignments;
	}

	public List<ISCAction> getSimActions() {
		return simActions;
	}

	public int getIndexOfCorrespondingAbstract(int index) {
		return correspondence.getIndexOfCorrespondingAbstract(index);
	}

	public int getIndexOfCorrespondingConcrete(int index) {
		return correspondence.getIndexOfCorrespondingConcrete(index);
	}

}
