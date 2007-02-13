package org.eventb.internal.ui.utils;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String BUNDLE_NAME = "org.eventb.internal.ui.utils.messages"; //$NON-NLS-1$

	public static String editorPage_axiomPage_title;
	public static String editorPage_axiomPage_tabTitle;
	public static String editorPage_eventPage_title;
	public static String editorPage_eventPage_tabTitle;
	public static String editorPage_constantPage_title;
	public static String editorPage_constantPage_tabTitle;
	public static String editorPage_variablePage_title;
	public static String editorPage_variablePage_tabTitle;
	public static String editorPage_theoremPage_title;
	public static String editorPage_theoremPage_tabTitle;
	public static String editorPage_carrierSetPage_title;
	public static String editorPage_carrierSetPage_tabTitle;
	public static String editorPage_dependencyPage_title;
	public static String editorPage_dependencyPage_tabTitle;
	public static String editorPage_invariantPage_title;
	public static String editorPage_invariantPage_tabTitle;
	public static String editorPage_prettyPrint_title;
	public static String editorPage_prettyPrint_tabTitle;
	public static String editorPage_edit_title;
	public static String editorPage_edit_tabTitle;
	public static String editorPage_synthethicViewPage_title;
	public static String editorPage_synthethicViewPage_tabTitle;
	
	public static String editorAction_paste_title;
	public static String editorAction_paste_toolTip;
	public static String editorAction_copy_title;
	public static String editorAction_copy_toolTip;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 * 
	 * @param message the message to be manipulated
	 * @param bindings An array of objects to be inserted into the message
	 * @return the manipulated String
	 */
	public static String bind(String message, Object... bindings) {
		return MessageFormat.format(message, bindings);
	}
	
	private Messages() {
		// Do not instantiate
	}

}
