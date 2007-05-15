/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;
import org.eventb.eventBKeyboard.EventBStyledTextModifyListener;
import org.eventb.eventBKeyboard.Text2EventBMathTranslator;

/**
 * @author htson
 *         <p>
 *         This is the class that hold a Text using to display and to retrive
 *         expressions which are in the mathematical language of Event-B.
 */
public class EventBStyledText extends EventBControl implements IEventBInputText {

	boolean translate;
	
	EventBStyledTextModifyListener listener;
	
	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param text
	 *            a Text widget
	 */
	public EventBStyledText(final StyledText text) {
		super(text);
		listener = new EventBStyledTextModifyListener();
		text.addModifyListener(listener);
		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// Do nothing
			}

			public void focusLost(FocusEvent e) {
				if (text.getEditable() && translate) {
					String translateStr = Text2EventBMathTranslator.translate(text
							.getText());
					if (!text.getText().equals(translateStr)) {
						text.setText(translateStr);
					}
				}
				commit();
			}

		});
	}

	protected void commit() {
		// Do nothing. Client should override this method.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.IEventBInputText#getTextWidget()
	 */
	public Text getTextWidget() {
		return (Text) getControl();
	}

	public void setTranslate(boolean translate) {
		this.translate = translate;
		listener.setEnable(translate);
	}

}