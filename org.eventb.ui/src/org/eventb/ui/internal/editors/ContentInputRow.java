/*******************************************************************************
 * Copyright (c) 2005 ETH-Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH RODIN Group
 *******************************************************************************/

package org.eventb.ui.internal.editors;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eventb.ui.editors.IEventBInputText;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 * <p>
 * An implementation of Event-B Input row for editing content of Rodin elements.
 */
public class ContentInputRow
	extends EventBInputRow 
{

	/**
	 * Contructor.
	 * <p>
	 * @param page The Detail page that holds this row
	 * @param toolkit The Form Toolkit used to create this
	 * @param parent The parent Composite
	 * @param label The label for the content (e.g. predicate, substitution)
	 * @param tip The tip for this row
	 */
	public ContentInputRow(EventBDetailsSection page, FormToolkit toolkit, Composite parent, String label, String tip) {
		super(page, toolkit, parent, label, tip, SWT.MULTI | SWT.V_SCROLL |SWT.BORDER |SWT.H_SCROLL);
	}
	
	
	/**
	 * Set the text for the text field part of the row.
	 */
	protected void setText() {
		try {
			textInput.setText(page.getInput().getContents());
		}
		catch (RodinDBException e) {
			// TODO Exception handle
			e.printStackTrace();
		}
	}

	
	/**
	 * Commit the change and notify the listeners.
	 */
	public void commit() {
		if (dirty) {
			try {
				IInternalElement input = page.getInput();
				if (input instanceof IUnnamedInternalElement) {
					System.out.println("Commit: " + input + " to be " + textInput.getText());
					input.setContents(textInput.getText());
					
					SectionPart masterPart = page.getBlock().getMasterPart();
					boolean expand = false;
					if (masterPart instanceof EventBTreePartWithButtons) {
						TreeViewer viewer = ((EventBTreePartWithButtons) masterPart).getViewer();
						expand = viewer.getExpandedState(input);
					}
	
					if (masterPart instanceof EventBTablePartWithButtons) {
						System.out.println("Master refresh");
						((EventBTablePartWithButtons) masterPart).commit();
						//((EventBTablePartWithButtons) masterPart).getViewer().setSelection(new StructuredSelection(input));
					}
					else if (masterPart instanceof EventBTreePartWithButtons) {
						System.out.println("Master refresh");
						TreeViewer viewer = ((EventBTreePartWithButtons) masterPart).getViewer();
						Control control = viewer.getControl();
						control.setRedraw(false);
						((EventBTreePartWithButtons) masterPart).commit();
						//viewer.setSelection(new StructuredSelection(input));
						viewer.setExpandedState(input, expand);
						control.setRedraw(true);
					}
				}
			}
			catch (RodinDBException e) {
				e.printStackTrace();
			}
			dirty = false;
		}
	}
	
	
	/**
	 * Creating the input text field part. 
	 */
	protected IEventBInputText createEventBInputText(Composite parent, FormToolkit toolkit, int style) {
		return new EventBMath(parent, toolkit, style);
	}

}
