/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/

package fr.systerel.internal.explorer.navigator.contentProviders.complex;

import org.eventb.core.IContextRoot;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import fr.systerel.internal.explorer.model.ModelController;
import fr.systerel.internal.explorer.model.ModelProject;
import fr.systerel.internal.explorer.navigator.ExplorerUtils;

/**
 * The content provider for Contexts. 
 *
 */
public class ComplexContextContentProvider extends AbstractComplexContentProvider {

	@Override
	protected IInternalElementType<?> getElementType() {
		return IContextRoot.ELEMENT_TYPE;
	}

	@Override
	protected IContextRoot[] convertToElementType(ModelProject project) {
		return ModelController.convertToIContext(project.getRootContexts());
	}

	@Override
	protected IContextRoot[] getRootChildren(IRodinProject project)
			throws RodinDBException {
		return ExplorerUtils.getContextRootChildren(project);
	}

}
