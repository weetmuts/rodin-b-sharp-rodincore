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
package org.eventb.internal.core.parser;

import java.util.Set;

import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.extension.IExtensionKind;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.internal.core.ast.extension.CompatibilityMediator;
import org.eventb.internal.core.ast.extension.PriorityMediator;
import org.eventb.internal.core.parser.GenParser.OverrideException;

/**
 * @author Nicolas Beauger
 *
 */
public class ExtendedGrammar extends BMathV2 {

	private final Set<IFormulaExtension> extensions;

	public ExtendedGrammar(Set<IFormulaExtension> extensions) {
		this.extensions = extensions;
	}
	
	@Override
	protected void addOperators() {
		super.addOperators();
	
		ExtendedExpression.init(this);
		ExtendedPredicate.init(this);
		try {
			for (IFormulaExtension extension : extensions) {
				
				final IExtensionKind extKind = extension.getKind();
				final int kind = tokens.getOrAdd(extension.getSyntaxSymbol());
				final int tag = FormulaFactory.getTag(extension);
				final IParserPrinter<? extends Formula<?>> parser = getParser(
						extKind.getProperties(), kind, tag);

				final String operatorId = extension.getId();
				final String groupId = extension.getGroupId();

				// FIXME the syntax symbol must not already exist as an
				// operator (an extension shall not add backtracking)
				if (parser instanceof INudParser<?>) {
				addOperator(extension.getSyntaxSymbol(), operatorId, groupId,
						(INudParser<? extends Formula<?>>) parser);
				} else if (parser instanceof ILedParser<?>) {
					addOperator(extension.getSyntaxSymbol(), operatorId, groupId,
							(ILedParser<? extends Formula<?>>) parser);
				} else {
					// should not be ever possible
					throw new IllegalStateException("Unparseable extension kind: "
							+ extKind);
					
				}
			}
		} catch (OverrideException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void addOperatorRelationships() {
		super.addOperatorRelationships();

		for (IFormulaExtension extension : extensions) {
			extension.addCompatibilities(new CompatibilityMediator(this));
			extension.addPriorities(new PriorityMediator(this));

		}
	}

}
