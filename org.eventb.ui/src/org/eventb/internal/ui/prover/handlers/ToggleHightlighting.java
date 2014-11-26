/*******************************************************************************
 * Copyright (c) 2011, 2014 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.prover.handlers;

import static org.eclipse.ui.handlers.HandlerUtil.getActiveEditorChecked;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.internal.ui.preferences.EventBPreferenceStore;
import org.eventb.internal.ui.preferences.PreferenceConstants;
import org.eventb.internal.ui.prover.ProverUI;


/**
 * Toggles the preference for the highlighting using selection.
 */
public class ToggleHightlighting extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Command command = event.getCommand();
	    boolean oldValue = HandlerUtil.toggleCommandState(command);
		EventBPreferenceStore.getPreferenceStore().setValue(
				PreferenceConstants.P_HIGHLIGHT_IN_PROVERUI, !oldValue);
		final IEditorPart activeEditor = getActiveEditorChecked(event);
		if (activeEditor instanceof ProverUI) {
			final ProverUI pu = ((ProverUI) activeEditor);
			pu.getHighlighter().activateHighlight(!oldValue);
		}
		return null;
	}

}
