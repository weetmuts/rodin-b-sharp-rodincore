/*******************************************************************************
 * Copyright (c) 2005, 2009 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - removed unnamed internal elements
 *     Systerel - separation of file and root element
 *******************************************************************************/
package org.rodinp.internal.core;

import java.lang.reflect.Array;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.basis.InternalElement;
import org.rodinp.internal.core.util.Util;

/**
 * @author lvoisin
 *
 */
public class InternalElementType<T extends IInternalElement> extends
		ContributedElementType<T> implements IInternalElementType<T>,
		IContributedItemType {

	// Name of the class implementing elements of this element type
	private final String className;

	// Class implementing elements of this element type
	// (cached value)
	protected Class<? extends T> classObject;

	public InternalElementType(IConfigurationElement configurationElement,
			ElementTypeManager elementTypeManager) {
		super(configurationElement, elementTypeManager);
		this.className = configurationElement.getAttribute("class");
	}

	@SuppressWarnings("unchecked")
	protected void computeClass() {
		Bundle bundle = Platform.getBundle(getBundleName());
		try {
			Class<?> clazz = bundle.loadClass(getClassName());
			classObject = (Class<? extends T>) clazz.asSubclass(InternalElement.class);
		} catch (Exception e) {
			String message = "Can't find constructor for element type "
					+ getId();
			Util.log(null, message);
			throw new IllegalStateException(message, e);
		}
	}

	protected void computeConstructor() {
		if (classObject == null) {
			computeClass();
		}
		try {
			constructor = classObject.getConstructor(String.class, IRodinElement.class);
		} catch (Exception e) {
			String message = "Can't find constructor for element type "
					+ getId();
			Util.log(null, message);
			throw new IllegalStateException(message, e);
		}
	}

	/**
	 * Creates a new internal element handle.
	 * 
	 * @param elementName
	 *            the name of the element to create
	 * @param parent
	 *            the new element's parent
	 * @return a handle on the internal element or <code>null</code> if the
	 *         element type is unknown
	 */
	public T createInstance(String elementName, IRodinElement parent) {
		if (constructor == null) {
			computeConstructor();
		}
		if (constructor == null) {
			return null;
		}
		try {
			return constructor.newInstance(elementName, parent);
		} catch (Exception e) {
			String message = "Can't create an element of type " + getId();
			Util.log(null, message);
			throw new IllegalStateException(message, e);
		}
	}
		
	String getClassName() {
		return className;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T[] getArray(int length) {
		if (classObject == null) {
			computeClass();
		}
		return (T[]) Array.newInstance(classObject, length);
	}


}