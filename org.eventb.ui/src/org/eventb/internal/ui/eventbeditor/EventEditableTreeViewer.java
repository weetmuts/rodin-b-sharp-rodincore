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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eventb.core.IEvent;
import org.eventb.core.IMachine;
import org.eventb.core.IVariable;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class extends the Editable Tree Viewer for editting Events.
 */
public class EventEditableTreeViewer extends EventBEditableTreeViewer {

	/**
	 * @author htson
	 *         <p>
	 *         The content provider class
	 */
	class EventContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		// The invisible root
		private IMachine invisibleRoot = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object child) {
			if (child instanceof IRodinElement)
				return ((IRodinElement) child).getParent();
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof IMachine) {
				try {
					return ((IMachine) parent)
							.getChildrenOfType(IEvent.ELEMENT_TYPE);
				} catch (RodinDBException e) {
					e.printStackTrace();
				}
			}

			if (parent instanceof IParent) {
				try {
					return ((IParent) parent).getChildren();
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return new Object[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object parent) {
			if (parent instanceof IRodinFile) {
				if (invisibleRoot == null) {
					invisibleRoot = (IMachine) parent;
					return getChildren(invisibleRoot);
				}
			}
			return getChildren(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			invisibleRoot = null;
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param editor
	 *            an Event-B Editor
	 * @param parent
	 *            the Composite parent of the viewer
	 * @param style
	 *            the style used to create the tree viewer
	 */
	public EventEditableTreeViewer(EventBEditor editor, Composite parent,
			int style) {
		super(editor, parent, style);
		this.setContentProvider(new EventContentProvider());
		this.setLabelProvider(new EventBTreeLabelProvider(editor));
		this.setSorter(new RodinElementSorter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#commit(org.rodinp.core.IRodinElement,
	 *      int, java.lang.String)
	 */
	public void commit(IRodinElement element, int col, String text) {

		switch (col) {
		case 0: // Commit name
			try {
				UIUtils.debugEventBEditor("Commit : " + element.getElementName()
						+ " to be : " + text);
				if (!element.getElementName().equals(text)) {
					((IInternalElement) element).rename(text, false, null);
				}
			} catch (RodinDBException e) {
				e.printStackTrace();
			}

			break;

		case 1: // Commit content
			try {
				UIUtils.debugEventBEditor("Commit content: "
						+ ((IInternalElement) element).getContents()
						+ " to be : " + text);
				if (!((IInternalElement) element).getContents().equals(text)) {
					((IInternalElement) element).setContents(text);
				}
			} catch (RodinDBException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#createTreeColumns()
	 */
	protected void createTreeColumns() {
		numColumn = 2;

		Tree tree = this.getTree();
		TreeColumn elementColumn = new TreeColumn(tree, SWT.LEFT);
		elementColumn.setText("Elements");
		elementColumn.setResizable(true);
		elementColumn.setWidth(200);

		TreeColumn predicateColumn = new TreeColumn(tree, SWT.LEFT);
		predicateColumn.setText("Contents");
		predicateColumn.setResizable(true);
		predicateColumn.setWidth(250);

		tree.setHeaderVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#isNotSelectable(java.lang.Object,
	 *      int)
	 */
	protected boolean isNotSelectable(Object object, int column) {
		if (!(object instanceof IRodinElement))
			return false;
		IRodinElement element = (IRodinElement) object;
		if (column == 0) {
			if (!editor.isNewElement(element))
				return true;
		}
		if (column == 0) {
			if (element instanceof IUnnamedInternalElement)
				return true;
		} else if (column == 1) {
			if (element instanceof IVariable)
				return true;
			if (element instanceof IEvent)
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#edit(org.rodinp.core.IRodinElement)
	 */
	protected void edit(IRodinElement element) {
		this.reveal(element);
		TreeItem item = TreeSupports.findItem(this.getTree(), element);
		if (element instanceof IUnnamedInternalElement)
			selectItem(item, 1);
		else if (element instanceof IVariable)
			selectItem(item, 0);
		else if (element instanceof IEvent)
			selectItem(item, 0);
		else
			selectItem(item, 1);
	}

}
