/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.tool.state;

/**
 * @author Stefan Hallerstede
 *
 */
public interface IToolStateType<T extends IToolState> {

	/**
	 * Returns the unique identifier of this state type.
	 * 
	 * @return the unique identifier of this state type
	 */
	String getId();

	/**
	 * Returns the human-readable name of this state type.
	 * 
	 * @return the name of this state type
	 */
	String getName();

	/**
	 * Returns a string representation of this state type, that is its unique
	 * identifier.
	 * 
	 * @return the unique identifier of this state type
	 */
	String toString();

}
