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
package org.eventb.internal.core.seqprover.eventbExtensions.rewriters;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IFormulaRewriter;
import org.eventb.core.seqprover.IVersionedReasoner;
import org.eventb.internal.core.seqprover.eventbExtensions.rewriters.AutoRewrites.Level;

/**
 * @author Nicolas Beauger
 *
 */
public class AutoRewritesL1 extends AbstractAutoRewrites implements
		IVersionedReasoner {

	private static final String REASONER_ID = AutoRewrites.REASONER_ID + "L1";
	private static final int VERSION = 0;

	public AutoRewritesL1() {
		super(true);
	}

	@Override
	public String getReasonerID() {
		return REASONER_ID;
	}

	@Override
	public int getVersion() {
		return VERSION;
	}

	@Override
	protected IFormulaRewriter getRewriter(FormulaFactory ff) {
		return new AutoRewriterImpl(ff, Level.L1);
	}

	@Override
	protected String getDisplayName() {
		return "simplification rewrites";
	}

}
