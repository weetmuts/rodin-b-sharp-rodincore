/*******************************************************************************
 * Copyright (c) 2010 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.tests.extension;

import java.util.Collections;
import java.util.Set;

import org.eventb.core.EventBPlugin;
import org.eventb.core.IEventBRoot;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.extension.IFormulaExtensionProvider;

/**
 * A dummy extension provider that returns singleton containing one extension
 * called "Prime".
 * 
 * @see Prime
 */
public class PrimeFormulaExtensionProvider implements IFormulaExtensionProvider {

	private final String PROVIDER_ID = EventBPlugin.PLUGIN_ID + ".tests"
			+ ".PrimeFormulaExtensionProvider";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public Set<IFormulaExtension> getFormulaExtensions(IEventBRoot root) {
		return Collections.singleton(Prime.getPrime());
	}

	@Override
	public void setFormulaFactory(IEventBRoot root, FormulaFactory ff) {
		// Not tested here
	}

}
