package org.eventb.internal.ui.eventbeditor.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class AtomicOperation extends AbstractOperation {

	protected final OperationTree command ;
	
	public AtomicOperation(OperationTree command) {
		super("AtomicCommand");
		this.command = command ;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor m) throws RodinDBException {
					try {
						command.execute(m, info) ;
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}, monitor);
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}


	@Override
	public IStatus redo(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor m) throws RodinDBException {
					try {
						command.redo(m, info) ;
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				}, monitor);
			return Status.OK_STATUS;
		} catch (RodinDBException e) {
			return e.getStatus();
		}
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			RodinCore.run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor m) throws RodinDBException {
					try {
						command.undo(m, info) ;
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}, monitor);
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

}
