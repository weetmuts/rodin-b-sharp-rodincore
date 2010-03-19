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
package org.eventb.core.ast.extension.notation;

/**
 * @author Nicolas Beauger
 * 
 */
public interface IFormulaChild extends INotationElement {

	public static enum Kind {
		PREDICATE, EXPRESSION
	}
	
	Kind getKind();
	
	int getIndex();
}
