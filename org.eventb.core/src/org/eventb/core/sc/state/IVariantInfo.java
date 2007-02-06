/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.sc.state;

import org.eventb.core.EventBPlugin;
import org.eventb.core.ast.Expression;
import org.eventb.core.sc.SCCore;
import org.eventb.core.tool.state.IToolStateType;

/**
 * State component for the variant of the current machine being checked.
 * 
 * @author Stefan Hallerstede
 *
 */
public interface IVariantInfo extends IState {

	final static IToolStateType<IVariantInfo> STATE_TYPE = 
		SCCore.getToolStateType(EventBPlugin.PLUGIN_ID + ".variantInfo");
	
	/**
	 * Returns the parsed and type-checked variant.
	 * 
	 * @return the parsed and type-checked variant
	 */
	Expression getExpression();
	
}
