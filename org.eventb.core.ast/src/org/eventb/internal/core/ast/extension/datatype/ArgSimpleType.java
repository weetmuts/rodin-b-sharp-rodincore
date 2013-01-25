/*******************************************************************************
 * Copyright (c) 2010, 2012 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.ast.extension.datatype;

import java.util.Map;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.ITypeMediator;
import org.eventb.core.ast.extension.datatype.ITypeParameter;

/**
 * @author Nicolas Beauger
 * 
 */
public class ArgSimpleType extends ArgumentType {

	private final Type type;

	public ArgSimpleType(Type type) {
		this.type = type;
	}

	@Override
	public Type toType(ITypeMediator mediator, TypeInstantiation instantiation) {
		return type;
	}

	@Override
	public boolean verifyType(Type proposedType, TypeInstantiation instantiation) {
		return proposedType.equals(type);
	}

	@Override
	public Expression toSet(FormulaFactory factory,
			Map<ITypeParameter, Expression> substitution) {
		return type.toExpression(factory);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ArgSimpleType)) {
			return false;
		}
		ArgSimpleType other = (ArgSimpleType) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return type.toString();
	}

}
