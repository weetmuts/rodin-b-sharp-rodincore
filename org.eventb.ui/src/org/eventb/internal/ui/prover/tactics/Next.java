package org.eventb.internal.ui.prover.tactics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IUserSupport;
import org.eventb.ui.prover.IProofCommand;
import org.rodinp.core.RodinDBException;

public class Next implements IProofCommand {

	@Override
	public void apply(IUserSupport us, Predicate hyp, String[] inputs,
			IProgressMonitor monitor) throws RodinDBException {
		us.nextUndischargedPO(false, monitor);
	}

	@Override
	public boolean isApplicable(IUserSupport us, Predicate hyp, String input) {
		return true;
	}

}
