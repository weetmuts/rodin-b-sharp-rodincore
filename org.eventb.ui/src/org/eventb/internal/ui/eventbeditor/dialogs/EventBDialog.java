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
 *     Systerel - add getNameInputText and getContentInputText to factor several methods
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.dialogs;

import static org.eclipse.jface.dialogs.IDialogConstants.CLIENT_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eventb.eventBKeyboard.Text2EventBMathTranslator;
import org.eventb.internal.ui.EventBMath;
import org.eventb.internal.ui.EventBSharedColor;
import org.eventb.internal.ui.EventBText;
import org.eventb.internal.ui.IEventBInputText;
import org.eventb.internal.ui.Pair;
import org.eventb.internal.ui.eventbeditor.EventBEditorUtils;

/**
 * @author htson
 *         <p>
 *         This class extends the Dialog class and provides an input dialog for
 *         new event with some local varialbes, guards and actSubstitutions.
 */
public abstract class EventBDialog extends Dialog {
	protected Collection<Text> dirtyTexts;

	protected FormToolkit toolkit;

	protected ScrolledForm scrolledForm;

	private String title;

	private final int MAX_WIDTH = 800;

	private final int MAX_HEIGHT = 500;

	protected static final int ADD_ID = CLIENT_ID + 1;
	protected static final int MORE_PARAMETER_ID = CLIENT_ID + 2;
	protected static final int MORE_GUARD_ID = CLIENT_ID + 3;
	protected static final int MORE_ACTION_ID = CLIENT_ID + 4;
	protected static final int MORE_INVARIANT_ID = CLIENT_ID + 5;
	protected static final int MORE_AXIOM_ID = CLIENT_ID + 6;
	protected static final int MORE_ELEMENT_ID = CLIENT_ID + 7;
	protected static final int MORE_ID = CLIENT_ID + 8;

	protected static final String MORE_PARAMETER_LABEL = "More &Par.";
	protected static final String MORE_GUARD_LABEL = "More &Grd.";
	protected static final String MORE_ACTION_LABEL = "More A&ct.";
	protected static final String MORE_INVARIANT_LABEL = "&More Inv.";
	protected static final String MORE_AXIOM_LABEL = "&More Axm.";
	protected static final String MORE_ELEMENT_LABEL = "&More Element";
	protected static final String MORE_LABEL = "&More";
	protected static final String ADD_LABEL = "&Add";
	protected static final String EMPTY = "";

	private final int FORM_SPACING = 10;

	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param parentShell
	 *            the parent shell of the dialog
	 * @param title
	 *            the title of the dialog
	 */
	public EventBDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
		dirtyTexts = new HashSet<Text>();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBackground(parent.getBackground());
		toolkit.setBorderStyle(SWT.BORDER);

		scrolledForm = toolkit.createScrolledForm(composite);
		final Composite body = scrolledForm.getBody();

		createContents();

		composite.pack();

		toolkit.paintBordersFor(body);
		applyDialogFont(body);
		return body;
	}

	protected abstract void createContents();

	protected abstract class AbstractListener implements ModifyListener{
		private final Text textWidget;

		public AbstractListener(Text textWidget) {
			this.textWidget = textWidget;
		}

		public void modifyText(ModifyEvent e) {
			final Text varText = (Text) e.widget;
			if (!dirtyTexts.contains(textWidget)) {
				final String text = varText.getText();
				if (text.equals(EMPTY))
					textWidget.setText(EMPTY);
				else
					textWidget.setText(text + " " + getSymbol() + " ");
			}
		}
		
		protected abstract String getSymbol();
	}
	
	protected class GuardListener extends AbstractListener {
		public GuardListener(Text textWidget) {
			super(textWidget);
		}

		@Override
		protected String getSymbol() {
			return "\u2208";
		}
	}

	protected class ActionListener extends AbstractListener {
		public ActionListener(Text textWidget) {
			super(textWidget);
		}

		@Override
		protected String getSymbol() {
			return "\u2254";
		}
	}

	protected class DirtyStateListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			final Text text = (Text) e.widget;
			if (EventBEditorUtils.DEBUG)
				EventBEditorUtils.debug("Modified: " + text.getText());
			if (text.getText().equals(EMPTY)) {
				dirtyTexts.remove(text);
				text.setBackground(EventBSharedColor.getSystemColor(SWT.COLOR_WHITE));
			} else if (text.isFocusControl()) {
				dirtyTexts.add(text);
				text.setBackground(EventBSharedColor.getSystemColor(SWT.COLOR_YELLOW));
			}
		}
	}

	protected void clearDirtyTexts() {
		for (Text text : dirtyTexts) {
			text.setBackground(EventBSharedColor.getSystemColor(SWT.COLOR_WHITE));
		}
		dirtyTexts.clear();
	}

	protected void updateSize() {
		final Composite parent = this.getContents().getParent();
		final Point curr = parent.getSize();
		final Point pt = parent.computeSize(SWT.DEFAULT,
				SWT.DEFAULT);

		if (curr.x < pt.x || curr.y < pt.y) {
			final int x = curr.x < pt.x ? pt.x : curr.x;
			final int y = curr.y < pt.y ? pt.y : curr.y;
			if (x <= MAX_WIDTH && y <= MAX_HEIGHT)
				parent.setSize(x, y);
		}
		else { // Bug: resize to force refresh
			parent.setSize(curr.x + 1, curr.y);
		}
		scrolledForm.reflow(true);
	}

	protected IEventBInputText createNameInputText(Composite composite, String text) {
		return new EventBText(createText(composite, text, 50, false));
	}

	protected EventBMath createContentInputText(Composite composite) {
		return new EventBMath(createText(composite, EMPTY, 150, true));
	}
	
	protected EventBText createBText(Composite parent, String value) {
		return new EventBText(createText(parent, value, true));
	}

	protected EventBText createBText(Composite parent, String value,
			int widthHint, boolean grabExcessHorizontalSpace) {
		return new EventBText(createText(parent, value, newGridData(
				grabExcessHorizontalSpace, widthHint)));
	}

	protected EventBText createBText(Composite parent, String value,
			int widthHint, boolean grabExcessHorizontalSpace, int horizontalSpan) {
		return new EventBText(createText(parent, value, newGridData(true,
				widthHint, horizontalSpan)));
	}

	private Text createText(Composite parent, String value,
			boolean grabExcessHorizontalSpace) {
		final GridData gd = newGridData(grabExcessHorizontalSpace);
		return createText(parent, value, gd);
	}

	protected GridData newGridData(boolean grabExcessHorizontalSpace) {
		return new GridData(SWT.FILL, SWT.NONE, grabExcessHorizontalSpace,
				false);
	}

	protected GridData newGridData(boolean grabExcessHorizontalSpace,
			int widthHint) {
		final GridData gd = newGridData(grabExcessHorizontalSpace);
		gd.widthHint = widthHint;
		return gd;
	}

	protected GridData newGridData(boolean grabExcessHorizontalSpace,
			int widthHint, int horizontalSpan) {
		final GridData gd = newGridData(grabExcessHorizontalSpace, widthHint);
		gd.horizontalSpan = horizontalSpan;
		return gd;
	}

	private Text createText(Composite parent, String value, int widthHint,
			boolean grabExcessHorizontalSpace) {
		final GridData gd = newGridData(grabExcessHorizontalSpace, widthHint);
		return createText(parent, value, gd);
	}

	private Text createText(Composite parent, String value, GridData gd) {
		final Text text = toolkit.createText(parent, value);
		text.setLayoutData(gd);
		text.addModifyListener(new DirtyStateListener());
		return text;
	}
	
	protected void createLabel(Composite parent, String text) {
		final Label widget = toolkit.createLabel(parent, text, SWT.NONE);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		widget.setLayoutData(gd);
	}

	protected GridLayout newLayout(int numColumns, int verticalSpacing,
			int horizontalSpacing) {
		final GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.verticalSpacing = verticalSpacing;
		layout.horizontalSpacing = horizontalSpacing;
		return layout;
	}
	
	protected void setFormGridLayout(Composite composite, int numColumns) {
		composite.setLayout(newLayout(numColumns, FORM_SPACING, FORM_SPACING));
	}
	
	protected void select(IEventBInputText text){
		text.getTextWidget().selectAll();
		text.getTextWidget().setFocus();
	}
	
	protected void dispose(Collection<IEventBInputText> collection) {
		for (IEventBInputText text : collection)
			text.dispose();
	}

	protected void disposePairs(
			Collection<Pair<IEventBInputText, IEventBInputText>> pairs) {
		for (Pair<IEventBInputText, IEventBInputText> pair : pairs) {
			pair.getFirst().dispose();
			pair.getSecond().dispose();
		}
	}
	
	protected void setDebugBackgroundColor() {
		if (EventBEditorUtils.DEBUG)
			scrolledForm.getBody().setBackground(
					EventBSharedColor.getSystemColor(SWT.COLOR_CYAN));
	}
	
	protected void setFormGridData() {
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledForm.setLayoutData(gd);
	}
	
	protected void setText(IEventBInputText text, String value){
		text.getTextWidget().setText(value);
	}
	
	protected void addGuardListener(IEventBInputText text,
			IEventBInputText toModify) {
		text.getTextWidget().addModifyListener(
				new GuardListener(toModify.getTextWidget()));
	}
	
	protected Composite getBody() {
		return scrolledForm.getBody();
	}
	
	protected void createDefaultButton(Composite parent, int id, String text) {
		createButton(parent, id, text, true);
	}

	protected void createButton(Composite parent, int id, String text) {
		createButton(parent, id, text, false);
	}
	
	protected boolean isValid(IEventBInputText text){
		return dirtyTexts.contains(text.getTextWidget());
	}

	protected void fillResult(Collection<IEventBInputText> fields,
			Collection<String> results) {
		for (IEventBInputText field : fields) {
			final Text text = field.getTextWidget();
			if (dirtyTexts.contains(text)) {
				results.add(text.getText());
			}
		}
	}

	protected void fillPairResult(
			Collection<Pair<IEventBInputText, IEventBInputText>> fields,
			Collection<Pair<String, String>> result) {
		for (Pair<IEventBInputText, IEventBInputText> pair : fields) {
			final IEventBInputText labelInput = pair.getFirst();
			final IEventBInputText contentInput = pair.getSecond();
			if (dirtyTexts.contains(contentInput.getTextWidget())) {
				final String name = getText(labelInput);
				result.add(new Pair<String, String>(name,
						translate(contentInput)));
			}
		}
	}
	
	protected String getText(IEventBInputText text) {
		return text.getTextWidget().getText();
	}

	protected String translate(IEventBInputText text) {
		return Text2EventBMathTranslator.translate(text.getTextWidget()
				.getText());
	}
	
	protected String[] getFirst(Collection<Pair<String, String>> pairs) {
		final Collection<String> result = new ArrayList<String>();
		for (Pair<String, String> p : pairs)
			result.add(p.getFirst());
		return result.toArray(new String[result.size()]);
	}

	protected String[] getSecond(Collection<Pair<String, String>> pairs) {
		final Collection<String> result = new ArrayList<String>();
		for (Pair<String, String> p : pairs)
			result.add(p.getSecond());
		return result.toArray(new String[result.size()]);
	}
	
	protected Pair<IEventBInputText, IEventBInputText> newWidgetPair(
			IEventBInputText name, IEventBInputText content) {
		return new Pair<IEventBInputText, IEventBInputText>(name, content);
	}
}