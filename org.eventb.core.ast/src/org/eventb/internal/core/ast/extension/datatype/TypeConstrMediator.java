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

import static org.eventb.core.ast.extension.ExtensionKindFactory.makePrefixKind;
import static org.eventb.core.ast.extension.IFormulaExtension.ATOMIC_EXPRESSION;
import static org.eventb.core.ast.extension.IOperatorProperties.FormulaType.EXPRESSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.ParametricType;
import org.eventb.core.ast.PowerSetType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.ICompatibilityMediator;
import org.eventb.core.ast.extension.IExpressionExtension;
import org.eventb.core.ast.extension.IExtendedFormula;
import org.eventb.core.ast.extension.IExtensionKind;
import org.eventb.core.ast.extension.IPriorityMediator;
import org.eventb.core.ast.extension.ITypeCheckMediator;
import org.eventb.core.ast.extension.ITypeMediator;
import org.eventb.core.ast.extension.IWDMediator;
import org.eventb.core.ast.extension.datatype.IDatatypeExtension;
import org.eventb.core.ast.extension.datatype.ITypeConstructorMediator;
import org.eventb.core.ast.extension.datatype.ITypeParameter;

/**
 * @author Nicolas Beauger
 * 
 */
public class TypeConstrMediator implements ITypeConstructorMediator {

	private static class TypeConstructor implements IExpressionExtension {

		private String typeName;
		private IExtensionKind kind;
		private String id;
		private String groupId;

		public TypeConstructor(String typeName, IExtensionKind kind, String id,
				String groupId) {
			this.typeName = typeName;
			this.kind = kind;
			this.id = id;
			this.groupId = groupId;
		}

		@Override
		public Predicate getWDPredicate(IExtendedFormula formula,
				IWDMediator wdMediator) {
			return wdMediator.makeTrueWD();
		}

		@Override
		public String getSyntaxSymbol() {
			return typeName;
		}

		@Override
		public IExtensionKind getKind() {
			return kind;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getGroupId() {
			return groupId;
		}

		@Override
		public void addPriorities(IPriorityMediator mediator) {
			// no priority
		}

		@Override
		public void addCompatibilities(ICompatibilityMediator mediator) {
			// no priority
		}

		@Override
		public Type typeCheck(ExtendedExpression expression,
				ITypeCheckMediator tcMediator) {
			final List<Type> prmTypes = new ArrayList<Type>();
			for (Expression child : expression.getChildExpressions()) {
				final Type alpha = tcMediator.newTypeVariable();
				final PowerSetType prmType = tcMediator.makePowerSetType(alpha);
				tcMediator.sameType(prmType, child.getType());
				prmTypes.add(prmType);
			}
			return tcMediator.makePowerSetType(tcMediator.makeParametricType(
					prmTypes, this));
		}

		@Override
		public Type synthesizeType(Expression[] childExprs,
				Predicate[] childPreds, ITypeMediator mediator) {
			final List<Type> childTypes = new ArrayList<Type>();
			for (Expression child : childExprs) {
				final Type childType = child.getType();
				if (childType == null) {
					return null;
				}
				final Type baseType = childType.getBaseType();
				if (baseType == null) {
					return null;
				}
				childTypes.add(baseType);
			}
			return mediator.makePowerSetType(mediator.makeParametricType(
					childTypes, this));
		}

		@Override
		public boolean verifyType(Type proposedType, Expression[] childExprs,
				Predicate[] childPreds) {
			final Type baseType = proposedType.getBaseType();
			if (baseType == null) {
				return false;
			}
			if (!(baseType instanceof ParametricType)) {
				return false;
			}
			final ParametricType genType = (ParametricType) baseType;
			if (genType.getExprExtension() != this) {
				return false;
			}
			final Type[] typeParameters = genType.getTypeParameters();
			assert childExprs.length == typeParameters.length;
			for (int i = 0; i < childExprs.length; i++) {
				final Type childType = childExprs[i].getType();
				if (!typeParameters[i].equals(childType.getBaseType())) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean conjoinChildrenWD() {
			return true;
		}

		@Override
		public boolean isATypeConstructor() {
			return true;
		}

	}

	private final List<ITypeParameter> typeParams = new ArrayList<ITypeParameter>();
	private final IDatatypeExtension datatype;

	public TypeConstrMediator(IDatatypeExtension datatype) {
		this.datatype = datatype;
	}

	@Override
	public void addTypeParam(String name) {
		typeParams.add(new TypeParam(name));
	}

	public List<ITypeParameter> getTypeParams() {
		return Collections.unmodifiableList(typeParams);
	}

	public IExpressionExtension getTypeConstructor() {
		final String typeName = datatype.getTypeName();
		final String id = datatype.getId();
		final String groupId = datatype.getGroupId();
		final IExtensionKind kind = computeKind(typeParams.size());

		return new TypeConstructor(typeName, kind, id, groupId);
	}

	private static IExtensionKind computeKind(int nbArgs) {
		if (nbArgs == 0) {
			return ATOMIC_EXPRESSION;
		}
		return makePrefixKind(EXPRESSION, nbArgs, EXPRESSION);
	}
}