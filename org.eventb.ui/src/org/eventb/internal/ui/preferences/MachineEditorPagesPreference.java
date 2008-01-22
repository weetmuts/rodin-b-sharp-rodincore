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

import org.eventb.internal.ui.eventbeditor.EventBMachineEditor;
import org.eventb.ui.EventBUIPlugin;

/**
 * @author htson
 *         <p>
 *         An extension of {@link EditorPagesPreference} to store the current
 *         preference for machine editor pages. This class is implemented as a
 *         singleton class.
 */
public class MachineEditorPagesPreference extends EditorPagesPreference {

	// Static singleton element.
	private static IEditorPagesPreference instance;

	/**
	 * Constructor.
	 * <p>
	 * Private constructor for singleton.
	 */
	private MachineEditorPagesPreference() {
		// Register for the changes in the preference store.
		EventBUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
	}

	/**
	 * Returns the singleton instance (initialise the instance if necessary).
	 * 
	 * @return the singleton instance of this class.
	 */
	public static IEditorPagesPreference getDefault() {
		if (instance == null) // Initialise the first time.
			instance = new MachineEditorPagesPreference();
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.preferences.IEditorPagesPreference#getEditorID()
	 */
	public String getEditorID() {
		return EventBMachineEditor.EDITOR_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.preferences.EditorPagesPreference#getPreferenceName()
	 */
	@Override
	protected String getPreferenceName() {
		return PreferenceConstants.P_MACHINE_EDITOR_PAGE;
	}

}