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

package org.eventb.internal.ui.eventbeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author htson
 * <p>
 * An implementation of the Event-B Form Page
 * for editing Variables (Rodin elements).
 */
public class VariablePage
	extends EventBFormPage 
{
	
	// Title, tab title and ID of the page.
	public static final String PAGE_ID = "Variables"; //$NON-NLS-1$
	public static final String PAGE_TITLE = "Variables";
	public static final String PAGE_TAB_TITLE = "Variables";

	/**
	 * Constructor.
	 * <p>
	 * @param editor The form editor that holds the page 
	 */
	public VariablePage(FormEditor editor) {
		super(editor, PAGE_ID, PAGE_TITLE, PAGE_TAB_TITLE);  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.EventBFormPage#createMasterSection(org.eclipse.ui.forms.IManagedForm, org.eclipse.swt.widgets.Composite, int, org.eventb.internal.ui.eventbeditor.EventBEditor)
	 */
	@Override
	protected EventBTablePartWithButtons createMasterSection(IManagedForm managedForm, Composite parent, int style, EventBEditor editor) {
		VariableMasterSection part = new VariableMasterSection(managedForm, parent, managedForm.getToolkit(), Section.NO_TITLE, (EventBEditor) this.getEditor());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 200;
		gd.minimumHeight = 150;
		gd.widthHint = 150;
		part.getSection().setLayoutData(gd);
		return part;
	}


}