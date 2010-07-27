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
package org.eventb.core.ast.extension.datatype;

import java.util.List;

/**
 * @author Nicolas Beauger
 * @since 2.0
 *
 */
public interface IDatatypeMediator {

	ITypeParameter getTypeParameter(String name);

	IArgumentType newArgumentType(ITypeParameter type);

	/**
	 * @param typeParams
	 *            type parameters
	 * @return a new type constructor argument type
	 */
	IArgumentType newArgumentTypeConstr(List<IArgumentType> typeParams);

}
