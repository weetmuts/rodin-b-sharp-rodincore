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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eventb.eventBKeyboard.preferences.PreferenceConstants;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class provides the labels, fonts and colors for the Editable
 *         Tree Viewer.
 */
public class EventBTreeLabelProvider implements ITableLabelProvider,
		ITableFontProvider, ITableColorProvider {
	
	// The associated Event-B Editor
	private EventBEditor editor;

	/**
	 * Constructor.
	 * <p>
	 * @param editor An Event-B Editor
	 */
	public EventBTreeLabelProvider(EventBEditor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 0)
			return null;
		if (element instanceof IRodinElement) {
			IRodinElement rodinElement = (IRodinElement) element;
			return UIUtils.getImage(rodinElement);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IRodinElement) {
			IRodinElement rodinElement = (IRodinElement) element;

			if (columnIndex == 0) {
				if (rodinElement instanceof IUnnamedInternalElement)
					return "";
				if (rodinElement instanceof IInternalElement)
					return ((IInternalElement) rodinElement).getElementName();
				return rodinElement.toString();
			}

			if (columnIndex == 1) {
				try {
					if (rodinElement instanceof IInternalElement)
						return ((IInternalElement) rodinElement).getContents();
				} catch (RodinDBException e) {
					e.printStackTrace();
				}
				return rodinElement.toString();
			}
		}

		return element.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object,
	 *      int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		Display display = Display.getCurrent();
		if (element instanceof IRodinElement) {
			if (editor.isNewElement((IRodinElement) element))
				return display.getSystemColor(SWT.COLOR_YELLOW);
		}
		return display.getSystemColor(SWT.COLOR_WHITE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object,
	 *      int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		Display display = Display.getCurrent();
		if (element instanceof IRodinElement) {
			if (editor.isNewElement((IRodinElement) element))
				return display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
		}
		return display.getSystemColor(SWT.COLOR_BLACK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object,
	 *      int)
	 */
	public Font getFont(Object element, int columnIndex) {
		return JFaceResources.getFont(PreferenceConstants.EVENTB_MATH_FONT);
	}

}
