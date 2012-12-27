/*******************************************************************************
 * Copyright (c) 2005, 2012 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui;

import org.eclipse.swt.widgets.Control;

/**
 * @author htson
 *         <p>
 *         This is the interface for Event-B Input Text
 */
public interface IEventBControl {

	/**
	 * Getting the Text Widget contains inside this Event-B Input Text.
	 * <p>
	 * 
	 * @return the Text Widget contains inside this Event-B Input Text
	 */
	public Control getControl();

	/**
	 * Dispose the control, should remove any extra resources/listeners that are
	 * located to this Event-B Control
	 */
	public void dispose();

}
