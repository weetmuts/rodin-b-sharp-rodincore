/*******************************************************************************
 * Copyright (c) 2005 ETH-Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH RODIN Group
 *******************************************************************************/

package org.eventb.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eventb.internal.ui.EventBUIPlugin;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;

/**
 * @author htson
 * This is a sample new wizard. Its role is to create a new project 
 * resource.
 */
public class NewProjectWizard 
	extends Wizard 
	implements INewWizard 
{
	public static final String WIZARD_ID = EventBUIPlugin.PLUGIN_ID + ".wizards.NewProjectWizard";
	
	// The wizard page.
	private NewProjectWizardPage page;

	// The selection when the wizard is launched (this is not used for this wizard).
	private ISelection selection;


	/**
	 * Constructor for NewProjectWizard.
	 */
	public NewProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewProjectWizardPage(selection);
		addPage(page);
	}


	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(projectName, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	
	/**
	 * The worker method. This will create a new project 
	 * (provided that it does not exist before).
	 */
	private void doFinish(
		String projectName,
		IProgressMonitor monitor)
		throws CoreException {
		// create an empty Rodin project
		monitor.beginTask("Creating " + projectName, 1);
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(projectName));
		
		if (resource != null) {
			throwCoreException("Project \"" + projectName + "\" is already existed.");
			return;
		}
		
		try {
			IRodinProject rodinProject = EventBUIPlugin.getRodinDatabase().getRodinProject(projectName);
			
			IProject project = rodinProject.getProject();
			if (!project.exists()) project.create(null);
			project.open(null);
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { RodinCore.NATURE_ID });
			project.setDescription(description, null);
			monitor.worked(1);
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Throw a Core exception.
	 * <p>
	 * @param message The message for displaying
	 * @throws CoreException a Core exception with the status contains the input message
	 */
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.eventb.internal.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * <p>
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}