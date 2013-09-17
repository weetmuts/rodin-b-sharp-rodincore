/*******************************************************************************
 * Copyright (c) 2006, 2013 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.core.sc.state;

import org.eventb.core.EventBPlugin;
import org.eventb.core.sc.SCCore;
import org.eventb.core.tool.IStateType;

/**
 * State components for keeping labeled elements of contexts, i.e. axioms and
 * theorems.
 * 
 * @author Stefan Hallerstede
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IContextLabelSymbolTable extends ILabelSymbolTable {

	final static IStateType<IContextLabelSymbolTable> STATE_TYPE = SCCore
			.getToolStateType(EventBPlugin.PLUGIN_ID
					+ ".contextLabelSymbolTable");

}
