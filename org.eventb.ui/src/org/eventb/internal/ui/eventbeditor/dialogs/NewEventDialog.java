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
 *     Systerel - used label prefix set by user
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.dialogs;

import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_LABEL;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachineRoot;
import org.eventb.internal.ui.IEventBInputText;
import org.eventb.internal.ui.Pair;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.eventbeditor.EventBEditorUtils;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class extends the Dialog class and provides an input dialog for
 *         new event with some parameters, guards and actSubstitutions.
 */
public class NewEventDialog extends EventBDialog {

	protected String labelResult;

	protected Collection<String> parsResult;

	private Collection<Pair<String, String>> grdResults;

	private Collection<Pair<String, String>> actResults;

	private IEventBInputText labelText;

	private Collection<IEventBInputText> parTexts;

	private Collection<Pair<IEventBInputText, IEventBInputText>> grdTexts;

	private Collection<Pair<IEventBInputText, IEventBInputText>> actTexts;

	private Composite parComposite;
	
	private Composite actionSeparator; 

	private int grdCount;

	private int parCount;

	private int actCount;

	protected final IEventBEditor<IMachineRoot> editor;
	
	private Composite composite;
	
	private final String guardPrefix;

	private final String actPrefix;
	
	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param parentShell
	 *            the parent shell of the dialog
	 * @param title
	 *            the title of the dialog
	 */
	public NewEventDialog(IEventBEditor<IMachineRoot> editor, Shell parentShell,
			String title) {
		super(parentShell, title);
		this.editor = editor;
		initValue();
		dirtyTexts = new HashSet<Text>();
		
		setShellStyle(getShellStyle() | SWT.RESIZE);

		guardPrefix = getAutoNamePrefix(IGuard.ELEMENT_TYPE);
		actPrefix = getAutoNamePrefix(IAction.ELEMENT_TYPE);
	}

	private void initValue(){
		labelResult = null;
		parsResult = new HashSet<String>();
		grdResults = new HashSet<Pair<String, String>>();
		actResults = new HashSet<Pair<String, String>>();
	}

	private String getAutoNamePrefix(IInternalElementType<?> type) {
		return UIUtils.getAutoNamePrefix(editor.getRodinInput(), type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ADD_ID, ADD_LABEL);
		createButton(parent, MORE_PARAMETER_ID, MORE_PARAMETER_LABEL);
		createButton(parent, MORE_GUARD_ID, MORE_GUARD_LABEL);
		createButton(parent, MORE_ACTION_ID, MORE_ACTION_LABEL);
		createDefaultButton(parent, OK_ID, OK_LABEL);
		createButton(parent, CANCEL_ID, CANCEL_LABEL);
	}

	@Override
	protected void createContents() {
		getBody().setLayout(new FillLayout());
		createDialogContents(getBody());
	}

	private void moveAbove(IEventBInputText text, Control control) {
		text.getTextWidget().moveAbove(control);
	}

	private String getFreeEventLabel() {
		final IMachineRoot root = editor.getRodinInput();
		return UIUtils.getFreeElementLabel(root, IEvent.ELEMENT_TYPE);
	}

	private void createLabel(String text) {
		createLabel(composite, text);
	}

	private void createLabels(String left, String right) {
		createLabel(left);
		createSpace();
		createLabel(right);
	}

	private IEventBInputText createNameText(String value) {
		return createNameInputText(composite, value);
	}

	private IEventBInputText createContentText() {
		return createContentInputText(composite);
	}

	private Composite createSpace() {
		final Composite separator = toolkit.createComposite(composite);
		final GridData gd = new GridData(SWT.NONE, SWT.NONE, false, false);
		gd.widthHint = 30;
		gd.heightHint = 20;
		separator.setLayoutData(gd);
		return separator;
	}
	
	private Composite createSeparator() {
		final Composite separator = toolkit.createCompositeSeparator(composite);
		final GridData gd = new GridData();
		gd.heightHint = 5;
		gd.horizontalSpan = 3;
		separator.setLayoutData(gd);
		return separator;
	}

	private Composite createContainer() {
		final Composite comp = toolkit.createComposite(composite);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		final GridLayout layout = newLayout(1, 0, 10);
		layout.makeColumnsEqualWidth = true;
		comp.setLayoutData(gd);
		comp.setLayout(layout);
		return comp;
	}
	
	private void createDialogContents(Composite parent) {
		parTexts = new ArrayList<IEventBInputText>();
		grdTexts = new ArrayList<Pair<IEventBInputText, IEventBInputText>>();
		actTexts = new ArrayList<Pair<IEventBInputText, IEventBInputText>>();

		composite = toolkit.createComposite(parent);
		setDebugBackgroundColor();
		setFormGridLayout(composite, 3);
		setFormGridData();

		createLabels("Label", "Parameter identifier(s)");

		labelText = createBText(createContainer(), getFreeEventLabel());
		createSpace();
		parComposite = createContainer();

		createSeparator();

		createLabels("Guard label(s)", "Guard predicate(s)");

		for (int i = 1; i <= 3; i++) {
			final IEventBInputText parText = createBText(parComposite, EMPTY);
			final IEventBInputText grdLabel = createNameText(guardPrefix + i);
			createSpace();
			final IEventBInputText grdPredicate = createContentText();

			addGuardListener(parText, grdPredicate);

			parTexts.add(parText);
			grdTexts.add(newWidgetPair(grdLabel, grdPredicate));
		}
		grdCount = 3;
		parCount = 3;

		changeColumn(parComposite, parCount);

		actionSeparator = createSeparator();
		actCount = 0;
		createLabels("Action label(s)", "Action substitution(s)");
		for (int i = 1; i <= 3; i++) {
			createAction();
		}
		select(labelText);
	}

	private void changeColumn(Composite comp, int numColumn) {
		final GridLayout layout = (GridLayout) comp.getLayout();
		layout.numColumns = numColumn;
		comp.setLayout(layout);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CANCEL_ID) {
			initValue();
		} else if (buttonId == MORE_PARAMETER_ID) {
			final IEventBInputText parLabel = createBText(parComposite, EMPTY);
			final IEventBInputText grdPred = createGuard();
			addGuardListener(parLabel, grdPred);

			parCount++;
			changeColumn(parComposite, parCount);
			changeWidthParameter();
			updateSize();
			
			parTexts.add(parLabel);
		} else if (buttonId == MORE_GUARD_ID) {
			createGuard();
			updateSize();
		} else if (buttonId == MORE_ACTION_ID) {
			createAction();
			updateSize();
		} else if (buttonId == OK_ID) {
			setFieldValues();
		} else if (buttonId == ADD_ID) {
			setFieldValues();
			addValues();
			initialise();
			updateSize();
		}
		super.buttonPressed(buttonId);
	}

	private void changeWidthParameter() {
		final GridData gd = (GridData) parComposite.getLayoutData();
		gd.widthHint = 50 * parCount + 10 * (parCount - 1);
	}
	
	private void createAction() {
		actCount++;
		final IEventBInputText actionLabel = createNameText(actPrefix + actCount);
		createSpace();
		final IEventBInputText actionSub = createContentText();
		actTexts.add(newWidgetPair(actionLabel, actionSub));
	}
	
	private IEventBInputText createGuard() {
		final IEventBInputText grdLabel = createNameText(guardPrefix + ++grdCount);
		moveAbove(grdLabel, actionSeparator);
		final Composite separator = createSpace();
		separator.moveAbove(actionSeparator);
		final IEventBInputText grdPred = createContentText();
		moveAbove(grdPred, actionSeparator);
		grdTexts.add(newWidgetPair(grdLabel, grdPred));
		return grdPred;
	}
	
	private void addValues() {
		try {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor pm) throws RodinDBException {

					final String[] grdNames = getGrdLabels();
					final String[] lGrdPredicates = getGrdPredicates();

					final String[] actNames = getActLabels();
					final String[] lActSub = getActSubstitutions();

					final String[] paramNames = parsResult.toArray(new String[parsResult
							.size()]);
					EventBEditorUtils.newEvent(editor, labelResult, paramNames,
							grdNames, lGrdPredicates, actNames, lActSub);

				}

			}, null);

		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initialise() {
		clearDirtyTexts();
		composite.dispose();
		createDialogContents(getBody());
		scrolledForm.reflow(true);
	}

	private void setFieldValues() {
		parsResult = new ArrayList<String>();
		grdResults = new ArrayList<Pair<String, String>>();
		actResults = new ArrayList<Pair<String, String>>();

		labelResult = getText(labelText);
		fillResult(parTexts, parsResult);
		fillPairResult(grdTexts, grdResults);
		fillPairResult(actTexts, actResults);
	}

	/**
	 * Get the label of the new event.
	 * <p>
	 * 
	 * @return label of the new event as input by user
	 */
	public String getLabel() {
		return labelResult;
	}

	/**
	 * Get the list of parameters of the new event.
	 * <p>
	 * 
	 * @return the list of new parameters as input by user
	 */
	public String[] getParameters() {
		return parsResult.toArray(new String[parsResult.size()]);
	}

	/**
	 * Get the list of guard labels of the new event.
	 * <p>
	 * 
	 * @return the list of the guard labels as input by user
	 */
	public String[] getGrdLabels() {
		return getFirst(grdResults);
	}

	/**
	 * Get the list of guard predicates of the new event.
	 * <p>
	 * 
	 * @return the list of the guard predicates as input by user
	 */
	public String[] getGrdPredicates() {
		return getSecond(grdResults);
	}

	/**
	 * Get the list of action labels of the new event.
	 * <p>
	 * 
	 * @return the list of the action labels as input by user
	 */
	public String[] getActLabels() {
		return getFirst(actResults);
	}

	/**
	 * Get the list of action subtitutions of the new event.
	 * <p>
	 * 
	 * @return the list the action substitutions as input by user
	 */
	public String[] getActSubstitutions() {
		return getSecond(actResults);
	}

	@Override
	public boolean close() {
		labelText.dispose();
		disposePairs(grdTexts);
		disposePairs(actTexts);
		return super.close();
	}
}