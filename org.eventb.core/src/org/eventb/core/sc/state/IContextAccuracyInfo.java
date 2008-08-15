/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.sc.state;

import org.eventb.core.EventBPlugin;
import org.eventb.core.sc.SCCore;
import org.eventb.core.tool.IStateType;

/**
 * The accuracy info for the current context.
 * 
 * @see IAccuracyInfo
 * 
 * @author Stefan Hallerstede
 *
 */
public interface IContextAccuracyInfo extends IAccuracyInfo {
	
	final static IStateType<IContextAccuracyInfo> STATE_TYPE = 
		SCCore.getToolStateType(EventBPlugin.PLUGIN_ID + ".contextAccuracyInfo");

}
