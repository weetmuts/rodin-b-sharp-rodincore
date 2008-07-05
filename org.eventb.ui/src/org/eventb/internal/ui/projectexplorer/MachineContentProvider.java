/*******************************************************************************
 * Copyright (c) 2008 Heinrich Heine Universitaet Duesseldorf and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.ui.projectexplorer;

import java.util.ArrayList;

import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineFile;
import org.eventb.core.ITheorem;
import org.eventb.core.IVariable;
import org.eventb.ui.projectexplorer.AbstractRodinContentProvider;
import org.eventb.ui.projectexplorer.TreeNode;

public class MachineContentProvider extends AbstractRodinContentProvider {

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (!(parentElement instanceof IMachineFile)) {
			return new Object[] {};
		}
		IMachineFile mch = (IMachineFile) parentElement;

		ArrayList<TreeNode<?>> list = new ArrayList<TreeNode<?>>();
		list.add(new TreeNode<IVariable>("Variables", mch,
				IVariable.ELEMENT_TYPE));
		list.add(new TreeNode<IInvariant>("Invariants", mch,
				IInvariant.ELEMENT_TYPE));
		list
				.add(new TreeNode<ITheorem>("Theorems", mch,
						ITheorem.ELEMENT_TYPE));
		list.add(new TreeNode<IEvent>("Events", mch, IEvent.ELEMENT_TYPE));

		return list.toArray();

	}

}
