/*******************************************************************************
 * Copyright (c) 2011, 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.prooftreeui.handlers;

import static org.eventb.internal.ui.UIUtils.showView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eventb.internal.ui.prooftreeui.TypeEnvView;

/**
 * Handler for the command <code>org.eventb.ui.showTypeEnv</code>. Shows the
 * type environment view.
 * 
 * @author Nicolas Beauger
 * @author Thomas Muller
 * 
 */
public class ShowTypeEnvHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		showView(TypeEnvView.VIEW_ID);
		return null;
	}

}