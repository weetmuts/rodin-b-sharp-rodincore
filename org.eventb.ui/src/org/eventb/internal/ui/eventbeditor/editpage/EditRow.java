/*******************************************************************************
 * Copyright (c) 2007 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui.eventbeditor.editpage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eventb.internal.ui.eventbeditor.EventBEditorUtils;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IAttributedElement;
import org.rodinp.core.IElementType;
import org.rodinp.core.IRodinElement;

public class EditRow {

	Composite composite;

	ScrolledForm form;

	IEditComposite[] editComposites;

	IElementComposite elementComp;

	ButtonComposite buttonComp;

	public EditRow(IElementComposite elementComp, ScrolledForm form,
			FormToolkit toolkit) {
		this.elementComp = elementComp;
		this.form = form;
	}

	public void createContents(IEventBEditor<?> editor, FormToolkit toolkit,
			Composite parent, Composite sibling, int level) {
		composite = toolkit.createComposite(parent);
		if (EventBEditorUtils.DEBUG) {
			composite.setBackground(composite.getDisplay().getSystemColor(
					SWT.COLOR_RED));
		}
		if (sibling != null) {
			assert sibling.getParent() == parent;
			composite.moveAbove(sibling);
		}
		IRodinElement element = elementComp.getElement();
		IElementType<? extends IRodinElement> type = element.getElementType();
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		IAttributeRelUISpecRegistry attributeRegistry = AttributeRelUISpecRegistry
				.getDefault();
		int numColumns = 1 + 3 * attributeRegistry.getNumberOfAttributes(type);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = numColumns + 1;
		composite.setLayout(gridLayout);

		createButtons(toolkit, level);
		editComposites = attributeRegistry.getEditComposites(element
				.getElementType());
		for (IEditComposite editComposite : editComposites) {
			editComposite.setForm(form);
			editComposite.setElement((IAttributedElement) element);
			editComposite.createComposite(editor, toolkit, composite);
		}
		toolkit.paintBordersFor(composite);
	}

	private void createButtons(FormToolkit toolkit, int level) {
		buttonComp = new ButtonComposite(elementComp);
		buttonComp.createContents(toolkit, composite, level);
	}

	public void refresh() {
		IRodinElement element = elementComp.getElement();
		for (IEditComposite editComposite : editComposites) {
			editComposite.setElement((IAttributedElement) element);
			editComposite.refresh();
		}
		buttonComp.updateLinks();
	}

	public boolean isSelected() {
		return buttonComp.isSelected();
	}

	public void dispose() {
		composite.dispose();
	}

	public Composite getComposite() {
		return composite;
	}

	public void setSelected(boolean select) {
		for (IEditComposite editComposite : editComposites) {
			editComposite.setSelected(select);
		}
		if (select) {
			composite.setBackground(composite.getDisplay().getSystemColor(
					SWT.COLOR_GRAY));
		} else {
			if (EventBEditorUtils.DEBUG) {
				composite.setBackground(composite.getDisplay().getSystemColor(
						SWT.COLOR_RED));
			} else {
				composite.setBackground(composite.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
			}
		}
		buttonComp.setSelected(select);
	}

	public void updateLinks() {
		buttonComp.updateLinks();
	}

	public void updateExpandStatus() {
		buttonComp.updateExpandStatus();
	}

}
