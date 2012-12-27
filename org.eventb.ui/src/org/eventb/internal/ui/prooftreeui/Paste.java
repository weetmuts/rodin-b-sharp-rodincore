/*******************************************************************************
 * Copyright (c) 2007, 2012 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - Refactored defining AbstractProofTreeAction
 *******************************************************************************/
package org.eventb.internal.ui.prooftreeui;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eventb.core.seqprover.IProofSkeleton;
import org.eventb.core.seqprover.tactics.BasicTactics;

public class Paste extends AbstractProofTreeAction {

	public Paste() {
		super(true);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		super.setUserSupport(targetPart);
	}

	@Override
	public void run(IAction action) {
		assert ProofTreeUI.buffer instanceof IProofSkeleton;
		final IProofSkeleton copyNode = (IProofSkeleton) ProofTreeUI.buffer;

		applyTactic(BasicTactics.rebuildTac(copyNode), true);

		if (ProofTreeUIUtils.DEBUG)
			ProofTreeUIUtils.debug("Paste: " + copyNode);
	}

	@Override
	protected boolean isEnabled(IAction action) {
		if (isInProofSkeletonView(action)) {
			traceDisabledness("In proof skeleton view", action);
			return false;
		}
		if (!isUserSupportPresent(action)) {
			traceDisabledness("No user support present", action);
			return false;
		}
		if (ProofTreeUI.buffer == null) {
			traceDisabledness("The copy buffer is empty", action);
			return false;
		}
		if (!(ProofTreeUI.buffer instanceof IProofSkeleton)) {
			traceDisabledness("The copy buffer is not a proof skeleton", action);
			return false;
		}
		return super.isEnabled(action);
	}

}
