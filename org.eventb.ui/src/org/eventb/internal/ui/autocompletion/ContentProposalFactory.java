/*******************************************************************************
 * Copyright (c) 2009 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.autocompletion;

import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.rodinp.core.location.IAttributeLocation;

public class ContentProposalFactory {

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for StyledText control.
	 */
	public static EventBContentProposalAdapter getContentProposal(
			IAttributeLocation location, StyledText text) {
		return new EventBContentProposalAdapter(text,
				new StyledTextContentAdapter(), getProposalProvider(location));
	}

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for a Text control.
	 */
	public static EventBContentProposalAdapter getContentProposal(
			IAttributeLocation location, Text text) {
		return new EventBContentProposalAdapter(text, new TextContentAdapter(),
				getProposalProvider(location));
	}

	private static ProposalProvider getProposalProvider(
			IAttributeLocation location) {
		return new ProposalProvider(location);
	}

}
