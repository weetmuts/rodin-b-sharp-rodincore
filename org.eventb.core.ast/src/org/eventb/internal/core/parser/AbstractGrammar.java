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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eventb.core.ast.Formula;
import org.eventb.core.ast.extension.CycleError;
import org.eventb.internal.core.parser.GenParser.OverrideException;
import org.eventb.internal.core.parser.GenParser.SyntaxCompatibleError;

/**
 * @author Nicolas Beauger
 *
 */
public abstract class AbstractGrammar {

	private static final String EOF_ID = "End of File";

	static int _EOF;
	static int _LPAR;
	static int _RPAR;
	static int _IDENT;
	static int _INTLIT;
	static int _COMMA;

	protected final IndexedSet<String> tokens = new IndexedSet<String>();
	
	private final SubParserRegistry subParsers = new SubParserRegistry();
	
	protected final OperatorRegistry opRegistry = new OperatorRegistry();
	
	private final Map<Integer, Integer> closeOpenKinds = new HashMap<Integer, Integer>();
	
	public boolean isOperator(Token token) {
		return subParsers.isOperator(token);
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
	// TODO split into several init methods, one for each data (?)
	public void init() {
		_EOF = tokens.reserved();
		_LPAR = tokens.getOrAdd("(");
		_RPAR = tokens.getOrAdd(")");
		_COMMA = tokens.getOrAdd(",");
		
		opRegistry.addOperator(_EOF, EOF_ID, GROUP0);
		addOpenClose("(", ")");
		try {
			_INTLIT = addReservedSubParser(Parsers.INTLIT_SUBPARSER);
			_IDENT = addReservedSubParser(Parsers.IDENT_SUBPARSER);
			subParsers.addNud(_LPAR, Parsers.CLOSED_SUGAR);
		} catch (OverrideException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<INudParser<? extends Formula<?>>> getNudParsers(Token token) {
		return subParsers.getNudParsers(token);
	}
	
	public ILedParser<? extends Formula<?>> getLedParser(Token token) {
		return subParsers.getLedParser(token);
	}
	
	protected void addOperator(String token, String operatorId, String groupId,
			INudParser<? extends Formula<?>> subParser)
			throws OverrideException {
		final int kind = tokens.getOrAdd(token);
		opRegistry.addOperator(kind, operatorId, groupId);
		subParsers.addNud(kind, subParser);
	}
	
	protected void addOperator(String token, String operatorId, String groupId,
			ILedParser<? extends Formula<?>> subParser)
			throws OverrideException {
		final int kind = tokens.getOrAdd(token);
		opRegistry.addOperator(kind, operatorId, groupId);
		subParsers.addLed(kind, subParser);
	}

	protected void addOperator(int kind, String operatorId, String groupId,
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

	private int addReservedSubParser(INudParser<? extends Formula<?>> subParser)
			throws OverrideException {
		final int kind = tokens.reserved();
		subParsers.addReserved(kind, subParser);
		return kind;
	}
	
	protected void addLiteralOperator(String token, int tag,
			INudParser<? extends Formula<?>> subParser) throws OverrideException {
		final int kind = tokens.getOrAdd(token);
		subParsers.addNud(kind, subParser);
	}

	protected void addGroupPrioritySequence(String... groupIds) throws CycleError {
		for (int i = 0; i < groupIds.length - 1; i++) {
			opRegistry.addGroupPriority(groupIds[i], groupIds[i+1]);
		}
	}
	
	public boolean hasLessPriority(int leftKind, int rightKind) throws SyntaxCompatibleError {
		return opRegistry.hasLessPriority(leftKind, rightKind);
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
	

}
