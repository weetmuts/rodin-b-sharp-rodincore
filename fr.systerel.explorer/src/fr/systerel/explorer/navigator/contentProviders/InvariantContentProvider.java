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


package fr.systerel.explorer.navigator.contentProviders;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinDBException;

import fr.systerel.explorer.model.ModelController;
import fr.systerel.explorer.model.ModelMachine;
import fr.systerel.explorer.navigator.IElementNode;

/**
 * The content provider for Invariant elements
 *
 */
public class InvariantContentProvider implements ITreeContentProvider {
	
	public Object[] getChildren(Object element) {
		if (element instanceof IMachineRoot) {
			Object[] results = new Object[1];
			//get the intermediary node for invariants
			results[0] = ModelController.getMachine((IMachineRoot) element).invariant_node;
			return results;
		}
		if (element instanceof IElementNode){
			IInternalElementType<?> type = ((IElementNode) element).getChildrenType();
			if (type.equals(IInvariant.ELEMENT_TYPE)) {
				IMachineRoot machine = (IMachineRoot) ((IElementNode) element).getParent();
				try {
					return machine.getInvariants();
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new Object[0];
	}
	public Object getParent(Object element) {
    	if (element instanceof IInvariant) {
    		IInvariant inv =  (IInvariant) element;
    		IMachineRoot mach = (IMachineRoot) inv.getRodinFile().getRoot();
     		ModelMachine machine = ModelController.getMachine(mach);
     		if (machine != null) {
    			return machine.invariant_node;
     		}
		}
    	if (element instanceof IElementNode) {
    		return ((IElementNode) element).getParent();
    	}
      return null;
	}

	public boolean hasChildren(Object element) {
      return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	public void dispose() {
    	// Do nothing

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	// Do nothing
		 
	}
}

