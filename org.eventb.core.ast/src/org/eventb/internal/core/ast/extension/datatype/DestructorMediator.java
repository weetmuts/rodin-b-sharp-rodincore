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

import java.util.Map;

import org.eventb.core.ast.ExtendedExpression;
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
import org.eventb.core.ast.extension.datatype.IDestructorMediator;
import org.eventb.core.ast.extension.datatype.ITypeParameter;
import org.eventb.internal.core.parser.BMath;

/**
 * @author Nicolas Beauger
 *
 */
public class DestructorMediator extends DatatypeMediator implements IDestructorMediator {


	public DestructorMediator(String typeName,
			Map<String, ITypeParameter> typeParams) {
		super(typeName, typeParams);
	}

	public void addDestructor(final String name, final String id, ITypeParameter returnType) {
		final IExpressionExtension destructor = new IExpressionExtension() {
			
			public Predicate getWDPredicate(IWDMediator wdMediator,
					IExtendedFormula formula) {
				return wdMediator.makeTrueWD();
			}
			
			public String getSyntaxSymbol() {
				return name;
			}
			
			public IExtensionKind getKind() {
				return PARENTHESIZED_UNARY_EXPRESSION;
			}
			
			public String getId() {
				return id;
			}
			
			public String getGroupId() {
				return BMath.BOUND_UNARY;
			}
			
			public void addPriorities(IPriorityMediator mediator) {
				// no priority
			}
			
			public void addCompatibilities(ICompatibilityMediator mediator) {
				// no compatibility				
			}
			
			public Type typeCheck(ITypeCheckMediator tcMediator,
					ExtendedExpression expression) {
				// TODO unification and return the datatype type
				return null;
			}
			
			public Type getType(ITypeMediator mediator, ExtendedExpression expression) {
				// TODO return the datatype type
				return null;
			}
		};
		extensions.add(destructor);
	}

}
