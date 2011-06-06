/*******************************************************************************
 * Copyright (c) 2011 Systerel and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Systerel - Initial API and implementation
 *******************************************************************************/
package fr.systerel.editor.handlers.refactoring;

import org.eventb.core.IAxiom;

public class AutoAxiomLabelRenaming extends AbstractRenameElementHandler {

	public AutoAxiomLabelRenaming() {
		type = IAxiom.ELEMENT_TYPE;
	}

}
