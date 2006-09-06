/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.pog;

import java.util.ArrayList;

import org.eventb.core.EventBPlugin;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.sc.IState;

/**
 * @author Stefan Hallerstede
 *
 */
public interface IMachineVariableTable extends IState, Iterable<FreeIdentifier> {

	final static String STATE_TYPE = EventBPlugin.PLUGIN_ID + ".machineVariableTable";

	boolean contains(FreeIdentifier variable);
	
	void add(FreeIdentifier variable, boolean preserved);
	
	ArrayList<FreeIdentifier> getPreservedVariables();
	
	void trimToSize();
		
}
