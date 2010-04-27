/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.ast.extension;

/**
 * @author Nicolas Beauger
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface ICompatibilityMediator {

	/**
	 * Adds a compatibility between operators of given ids.
	 * <p>
	 * N.B: Compatibility is 'oriented', thus compatibility from left to right
	 * does not imply compatibility from right to left.
	 * </p>
	 * <p>
	 * If a priority is set between left and right, compatibility is assumed.
	 * </p>
	 * <p>
	 * If there is no priority and no compatibility set between left and right,
	 * operators are considered incompatible.
	 * </p>
	 * 
	 * @param leftOpId
	 *            an operator id
	 * @param rightOpId
	 *            an operator id
	 */
	void addCompatibility(String leftOpId, String rightOpId);

	/**
	 * Adds a compatibility between groups of given ids.
	 * 
	 * @param leftGroupId
	 *            a group id
	 * @param rightGroupId
	 *            a group id
	 */
	void addGroupCompatibility(String leftGroupId, String rightGroupId);
}
