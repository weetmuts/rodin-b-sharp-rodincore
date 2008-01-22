/*******************************************************************************
 * Copyright (c) 2008 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.eventbeditor.EditorPagesRegistry;
import org.eventb.internal.ui.eventbeditor.IEditorPagesRegistry;
import org.eventb.ui.EventBUIPlugin;

/**
 * @author htson
 *         <p>
 *         An abstract class for implementing preference page for selecting
 *         editor pages, e.g. for machine or context editors. Through this
 *         preference, the users can customize the list of pages to be visible
 *         in the editors. The changes only effect newly created editors, it has
 *         no effect on editors which already open.
 */
public abstract class EventBEditorPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * The preference key for the list of selected pages.
	 */
	private String pagesFieldName;
	
	/**
	 * The description for the list of selected pages.
	 */
	private String pagesFieldDescription;

	/**
	 * The editor pages preference {@link IEditorPagesPreference} associated
	 * with this preference page
	 */
	IEditorPagesPreference preference;

	/**
	 * Constructor.
	 * 
	 * @param preference
	 *            the editor pages preference {@link IEditorPagesPreference}.
	 * @param description
	 *            the string description of the preference page.
	 * @param pagesFieldName
	 *            the preference key for the list of selected pages.
	 * @param pagesFieldDescription
	 *            the string description of the list of selected pages.
	 */
	public EventBEditorPreferencePage(IEditorPagesPreference preference,
			String description, String pagesFieldName,
			String pagesFieldDescription) {
		super();
		this.preference = preference;
		this.pagesFieldName = pagesFieldName;
		this.pagesFieldDescription = pagesFieldDescription;
		setPreferenceStore(EventBUIPlugin.getDefault().getPreferenceStore());
		setDescription(description);
	}

	/**
	 * Creates the field editor selected pages field using
	 * {@link TwoListSelectionEditor}. The field editor knows how to save and
	 * restore itself.
	 * <p>
	 * The editor are created as a part of a tab folder.
	 * </p>
	 */
	@Override
	public void createFieldEditors() {
		final TabFolder tabFolder = new TabFolder( getFieldEditorParent(), SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		tabFolder.setLayoutData(gd);
		
		final Composite composite = new Composite(tabFolder, SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);
		final TabItem tab= new TabItem(tabFolder, SWT.NONE);
		tab.setText("Pages"); 
		tab.setControl(composite);

		TwoListSelectionEditor pagesEditor = new TwoListSelectionEditor(
				pagesFieldName, pagesFieldDescription, composite) {
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eventb.internal.ui.preferences.TwoListSelectionEditor#parseString(java.lang.String)
			 */
			@Override
			protected ArrayList<Object> parseString(String stringList) {
				String[] pageIDs = UIUtils.parseString(stringList);
				ArrayList<Object> result = new ArrayList<Object>();
				IEditorPagesRegistry registry = EditorPagesRegistry.getDefault();
				for (String pageID : pageIDs) {
					if (registry.isValid(preference.getEditorID(), pageID)) {
						result.add(pageID);
					}
				}
				return result;
			}
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eventb.internal.ui.preferences.TwoListSelectionEditor#getLabel(java.lang.Object)
			 */
			@Override
			protected String getLabel(Object object) {
				IEditorPagesRegistry registry = EditorPagesRegistry.getDefault();
				return registry.getPageName(preference.getEditorID(), (String) object);
			}
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eventb.internal.ui.preferences.TwoListSelectionEditor#getDeclaredObjects()
			 */
			@Override
			protected Collection<Object> getDeclaredObjects() {
				IEditorPagesRegistry registry = EditorPagesRegistry.getDefault();
				Collection<String> pageIDs = registry.getAllPageIDs(preference.getEditorID());
				Collection<Object> result = new ArrayList<Object>();
				for (String pageID : pageIDs) {
					result.add(pageID);
				}
				return result;
			}
		
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eventb.internal.ui.preferences.TwoListSelectionEditor#createList(java.util.ArrayList)
			 */
			@Override
			protected String createList(ArrayList<Object> objects) {
				return UIUtils.toCommaSeparatedList(objects);
			}
		
		};
		addField(pagesEditor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Do nothing.
	}

}