/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.indexers;

import static org.eventb.core.EventBPlugin.*;
import static org.eventb.internal.core.indexers.EventBIndexUtil.getRodinLocation;

import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.DefaultVisitor;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.SourceLocation;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.indexer.IDeclaration;
import org.rodinp.core.indexer.IIndexingBridge;
import org.rodinp.core.indexer.IOccurrenceKind;
import org.rodinp.core.location.IInternalLocation;

/**
 * @author Nicolas Beauger
 * 
 */
public class FormulaIndexer extends DefaultVisitor {

	private final IInternalElement visited;
	private final IAttributeType.String attributeType;
	private final IdentTable visibleIdents;
	private final IIndexingBridge bridge;

	public FormulaIndexer(IInternalElement visited,
			IAttributeType.String attributeType, IdentTable visibleIdents,
			IIndexingBridge bridge) {
		this.visited = visited;
		this.attributeType = attributeType;
		this.visibleIdents = visibleIdents;
		this.bridge = bridge;
	}

	@Override
	public boolean visitFREE_IDENT(FreeIdentifier ident) {
		index(ident, REFERENCE);

		return true;
	}

	@Override
	public boolean enterBECOMES_EQUAL_TO(BecomesEqualTo assign) {
		return false;
	}

	@Override
	public boolean exitBECOMES_EQUAL_TO(BecomesEqualTo assign) {
		for (FreeIdentifier ident : assign.getAssignedIdentifiers()) {
			index(ident, MODIFICATION);
		}

		for (Expression expression : assign.getExpressions()) {
			expression.accept(this);
		}

		return true;
	}

	/**
	 * @param ident
	 * @param kind
	 */
	private void index(FreeIdentifier ident, IOccurrenceKind kind) {
		if (ident.isPrimed()) {
			ident = ident.withoutPrime(FormulaFactory.getDefault());
		}

		if (visibleIdents.contains(ident)) {
			final IDeclaration declaration = visibleIdents.get(ident);
			final SourceLocation srcLoc = ident.getSourceLocation();
			final IInternalLocation loc =
					getRodinLocation(visited, attributeType, srcLoc);

			bridge.addOccurrence(declaration, kind, loc);
		}
	}

}