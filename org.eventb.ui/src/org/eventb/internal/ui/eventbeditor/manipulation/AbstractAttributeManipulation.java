/*******************************************************************************
 * Copyright (c) 2009, 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.eventbeditor.manipulation;

import org.eventb.internal.ui.UIUtils;
import org.eventb.ui.manipulation.IAttributeManipulation;
import org.rodinp.core.IAttributeType;

public abstract class AbstractAttributeManipulation implements
		IAttributeManipulation {

	protected void logNotPossibleValues(IAttributeType attribute, String value) {
		UIUtils.log(null, value + " is not a possible value for attribute "
				+ attribute);
	}

	protected void logCantRemove(IAttributeType attribute) {
		UIUtils.log(null, "Attribute " + attribute + " cannot be removed");
	}

}
