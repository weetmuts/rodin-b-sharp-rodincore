/*******************************************************************************
 * Copyright (c) 2005, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - used EventBSharedColor
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.dialogs;

import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eventb.internal.ui.EventBText;
import org.eventb.internal.ui.IEventBInputText;

/**
 * @author htson
 *         <p>
 *         This class extends the Dialog class and provides an input dialog for
 *         entering a list of carrier set.
 */
public class NewCarrierSetDialog extends EventBDialog {

	private final String defaultPrefix;

	private final Collection<String> namesResults;

	private final List<IEventBInputText> namesTexts;

	private final String message;
	
	private final int NB_ELEMENTS = 3;

	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param parentShell
	 *            The parent shell of the dialog
	 * @param title
	 *            The title of the dialog
	 * @param message
	 *            The text message of the dialog
	 * @param defaultPrefix
	 *            The default prefix of for the attributes
	 */
	public NewCarrierSetDialog(Shell parentShell, String title,
			String message, String defaultPrefix) {
		super(parentShell, title);
		this.message = message;
		this.defaultPrefix = defaultPrefix;
		namesTexts = new ArrayList<IEventBInputText>();
		namesResults = new ArrayList<String>();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, MORE_ID, MORE_LABEL);
		createDefaultButton(parent, OK_ID, OK_LABEL);
		createButton(parent, CANCEL_ID, CANCEL_LABEL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createContents() {
		setDebugBackgroundColor();
		setFormGridLayout(getBody(), 2);
		setFormGridData();
		
		for (int i = 0; i < NB_ELEMENTS; i++) {
			final String prefix = (i == 0) ? defaultPrefix : EMPTY;
			createLabel(message);
			createBText(prefix);
		}
		final IEventBInputText first = namesTexts.get(0);
		select(first);
		dirtyTexts.add(first.getTextWidget());
	}
	
	private void createLabel(String label) {
		createLabel(getBody(), message);
	}

	private void createBText(String value) {
		final EventBText text = createBText(getBody(), value);
		namesTexts.add(text);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CANCEL_ID) {
			// do nothing
		} else if (buttonId == MORE_ID) {
			createLabel(message);
			createBText(EMPTY);
			toolkit.paintBordersFor(getBody());
			updateSize();
		} else if (buttonId == OK_ID) {
			fillResult(namesTexts, namesResults);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Get the list of names.
	 * <p>
	 * 
	 * @return The list of names that the user entered
	 */
	public Collection<String> getNames() {
		return namesResults;
	}

	@Override
	public boolean close() {
		dispose(namesTexts);
		return super.close();
	}
}