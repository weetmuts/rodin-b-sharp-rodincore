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

import static org.eventb.internal.core.parser.OperatorRegistry.GROUP0;
import static org.eventb.internal.core.parser.OperatorRegistry.OperatorRelationship.COMPATIBLE;
import static org.eventb.internal.core.parser.OperatorRegistry.OperatorRelationship.LEFT_PRIORITY;
import static org.eventb.internal.core.parser.OperatorRegistry.OperatorRelationship.RIGHT_PRIORITY;
import static org.eventb.internal.core.parser.SubParsers.IDENT_SUBPARSER;
import static org.eventb.internal.core.parser.SubParsers.INTLIT_SUBPARSER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eventb.core.ast.Formula;
import org.eventb.core.ast.extension.CycleError;
import org.eventb.core.ast.extension.IOperatorProperties;
import org.eventb.internal.core.lexer.Token;
import org.eventb.internal.core.parser.GenParser.OverrideException;
import org.eventb.internal.core.parser.OperatorRegistry.OperatorRelationship;

/**
 * @author Nicolas Beauger
 *
 */
public abstract class AbstractGrammar {

	private static final String EOF_ID = "End of File";
	private static final String NOOP_ID = "No Operator";
	private static final String OPEN_ID = "Open";
	private static final String IDENT_IMAGE = "an identifier";
	private static final String INTLIT_IMAGE = "an integer literal";

	// FIXME make private
	protected static final IndexedSet<String> publicTokens = new IndexedSet<String>();
	
	
	public static final int _EOF = publicTokens.reserved("End Of Formula");
	public static final int _NOOP = publicTokens.reserved("No Operator");
	public static final int _OPEN = publicTokens.reserved("Open");
	public static final int _IDENT = publicTokens.reserved(IDENT_IMAGE);
	public static final int _INTLIT = publicTokens.reserved(INTLIT_IMAGE);
	public static final int _LPAR = publicTokens.getOrAdd("(");
	public static final int _RPAR = publicTokens.getOrAdd(")");
	public static final int _COMMA = publicTokens.getOrAdd(",");

	protected final IndexedSet<String> tokens = new IndexedSet<String>(publicTokens);
	
	private final LexKindParserDB subParsers = new LexKindParserDB();
	
	private final OperatorRegistry opRegistry = new OperatorRegistry();
	
	// used by extended grammar to fetch appropriate parser
	// and by extended formulae to fetch appropriate printers
	// TODO try to generalise to standard language operators
	private final PropertyParserDB propParsers = new PropertyParserDB();
	
	private final Map<Integer, Integer> closeOpenKinds = new HashMap<Integer, Integer>();
	
	public boolean isOperator(int kind) {
		// TODO could be replaced by 'there exists a tag for the given kind'
		return opRegistry.hasGroup(kind) && !tokens.isReserved(kind);
	}
	
	public IndexedSet<String> getTokens() {
		return tokens;
	}

	/**
	 * Initialises tokens, parsers and operator relationships.
	 * <p>
	 * Subclasses are expected to override and call this method first.
	 * </p>
	 */
	public final void init() {
		
		opRegistry.addOperator(_EOF, EOF_ID, GROUP0);
		opRegistry.addOperator(_NOOP, NOOP_ID, GROUP0);
		opRegistry.addOperator(_OPEN, OPEN_ID, GROUP0);
		addOpenClose("(", ")");
		
		// TODO call IntegerLiteral.init() and Identifier.init()
		subParsers.addNud(_INTLIT, INTLIT_SUBPARSER);
		subParsers.addNud(_IDENT, IDENT_SUBPARSER);
		subParsers.addNud(_LPAR, MainParsers.CLOSED_SUGAR);
		addOperators();
		addOperatorRelationships();
	}

	protected abstract void addOperators();
	protected abstract void addOperatorRelationships();
	
	public void addCompatibility(String leftOpId, String rightOpId) {
		opRegistry.addCompatibility(leftOpId, rightOpId);
	}
	
	public void addAssociativity(String opId) {
		opRegistry.addAssociativity(opId);
	}
	
	public void addPriority(String lowOpId, String highOpId) throws CycleError {
		opRegistry.addPriority(lowOpId, highOpId);
	}
	
	public void addGroupPriority(String lowGroupId, String highGroupId) throws CycleError {
		opRegistry.addGroupPriority(lowGroupId, highGroupId);
	}

	public List<INudParser<? extends Formula<?>>> getNudParsers(Token token) {
		return subParsers.getNudParsers(token);
	}
	
	public ILedParser<? extends Formula<?>> getLedParser(Token token) {
		return subParsers.getLedParser(token);
	}
	
	public IParserPrinter<? extends Formula<?>> getParser(IOperatorProperties operProps, int kind,
			int tag) {
		return propParsers.getParser(operProps, kind, tag);
	}

	public void addParser(IPropertyParserInfo<? extends Formula<?>> parserInfo)
			throws OverrideException {
		propParsers.add(parserInfo);
	}
	
	// TODO remove all other addOperator() methods
	public void addOperator(IOperatorInfo<? extends Formula<?>> operInfo)
			throws OverrideException {
		final int kind = tokens.getOrAdd(operInfo.getImage());
		opRegistry.addOperator(kind, operInfo.getId(), operInfo.getGroupId());
		final IParserPrinter<? extends Formula<?>> parser = operInfo.makeParser(kind);
		if (parser instanceof INudParser<?>) {
			subParsers.addNud(kind, (INudParser<? extends Formula<?>>) parser);
		} else {
			subParsers.addLed(kind, (ILedParser<? extends Formula<?>>) parser);
		}
	}
	
	public void addOperator(String token, String operatorId, String groupId,
			INudParser<? extends Formula<?>> subParser) {
		final int kind = tokens.getOrAdd(token);
		opRegistry.addOperator(kind, operatorId, groupId);
		subParsers.addNud(kind, subParser);
	}

	public void addOperator(String token, String operatorId, String groupId,
			ILedParser<? extends Formula<?>> subParser)
			throws OverrideException {
		final int kind = tokens.getOrAdd(token);
		opRegistry.addOperator(kind, operatorId, groupId);
		subParsers.addLed(kind, subParser);
	}

	public void addOperator(int kind, String operatorId, String groupId,
			INudParser<? extends Formula<?>> subParser)
			throws OverrideException {
		opRegistry.addOperator(kind, operatorId, groupId);
		subParsers.addNud(kind, subParser);
	}

	protected void addOpenClose(String open, String close) {
		final int openKind = tokens.getOrAdd(open);
		final int closeKind = tokens.getOrAdd(close);
		closeOpenKinds.put(closeKind, openKind);
	}

	public boolean isOpen(int kind) {
		return closeOpenKinds.containsValue(kind);
	}

	public boolean isClose(int kind) {
		return closeOpenKinds.containsKey(kind);
	}

	public void addReservedSubParser(int reservedKind,
			INudParser<? extends Formula<?>> subParser) {
		subParsers.addNud(reservedKind, subParser);
	}
	
	protected void addGroupPrioritySequence(String... groupIds) throws CycleError {
		for (int i = 0; i < groupIds.length - 1; i++) {
			opRegistry.addGroupPriority(groupIds[i], groupIds[i+1]);
		}
	}
	
	public OperatorRelationship getOperatorRelationship(int leftKind,
			int rightKind) {
		return opRegistry.getOperatorRelationship(leftKind, rightKind);
	}
	
	public int getEOF() {
		return _EOF;
	}
	
	public int getIDENT() {
		return _IDENT;
	}
	
	public int getINTLIT() {
		return _INTLIT;
	}
	
	public String getImage(int kind) {
		return tokens.getElem(kind);
	}

	public int getKind(String image) {
		final int kind = tokens.getIndex(image);
		if (kind == IndexedSet.NOT_AN_INDEX) {
			// TODO consider throwing a caught exception (for extensions to manage)
			throw new IllegalArgumentException("No such token: " + image);
		}
		return kind;
	}

	/**
	 * Returns whether parentheses are needed around a formula tag when it
	 * appears as a child of formula parentTag.
	 * 
	 * @param isRightChild
	 *            <code>true</code> if tag node is the right child parentTag,
	 *            <code>false</code> if it is the left child or a unique child
	 * @param childKind
	 * @param parentKind
	 * FIXME remove version argument, each grammar should answer separately
	 * @return <code>true</code> iff parentheses are needed
	 * @since 2.0
	 */
	public boolean needsParentheses(boolean isRightChild, int childKind,
			int parentKind) {
//		if (childKind == parentKind) {
//			// FIXME false for maplets
//			// FIXME missing case for 1 + - 2 (PLUS UNMINUS)
//			// FIXME false for FUNIMAGE
//			return true;
//		}
		if (parentKind == _EOF) { // TODO maybe not needed
			return false;
		}
		if (!isOperator(parentKind) || !isOperator(childKind)) {
			return false; // IDENT for instance
		}
		if (childKind == parentKind && opRegistry.isAssociative(parentKind)) {
			return true;
		}
		final OperatorRelationship relParentChild = getOperatorRelationship(parentKind,
				childKind);
		if (relParentChild == LEFT_PRIORITY) {
			// Rule 1: parent priority => parentheses
			return true;
		}
		if (relParentChild == RIGHT_PRIORITY) {
			// Rule 2: child priority => no parentheses
			return false;
		}
		// no priority is defined, now it is only a matter of left/right compatibility
		if (isRightChild && relParentChild == COMPATIBLE) {
			// parent on the left, child on the right
			// Rule 3: compatible right child => parentheses
			return true;
		}
		if (!isRightChild && getOperatorRelationship(childKind, parentKind) == COMPATIBLE) {
			// child on the left, parent on the right
			// Rule 4: compatible left child => no parentheses
			return false;
		}
		return true; // Other cases => parentheses
	}

}
