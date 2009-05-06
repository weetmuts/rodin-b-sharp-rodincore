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
 *     Systerel - added history support
 *     Systerel - separation of file and root element
 *     Systerel - increased index of label when add new input
 *     Systerel - used label prefix set by user
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.dialogs;

import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.internal.ui.IEventBInputText;
import org.eventb.internal.ui.Pair;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.eventbeditor.EventBEditorUtils;
import org.eventb.ui.eventbeditor.IEventBEditor;

/**
 * @author htson
 *         <p>
 *         This class extends the Dialog class and provides an input dialog for
 *         creating a new variable along with its type invariant and
 *         initilisation.
 */
public class NewVariableDialog extends EventBDialog {

	private final String invPrefix;

	private String invIndex;

	private String identifierResult;

	private Collection<Pair<String, String>> invariantsResult;

	private String initLabelResult;

	private String initSubstitutionResult;

	private IEventBInputText identifierText;

	private Collection<Pair<IEventBInputText, IEventBInputText>> invariantsTexts;

	private IEventBInputText initLabelText;

	private IEventBInputText initSubstitutionText;

	private final IEventBEditor<IMachineRoot> editor;

	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param parentShell
	 *            the parent shell of the dialog
	 * @param title
	 *            the title of the dialog
	 */
	public NewVariableDialog(IEventBEditor<IMachineRoot> editor,
			Shell parentShell, String title,
			String invPrefix) {
		super(parentShell, title);
		this.editor = editor;
		this.invPrefix = invPrefix;
		this.invIndex = getInvariantFirstIndex();
		invariantsTexts = new ArrayList<Pair<IEventBInputText, IEventBInputText>>();
	}

	private String getInvariantFirstIndex() {
		return UIUtils.getFreeElementLabelIndex(editor.getRodinInput(),
				IInvariant.ELEMENT_TYPE, invPrefix);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ADD_ID, ADD_LABEL);
		createButton(parent, MORE_INVARIANT_ID, MORE_INVARIANT_LABEL);
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
		setFormGridLayout(getBody(), 3);
		setFormGridData();

		
		createLabel(getBody(), "Identifier");

		identifierText = createBText(getBody(), EMPTY, 200, true, 2);
		
		createLabel(getBody(), "Initialisation");

		initLabelText = createNameInputText(getBody(),
				getFreeInitialisationActionName());
		initSubstitutionText = createContentInputText(getBody());
		
		identifierText.getTextWidget().addModifyListener(
				new ActionListener(initSubstitutionText.getTextWidget()));

		final Pair<IEventBInputText, IEventBInputText> invariant = createInvariant();

		addGuardListener(identifierText, invariant.getSecond());

		setText(identifierText, getFreeVariable());

		select(identifierText);
	}

	private Pair<IEventBInputText, IEventBInputText> createInvariant() {
		createLabel(getBody(), "Invariant");
		final IEventBInputText invariantNameText = createNameInputText(getBody(),
				getNewInvariantName(invIndex, invariantsTexts.size()));
		final IEventBInputText invariantPredicateText = createContentInputText(getBody());
		final Pair<IEventBInputText, IEventBInputText> p = newWidgetPair(
				invariantNameText, invariantPredicateText);
		invariantsTexts.add(p);
		return p;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CANCEL_ID) {
			identifierResult = null;
			invariantsResult = null;
			initLabelResult = null;
			initSubstitutionResult = null;
		} else if (buttonId == MORE_INVARIANT_ID) {
			createInvariant();
			updateSize();
		} else if (buttonId == OK_ID) {
			setFieldValues();
		} else if (buttonId == ADD_ID) {
			setFieldValues();
			addValues();
			initialise();
		}
		super.buttonPressed(buttonId);
	}
	
	private String getNewInvariantName(String firstIndex, int num) {
		final int index = Integer.parseInt(firstIndex) + num;
		return invPrefix + index;
	}
	
	private void addValues() {

		final String varName = getName();
		final Collection<Pair<String, String>> invariant = getInvariants();
		final String actName = getInitActionName();
		final String actSub = getInitActionSubstitution();
		EventBEditorUtils.newVariable(editor, varName, invariant, actName,
				actSub);

	}

	private void initialise() {
		clearDirtyTexts();
		invIndex = getInvariantFirstIndex();

		int num = 0 ;
		for (Pair<IEventBInputText, IEventBInputText> pair : invariantsTexts) {
			setText(pair.getFirst(), getNewInvariantName(invIndex, num));
			setText(pair.getSecond(), EMPTY);
			num++;
		}	

		setText(initLabelText, getFreeInitialisationActionName());
		setText(initSubstitutionText, EMPTY);
		setText(identifierText, getFreeVariable());
		select(identifierText);
	}

	private String getFreeVariable() {
		return UIUtils.getFreeElementIdentifier(editor.getRodinInput(),
				IVariable.ELEMENT_TYPE);
	}
	
	private String getFreeInitialisationActionName() {
		return EventBEditorUtils.getFreeInitialisationActionName(editor
				.getRodinInput());
	}
	
	private void setFieldValues() {
		invariantsResult = new ArrayList<Pair<String, String>>();

		identifierResult = getText(identifierText);
		fillPairResult(invariantsTexts, invariantsResult);
		if (dirtyTexts.contains(initSubstitutionText.getTextWidget())) {
			initLabelResult = getText(initLabelText);
			initSubstitutionResult = getText(initSubstitutionText);
		} else {
			initLabelResult = null;
			initSubstitutionResult = null;
		}
	}

	/**
	 * Get the variable name.
	 * <p>
	 * 
	 * @return the variable name as input by the user
	 */
	public String getName() {
		return identifierResult;
	}

	/**
	 * Get the invariant name.
	 * <p>
	 * 
	 * @return the invariant name as input by the user
	 */
	public Collection<Pair<String, String>> getInvariants() {
		return invariantsResult;
	}

	/**
	 * Get the initialization action.
	 * <p>
	 * 
	 * @return the initialization action as input by the user
	 */
	public String getInitActionSubstitution() {
		return initSubstitutionResult;
	}

	public String getInitActionName() {
		return initLabelResult;
	}

	@Override
	public boolean close() {
		identifierText.dispose();
		disposePairs(invariantsTexts);
		initSubstitutionText.dispose();
		return super.close();
	}

}