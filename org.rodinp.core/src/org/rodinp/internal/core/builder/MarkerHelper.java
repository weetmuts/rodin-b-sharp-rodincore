/*******************************************************************************
 * Copyright (c) 2005-2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.internal.core.builder;

import static org.eclipse.core.resources.IMarker.MESSAGE;
import static org.eclipse.core.resources.IMarker.SEVERITY;
import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IResource.DEPTH_ZERO;
import static org.rodinp.core.RodinMarkerUtil.CYCLE_DETECTED;
import static org.rodinp.core.RodinMarkerUtil.RODIN_PROBLEM_MARKER;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.rodinp.internal.core.util.Messages;
import org.rodinp.internal.core.util.Util;

/**
 * Helper class for build problem marker management.
 * 
 * @author Laurent Voisin
 */
public class MarkerHelper {

	private MarkerHelper() {
		// Non instanciable class
	}

	/**
	 * Adds a build problem marker to the given resource. The severity of the
	 * problem is {@link #SEVERITY_ERROR}.
	 * 
	 * @param resource
	 *            resource to mark
	 * @param cycle
	 *            <code>true</code> iff the problem is related to a dependency
	 *            graph cycle
	 * @param messageId
	 *            id of the marker message (from the {@link Messages} class)
	 * @param args
	 *            arguments to the marker message
	 */
	public static void addMarker(IResource resource, boolean cycle,
			String messageId, Object... args) {

		try {
			IMarker marker = resource.createMarker(RODIN_PROBLEM_MARKER);
			marker.setAttribute(SEVERITY, SEVERITY_ERROR);
			marker.setAttribute(CYCLE_DETECTED, cycle);
			String message = Messages.bind(messageId, args);
			marker.setAttribute(MESSAGE, message);
		} catch (CoreException e) {
			Util.log(e, "when adding a build problem marker");
		}
	}

	/**
	 * Deletes all build problem markers on the given file.
	 * 
	 * @param file
	 *            file to clean up
	 */
	public static void deleteBuildMarkers(IFile file) {
		try {
			if (file.exists()) {
				file.deleteMarkers(RODIN_PROBLEM_MARKER, false, DEPTH_ZERO);
			}
		} catch (CoreException e) {
			Util.log(e, "when deleting markers in builder");
		}
	}

}
