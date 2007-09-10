/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/
package org.eventb.internal.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IExpressionElement;
import org.eventb.ui.IElementModifier;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         An modifier class for expression elements.
 *         </p>
 */
public class ExpressionModifier implements IElementModifier {

	/* (non-Javadoc)
	 * @see org.eventb.ui.IElementModifier#modify(org.rodinp.core.IRodinElement, java.lang.String)
	 */
	public void modify(IRodinElement element, String text)
			throws RodinDBException {
		// Try to set the expression string if the element is an expression
		// element.
		if (element instanceof IExpressionElement) {
			IExpressionElement eElement = (IExpressionElement) element;
			String expressionString = null;
			try {
				expressionString = eElement.getExpressionString();
			}
			catch (RodinDBException e) {
				// Do nothing
			}
			
			// Set the expression string if the expression string is
			// <code>null</code> or is not equal the input text.
			if (expressionString == null || !expressionString.equals(text))
				eElement.setExpressionString(text, new NullProgressMonitor());
		}
		// Do nothing if the element is not an expression element
		return;
	}

}
