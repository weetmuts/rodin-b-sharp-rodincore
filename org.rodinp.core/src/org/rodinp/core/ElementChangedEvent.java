/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * Strongly inspired by org.eclipse.jdt.core.ElementChangedEvent.java which is
 * 
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rodinp.core;

import java.util.EventObject;

/**
 * An element changed event describes a change to the structure or contents
 * of a tree of Rodin elements. The changes to the elements are described by
 * the associated delta object carried by this event.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the Rodin database.
 * </p>
 *
 * @see IElementChangedListener
 * @see IRodinElementDelta
 */
public class ElementChangedEvent extends EventObject {
	
	private static final long serialVersionUID = 494648861081632942L;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more Rodin element(s) expressed as a hierarchical
	 * Rodin element delta as returned by <code>getDelta()</code>.
	 *
	 * Note: this notification occurs during the corresponding POST_CHANGE
	 * resource change notification, and contains a full delta accounting for
	 * any RodinModel operation and/or resource change.
	 *
	 * @see IRodinElementDelta
	 * @see org.eclipse.core.resources.IResourceChangeEvent
	 * @see #getDelta()
	 */
	public static final int POST_CHANGE = 1;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more Rodin element(s) expressed as a hierarchical
	 * Rodin element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs as a result of a working copy reconcile
	 * operation.
	 *
	 * @see IRodinElementDelta
	 * @see org.eclipse.core.resources.IResourceChangeEvent
	 * @see #getDelta()
	 */
	public static final int 	POST_RECONCILE = 4;	
	
	/*
	 * Event type indicating the nature of this event. 
	 * It can be a combination either:
	 *  - POST_CHANGE
	 *  - PRE_AUTO_BUILD
	 *  - POST_RECONCILE
	 */
	private int type; 
	
	/**
	 * Creates an new element changed event (based on a <code>IRodinElementDelta</code>).
	 *
	 * @param delta the Rodin element delta.
	 * @param type the type of this event (currently always POST_CHANGE)
	 */
	public ElementChangedEvent(IRodinElementDelta delta, int type) {
		super(delta);
		this.type = type;
	}
	
	/**
	 * Returns the delta describing the change.
	 *
	 * @return the delta describing the change
	 */
	public IRodinElementDelta getDelta() {
		return (IRodinElementDelta) this.source;
	}
	
	/**
	 * Returns the type of event being reported.
	 * <p>
	 * Currently, only <code>POST_CHANGE</code> is used.
	 * </p>
	 * 
	 * @return one of the event type constants
	 * @see #POST_CHANGE
	 * @see #POST_RECONCILE
	 */
	public int getType() {
		return this.type;
	}
}
