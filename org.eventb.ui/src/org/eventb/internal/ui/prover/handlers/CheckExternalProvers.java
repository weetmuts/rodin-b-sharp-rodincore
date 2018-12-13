/*******************************************************************************
 * Copyright (c) 2018 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.prover.handlers;

import static org.eclipse.jface.dialogs.ErrorDialog.openError;
import static org.eclipse.jface.dialogs.MessageDialog.openInformation;
import static org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindowChecked;
import static org.eclipse.ui.statushandlers.StatusManager.LOG;
import static org.eclipse.ui.statushandlers.StatusManager.SHOW;
import static org.eventb.core.seqprover.SequentProver.checkAutoTactics;
import static org.eventb.internal.ui.UIUtils.log;
import static org.eventb.internal.ui.handlers.Messages.proof_checkExternalProvers_error_message;
import static org.eventb.internal.ui.handlers.Messages.proof_checkExternalProvers_ok_message;
import static org.eventb.internal.ui.handlers.Messages.proof_checkExternalProvers_title;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eventb.ui.EventBUIPlugin;

/**
 * Default handler for the check external prover command.
 *
 * @author Laurent Voisin
 */
public class CheckExternalProvers extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = getActiveWorkbenchWindowChecked(event);
		final IProgressService ps = window.getWorkbench().getProgressService();
		try {
			ps.busyCursorWhile(pm -> run(window.getShell(), pm));
		} catch (InterruptedException e) {
			// Do nothing
		} catch (InvocationTargetException e) {
			final IStatus status = new Status(IStatus.ERROR, //
					EventBUIPlugin.PLUGIN_ID, //
					e.getLocalizedMessage(), e);
			StatusManager.getManager().handle(status, SHOW | LOG);
		}
		return null;
	}

	private void run(Shell shell, IProgressMonitor pm) {
		final IStatus status = checkAutoTactics(true, pm);
		shell.getDisplay().asyncExec(() -> {
			if (status.isOK()) {
				openInformation(shell, proof_checkExternalProvers_title, //
						proof_checkExternalProvers_ok_message);
			} else {
				log(status);
				openError(shell, proof_checkExternalProvers_title, //
						proof_checkExternalProvers_error_message, status);
			}
		});
	}

}
