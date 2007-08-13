/*******************************************************************************
 * Copyright (c) 2007 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui.eventbeditor.editpage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eventb.internal.ui.EventBImage;
import org.eventb.internal.ui.EventBUIExceptionHandler;
import org.eventb.ui.IEventBSharedImages;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.RodinDBException;

public class BeforeHyperlinkComposite extends AbstractHyperlinkComposite {

	ImageHyperlink addBeforeHyperlink;
	
	public BeforeHyperlinkComposite(EditPage page, IInternalParent parent,
			IInternalElementType<?> type,
			FormToolkit toolkit, Composite compParent) {
		super(page, parent, type, toolkit, compParent);
	}

	@Override
	public void createHyperlinks(FormToolkit toolkit, int level) {
		Composite tmp = toolkit.createComposite(composite);
		GridData gridData = new GridData();
		gridData.widthHint = (level + 1) * 40; 
		gridData.heightHint = 0;
		tmp.setLayoutData(gridData);

		addBeforeHyperlink = toolkit.createImageHyperlink(
				composite, SWT.TOP);
		addBeforeHyperlink.setImage(EventBImage
				.getImage(IEventBSharedImages.IMG_ADD));
		addBeforeHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				IEventBEditor<?> editor = (IEventBEditor<?>) page.getEditor();
				try {
					IInternalElement[] children = parent.getChildrenOfType(type);
					assert (children.length != 0);
					IInternalElement first = children[0];
					IInternalElement element = AttributeRelUISpecRegistry
							.getDefault().createElement(editor, parent, type,
									first);
					page.recursiveExpand(element);
				} catch (RodinDBException exception) {
					EventBUIExceptionHandler
							.handleCreateElementException(exception);
				} catch (CoreException exception) {
					EventBUIExceptionHandler
						.handleCreateElementException(exception);
				}
			}

		});
		addBeforeHyperlink.setLayoutData(new GridData());
		super.createHyperlinks(toolkit, level);
	}

}
