/*******************************************************************************
 * Copyright (c) 2007, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added history support
 *     Systerel - separation of file and root element
 *     Systerel - used IAttributeFactory
 *******************************************************************************/
package org.eventb.internal.ui.propertiesView;

import org.eventb.core.IRefinesEvent;
import org.eventb.internal.ui.eventbeditor.editpage.IAttributeFactory;
import org.eventb.internal.ui.eventbeditor.editpage.RefinesEventAbstractEventLabelAttributeFactory;

public class RefinesEventSection extends CComboSection<IRefinesEvent> {

	@Override
	String getLabel() {
		return "Ref. Evt.";
	}

	@Override
	protected IAttributeFactory<IRefinesEvent> createFactory() {
		return new RefinesEventAbstractEventLabelAttributeFactory();
	}

}
