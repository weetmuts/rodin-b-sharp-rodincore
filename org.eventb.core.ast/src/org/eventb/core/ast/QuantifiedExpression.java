/*******************************************************************************
 * Copyright (c) 2005, 2010 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added accept for ISimpleVisitor
 *     Systerel - mathematical language v2
 *     Systerel - added support for predicate variables
 *     Systerel - added form filtering
 *******************************************************************************/
package org.eventb.core.ast;

import static org.eventb.core.ast.QuantifiedHelper.areEqualQuantifiers;
import static org.eventb.core.ast.QuantifiedHelper.checkBoundIdentTypes;
import static org.eventb.core.ast.QuantifiedHelper.getBoundIdentsAbove;
import static org.eventb.core.ast.QuantifiedHelper.getSyntaxTreeQuantifiers;
import static org.eventb.core.ast.QuantifiedUtil.catenateBoundIdentLists;
import static org.eventb.internal.core.parser.BMath.BRACE_SETS;
import static org.eventb.internal.core.parser.BMath.QUANTIFICATION;
import static org.eventb.internal.core.parser.SubParsers.CSET_EXPLICIT;
import static org.eventb.internal.core.parser.SubParsers.CSET_IMPLICIT;
import static org.eventb.internal.core.parser.SubParsers.CSET_LAMBDA;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eventb.internal.core.ast.IdentListMerger;
import org.eventb.internal.core.ast.IntStack;
import org.eventb.internal.core.ast.LegibilityResult;
import org.eventb.internal.core.ast.Position;
import org.eventb.internal.core.ast.extension.IToStringMediator;
import org.eventb.internal.core.parser.BMath;
import org.eventb.internal.core.parser.IParserPrinter;
import org.eventb.internal.core.parser.GenParser.OverrideException;
import org.eventb.internal.core.parser.SubParsers.ExplicitQuantExpr;
import org.eventb.internal.core.parser.SubParsers.ImplicitQuantExpr;
import org.eventb.internal.core.typecheck.TypeCheckResult;
import org.eventb.internal.core.typecheck.TypeUnifier;
import org.eventb.internal.core.typecheck.TypeVariable;

/**
 * QuantifiedExpression is the class for all quantified expressions in an
 * event-B formula.
 * <p>
 * It can accept tags {QUNION, QINTER, CSET}. The list of quantifiers is
 * inherited from QuantifiedFormula.
 * </p>
 * 
 * @author François Terrier
 * @since 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class QuantifiedExpression extends Expression {
	
	// children + form
	private final BoundIdentDecl[] quantifiedIdentifiers;
	private final Expression expr;
	private final Predicate pred;
	private final Form form;
	
	/**
	 * Enumerations of the possible forms that a quantified expression can take.
	 * <p>
	 * There are several equivalent notations for quantified expressions. This
	 * enumerates all the possible forms it can take.
	 * </p>
	 */
	public static enum Form {
		/**
		 * Formula is a lambda abstraction.
		 */
		Lambda,
		/**
		 * Formula is in its implicit form (i.e. { E | P } where E is an
		 * expression and P is a predicate)
		 */
		Implicit,
		/**
		 * Formula is in its explicit for (i.e. { L \u00b7 P | E } where L
		 * is a list of identifier, E is an expression and P is a predicate.)
		 */
		Explicit
	}

	/**
	 * Implements a checker for lambda patterns. From the end-user point of view, a
	 * lambda pattern must be made of pairwise distinct identifiers conjoined into
	 * maplets. Internally, this translates to having a pattern made of bound
	 * identifiers whose indexes are sorted in decreasing order when traversing the
	 * maplets from left to right.
	 * <p>
	 * This class implements an algorithm for checking that a lambda pattern indeed
	 * fulfills this constraint.
	 * </p>
	 */
	private static class PatternChecker {

		private int expectedIndex;

		public PatternChecker(int nbBoundIdentDecls) {
			this.expectedIndex = nbBoundIdentDecls - 1;
		}

		public boolean verify(Expression pattern) {
			return traverse(pattern) && expectedIndex == -1;
		}

		private boolean traverse(Expression pattern) {
			switch (pattern.getTag()) {
			case QuantifiedExpression.MAPSTO:
				final BinaryExpression maplet = (BinaryExpression) pattern;
				return traverse(maplet.getLeft()) && traverse(maplet.getRight());
			case QuantifiedExpression.BOUND_IDENT:
				final BoundIdentifier ident = (BoundIdentifier) pattern;
				return ident.getBoundIndex() == expectedIndex--;
			}
			return false;
		}

	}
	
	// offset of the tag interval in Formula
	protected final static int firstTag = FIRST_QUANTIFIED_EXPRESSION;
	protected final static String[] tags = {
		"\u22c3", // QUNION
		"\u22c2", // QINTER
		"CSET"    // CSET
	};
	// For testing purposes
	public static final int TAGS_LENGTH = tags.length;

	private static final String CSET_ID = "Comprehension Set";
	private static final String LAMBDA_ID = "Lambda";
	private static final String QUNION_ID = "Quantified Union";
	private static final String QINTER_ID = "Quantified Intersection";

	/**
	 * @since 2.0
	 */
	public static void init(BMath grammar) {
		try {
			grammar.addOperator("\u22c3", QUNION_ID, QUANTIFICATION, new ExplicitQuantExpr(QUNION));
			grammar.addOperator("\u22c3", QUNION_ID, QUANTIFICATION, new ImplicitQuantExpr(QUNION));
			grammar.addOperator("\u22c2", QINTER_ID, QUANTIFICATION, new ExplicitQuantExpr(QINTER));
			grammar.addOperator("\u22c2", QINTER_ID, QUANTIFICATION, new ImplicitQuantExpr(QINTER));
			grammar.addOperator("{", CSET_ID, BRACE_SETS, CSET_EXPLICIT);
			grammar.addOperator("{", CSET_ID, BRACE_SETS, CSET_IMPLICIT);
			grammar.addOperator("\u03bb", LAMBDA_ID, QUANTIFICATION, CSET_LAMBDA);
		} catch (OverrideException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void toString(IToStringMediator mediator) {
		final IParserPrinter<QuantifiedExpression> parser;
		switch (getTag()) {
		case CSET:
			switch (form) {
			case Explicit:
				parser = CSET_EXPLICIT;
				break;
			case Implicit:
				parser = CSET_IMPLICIT;
				break;
			case Lambda:
				parser = CSET_LAMBDA;
				break;
			default:
				throw newIllegalForm(form);
			}
			break;
		case QUNION:
		case QINTER:
			switch (form) {
			case Explicit:
				parser = new ExplicitQuantExpr(getTag());
				break;
			case Implicit:
				parser = new ImplicitQuantExpr(getTag());
				break;
			default:
				throw newIllegalForm(form);
			}
			break;
		default:
			throw newIllegalForm(form);
		}
		parser.toString(mediator, this);
	}

	private static IllegalStateException newIllegalForm(Form form) {
		return new IllegalStateException(
				"Illegal form for quantified expression: " + form);
	}
	
	/**
	 * @param expr the expression in the quantified expression. Must not be <code>null</code>
	 * @param pred the predicate in the quantified expression. Must not be <code>null</code>
	 * @param boundIdentifiers the identifiers that are bound to this specific quantified expression. Must not be <code>null</code>
	 * @param tag the associated tag
	 * @param location the location in the formula {@link org.eventb.core.ast.SourceLocation}
	 * @param form form of the quantified expression
	 */
	protected QuantifiedExpression(Expression expr, Predicate pred,
			BoundIdentDecl[] boundIdentifiers, int tag,
			SourceLocation location, Form form, FormulaFactory factory) {
		
		super(tag, location, combineHashCodes(
				boundIdentifiers.length, 
				pred.hashCode(), 
				expr.hashCode())
		);
		
		this.quantifiedIdentifiers = boundIdentifiers.clone();
		this.expr = expr;
		this.pred = pred;

		checkPreconditions(form);
		setPredicateVariableCache(this.pred, this.expr);
		synthesizeType(factory, null);

		// Must be after synthesizeType()
		this.form = filterForm(form);
	}
	
	protected QuantifiedExpression(Expression expr, Predicate pred,
			Collection<BoundIdentDecl> boundIdentifiers, int tag,
			SourceLocation location, Form form, FormulaFactory factory) {

		super(tag, location, combineHashCodes(
				boundIdentifiers.size(), 
				pred.hashCode(),
				expr.hashCode())
		);

		BoundIdentDecl[] model = new BoundIdentDecl[boundIdentifiers.size()];
		this.quantifiedIdentifiers = boundIdentifiers.toArray(model);
		this.expr = expr;
		this.pred = pred;

		checkPreconditions(form);
		setPredicateVariableCache(this.pred, this.expr);
		synthesizeType(factory, null);

		// Must be after synthesizeType()
		this.form = filterForm(form);
	}
	
	// Common initialization.
	private void checkPreconditions(Form inputForm) {
		assert getTag() >= firstTag && getTag() < firstTag+tags.length;
		assert quantifiedIdentifiers != null;
		assert 1 <= quantifiedIdentifiers.length;
		assert pred != null;
		assert expr != null;

		if (inputForm == Form.Lambda) {
			assert getTag() == Formula.CSET;
			assert expr.getTag() == Formula.MAPSTO;
		}
	}
	
	@Override
	protected void synthesizeType(FormulaFactory ff, Type givenType) {
		final IdentListMerger freeIdentMerger = 
			IdentListMerger.makeMerger(pred.freeIdents, expr.freeIdents);
		this.freeIdents = freeIdentMerger.getFreeMergedArray();

		final IdentListMerger boundIdentMerger = 
			IdentListMerger.makeMerger(pred.boundIdents, expr.boundIdents);
		final BoundIdentifier[] boundIdentsBelow = 
			boundIdentMerger.getBoundMergedArray(); 
		this.boundIdents = 
			getBoundIdentsAbove(boundIdentsBelow, quantifiedIdentifiers, ff);

		if (freeIdentMerger.containsError() || boundIdentMerger.containsError()) {
			// Incompatible type environments, don't bother going further.
			return;
		}
		
		// Check types of identifiers bound here.
		if (! checkBoundIdentTypes(boundIdentsBelow, quantifiedIdentifiers)) {
			return;
		}
		
		// Fast exit if children are not typed
		// (the most common case where type synthesis can't be done)
		if (! pred.isTypeChecked() || ! expr.isTypeChecked()) {
			return;
		}
		
		final Type exprType = expr.getType();
		final Type resultType;
		switch (getTag()) {
		case Formula.QUNION:
		case Formula.QINTER:
			final Type alpha = exprType.getBaseType();
			if (alpha != null) {
				resultType = exprType;
			} else {
				return;
			}
			break;
		case Formula.CSET:
			resultType = ff.makePowerSetType(exprType);
			break;
		default:
			assert false;
			return;
		}
		
		setFinalType(resultType, givenType);
	}
	
	private Form filterForm(Form inputForm) {
		switch (inputForm) {
		case Lambda:
			final PatternChecker checker = new PatternChecker(
					quantifiedIdentifiers.length);
			if (checker.verify(((BinaryExpression) expr).getLeft())) {
				return Form.Lambda;
			}
			// Fall through
		case Implicit:
			if (expr.freeIdents.length == 0) {
				// Expression is closed
				return Form.Implicit;
			}
			// Fall through
		case Explicit:
			// Fall through
		}
		return Form.Explicit;
	}
	
	// fills the parentheses maps
	static {
		BitSet propagate = new BitSet();
		BitSet propagateRight = new BitSet();
		
		propagate.set(Formula.NO_TAG);

		propagate.set(Formula.CSET);
		propagate.set(Formula.QUNION);
		propagate.set(Formula.QINTER);
		propagate.set(Formula.SETEXT);
		propagate.set(Formula.KBOOL);
		propagate.set(Formula.KCARD);
		propagate.set(Formula.POW);
		propagate.set(Formula.POW1);
		propagate.set(Formula.KUNION);
		propagate.set(Formula.KFINITE);
		propagate.set(Formula.KINTER);
		propagate.set(Formula.KDOM);
		propagate.set(Formula.KRAN);
		addDeprecatedUnaryTags(propagate);
		propagate.set(Formula.KMIN);
		propagate.set(Formula.KMAX);
		propagateRight.set(Formula.FUNIMAGE);
		propagateRight.set(Formula.RELIMAGE);
		
		
		propagate.set(Formula.EQUAL);
		propagate.set(Formula.NOTEQUAL);
		propagate.set(Formula.IN);
		propagate.set(Formula.NOTIN);
		propagate.set(Formula.SUBSET);
		propagate.set(Formula.NOTSUBSET);
		propagate.set(Formula.SUBSETEQ);
		propagate.set(Formula.NOTSUBSETEQ);
		propagate.set(Formula.LT);
		propagate.set(Formula.LE);
		propagate.set(Formula.GT);
		propagate.set(Formula.GE);
		propagate.set(Formula.FUNIMAGE);
		propagate.set(Formula.RELIMAGE);
		propagate.set(Formula.MAPSTO);
		propagate.set(Formula.REL);
		propagate.set(Formula.TREL);
		propagate.set(Formula.SREL);
		propagate.set(Formula.STREL);
		propagate.set(Formula.PFUN);
		propagate.set(Formula.TFUN);
		propagate.set(Formula.PINJ);
		propagate.set(Formula.TINJ);
		propagate.set(Formula.PSUR);
		propagate.set(Formula.TSUR);
		propagate.set(Formula.TBIJ);
		propagate.set(Formula.BUNION);
		propagate.set(Formula.BCOMP);
		propagate.set(Formula.OVR);
		propagate.set(Formula.CPROD);
		propagate.set(Formula.PPROD);
		propagate.set(Formula.SETMINUS);
		propagate.set(Formula.CPROD);
		propagate.set(Formula.FCOMP);
		propagate.set(Formula.BINTER);
		propagate.set(Formula.DOMRES);
		propagate.set(Formula.DOMSUB);
		propagate.set(Formula.RANRES);
		propagate.set(Formula.RANSUB);
		propagate.set(Formula.UPTO);
		propagate.set(Formula.PLUS);
		propagate.set(Formula.MINUS);
		propagate.set(Formula.UNMINUS);
		propagate.set(Formula.DIV);
		propagate.set(Formula.MOD);
		propagate.set(Formula.EXPN);
		
	}

	@SuppressWarnings("deprecation")
	private static void addDeprecatedUnaryTags(BitSet bitset) {
		bitset.set(Formula.KPRJ1);
		bitset.set(Formula.KPRJ2);
		bitset.set(Formula.KID);
	}

	/**
	 * Returns the form of this expression. This form corresponds to the way the
	 * expression was initially parsed. It doesn't have any impact on the
	 * mathematical meaning of this expression, which is always the same,
	 * whatever the form.
	 * 
	 * @return the form of this expression.
	 */
	public Form getForm() {
		return form;
	}
	
	/**
	 * Returns the list of the identifiers which are declared as bound by this formula.
	 * 
	 * @return list of bound identifier declarations
	 */
	public BoundIdentDecl[] getBoundIdentDecls() {
		return quantifiedIdentifiers.clone();
	}
	
	/**
	 * Returns the expression of this node.
	 * 
	 * @return the expression of the quantified formula
	 */
	public Expression getExpression() {
		return expr;
	}
	
	/**
	 * Returns the predicate of this node.
	 * 
	 * @return the predicate of the quantified formula
	 */
	public Predicate getPredicate() {
		return pred;
	}
	
	@Override
	protected String getSyntaxTree(String[] boundNames, String tabs) {
		final String typeName = getType()!=null?" [type: "+getType().toString()+"]":"";
		final String[] boundNamesBelow = catenateBoundIdentLists(boundNames, quantifiedIdentifiers);
		
		return tabs
				+ this.getClass().getSimpleName()
				+ " ["
				+ tags[getTag() - firstTag] 
				+ ", " + form.toString()
				+ "]" 
				+ typeName
				+ "\n"
				+ getSyntaxTreeQuantifiers(boundNames, tabs + "\t", quantifiedIdentifiers)
				+ expr.getSyntaxTree(boundNamesBelow,tabs + "\t")
				+ pred.getSyntaxTree(boundNamesBelow,tabs + "\t");
	}
	
	@Override
	protected void isLegible(LegibilityResult result, BoundIdentDecl[] boundAbove) {
		
		for (BoundIdentDecl decl: quantifiedIdentifiers) {
			decl.isLegible(result, boundAbove);
			if (! result.isSuccess()) {
				break;
			}
		}
		
		final BoundIdentDecl[] boundBelow = catenateBoundIdentLists(boundAbove, quantifiedIdentifiers);
		if (result.isSuccess()) {
			pred.isLegible(result, boundBelow);
		}
		if (result.isSuccess()) {
			expr.isLegible(result, boundBelow);
		}
	}
	
	@Override
	protected boolean equals(Formula<?> other, boolean withAlphaConversion) {
		if (this.getTag() != other.getTag()) {
			return false;
		}
		QuantifiedExpression temp = (QuantifiedExpression) other;
		return hasSameType(other)
				&& areEqualQuantifiers(quantifiedIdentifiers,
						temp.quantifiedIdentifiers, withAlphaConversion)
				&& expr.equals(temp.expr, withAlphaConversion)
				&& pred.equals(temp.pred, withAlphaConversion);
	}

	@Override
	protected void typeCheck(TypeCheckResult result, BoundIdentDecl[] quantifiedIdents) {
		for (BoundIdentDecl decl: quantifiedIdentifiers) {
			decl.typeCheck(result, quantifiedIdents);
		}
		
		final BoundIdentDecl[] newQuantifiers = 
			catenateBoundIdentLists(quantifiedIdents, quantifiedIdentifiers);
		pred.typeCheck(result,newQuantifiers);
		expr.typeCheck(result,newQuantifiers);

		Type resultType;
		switch (getTag()) {
		case Formula.QUNION:
		case Formula.QINTER:
			final TypeVariable alpha = result.newFreshVariable(null);
			resultType = result.makePowerSetType(alpha);
			result.unify(expr.getType(), resultType, this);
			break;
		case Formula.CSET:
			resultType = result.makePowerSetType(expr.getType());
			break;
		default:
			assert false;
			return;
		}
		setTemporaryType(resultType, result);
	}
	
	@Override
	protected boolean solveChildrenTypes(TypeUnifier unifier) {
		boolean success = true;
		for (BoundIdentDecl ident: quantifiedIdentifiers) {
			success &= ident.solveType(unifier);
		}
		success &= expr.solveType(unifier);
		success &= pred.solveType(unifier);
		return success;
	}

	@Override
	protected void collectFreeIdentifiers(LinkedHashSet<FreeIdentifier> freeIdentSet) {
		// Take care to go from left to right
		switch (form) {
		case Lambda:
		case Explicit:
			pred.collectFreeIdentifiers(freeIdentSet);
			expr.collectFreeIdentifiers(freeIdentSet);
			break;

		case Implicit:
			expr.collectFreeIdentifiers(freeIdentSet);
			pred.collectFreeIdentifiers(freeIdentSet);
			break;

		default:
			assert false;
		}
	}

	/**
	 * Returns the list of all names that either occur free in this formula, or
	 * have been quantified somewhere above this node (that is closer to the
	 * root of the tree).
	 * 
	 * @param boundNames
	 *            array of names that are declared above this formula. These
	 *            names must be stored in the order in which they appear when
	 *            the formula is written from left to right
	 * @return the list of all names that occur in this formula and are not
	 *         declared within.
	 */
	public Set<String> collectNamesAbove(String[] boundNames) {
		Set<String> result = new HashSet<String>();
		expr.collectNamesAbove(result, boundNames, quantifiedIdentifiers.length);
		pred.collectNamesAbove(result, boundNames, quantifiedIdentifiers.length);
		return result;
	}

	@Override
	protected void collectNamesAbove(Set<String> names, String[] boundNames, int offset) {
		final int newOffset = offset + quantifiedIdentifiers.length;
		pred.collectNamesAbove(names, boundNames, newOffset);
		expr.collectNamesAbove(names, boundNames, newOffset);
	}

	@Override
	protected Expression bindTheseIdents(Map<String, Integer> binding, int offset, FormulaFactory factory) {
		final int newOffset = offset + quantifiedIdentifiers.length; 
		Predicate newPred = pred.bindTheseIdents(binding, newOffset, factory);
		Expression newExpr = expr.bindTheseIdents(binding, newOffset, factory);
		if (newExpr == expr && newPred == pred) {
			return this;
		}
		return factory.makeQuantifiedExpression(getTag(), quantifiedIdentifiers, newPred, newExpr, getSourceLocation(), form);
	}

	@Override
	public boolean accept(IVisitor visitor) {
		boolean goOn = true;

		switch (getTag()) {
		case QUNION: goOn = visitor.enterQUNION(this); break;
		case QINTER: goOn = visitor.enterQINTER(this); break;
		case CSET:   goOn = visitor.enterCSET(this);   break;
		default:     assert false;
		}

		for (int i = 0; goOn && i < quantifiedIdentifiers.length; i++) {
			goOn = quantifiedIdentifiers[i].accept(visitor);
			if (goOn) goOn = acceptContinue(visitor);
		}
		if (goOn) goOn = pred.accept(visitor);
		if (goOn) goOn = acceptContinue(visitor);
		if (goOn) goOn = expr.accept(visitor);
		
		switch (getTag()) {
		case QUNION: return visitor.exitQUNION(this);
		case QINTER: return visitor.exitQINTER(this);
		case CSET:   return visitor.exitCSET(this);
		default:     return true;
		}
	}
	
	@Override
	public void accept(ISimpleVisitor visitor) {
		visitor.visitQuantifiedExpression(this);
	}

	private boolean acceptContinue(IVisitor visitor) {
		switch (getTag()) {
		case QUNION: return visitor.continueQUNION(this);
		case QINTER: return visitor.continueQINTER(this);
		case CSET:   return visitor.continueCSET(this);   
		default:     assert false; return true;
		}
	}

	private Predicate getWDPredicateQINTER(FormulaFactory formulaFactory) {
		final SourceLocation loc = getSourceLocation();
		Predicate conj0 = getWDPredicateQUNION(formulaFactory);
		Predicate conj1 = getWDSimplifyQ(formulaFactory, EXISTS,
				quantifiedIdentifiers, pred, loc);
		return getWDSimplifyC(formulaFactory, conj0, conj1);
	}

	private Predicate getWDPredicateQUNION(FormulaFactory formulaFactory) {
		Predicate conj0 = pred.getWDPredicateRaw(formulaFactory);
		Predicate conj1 = getWDSimplifyI(formulaFactory, pred, 
				expr.getWDPredicateRaw(formulaFactory));
		Predicate inner = getWDSimplifyC(formulaFactory, conj0, conj1);
		final SourceLocation loc = getSourceLocation();
		return getWDSimplifyQ(formulaFactory, FORALL, quantifiedIdentifiers,
				inner, loc);
	}

	@Override
	protected Predicate getWDPredicateRaw(FormulaFactory formulaFactory) {
		switch (getTag()) {
		case QUNION:
		case CSET:
			return getWDPredicateQUNION(formulaFactory);
		case QINTER:
			return getWDPredicateQINTER(formulaFactory);
		default:
			assert false; 
			return formulaFactory.makeLiteralPredicate(BFALSE, null);
		}
	}

	@Override
	public Expression rewrite(IFormulaRewriter rewriter) {
		final int nbOfBoundIdentDecls = quantifiedIdentifiers.length;
		
		rewriter.enteringQuantifier(nbOfBoundIdentDecls);
		final Predicate newPred = pred.rewrite(rewriter);
		final Expression newExpr = expr.rewrite(rewriter);
		rewriter.leavingQuantifier(nbOfBoundIdentDecls);

		// TODO: implement cleanup of unused bound ident decls.
		
		final QuantifiedExpression before;
		if (newPred == pred && newExpr == expr) {
			before = this;
		} else {
			before = rewriter.getFactory().makeQuantifiedExpression(getTag(),
					quantifiedIdentifiers, newPred, newExpr,
					getSourceLocation(), form);
		}
		return checkReplacement(rewriter.rewrite(before));
	}

	@Override
	protected void addGivenTypes(Set<GivenType> set) {
		for (BoundIdentDecl decl: quantifiedIdentifiers) {
			decl.addGivenTypes(set);
		}
		expr.addGivenTypes(set);
		pred.addGivenTypes(set);
	}

	// TODO add instantiation of condition

	// TODO add instantiation of subexpression

	@Override
	protected void getPositions(IFormulaFilter filter, IntStack indexes,
			List<IPosition> positions) {
		
		if (filter.select(this)) {
			positions.add(new Position(indexes));
		}

		indexes.push(0);
		for (BoundIdentDecl decl: quantifiedIdentifiers) {
			decl.getPositions(filter, indexes, positions);
			indexes.incrementTop();
		}
		pred.getPositions(filter, indexes, positions);
		indexes.incrementTop();
		expr.getPositions(filter, indexes, positions);
		indexes.pop();
	}
	
	@Override
	protected Formula<?> getChild(int index) {
		if (index < quantifiedIdentifiers.length) {
			return quantifiedIdentifiers[index];
		}
		index = index - quantifiedIdentifiers.length;
		switch (index) {
		case 0:
			return pred;
		case 1:
			return expr;
		default:
			return null;
		}
	}

	@Override
	protected IPosition getDescendantPos(SourceLocation sloc, IntStack indexes) {
		IPosition pos;
		if (form == Form.Explicit) {
			indexes.push(0);
			for (BoundIdentDecl decl: quantifiedIdentifiers) {
				pos = decl.getPosition(sloc, indexes);
				if (pos != null)
					return pos;
				indexes.incrementTop();
			}
		} else {
			indexes.push(quantifiedIdentifiers.length);
		}
		pos = pred.getPosition(sloc, indexes);
		if (pos != null)
			return pos;
		indexes.incrementTop();
		if (form != Form.Lambda) {
			pos = expr.getPosition(sloc, indexes);
			if (pos != null)
				return pos;
		} else {
			// For a lambda expression, we have to skip over the maplet expression
			BinaryExpression maplet = (BinaryExpression) expr;
			indexes.push(0);
			pos = maplet.getLeft().getPosition(sloc, indexes);
			if (pos != null)
				return pos;
			indexes.incrementTop();
			pos = maplet.getRight().getPosition(sloc, indexes);
			if (pos != null)
				return pos;
			indexes.pop();
		}
		indexes.pop();
		return new Position(indexes);
	}

	@Override
	protected Expression rewriteChild(int index, SingleRewriter rewriter) {
		BoundIdentDecl[] newDecls = quantifiedIdentifiers;
		Predicate newPred = pred;
		Expression newExpr = expr;
		final int length = quantifiedIdentifiers.length;
		if (index < length) {
			newDecls = quantifiedIdentifiers.clone();
			newDecls[index] = rewriter.rewrite(quantifiedIdentifiers[index]);
		} else if (index == length) {
			newPred = rewriter.rewrite(pred);
		} else if (index == length + 1) {
			newExpr = rewriter.rewrite(expr);
		} else {
			throw new IllegalArgumentException("Position is outside the formula");
		}
		return rewriter.factory.makeQuantifiedExpression(getTag(),
				newDecls, newPred, newExpr, getSourceLocation(), form);
	}

}
