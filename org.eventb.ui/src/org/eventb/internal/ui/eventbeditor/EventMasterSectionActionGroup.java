/*******************************************************************************
 * Copyright (c) 2005-2006 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui.eventbeditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineFile;
import org.eventb.core.IRefinesEvent;
import org.eventb.core.IRefinesMachine;
import org.eventb.internal.ui.EventBImage;
import org.eventb.internal.ui.EventBImageDescriptor;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class provides the actions that will be used with the Event
 *         Editable Tree Viewer.
 */
public class EventMasterSectionActionGroup extends ActionGroup {

	// The Event-B Editor.
	private EventBEditor editor;

	// The tree viewer in the master section
	private TreeViewer viewer;

	// Some actions
	protected Action addEvent;

	protected Action addLocalVariable;

	protected Action addGuard;

	protected Action addAction;

	protected Action delete;

	protected Action handleUp;

	protected Action handleDown;

	protected Action showAbstraction;

	private class ShowAbstraction extends Action {
		IRodinFile abstractFile;

		IRodinElement concreteElement;

		ShowAbstraction(IRodinFile abstractFile, IRodinElement concreteElement) {
			this.abstractFile = abstractFile;
			this.concreteElement = concreteElement;
			this.setText(EventBPlugin.getComponentName(abstractFile
					.getElementName()));
			this.setToolTipText("Show the corresponding abstract event");
			this.setImageDescriptor(new EventBImageDescriptor(
					EventBImage.IMG_REFINES));
		}

		public void run() {
			try {
				IInternalElement event = TreeSupports.getEvent(concreteElement);
				IRodinElement abs_evt = getAbstractElement(event);

				while (abs_evt != null && abs_evt.exists()
						&& !abs_evt.getOpenable().equals(abstractFile)) {
					abs_evt = getAbstractElement(abs_evt);
				}
				if (abs_evt != null && abs_evt.exists()) {
					UIUtils.linkToEventBEditor(abs_evt);
				} else
					UIUtils.linkToEventBEditor(abstractFile);

			} catch (RodinDBException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Constructor: Create the actions.
	 * <p>
	 * 
	 * @param eventBEditor
	 *            The Event-B Editor
	 * @param treeViewer
	 *            The tree viewer associated with this action group
	 */
	public EventMasterSectionActionGroup(EventBEditor eventBEditor,
			TreeViewer treeViewer) {
		this.editor = eventBEditor;
		this.viewer = treeViewer;

		// Add an event.
		addEvent = new Action() {
			public void run() {
				EventBEditorUtils.addEvent(editor, viewer);
			}
		};
		addEvent.setText("New &Event");
		addEvent.setToolTipText("Create a new event");
		addEvent.setImageDescriptor(EventBImage
				.getImageDescriptor(EventBImage.IMG_NEW_EVENT_PATH));

		// Add a local variable.
		addLocalVariable = new Action() {
			public void run() {
				EventBEditorUtils.addLocalVariable(editor, viewer);
			}
		};
		addLocalVariable.setText("New &Local Variable");
		addLocalVariable.setToolTipText("Create a new (local) variable");
		addLocalVariable.setImageDescriptor(EventBImage
				.getImageDescriptor(EventBImage.IMG_NEW_VARIABLES_PATH));

		// Add a guard.
		addGuard = new Action() {
			public void run() {
				EventBEditorUtils.addGuard(editor, viewer);
			}
		};
		addGuard.setText("New &Guard");
		addGuard.setToolTipText("Create a new guard");
		addGuard.setImageDescriptor(EventBImage
				.getImageDescriptor(EventBImage.IMG_NEW_GUARD_PATH));

		// Add an action.
		addAction = new Action() {
			public void run() {
				EventBEditorUtils.addAction(editor, viewer);
			}
		};
		addAction.setText("New &Action");
		addAction.setToolTipText("Create a new action");
		addAction.setImageDescriptor(EventBImage
				.getImageDescriptor(EventBImage.IMG_NEW_ACTION_PATH));

		// Delete the current selected element in the tree viewer.
		delete = new Action() {
			public void run() {
				EventBEditorUtils.deleteElements(viewer);
			}
		};
		delete.setText("&Delete");
		delete.setToolTipText("Delete selected element");
		delete.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		// Handle the up action.
		handleUp = new Action() {
			public void run() {
				EventBEditorUtils.handleUp(viewer);
			}
		};
		handleUp.setText("&Up");
		handleUp.setToolTipText("Move the element up");
		handleUp.setImageDescriptor(new EventBImageDescriptor(
				EventBImage.IMG_NEW_PROJECT));

		// Handle the down action.
		handleDown = new Action() {
			public void run() {
				EventBEditorUtils.handleDown(viewer);
			}
		};
		handleDown.setText("D&own");
		handleDown.setToolTipText("Move the element down");
		handleDown.setImageDescriptor(new EventBImageDescriptor(
				EventBImage.IMG_NEW_PROJECT));

		// Handle Show Abstraction action.
		showAbstraction = new Action() {
			public void run() {
				IStructuredSelection ssel = (IStructuredSelection) viewer
						.getSelection();
				if (ssel.size() == 1) {
					Object obj = ssel.getFirstElement();
					IInternalElement event = TreeSupports.getEvent(obj);

					IMachineFile file = (IMachineFile) editor.getRodinInput();
					try {
						IRodinElement[] refines = file
								.getChildrenOfType(IRefinesMachine.ELEMENT_TYPE);
						if (refines.length == 1) {
							IRodinElement refine = refines[0];
							String name = ((IInternalElement) refine)
									.getContents();
							IRodinProject prj = file.getRodinProject();
							IMachineFile refinedFile = (IMachineFile) prj
									.getRodinFile(EventBPlugin
											.getMachineFileName(name));
							UIUtils.debugEventBEditor("Refined: "
									+ refinedFile.getElementName());

							IInternalElement abs_evt = null;
							IRodinElement[] abs_evts = event
									.getChildrenOfType(IRefinesEvent.ELEMENT_TYPE);
							if (abs_evts.length == 0) {
								abs_evt = refinedFile.getInternalElement(event
										.getElementType(), event
										.getElementName());
							} else {
								abs_evt = refinedFile.getInternalElement(event
										.getElementType(),
										((IInternalElement) abs_evts[0])
												.getContents());
							}
							if (abs_evt.exists())
								UIUtils.linkToEventBEditor(abs_evt);
							else
								UIUtils.linkToEventBEditor(refinedFile);

							// if (refinedFile.exists()) {
							// IWorkbenchPage activePage = EventBUIPlugin
							// .getActivePage();
							// IEditorReference[] editors = activePage
							// .getEditorReferences();
							//
							// for (IEditorReference editor : editors) {
							// IEditorPart part = editor.getEditor(true);
							// if (activePage.isPartVisible(part)) {
							// if (part instanceof EventBMachineEditor) {
							// activePage.openEditor();
							// }
							// }
							//								
							// IRodinFile rodinInput = ((EventBMachineEditor)
							// part)
							// .getRodinInput();
							// UIUtils.debugEventBEditor("Trying: "
							// + rodinInput.getElementName());
							// if (rodinInput.equals(refinedFile)) {
							// UIUtils.debugEventBEditor("Focus");
							// if (activePage.isPartVisible(part)) {
							// IStructuredSelection ssel =
							// (IStructuredSelection) event
							// .getSelection();
							// if (ssel.size() == 1) {
							// IInternalElement obj = (IInternalElement) ssel
							// .getFirstElement();
							// IInternalElement element = refinedFile
							// .getInternalElement(
							// obj
							// .getElementType(),
							// obj
							// .getElementName());
							// if (element != null)
							// ((EventBEditor) part)
							// .setSelection(element);
							// }
							// }
							// }
							// }
							// }
							// }
						}
					} catch (RodinDBException e) {
						e.printStackTrace();
					}

				}
			}
		};
		showAbstraction.setText("Abstraction");
		showAbstraction.setToolTipText("Show the corresponding abstract event");
		showAbstraction.setImageDescriptor(new EventBImageDescriptor(
				EventBImage.IMG_REFINES));
	}

	/**
	 * Fill the context menu with the actions create initially.
	 * <p>
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		ISelection sel = getContext().getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.size() == 1) {
				Object obj = ssel.getFirstElement();

				if (obj instanceof IEvent) {
					menu.add(addLocalVariable);
					menu.add(addGuard);
					menu.add(addAction);
					// MenuManager newMenu = new MenuManager("&New");
					// newMenu.add(addLocalVariable);
					// newMenu.add(addGuard);
					// newMenu.add(addAction);
					// menu.add(newMenu);
					menu.add(new Separator());
				}
			}

			menu.add(addEvent);
			menu.add(new Separator());
			IRodinFile file = (IRodinFile) editor.getRodinInput();
			if (ssel.size() == 1) {

				try {
					IRodinFile abstractFile = getAbstractFile(file);
					if (abstractFile != null && abstractFile.exists()) {
						MenuManager abstractionMenu = new MenuManager(
								"&Abstraction");
						menu.add(abstractionMenu);
						abstractionMenu.add(new ShowAbstraction(abstractFile,
								(IRodinElement) ssel.getFirstElement()));

						abstractFile = getAbstractFile(abstractFile);
						while (abstractFile != null && abstractFile.exists()) {
							abstractionMenu.add(new ShowAbstraction(
									abstractFile, (IRodinElement) ssel
											.getFirstElement()));
							abstractFile = getAbstractFile(abstractFile);
						}
					}

				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!sel.isEmpty()) {
				menu.add(new Separator());
				menu.add(delete);
			}
		}
		// menu.add(deleteAction);
		// menu.add(new Separator());
		// drillDownAdapter.addNavigationActions(menu);

		// Other plug-ins can contribute there actions here
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public IRodinFile getAbstractFile(IRodinFile concreteFile)
			throws RodinDBException {
		IRodinElement[] refines = concreteFile
				.getChildrenOfType(IRefinesMachine.ELEMENT_TYPE);
		if (refines.length == 1) {
			IRodinElement refine = refines[0];
			String name = ((IInternalElement) refine).getContents();
			IRodinProject prj = concreteFile.getRodinProject();
			return prj.getRodinFile(EventBPlugin.getMachineFileName(name));
		}
		return null;
	}

	public IRodinElement getAbstractElement(IRodinElement concreteElement)
			throws RodinDBException {
		IRodinFile rodinFile = (IRodinFile) concreteElement.getOpenable();
		IRodinFile abstractFile = getAbstractFile(rodinFile);
		if (abstractFile == null)
			return null;
		if (!abstractFile.exists())
			return null;

		IRodinElement abstractElement = null;
		if (concreteElement instanceof IEvent) {
			IRodinElement[] abs_evts = ((IEvent) concreteElement)
					.getChildrenOfType(IRefinesEvent.ELEMENT_TYPE);
			if (abs_evts.length == 0) {
				abstractElement = abstractFile.getInternalElement(
						IEvent.ELEMENT_TYPE, ((IEvent) concreteElement)
								.getElementName());
			} else {
				abstractElement = abstractFile.getInternalElement(
						IEvent.ELEMENT_TYPE, ((IInternalElement) abs_evts[0])
								.getContents());
			}
		}
		return abstractElement;
	}

}
