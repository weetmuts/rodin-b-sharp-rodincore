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

package org.eventb.internal.ui.prover;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IPRFile;
import org.eventb.core.IPRSequent;
import org.eventb.core.pm.IPOChangeEvent;
import org.eventb.core.pm.IPOChangedListener;
import org.eventb.core.pm.UserSupport;
import org.eventb.internal.ui.EventBUIPlugin;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.obligationexplorer.ObligationExplorer;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class ProverUI
	extends FormEditor
	implements IPOChangedListener
{

	/**
	 * The plug-in identifier of the Prover UI (value
	 * <code>"org.eventb.internal.ui"</code>).
	 */
	public static final String EDITOR_ID = EventBUIPlugin.PLUGIN_ID + ".editors.ProverUI";

	/** The outline page */
	private ProofTreeUIPage fProofTreeUI;
	
	private ProofControlPage fProofControlPage;
	
	private ProofInformationPage fProofInformationPage;
	
	private UserSupport userSupport;
	
	
	// The associated rodin file handle
	private IPRFile prFile = null;
	
	
	
	/**
	 * Default constructor.
	 */
	public ProverUI() {
		super();
		this.userSupport = new UserSupport();
		userSupport.addPOChangedListener(this);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void setInput(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			IFile inputFile = ((IFileEditorInput) input).getFile();
			prFile = (IPRFile) RodinCore.create(inputFile);
			try {
				userSupport.setInput(prFile);
			}
			catch (RodinDBException e) {
				e.printStackTrace();
			}
			this.setPartName(EventBPlugin.getComponentName(prFile.getElementName()));
		}
		super.setInput(input);
	}

//	private IPRFile getPRFileInput() {
//		if (prFile == null) {
//			IFile inputFile = ((IFileEditorInput) this.getEditorInput()).getFile();
//			prFile = (IPRFile) RodinCore.create(inputFile);
//		}
//		return prFile;
//	}
	
	public void setCurrentPO(IPRSequent prSequent) {
		try {
			userSupport.setCurrentPO(prSequent);
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}
	}
	
	public UserSupport getUserSupport() {return userSupport;}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void addPages() {
		try {
			addPage(new ProofsPage(this));
		}
		catch (PartInitException e) {
			MessageDialog.openError(null,
					"Prover UI Editor",
					"Error creating pages for Prover UI Editor");
			//TODO Handle exception
		}
	}

	
	/** The <code>EventBMachineEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs any extra 
	 * disposal actions required by the event-B editor.
	 */
	public void dispose() {
		if (fProofTreeUI != null)
			fProofTreeUI.setInput(null);
		super.dispose();
	}	
			
	/**
	 * The <code>ProverUI</code> implementation of this 
	 * method performs gets the content outline page if request
	 * is for a an outline page.
	 * <p> 
	 * @param required the required type
	 * <p>
	 * @return an adapter for the required type or <code>null</code>
	 */ 
	public Object getAdapter(Class required) {
		if (IProofTreeUIPage.class.equals(required)) {
			if (fProofTreeUI == null) {
				fProofTreeUI = new ProofTreeUIPage(userSupport);
				if (userSupport.getCurrentPO() != null) {
					fProofTreeUI.setInput(userSupport.getCurrentPO().getProofTree());
				}
			}
			return fProofTreeUI;
		}
		if (IProofControlPage.class.equals(required)) {
			if (fProofControlPage == null) {
				fProofControlPage = new ProofControlPage(this);
				//if (getEditorInput() != null)
					// fRemainingGoalsPage.setInput(userSupport.getCurrentPO());
			}
			return fProofControlPage;
		}

		if (IProofInformationPage.class.equals(required)) {
			if (fProofInformationPage == null) {
				fProofInformationPage = new ProofInformationPage(this);
				//if (getEditorInput() != null)
					// fRemainingGoalsPage.setInput(userSupport.getCurrentPO());
			}
			return fProofInformationPage;
		}
		return super.getAdapter(required);
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		MessageDialog.openInformation(null, null, "Saving");
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		//try {
			// TODO Commit the information in the UI to the database
			// clear the dirty state on all the pages
			if (this.pages != null) {
				for (int i = 0; i < pages.size(); i++) {
					Object page = pages.get(i);
					//if (UIUtils.DEBUG) System.out.println("Trying to save page " + i + " : " + page);
					if (page instanceof IFormPage) {
						//if (UIUtils.DEBUG) System.out.println("Saving");
						IFormPage fpage = (IFormPage) page;
						
						fpage.doSave(monitor);
						//if (UIUtils.DEBUG) System.out.println("Dirty? " + i + " " + fpage.isDirty());
					}
				}
			}

		// Save the file from the database to disk
		try {
			UIUtils.debug("Save to disk");
			IPRFile prFile = this.getRodinInput();
			prFile.save(monitor, true);
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}

		editorDirtyStateChanged(); // Refresh the dirty state of the editor
	}

	
	/**
	 * Getting the outline page associated with this editor
	 * @return the outline page
	 */
	protected ProofTreeUIPage getProofTreeUI() {return fProofTreeUI;}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#setFocus()
	 */
	@Override
	public void setFocus() {
		super.setFocus();
		UIUtils.debug("Focus");
		// Find obligationExplorer and sync
		syncObligationExplorer();
	}
	
	private void syncObligationExplorer() {
		UIUtils.debug("Sync");
		IWorkbenchPage activePage = EventBUIPlugin.getActivePage();
		if (activePage != null) {
			ObligationExplorer obligationExplorer = (ObligationExplorer) activePage.findView(ObligationExplorer.VIEW_ID);
			if (obligationExplorer != null) {
				IPRSequent prSequent = this.getUserSupport().getCurrentPO().getPRSequent();
//				TreeViewer viewer = obligationExplorer.getTreeViewer();
//				UIUtils.debug("Make new selection ");
				obligationExplorer.externalSetSelection(prSequent);
				obligationExplorer.getTreeViewer().reveal(prSequent);
			}
		}
	}
//	public void setSelection(Object obj) {
//		
//		this.setActivePage(ProofsPage.PAGE_ID);
//		
//		// select the element within the page
//		ProofsPage page = (ProofsPage) this.getActivePageInstance();
//		page.setSelection(obj);
//		return;
//	}
	
	/**
	 * Getting the RodinFile associated with this editor
	 * <p>
	 * @return a handle to a Rodin file
	 */
	private IPRFile getRodinInput() {
		if (prFile == null) {
			FileEditorInput editorInput = (FileEditorInput) this.getEditorInput();
			
			IFile inputFile = editorInput.getFile();
			
			prFile = (IPRFile) RodinCore.create(inputFile);
		}
		return prFile;
	}
	
	public IPRSequent getCurrentProverSequent() {
		return getUserSupport().getCurrentPO().getPRSequent();
	}


	/* (non-Javadoc)
	 * @see org.eventb.core.pm.IPOChangedListener#poChanged(org.eventb.core.pm.IPOChangeEvent)
	 */
	public void poChanged(IPOChangeEvent e) {
		UIUtils.debug("PO Changed");
		Display display = EventBUIPlugin.getDefault().getWorkbench().getDisplay();
		display.syncExec (new Runnable () {
			public void run () {
				syncObligationExplorer();
			}
		});
	}
}
