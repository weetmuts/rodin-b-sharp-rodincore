/*******************************************************************************
 * Copyright (c) 2005-2006 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/
package org.eventb.internal.ui.eventbeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eventb.internal.ui.EventBMath;

/**
 * @author htson
 *         <p>
 *         This class extends the Dialog class and provides an input dialog for
 *         new event with some local varialbes, guards and actions.
 */
public class NewEventInputDialog extends Dialog {
	private String defaultName;

	private String name;

	private Collection<String> varNames;

	private Collection<String> grdNames;

	private Collection<String> grdPredicates;

	private Collection<String> actions;

	private Text nameText;

	private Collection<Text> varNameTexts;

	private Collection<Text> grdNameTexts;

	private Collection<Text> grdPredicateTexts;

	private Collection<Text> actionTexts;

	private ScrolledForm scrolledForm;

	private String title;

	/**
	 * Constructor.
	 * <p>
	 * @param parentShell the parent shell of the dialog
	 * @param title the title of the dialog
	 * @param defaultName the default name for the event
	 */
	public NewEventInputDialog(Shell parentShell, String title,
			String defaultName) {
		super(parentShell);
		this.title = title;
		this.defaultName = defaultName;
		varNames = new HashSet<String>();
		grdNames = new HashSet<String>();
		grdPredicates = new HashSet<String>();
		actions = new HashSet<String>();
		varNameTexts = new ArrayList<Text>();
		grdNameTexts = new ArrayList<Text>();
		grdPredicateTexts = new ArrayList<Text>();
		actionTexts = new ArrayList<Text>();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);

		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBackground(parent.getBackground());
		toolkit.setBorderStyle(SWT.BORDER);

		scrolledForm = toolkit.createScrolledForm(composite);
		Composite body = scrolledForm.getBody();

		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledForm.setLayoutData(gd);

		Label label = toolkit.createLabel(body, "Name", SWT.LEFT);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		label.setLayoutData(gd);

		Composite separator = toolkit.createComposite(body);
		gd = new GridData(SWT.NONE, SWT.NONE, false, false);
		gd.widthHint = 30;
		gd.heightHint = 20;
		separator.setLayoutData(gd);

		label = toolkit.createLabel(body, "Variable name(s)", SWT.CENTER);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);

		nameText = toolkit.createText(body, defaultName);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		nameText.setLayoutData(gd);

		separator = toolkit.createComposite(body);
		gd = new GridData(SWT.NONE, SWT.NONE, false, false);
		gd.widthHint = 30;
		gd.heightHint = 20;
		separator.setLayoutData(gd);

		for (int i = 0; i < 3; i++) {
			Text text = toolkit.createText(body, "");
			gd = new GridData(SWT.FILL, SWT.NONE, true, false);
			text.setLayoutData(gd);
			varNameTexts.add(text);
		}

		separator = toolkit.createCompositeSeparator(body);
		GridData separatorGD = new GridData();
		separatorGD.heightHint = 5;
		separatorGD.horizontalSpan = 5;
		separator.setLayoutData(separatorGD);

		label = toolkit.createLabel(body, "Guard name(s)", SWT.LEFT);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		label.setLayoutData(gd);

		separator = toolkit.createComposite(body);
		gd = new GridData(SWT.NONE, SWT.NONE, false, false);
		gd.widthHint = 30;
		gd.heightHint = 20;
		separator.setLayoutData(gd);

		label = toolkit.createLabel(body, "Guard predicate(s)", SWT.LEFT);
		gd = new GridData(SWT.FILL, SWT.NONE, true, false);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);

		for (int i = 1; i <= 3; i++) {
			Text text = toolkit.createText(body, "grd" + i);
			gd = new GridData(SWT.FILL, SWT.NONE, false, false);
			text.setLayoutData(gd);
			grdNameTexts.add(text);

			separator = toolkit.createComposite(body);
			gd = new GridData(SWT.NONE, SWT.NONE, false, false);
			gd.widthHint = 30;
			gd.heightHint = 20;
			separator.setLayoutData(gd);

			text = toolkit.createText(body, "");
			gd = new GridData(SWT.FILL, SWT.NONE, true, false);
			gd.horizontalSpan = 3;
			gd.widthHint = 200;
			text.setLayoutData(gd);
			grdPredicateTexts.add(text);
			new EventBMath(text);
		}

		separator = toolkit.createCompositeSeparator(body);
		separator.setLayoutData(separatorGD);

		label = toolkit.createLabel(body, "Action(s)", SWT.LEFT);
		gd = new GridData(SWT.FILL, SWT.NONE, true, false);
		gd.horizontalSpan = 5;
		label.setLayoutData(gd);

		for (int i = 0; i < 3; i++) {
			Text text = toolkit.createText(body, "");
			gd = new GridData(SWT.FILL, SWT.NONE, true, false);
			gd.horizontalSpan = 5;
			gd.widthHint = 250;
			text.setLayoutData(gd);
			actionTexts.add(text);
			new EventBMath(text);
		}

		composite.pack();

		toolkit.paintBordersFor(body);
		applyDialogFont(body);
		return body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CANCEL_ID) {
			name = null;
			varNames = new HashSet<String>();
			grdNames = new HashSet<String>();
			grdPredicates = new HashSet<String>();
			actions = new HashSet<String>();
		} else if (buttonId == IDialogConstants.OK_ID) {
			name = nameText.getText();

			varNames = new ArrayList<String>();
			Object[] varNameList = varNameTexts.toArray();
			for (int i = 0; i < varNameList.length; i++) {
				Text nameText = (Text) varNameList[i];
				if (!nameText.getText().equals("")) {
					varNames.add(nameText.getText());
				}
			}

			grdNames = new ArrayList<String>();
			grdPredicates = new ArrayList<String>();
			Object[] grdNameList = grdNameTexts.toArray();
			Object[] grdPredicateList = grdPredicateTexts.toArray();
			for (int i = 0; i < grdNameList.length; i++) {
				Text predicateText = (Text) grdPredicateList[i];
				if (!predicateText.getText().equals("")) {
					Text nameText = (Text) grdNameList[i];
					grdNames.add(nameText.getText());
					grdPredicates.add(predicateText.getText());
				}
			}

			actions = new ArrayList<String>();
			Object[] actionList = actionTexts.toArray();
			for (int i = 0; i < actionList.length; i++) {
				Text actionText = (Text) actionList[i];
				if (!actionText.getText().equals("")) {
					actions.add(actionText.getText());
				}
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Get the name of the new event.
	 * <p>
	 * @return name of the new event as input by user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the list of local variables of the new event.
	 * <p>
	 * @return the list of new local variables as input by user
	 */
	public String[] getVariables() {
		return (String[]) varNames.toArray(new String[varNames.size()]);
	}

	/**
	 * Get the list of guard names of the new event.
	 * <p>
	 * @return the list of the guard names as input by user
	 */
	public String[] getGrdNames() {
		return (String[]) grdNames.toArray(new String[grdNames.size()]);
	}

	/**
	 * Get the list of guard predicates of the new event.
	 * <p>
	 * @return the list of the guard predicates as input by user
	 */
	public String[] getGrdPredicates() {
		return (String[]) grdPredicates
				.toArray(new String[grdPredicates.size()]);
	}

	/**
	 * Get the list of action of the new event.
	 * <p> 
	 * @return the list the actions as input by user
	 */
	public String[] getActions() {
		return (String[]) actions.toArray(new String[actions.size()]);
	}

}
