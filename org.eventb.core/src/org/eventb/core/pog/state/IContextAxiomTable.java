/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.pog.state;

import org.eventb.core.EventBPlugin;

/**
 * Common protocol for accessing all axioms of a context.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @author Stefan Hallerstede
 *
 */
public interface IContextAxiomTable extends IPredicateTable {

	final static String STATE_TYPE = EventBPlugin.PLUGIN_ID + ".contextAxiomTable";

}
