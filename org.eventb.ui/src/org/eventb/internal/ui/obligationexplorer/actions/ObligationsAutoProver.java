package org.eventb.internal.ui.obligationexplorer.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IEventBFile;
import org.eventb.core.IPSFile;
import org.eventb.core.IPSStatus;
import org.eventb.core.pm.IProofComponent;
import org.eventb.core.pm.IProofManager;
import org.eventb.internal.core.pom.AutoProver;
import org.eventb.internal.ui.EventBUIExceptionHandler;
import org.eventb.internal.ui.EventBUIExceptionHandler.UserAwareness;
import org.eventb.internal.ui.obligationexplorer.ObligationExplorer;
import org.eventb.internal.ui.proofcontrol.ProofControlUtils;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

public class ObligationsAutoProver implements IViewActionDelegate {

	ISelection sel;

	TreeViewer viewer;
	
	public ObligationsAutoProver() {
		// TODO Auto-generated constructor stub
	}

	public void init(IViewPart view) {
		// The enablement condition should guarantee that the part is the
		// Obligation Explorer.
		assert view instanceof ObligationExplorer;
		
		viewer = ((ObligationExplorer) view).getTreeViewer();
		// The viewer must be initialised.
		assert viewer != null;
	}

	public void run(IAction action) {
		// Rerun the auto prover on selected elements.
		// The enablement condition guarantees that only machineFiles and
		// contextFiles are selected.
		
		assert (sel instanceof IStructuredSelection);
		IStructuredSelection ssel = (IStructuredSelection) sel;
		
		final Object [] objects = TreeSupports.treeSelectionToSet(viewer, ssel); 
				
		// Run the auto prover on all remaining POs
		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				final IProofManager pm = EventBPlugin.getProofManager();
				for (Object obj : objects) {
					if (obj instanceof IRodinProject) {
						// Run the Auto Prover on all IPSFile in this project
						IRodinProject rodinPrj = (IRodinProject) obj;
						IPSFile[] psFiles;
						try {
							psFiles = rodinPrj
									.getChildrenOfType(IPSFile.ELEMENT_TYPE);
						} catch (RodinDBException e) {
							EventBUIExceptionHandler
									.handleGetChildrenException(e,
											UserAwareness.IGNORE);
							continue;
						}
						for (IPSFile psFile : psFiles) {
							IProofComponent pc = pm.getProofComponent(psFile);
							IPSStatus[] statuses;
							try {
								statuses = psFile.getStatuses();
							} catch (RodinDBException e) {
								EventBUIExceptionHandler
										.handleGetChildrenException(e,
												UserAwareness.IGNORE);
								continue;
							}
							try {
								AutoProver.run(pc, statuses, monitor);
							} catch (RodinDBException e) {
								EventBUIExceptionHandler.handleRodinException(
										e, UserAwareness.IGNORE);
								continue;
							}							
						}
					}
					if (obj instanceof IEventBFile) {
						final IEventBFile file = (IEventBFile) obj;
						final IProofComponent pc = pm.getProofComponent(file);
						IPSStatus[] statuses;
						try {
							statuses = pc.getPSFile().getStatuses();
						} catch (RodinDBException e) {
							EventBUIExceptionHandler
									.handleGetChildrenException(e,
											UserAwareness.IGNORE);
							continue;
						}
						try {
							AutoProver.run(pc, statuses, monitor);
							// RecalculateAutoStatus.run(prFile, psFile, statuses, monitor);
						} catch (RodinDBException e) {
							EventBUIExceptionHandler.handleRodinException(
									e, UserAwareness.IGNORE);
							continue;
						}
					}
					
					if (obj instanceof IPSStatus) {
						
						IPSStatus status = (IPSStatus)obj;
						IPSFile psFile = (IPSFile) status.getOpenable();
						final IProofComponent pc = pm.getProofComponent(psFile);
						IPSStatus[] statuses = new IPSStatus[]{status};
						try {
							AutoProver.run(pc, statuses, monitor);
							// RecalculateAutoStatus.run(prFile, psFile, statuses, monitor);
						} catch (RodinDBException e) {
							EventBUIExceptionHandler.handleRodinException(
									e, UserAwareness.IGNORE);
							continue;
						}

					}
				}
			}
			
		};
		
		runWithProgress(op);
	}


	private void runWithProgress(IRunnableWithProgress op) {
		final Shell shell = Display.getDefault().getActiveShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, op);
		} catch (InterruptedException exception) {
			if (ProofControlUtils.DEBUG)
				ProofControlUtils.debug("Interrupt");
			return;
		} catch (InvocationTargetException exception) {
			final Throwable realException = exception.getTargetException();
			if (ProofControlUtils.DEBUG)
				ProofControlUtils.debug("Interrupt");
			realException.printStackTrace();
			final String message = realException.getMessage();
			MessageDialog.openError(shell, "Unexpected Error", message);
			return;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.sel = selection;
	}

}
