/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     ETH Zurich - adapted from org.eclipse.jdt.internal.ui.filters.CustomFiltersDialog
 *******************************************************************************/
package org.eventb.internal.ui.prooftreeui;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eventb.core.seqprover.IReasonerDesc;
import org.eventb.core.seqprover.IReasonerRegistry;
import org.eventb.core.seqprover.SequentProver;

public class ProofTreeUIFiltersDialog extends SelectionDialog {

	private static final String SEPARATOR = ","; //$NON-NLS-1$

	private ProofTreeUIPage proofTreeUI;

	static List<RuleFilter> fBuiltInFilters = null;
	static Map<String, IReasonerDesc> reasonerDescs = null;

	CheckboxTableViewer fCheckBoxList;

	Button fEnableUserDefinedPatterns;

	Text fUserDefinedPatterns;

	Stack<ViewerFilter> fFilterDescriptorChangeHistory;

	/**
	 * Creates a dialog to customize Java element filters.
	 * 
	 * @param shell
	 *            the parent shell
	 */
	public ProofTreeUIFiltersDialog(Shell shell, ProofTreeUIPage proofTreeUI) {
		super(shell);

		this.proofTreeUI = proofTreeUI;

		if (fBuiltInFilters == null)
			getFilters();

		fFilterDescriptorChangeHistory = new Stack<ViewerFilter>();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	private void getFilters() {
		if (ProofTreeUIUtils.DEBUG)
			ProofTreeUIUtils.debug("Get initial filter list");
		final IReasonerRegistry reasonerRegistry = 
			SequentProver.getReasonerRegistry();
		String[] reasoners = reasonerRegistry.getRegisteredIDs();
		fBuiltInFilters = new ArrayList<RuleFilter>(reasoners.length);
		reasonerDescs = new HashMap<>(reasoners.length);
		for (String reasoner : reasoners) {
			fBuiltInFilters.add(new RuleFilter(reasoner));
			reasonerDescs.put(reasoner, reasonerRegistry.getReasonerDesc(reasoner));
		}
		// First, sort by user-facing name; if equal (reasoners that have several levels
		// have the same name), use ID to sort by level
		fBuiltInFilters.sort(comparing((RuleFilter filter) -> reasonerDescs.get(filter.reasonerID).getName())
				.thenComparing(filter -> filter.reasonerID));
	}

	@Override
	protected void configureShell(Shell shell) {
		setTitle("Proof Rule Filters");
		setMessage("Please choose a set of reasoners to filter out");
		super.configureShell(shell);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
		// IJavaHelpContextIds.CUSTOM_FILTERS_DIALOG);
	}

	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);

		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		Composite group = composite;

		// Checkbox
		fEnableUserDefinedPatterns = new Button(group, SWT.CHECK);
		fEnableUserDefinedPatterns.setFocus();
		fEnableUserDefinedPatterns.setText("Filter nodes based on their label");

		// Pattern field
		fUserDefinedPatterns = new Text(group, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = convertWidthInCharsToPixels(59);
		fUserDefinedPatterns.setLayoutData(data);
		String patterns = "Some patterns";
		fUserDefinedPatterns.setText(patterns);

		// Info text
		final Label info = new Label(group, SWT.LEFT);
		info.setText("The patterns are separated by commas, where * = anystring, ? = anycharacter");

		// Enabling / disabling of pattern group
		fEnableUserDefinedPatterns.setSelection(false);
		fUserDefinedPatterns.setEnabled(false);
		fEnableUserDefinedPatterns.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = fEnableUserDefinedPatterns.getSelection();
				fUserDefinedPatterns.setEnabled(state);
				if (state)
					fUserDefinedPatterns.setFocus();
			}
		});

		// Filters provided by extension point
		if (fBuiltInFilters.size() > 0)
			createCheckBoxList(group);

		applyDialogFont(parent);
		return parent;
	}

	private void createCheckBoxList(Composite parent) {
		// Filler
		new Label(parent, SWT.NONE);

		Label info = new Label(parent, SWT.LEFT);
		info.setText("Select the rules to be filtered");

		fCheckBoxList = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = fCheckBoxList.getTable().getItemHeight() * 10;
		fCheckBoxList.getTable().setLayoutData(data);

		fCheckBoxList.setLabelProvider(createLabelProvider());
		fCheckBoxList.setContentProvider(new ArrayContentProvider());
		fCheckBoxList.setInput(fBuiltInFilters);
		Object[] filters = proofTreeUI.getFilters();
		if (filters != null)
			setInitialSelections(filters);

		List<?> initialSelection = getInitialElementSelections();
		if (initialSelection != null && !initialSelection.isEmpty())
			checkInitialSelections();

		// Description
		info = new Label(parent, SWT.LEFT);
		info.setText("Description of the rule");

		final Text description = new Text(parent, SWT.LEFT | SWT.WRAP
				| SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = convertHeightInCharsToPixels(3);
		description.setLayoutData(data);
		fCheckBoxList
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
							var filter = (RuleFilter) ((IStructuredSelection) selection).getFirstElement();
							String text = "Id: " + filter.reasonerID;
							int version = reasonerDescs.get(filter.reasonerID).getVersion();
							if (version != IReasonerDesc.NO_VERSION) {
								text += "\nVersion: " + version;
							}
							description.setText(text);
						}
					}
				});
		fCheckBoxList.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (element instanceof ViewerFilter) {
					// renew if already touched
					fFilterDescriptorChangeHistory.remove(element);
					fFilterDescriptorChangeHistory.push((ViewerFilter) element);
				}
			}
		});

		addSelectionButtons(parent);
	}

	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		// Select All button
		String label = "Select all";
		Button selectButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID, label, false);
		// SWTUtil.setButtonDimensionHint(selectButton);
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fCheckBoxList.setAllChecked(true);
				fFilterDescriptorChangeHistory.clear();
				for (ViewerFilter filter : fBuiltInFilters)
					fFilterDescriptorChangeHistory.push(filter);
			}
		};
		selectButton.addSelectionListener(listener);

		// Deselect All button
		label = "Deselect all";
		Button deselectButton = createButton(buttonComposite,
				IDialogConstants.DESELECT_ALL_ID, label, false);
		// SWTUtil.setButtonDimensionHint(deselectButton);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fCheckBoxList.setAllChecked(false);
				fFilterDescriptorChangeHistory.clear();
				for (ViewerFilter filter : fBuiltInFilters)
					fFilterDescriptorChangeHistory.push(filter);
			}
		};
		deselectButton.addSelectionListener(listener);
	}

	private void checkInitialSelections() {
		var patternBuilder = new StringBuilder();
		String separator = "";
		for (Object itemToCheck : getInitialElementSelections()) {
			if (itemToCheck instanceof RuleFilter) {
				fCheckBoxList.setChecked(itemToCheck, true);
			} else if (itemToCheck instanceof RulePatternFilter) {
				patternBuilder.append(separator).append(((RulePatternFilter) itemToCheck).pattern);
				separator = SEPARATOR;
			}
		}
		String pattern = patternBuilder.toString();
		if (!pattern.isEmpty()) {
			fUserDefinedPatterns.setText(pattern);
			fUserDefinedPatterns.setEnabled(true);
			fEnableUserDefinedPatterns.setSelection(true);
		}
	}

	@Override
	protected void okPressed() {
		if (fBuiltInFilters != null) {
			ArrayList<ViewerFilter> result = new ArrayList<ViewerFilter>();
			for (ViewerFilter filter : fBuiltInFilters) {
				if (fCheckBoxList.getChecked(filter)) {
					result.add(filter);
				}
			}
			if (fUserDefinedPatterns.isEnabled()) {
				String rawPattern = fUserDefinedPatterns.getText();
				if (!rawPattern.isBlank()) {
					for (String pattern : rawPattern.split(SEPARATOR)) {
						result.add(new RulePatternFilter(pattern.strip()));
					}
				}
			}
			setResult(result);
		}
		super.okPressed();
	}

	private ILabelProvider createLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				var filter = (RuleFilter) element;
				return reasonerDescs.get(filter.reasonerID).getName();
			}
		};
	}

	/**
	 * @return a stack with the filter descriptor check history
	 * @since 3.0
	 */
	public Stack<ViewerFilter> getFilterDescriptorChangeHistory() {
		return fFilterDescriptorChangeHistory;
	}

	/**
	 * A custom pattern filter for rules.
	 *
	 * Contrary to the default pattern filter, this one only matches the current
	 * element (not its children: this is done in ProofTreeUIPage) and the match is
	 * reversed: elements that match the pattern are hidden, not shown.
	 *
	 * The original pattern is also kept to restore the contents of the text field
	 * when re-opening the filters dialog.
	 */
	private static class RulePatternFilter extends PatternFilter {
		protected final String pattern;

		public RulePatternFilter(String pattern) {
			super();
			this.pattern = pattern;
			setPattern(pattern);
		}

		@Override
		public boolean isElementVisible(Viewer viewer, Object element) {
			return !isLeafMatch(viewer, element);
		}
	}

}
