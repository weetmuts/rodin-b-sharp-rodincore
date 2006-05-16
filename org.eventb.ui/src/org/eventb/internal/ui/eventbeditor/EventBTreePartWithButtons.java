package org.eventb.internal.ui.eventbeditor;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.rodinp.core.IRodinElement;

public abstract class EventBTreePartWithButtons
	extends EventBPartWithButtons
	implements IStatusChangedListener
{
	// The group of actions for the tree part.
	protected EventBMasterSectionActionGroup groupActionSet;
	
	public EventBTreePartWithButtons(final IManagedForm managedForm, Composite parent, FormToolkit toolkit, 
			int style, EventBEditor editor, String [] buttonLabels, String title, String description) {
		super(managedForm, parent, toolkit, style, editor, buttonLabels, title, description);
		makeActions();
		editor.addStatusListener(this);
	}
	
	@Override
	protected Viewer createViewer(IManagedForm managedForm, FormToolkit toolkit, Composite parent) {
		return createTreeViewer(managedForm, toolkit, parent);
	}

	/*
	 * Create the actions that can be used in the tree.
	 */
	private void makeActions() {
		groupActionSet = new EventBMasterSectionActionGroup(editor, (TreeViewer) this.getViewer());
	}
	
	abstract protected EventBEditableTreeViewer createTreeViewer(IManagedForm managedForm, FormToolkit toolkit, Composite parent);

	/**
	 * Set the selection in the table viewer.
	 * <p>
	 * @param element A Rodin element
	 */
	public void setSelection(IRodinElement element) {
		StructuredViewer viewer = (StructuredViewer) this.getViewer();
		viewer.setSelection(new StructuredSelection(element));
		edit(element);
	}

	protected void selectItem(TreeItem item, int column) {
		((EventBEditableTreeViewer) getViewer()).selectItem(item, column);
	}
	

	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.IStatusChangedListener#statusChanged(java.util.Collection)
	 */
	public void statusChanged(IRodinElement element) {
		((EventBEditableTreeViewer) this.getViewer()).statusChanged(element);
		updateButtons();
	}
	
}
