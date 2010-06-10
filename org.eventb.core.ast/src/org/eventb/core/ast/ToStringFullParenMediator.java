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
package org.eventb.core.ast;

import org.eventb.internal.core.ast.extension.IToStringMediator;

/**
 * @author Nicolas Beauger
 */
/* package */class ToStringFullParenMediator implements IToStringMediator {

	protected final StringBuilder builder;
	protected final String[] boundNames;
	protected final String operator;

	public ToStringFullParenMediator(StringBuilder builder, String[] boundNames, String operator) {
		this.builder = builder;
		this.boundNames = boundNames;
		this.operator = operator;
	}

	public void append(String string) {
		builder.append(string);
	}

	public void append(Formula<?> child, boolean isRight) {
		builder.append('(');
		child.toStringFullyParenthesized(builder, boundNames);
		builder.append(')');
	}

	public void appendOperator() {
		builder.append(operator);
	}

}
