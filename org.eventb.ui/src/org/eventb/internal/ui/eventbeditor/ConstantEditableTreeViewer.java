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
import org.eventb.core.IConstant;
import org.eventb.core.IContext;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This sub-class Event-B Editable tree viewer for editing carrier set
 *         elements.
 */
public class ConstantEditableTreeViewer extends EventBEditableTreeViewer {

	/**
	 * @author htson
	 *         <p>
	 *         The content provider class.
	 */
	class CarrierSetContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {
		// The invisible root of the tree.
		private IContext invisibleRoot = null;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object child) {
			if (child instanceof IRodinElement)
				return ((IRodinElement) child).getParent();
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof IContext) {
				try {
					return ((IContext) parent)
							.getChildrenOfType(IConstant.ELEMENT_TYPE);
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
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

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object parent) {
			if (parent instanceof IRodinFile) {
				if (invisibleRoot == null) {
					invisibleRoot = (IContext) parent;
					return getChildren(invisibleRoot);
				}
			}
			return getChildren(parent);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			invisibleRoot = null;
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * @param editor The Event-B Editor
	 * @param parent The composit parent
	 * @param style The style used to create the tree viewer 
	 */
	public ConstantEditableTreeViewer(EventBEditor editor, Composite parent,
			int style) {
		super(editor, parent, style);
		this.setContentProvider(new CarrierSetContentProvider());
		this.setLabelProvider(new EventBTreeLabelProvider(editor));
		this.setSorter(new RodinElementSorter());
	}

	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#commit(org.rodinp.core.IRodinElement, int, java.lang.String)
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
		}
	}

	protected void createTreeColumns() {
		numColumn = 1;

		Tree tree = this.getTree();
		TreeColumn elementColumn = new TreeColumn(tree, SWT.LEFT);
		elementColumn.setText("Name");
		elementColumn.setResizable(true);
		elementColumn.setWidth(200);

		tree.setHeaderVisible(true);
	}

	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#isNotSelectable(java.lang.Object, int)
	 */
	protected boolean isNotSelectable(Object object, int column) {
		if (!(object instanceof IRodinElement))
			return false;
		if (column == 0) {
			if (!editor.isNewElement((IRodinElement) object))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#edit(org.rodinp.core.IRodinElement)
	 */
	protected void edit(IRodinElement element) {
		this.reveal(element);
		TreeItem item = TreeSupports.findItem(this.getTree(), element);
		selectItem(item, 0);
	}

}
