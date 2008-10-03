/*******************************************************************************
 * Copyright (c) 2007, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added history support
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.editpage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.IConvergenceElement;
import org.eventb.core.IEvent;
import org.eventb.core.IConvergenceElement.Convergence;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IAttributedElement;
import org.rodinp.core.RodinDBException;

public class ConvergenceAttributeFactory implements IAttributeFactory {

	public final static String ORDINARY = "ordinary";

 	public final static String CONVERGENT = "convergent";

 	public final static String ANTICIPATED = "anticipated";

	public String getValue(IAttributedElement element,
			IProgressMonitor monitor) throws RodinDBException {
		IEvent event = (IEvent) element;
		Convergence convergence = event.getConvergence();
		if (convergence == Convergence.ORDINARY)
			return ORDINARY;
		if (convergence == Convergence.CONVERGENT)
			return CONVERGENT;
		if (convergence == Convergence.ANTICIPATED)
			return ANTICIPATED;
		return ORDINARY;
	}

	public void setValue(IAttributedElement element, String newValue,
			IProgressMonitor monitor) throws RodinDBException {
		assert element instanceof IEvent;
		IEvent event = (IEvent) element;
		if (newValue.equals(ORDINARY))
			event.setConvergence(Convergence.ORDINARY,
					new NullProgressMonitor());
		else if (newValue.equals(CONVERGENT))
			event.setConvergence(Convergence.CONVERGENT,
					new NullProgressMonitor());
		else if (newValue.equals(ANTICIPATED))
			event.setConvergence(Convergence.ANTICIPATED,
					new NullProgressMonitor());
	}

	public String[] getPossibleValues(IAttributedElement element,
			IProgressMonitor monitor) {
		return new String[] { ORDINARY, CONVERGENT, ANTICIPATED };
	}

	public void removeAttribute(IAttributedElement element,
			IProgressMonitor monitor) throws RodinDBException {
		element.removeAttribute(
				EventBAttributes.CONVERGENCE_ATTRIBUTE,
				new NullProgressMonitor());
	}

	public void setDefaultValue(IEventBEditor<?> editor,
			IAttributedElement element, IProgressMonitor monitor)
			throws RodinDBException {
		if (!(element instanceof IConvergenceElement)) {
			return;
		}
		IConvergenceElement cElement = (IConvergenceElement) element;
		cElement.setConvergence(IConvergenceElement.Convergence.ORDINARY,
				monitor);
	}
	public boolean hasValue(IAttributedElement element, IProgressMonitor monitor)
			throws RodinDBException {
		assert element instanceof IConvergenceElement;
		return ((IConvergenceElement) element).hasConvergence();
	}
}
