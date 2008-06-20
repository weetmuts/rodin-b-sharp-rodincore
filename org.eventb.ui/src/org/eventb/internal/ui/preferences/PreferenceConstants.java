/*******************************************************************************
 * Copyright (c) 2006, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added "show borders" and "font color" options
 *******************************************************************************/
package org.eventb.internal.ui.preferences;

/**
 * @author htson
 *         <p>
 *         Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	/**
	 * Preference key for the enablement of Post-Tactics. 
	 */
	public static final String P_POSTTACTIC_ENABLE = "Post-Tactic enable"; //$NON-NLS-1$

	/**
	 * Preference key for the list of selected Post-Tactics.
	 */
	public static final String P_POSTTACTICS = "Post-Tactics"; //$NON-NLS-1$

	/**
	 * Preference key for the enablement of Auto-Tactics.
	 */
	public static final String P_AUTOTACTIC_ENABLE = "Auto-Tactic enable"; //$NON-NLS-1$

	/**
	 * Preference key for the list of selected Auto-Tactics.
	 */
	public static final String P_AUTOTACTICS = "Auto-Tactics"; //$NON-NLS-1$

	/**
	 * Preference key for the list of machine editor pages.
	 */
	public static final String P_MACHINE_EDITOR_PAGE = "Machine editor pages"; //$NON-NLS-1$

	/**
	 * Preference key for the list of context editor pages.
	 */
	public static final String P_CONTEXT_EDITOR_PAGE = "Context editor pages"; //$NON-NLS-1$

	/**
	 * Preference key for the enablement of border drawing.
	 */
	public static final String P_BORDER_ENABLE = "Border enable"; //$NON-NLS-1$

	/**
	 * Preference key for the choice of font color.
	 */
	public static final String P_TEXT_FOREGROUND = "Text foreground"; //$NON-NLS-1$
}
