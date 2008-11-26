/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
  *******************************************************************************/


package fr.systerel.explorer.statistics;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eventb.core.IAxiom;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPSStatus;
import org.eventb.core.ITheorem;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;

import fr.systerel.explorer.model.ModelController;
import fr.systerel.explorer.model.ModelProject;
import fr.systerel.explorer.navigator.IElementNode;

/**
 * This is the content provider for the overview of the statistics.
 *
 */
public class StatisticsContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			Object[] elements = (Object[]) inputElement;
			ArrayList<IStatistics> result = new ArrayList<IStatistics>();
			IStatistics stats;
			//get the statistics for all elements
			for (Object element : elements) {
				stats = getStatistics(element);
				if (stats != null) {
					result.add(stats);
				}
			
			}
			if ( result.size() ==1) {
				return result.toArray();
			}
			// create an AggregateStatistics if there is more than one result
			if ( result.size() >1) {
				Object[] res=  new Object[1];
				res[0] = new AggregateStatistics(result.toArray(new IStatistics[result.size()]));
				return res;
			}
		}
		return new Object[0];
	}

	public void dispose() {
		// do nothing

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
	
	/**
	 * Indicates whether elements of the given type can possibly have proof obligations 
	 * associated with them.
	 * @param type The type of the elements
	 * @return true, if they can have proof obligations, false otherwise.
	 */
	protected boolean canHavePOs(IInternalElementType<?> type) {
		if (type == IInvariant.ELEMENT_TYPE) {
			return true;
		}
		if (type == IAxiom.ELEMENT_TYPE) {
			return true;
		}
		if (type == IEvent.ELEMENT_TYPE) {
			return true;
		}
		if (type == ITheorem.ELEMENT_TYPE) {
			return true;
		}
		if (type == IPSStatus.ELEMENT_TYPE) {
			return true;
		}
		return false;
	}

	
	
	protected IStatistics getStatistics(Object inputElement) {
		if (inputElement instanceof IMachineRoot) {
			return  new Statistics(ModelController.getMachine((IMachineRoot) inputElement));
		}
		if (inputElement instanceof IContextRoot) {
			return new Statistics(ModelController.getContext((IContextRoot) inputElement));
		}
		if (inputElement instanceof IInvariant) {
			return  new Statistics(ModelController.getInvariant((IInvariant) inputElement));
		}
		if (inputElement instanceof IAxiom) {
			return new Statistics(ModelController.getAxiom((IAxiom) inputElement));
		}
		if (inputElement instanceof ITheorem) {
			return new Statistics(ModelController.getTheorem((ITheorem) inputElement));
		}
		if (inputElement instanceof IEvent) {
			return new Statistics(ModelController.getEvent((IEvent) inputElement));
		}
		if (inputElement instanceof IElementNode) {
			IElementNode node = (IElementNode) inputElement;
			if (canHavePOs(node.getChildrenType())) {
				return new Statistics(inputElement);
			}
		}
		if (inputElement instanceof IProject) {
			IRodinProject rodinProject = RodinCore.valueOf((IProject) inputElement);
			if (rodinProject.exists()) {
				ModelProject modelProject = ModelController
						.getProject(rodinProject);
				if (modelProject != null) {
					return new Statistics(modelProject);
				}
			}
		}
		return null;
	}
}
