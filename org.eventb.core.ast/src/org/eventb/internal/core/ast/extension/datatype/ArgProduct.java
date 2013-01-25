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

import static org.eventb.core.ast.Formula.CPROD;

import java.util.Map;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ProductType;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.ITypeMediator;
import org.eventb.core.ast.extension.datatype.ITypeParameter;

/**
 * @author Nicolas Beauger
 * 
 */
public class ArgProduct extends ArgumentType {

	private final ArgumentType left;
	private final ArgumentType right;

	public ArgProduct(ArgumentType left, ArgumentType right) {
		this.left = left;
		this.right = right;

	}

	@Override
	public Type toType(ITypeMediator mediator, TypeInstantiation instantiation) {
		final Type leftType = left.toType(mediator, instantiation);
		final Type rightType = right.toType(mediator, instantiation);
		return mediator.makeProductType(leftType, rightType);
	}

	@Override
	public boolean verifyType(Type proposedType, TypeInstantiation instantiation) {
		if (!(proposedType instanceof ProductType)) {
			return false;
		}
		final ProductType prod = (ProductType) proposedType;
		final Type leftType = prod.getLeft();
		final Type rightType = prod.getRight();
		return left.verifyType(leftType, instantiation)
				&& right.verifyType(rightType, instantiation);
	}

	@Override
	public Expression toSet(FormulaFactory factory,
			Map<ITypeParameter, Expression> substitution) {
		final Expression leftSet = left.toSet(factory, substitution);
		final Expression rightSet = right.toSet(factory, substitution);
		return factory.makeBinaryExpression(CPROD, leftSet, rightSet, null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		if (!(obj instanceof ArgProduct)) {
			return false;
		}
		ArgProduct other = (ArgProduct) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "'(" + left + "×" + right + ")";
	}

}
