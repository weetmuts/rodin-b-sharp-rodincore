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
package org.eventb.internal.core.ast.extension.datatype;

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.ast.GenericType;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.ITypeMediator;
import org.eventb.core.ast.extension.datatype.IArgumentType;

/**
 * @author Nicolas Beauger
 *
 */
public class ArgGenTypeRef implements IArgumentType {

	private final List<IArgumentType> argTypes;

	public ArgGenTypeRef(List<IArgumentType> argTypes) {
		this.argTypes = argTypes;
	}

	@Override
	public Type toType(ITypeMediator mediator, TypeInstantiation instantiation) {
		final List<Type> argTypesInst = new ArrayList<Type>();
		for (IArgumentType arg : argTypes) {
			final Type argType = arg.toType(mediator, instantiation);
			argTypesInst.add(argType);
		}
		return mediator.makeGenericType(argTypesInst, instantiation.getTypeExtn());
	}

	@Override
	public boolean verifyType(Type proposedType, TypeInstantiation instantiation) {
		if (!(proposedType instanceof GenericType)) {
			return false;
		}
		final GenericType genType = (GenericType) proposedType;
		if (genType.getExprExtension() != instantiation.getTypeExtn()) {
			return false;
		}
		final Type[] typeParams = genType.getTypeParameters();
		assert typeParams.length == argTypes.size();
		for (int i = 0; i < typeParams.length; i++) {
			if (!argTypes.get(i).verifyType(typeParams[i], instantiation)) {
				return false;
			}
		}
		return true;
	}
}
