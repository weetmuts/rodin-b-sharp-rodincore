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
import org.eventb.core.IAxiom;
import org.eventb.core.IContextFile;
import org.eventb.internal.ui.EventBUIExceptionHandler;
import org.eventb.ui.ElementSorter;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This sub-class Event-B Editable tree viewer for editing axiom
 *         elements.
 */
public class AxiomEditableTreeViewer extends EventBEditableTreeViewer {

	/**
	 * @author htson
	 *         <p>
	 *         The content provider class.
	 */
	class AxiomContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		// The invisible root of the tree
		private IContextFile invisibleRoot = null;

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
			if (parent instanceof IContextFile) {
				try {
					return ((IContextFile) parent)
							.getChildrenOfType(IAxiom.ELEMENT_TYPE);
				} catch (RodinDBException e) {
					EventBUIExceptionHandler.handleGetChildrenException(e);
				}
			}

			if (parent instanceof IParent) {
				try {
					return ((IParent) parent).getChildren();
				} catch (RodinDBException e) {
					EventBUIExceptionHandler.handleGetChildrenException(e);
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
					invisibleRoot = (IContextFile) parent;
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
			// Do nothing
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
	 * The constructor.
	 * <p>
	 * 
	 * @param editor
	 *            The Event-B Editor
	 * @param parent
	 *            The Composite parent of this tree viewer
	 * @param style
	 *            The style used to create the tree viewer
	 */
	public AxiomEditableTreeViewer(IEventBEditor<?> editor, Composite parent,
			int style) {
		super(editor, parent, style);
		this.setContentProvider(new AxiomContentProvider());
		this.setLabelProvider(new EventBTreeLabelProvider(editor, this));
		this.setSorter(new ElementSorter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.internal.ui.eventbeditor.EventBEditableTreeViewer#createTreeColumns()
	 */
	@Override
	protected void createTreeColumns() {
		numColumn = 2;

		Tree tree = this.getTree();
		TreeColumn elementColumn = new TreeColumn(tree, SWT.LEFT);
		elementColumn.setText("Label");
		elementColumn.setResizable(true);
		elementColumn.setWidth(200);

		TreeColumn predicateColumn = new TreeColumn(tree, SWT.LEFT);
		predicateColumn.setText("Predicate");
		predicateColumn.setResizable(true);
		predicateColumn.setWidth(250);

		tree.setHeaderVisible(true);
	}

}
