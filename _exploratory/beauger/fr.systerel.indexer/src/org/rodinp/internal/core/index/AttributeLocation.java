/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.rodinp.internal.core.index;

import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.index.IAttributeLocation;

public class AttributeLocation extends RodinLocation implements
		IAttributeLocation {

	private final IAttributeType attributeType;

	public AttributeLocation(IInternalParent element,
			IAttributeType attributeType) {
		super(element);
		if (attributeType == null) {
			throw new NullPointerException("null attribute type");
		}
		this.attributeType = attributeType;
	}

	public IAttributeType getAttributeType() {
		return attributeType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + attributeType.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AttributeLocation))
			return false;
		final AttributeLocation other = (AttributeLocation) obj;
		return this.attributeType == other.attributeType;
	}
	
	@Override
	public String toString() {
		return super.toString() + "." + attributeType;
	}

}
