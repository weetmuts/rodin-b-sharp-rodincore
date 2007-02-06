/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.sc.state;

import org.eventb.core.EventBPlugin;
import org.eventb.core.ISCMachineFile;
import org.eventb.core.sc.SCCore;
import org.eventb.core.tool.state.IToolStateType;

/**
 * State component providing information about an abstract machine of
 * a machine.
 * 
 * @author Stefan Hallerstede
 *
 */
public interface IAbstractMachineInfo extends IState {

	final static IToolStateType<IAbstractMachineInfo> STATE_TYPE = 
		SCCore.getToolStateType(EventBPlugin.PLUGIN_ID + ".abstractMachineInfo");
	
	/**
	 * Returns a handle to the abstract machine if there is one, and 
	 * <code>null</code> otherwise.
	 * 
	 * @return a handle to the abstract machine if there is one, and 
	 * <code>null</code> otherwise
	 */
	ISCMachineFile getAbstractMachine();

}
