/*
 * Created on 11-may-2005
 *
 */
package org.eventb.core.ast;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eventb.internal.core.ast.LegibilityResult;
import org.eventb.internal.core.ast.Substitution;
import org.eventb.internal.core.typecheck.TypeCheckResult;
import org.eventb.internal.core.typecheck.TypeUnifier;
import org.eventb.internal.core.typecheck.TypeVariable;

/**
 * UnaryExpression is the base class for all unary expressions in an event-B
 * formula.
 * <p>
 * It can accept tags {KCARD, POW, POW1, KUNION, KINTER, KDOM, KRAN, KPRJ1,
 * KPRJ2, KID, KMIN, KMAX, CONVERSE, UNMINUS}.
 * </p>
 * 
 * @author François Terrier
 */
public class UnaryExpression extends Expression {

	
	protected final Expression child;

	// offset in the corresponding tag interval
	protected static final int firstTag = FIRST_UNARY_EXPRESSION;
	protected static final String[] tags = {
		"card",   // KCARD
		"\u2119", // POW
		"\u21191",// POW1
		"union",  // KUNION
		"inter",  // KINTER
		"dom",    // KDOM
		"ran",    // KRAN
		"prj1",   // KPRJ1
		"prj2",   // KPRJ2
		"id",     // KID
		"min",    // KMIN
		"max",    // KMAX
		"\u223c", // CONVERSE
		"\u2212"  // UNMINUS
	};
	// For testing purposes
	public static final int TAGS_LENGTH = tags.length;

	// indicates whether the corresponding operator has to be
	// written before or after the operand
	private static final boolean[] isPrefix = {
		true, // KCARD
		true, // POW
		true, // POW1
		true, // KUNION
		true, // KINTER
		true, // KDOM
		true, // KRAN
		true, // KPRJ1
		true, // KPRJ2
		true, // KID
		true, // KMIN
		true, // KMAX
		false,// CONVERSE
		true  // UNMINUS
	};
	
	// indicates when the operand has to be parenthesized
	// this is used by method toString
	private static final boolean[] alwaysParenthesized = {
		true, // KCARD
		true, // POW
		true, // POW1
		true, // KUNION
		true, // KINTER
		true, // KDOM
		true, // KRAN
		true, // KPRJ1
		true, // KPRJ2
		true, // KID
		true, // KMIN
		true, // KMAX
		false,// CONVERSE
		false // UNMINUS
	};
	                             
	protected UnaryExpression(Expression child, int tag, SourceLocation location,
			FormulaFactory factory) {
		
		super(tag, location, child.hashCode());
		this.child = child;

		assert tag >= firstTag && tag < firstTag+tags.length;
		assert child != null;
		
		synthesizeType(factory);
	}

	private void synthesizeType(FormulaFactory ff) {
		this.freeIdents = child.freeIdents;
		this.boundIdents = child.boundIdents;

		Type childType = child.getType();
		
		// Fast exit if children are not typed
		// (the most common case where type synthesis can't be done)
		if (childType == null) {
			return;
		}
		
		final Type resultType;
		final Type alpha, beta;
		switch (getTag()) {
		case Formula.UNMINUS:
			if (childType instanceof IntegerType) {
				resultType = ff.makeIntegerType();
			} else {
				resultType = null;
			}
			break;
		case Formula.CONVERSE:
			alpha = childType.getSource();
			beta = childType.getTarget();
			if (alpha != null) {
				resultType = ff.makeRelationalType(beta, alpha);
			} else {
				resultType = null;
			}
			break;
		case Formula.KCARD:
			alpha = childType.getBaseType();
			if (alpha != null) {
				resultType = ff.makeIntegerType();
			} else {
				resultType = null;
			}
			break;
		case Formula.POW:
		case Formula.POW1:
			alpha = childType.getBaseType();
			if (alpha != null) {
				resultType = ff.makePowerSetType(childType);
			} else {
				resultType = null;
			}
			break;
		case Formula.KUNION:
		case Formula.KINTER:
			final Type baseType = childType.getBaseType();
			if (baseType != null && baseType.getBaseType() != null) {
				resultType = baseType;
			} else {
				resultType = null;
			}
			break;
		case Formula.KDOM:
			alpha = childType.getSource();
			if (alpha != null) {
				resultType = ff.makePowerSetType(alpha);
			} else {
				resultType = null;
			}
			break;
		case Formula.KRAN:
			beta = childType.getTarget();
			if (beta != null) {
				resultType = ff.makePowerSetType(beta);
			} else {
				resultType = null;
			}
			break;
		case Formula.KPRJ1:
			alpha = childType.getSource();
			beta = childType.getTarget();
			if (alpha != null) {
				resultType = ff.makeRelationalType(
						ff.makeProductType(alpha, beta),
						alpha);
			} else {
				resultType = null;
			}
			break;
		case Formula.KPRJ2:
			alpha = childType.getSource();
			beta = childType.getTarget();
			if (alpha != null) {
				resultType = ff.makeRelationalType(
						ff.makeProductType(alpha, beta),
						beta);
			} else {
				resultType = null;
			}
			break;
		case Formula.KID:
			alpha = childType.getBaseType();
			if (alpha != null) {
				resultType = ff.makeRelationalType(alpha, alpha);
			} else {
				resultType = null;
			}
			break;
		case Formula.KMIN:
		case Formula.KMAX:
			alpha = childType.getBaseType();
			if (alpha instanceof IntegerType) {
				resultType = alpha;
			} else {
				resultType = null;
			}
			break;
		default:
			assert false;
			resultType = null;
		}
		setType(resultType, null);
	}

	// for the operands that do not always need to be parenthesized,
	// indicates when parentheses should be output in the toString method
	private static BitSet[] leftParenthesesMap = new BitSet[tags.length];
	private static BitSet[] rightParenthesesMap = new BitSet[tags.length];
	
	
	static {
		assert isPrefix.length == tags.length;
		assert alwaysParenthesized.length == tags.length;
		assert leftParenthesesMap.length == tags.length;
		assert rightParenthesesMap.length == tags.length;

		for(int i=0; i<tags.length; i++) {
			leftParenthesesMap[i] = new BitSet();
			rightParenthesesMap[i] = new BitSet();
		}
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.PLUS);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.MINUS);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.UNMINUS);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.MUL);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.DIV);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.MOD);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.CONVERSE);
		rightParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.EXPN);
		leftParenthesesMap[Formula.UNMINUS-firstTag] = (BitSet)rightParenthesesMap[Formula.UNMINUS-firstTag].clone();
		leftParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.FUNIMAGE);
		leftParenthesesMap[Formula.UNMINUS-firstTag].set(Formula.RELIMAGE);
		
	}
	
	/**
	 * Returns the unique child of this node.
	 * 
	 * @return child of this node.
	 */
	public Expression getChild() {
		return child;
	}
	
	@Override
	protected String toString(boolean isRightChild, int parentTag, String[] boundNames) {
		if (isPrefix[getTag()-firstTag]) {
			if (isAlwaysParenthesized()) {
				return getTagOperator()+"("+child.toString(false, getTag(), boundNames)+")";
			}
			else if ((isRightChild && rightParenthesesMap[getTag()-firstTag].get(parentTag)) || (!isRightChild && leftParenthesesMap[getTag()-firstTag].get(parentTag))) {
				return "("+getTagOperator()+child.toString(false, getTag(), boundNames)+")";
			}
			else {
				return getTagOperator()+child.toString(false, getTag(), boundNames);
			}
		}
		else {
			if (isAlwaysParenthesized()) {
				// for now this is never the case
				return "("+child.toString(false, getTag(), boundNames)+")"+getTagOperator();
			}
			else if ((isRightChild && rightParenthesesMap[getTag()-firstTag].get(parentTag)) || (!isRightChild && leftParenthesesMap[getTag()-firstTag].get(parentTag))) {
				return "("+child.toString(false, getTag(), boundNames)+getTagOperator()+")";
			}
			else {
				return child.toString(false, getTag(), boundNames)+getTagOperator();
			}
		}
		
	}

	protected String getTagOperator() {
		return tags[getTag()-firstTag];
	}
	
	// true if always needs parentheses
	protected boolean isAlwaysParenthesized() {
		return alwaysParenthesized[getTag()-firstTag];
	}

	@Override
	protected boolean equals(Formula other, boolean withAlphaConversion) {
		UnaryExpression otherExpr = (UnaryExpression) other;
		return hasSameType(other)
				&& child.equals(otherExpr.child, withAlphaConversion);
	}

	@Override
	public Expression flatten(FormulaFactory factory) {
		Expression normalizedChild = child.flatten(factory);
		if (getTag()==Formula.UNMINUS && normalizedChild.getTag() == Formula.INTLIT) {
			IntegerLiteral intLit = (IntegerLiteral) normalizedChild;
			return factory.makeIntegerLiteral(intLit.getValue().negate(), getSourceLocation());
		}
		if (child != normalizedChild) {
			return factory.makeUnaryExpression(getTag(), normalizedChild, getSourceLocation());
		}
		return this;
	}

	@Override
	protected void typeCheck(TypeCheckResult result, BoundIdentDecl[] quantifiedIdentifiers) {
		final SourceLocation loc = getSourceLocation();
		TypeVariable alpha, beta;
		Type resultType;
		
		child.typeCheck(result,quantifiedIdentifiers);
		
		switch (getTag()) {
		case Formula.UNMINUS:
			resultType = result.makeIntegerType();
			result.unify(child.getType(), resultType, getSourceLocation());
			break;
		case Formula.CONVERSE:
			alpha = result.newFreshVariable(null);
			beta = result.newFreshVariable(null);
			result.unify(child.getType(), result.makeRelationalType(alpha, beta), loc);
			resultType = result.makeRelationalType(beta, alpha);
			break;
		case Formula.KCARD:
			alpha = result.newFreshVariable(null);
			result.unify(child.getType(), result.makePowerSetType(alpha), loc);
			resultType = result.makeIntegerType();
			break;
		case Formula.POW:
		case Formula.POW1:
			alpha = result.newFreshVariable(null);
			resultType = result.makePowerSetType(alpha);
			result.unify(child.getType(), resultType, loc);
			resultType = result.makePowerSetType(resultType);
			break;
		case Formula.KUNION:
		case Formula.KINTER:
			alpha = result.newFreshVariable(null);
			resultType = result.makePowerSetType(alpha);
			result.unify(child.getType(), result.makePowerSetType(resultType), loc);
			break;
		case Formula.KDOM:
			alpha = result.newFreshVariable(null);
			beta = result.newFreshVariable(null);
			result.unify(child.getType(), result.makeRelationalType(alpha, beta), loc);
			resultType = result.makePowerSetType(alpha);
			break;
		case Formula.KRAN:
			alpha = result.newFreshVariable(null);
			beta = result.newFreshVariable(null);
			result.unify(child.getType(), result.makeRelationalType(alpha, beta), loc);
			resultType = result.makePowerSetType(beta);
			break;
		case Formula.KPRJ1:
			alpha = result.newFreshVariable(null);
			beta = result.newFreshVariable(null);
			result.unify(child.getType(), result.makeRelationalType(alpha, beta), loc);
			resultType = result.makeRelationalType(result.makeProductType(alpha, beta), alpha);
			break;
		case Formula.KPRJ2:
			alpha = result.newFreshVariable(null);
			beta = result.newFreshVariable(null);
			result.unify(child.getType(), result.makeRelationalType(alpha, beta), loc);
			resultType = result.makeRelationalType(result.makeProductType(alpha, beta), beta);
			break;
		case Formula.KID:
			alpha = result.newFreshVariable(null);
			result.unify(child.getType(), result.makePowerSetType(alpha), loc);
			resultType = result.makeRelationalType(alpha, alpha);
			break;
		case Formula.KMIN:
		case Formula.KMAX:
			resultType = result.makeIntegerType();
			result.unify(child.getType(), result.makePowerSetType(resultType), loc);
			break;
		default:
			assert false;
			resultType = null;
		}
		setType(resultType, result);
	}
	
	@Override
	protected boolean solveType(TypeUnifier unifier) {
		boolean success = child.solveType(unifier);
		return finalizeType(success, unifier);
	}

	@Override
	protected String getSyntaxTree(String[] boundNames, String tabs) {
		return tabs + this.getClass().getSimpleName() + " [" + getTagOperator()
				+ "]" + getTypeName() + "\n"
				+ child.getSyntaxTree(boundNames, tabs + "\t");
	}

	@Override
	protected void isLegible(LegibilityResult result, BoundIdentDecl[] quantifiedIdents) {
		child.isLegible(result, quantifiedIdents);
	}

	@Override
	protected String toStringFullyParenthesized(String[] boundNames) {
		if (isPrefix[getTag()-firstTag]) {
			return getTagOperator()+"("+child.toStringFullyParenthesized(boundNames)+")";
		}
		else {
			return "("+child.toStringFullyParenthesized(boundNames)+")"+getTagOperator();
		}
	}

	@Override
	protected void collectFreeIdentifiers(LinkedHashSet<FreeIdentifier> freeIdentSet) {
		child.collectFreeIdentifiers(freeIdentSet);
	}

	@Override
	protected void collectNamesAbove(Set<String> names, String[] boundNames, int offset) {
		child.collectNamesAbove(names, boundNames, offset);
	}
	
	@Override
	protected Expression bindTheseIdents(Map<String, Integer> binding, int offset, FormulaFactory factory) {
		Expression newChild = child.bindTheseIdents(binding, offset, factory);
		if (newChild == child) {
			return this;
		}
		return factory.makeUnaryExpression(getTag(), newChild, getSourceLocation());
	}

	@Override
	public boolean accept(IVisitor visitor) {
		boolean goOn = true;

		switch (getTag()) {
		case KCARD:    goOn = visitor.enterKCARD(this);    break;
		case POW:      goOn = visitor.enterPOW(this);      break;
		case POW1:     goOn = visitor.enterPOW1(this);     break;
		case KUNION:   goOn = visitor.enterKUNION(this);   break;
		case KINTER:   goOn = visitor.enterKINTER(this);   break;
		case KDOM:     goOn = visitor.enterKDOM(this);     break;
		case KRAN:     goOn = visitor.enterKRAN(this);     break;
		case KPRJ1:    goOn = visitor.enterKPRJ1(this);    break;
		case KPRJ2:    goOn = visitor.enterKPRJ2(this);    break;
		case KID:      goOn = visitor.enterKID(this);      break;
		case KMIN:     goOn = visitor.enterKMIN(this);     break;
		case KMAX:     goOn = visitor.enterKMAX(this);     break;
		case CONVERSE: goOn = visitor.enterCONVERSE(this); break;
		case UNMINUS:  goOn = visitor.enterUNMINUS(this);  break;
		default:       assert false;
		}

		if (goOn) goOn = child.accept(visitor);
		
		switch (getTag()) {
		case KCARD:    return visitor.exitKCARD(this);
		case POW:      return visitor.exitPOW(this);
		case POW1:     return visitor.exitPOW1(this);
		case KUNION:   return visitor.exitKUNION(this);
		case KINTER:   return visitor.exitKINTER(this);
		case KDOM:     return visitor.exitKDOM(this);
		case KRAN:     return visitor.exitKRAN(this);
		case KPRJ1:    return visitor.exitKPRJ1(this);
		case KPRJ2:    return visitor.exitKPRJ2(this);
		case KID:      return visitor.exitKID(this);
		case KMIN:     return visitor.exitKMIN(this);
		case KMAX:     return visitor.exitKMAX(this);
		case CONVERSE: return visitor.exitCONVERSE(this);
		case UNMINUS:  return visitor.exitUNMINUS(this);
		default:       return true;
		}
	}
	
	private Predicate getWDPredicateKCARD(FormulaFactory formulaFactory) {
		Predicate conj0 = child.getWDPredicateRaw(formulaFactory);
		Predicate conj1 = 
			formulaFactory.makeSimplePredicate(KFINITE, child, null);
		return getWDSimplifyC(formulaFactory, conj0, conj1);
	}

	private Predicate getWDPredicateKINTER(FormulaFactory formulaFactory) {
		Predicate conj0 = child.getWDPredicateRaw(formulaFactory);
		Expression emptyset = formulaFactory.makeEmptySet(child.getType(), null);
		Predicate conj1 = formulaFactory.makeRelationalPredicate(NOTEQUAL, child, emptyset, null);
		return getWDSimplifyC(formulaFactory, conj0, conj1);
	}

	@Override
	public Predicate getWDPredicateRaw(FormulaFactory formulaFactory) {
		switch (getTag()) {
		case KCARD:    return getWDPredicateKCARD(formulaFactory);
		case KMIN:
		case KMAX:
		case KINTER:   return getWDPredicateKINTER(formulaFactory);
		default:
			return child.getWDPredicateRaw(formulaFactory);
		}
		
	}

	@Override
	protected boolean isWellFormed(int noOfBoundVars) {
		return child.isWellFormed(noOfBoundVars);
	}

	@Override
	public UnaryExpression applySubstitution(Substitution subst, FormulaFactory ff) {
		Expression newChild = child.applySubstitution(subst, ff);
		if (newChild == child)
			return this;
		return ff.makeUnaryExpression(getTag(), newChild, getSourceLocation());
	}

	@Override
	public boolean isATypeExpression() {
		return getTag() == POW && child.isATypeExpression();
	}

	@Override
	public Type toType(FormulaFactory factory) throws InvalidExpressionException {
		if (getTag() != POW)
			throw new InvalidExpressionException();
		Type childAsType = child.toType(factory);
		return factory.makePowerSetType(childAsType);
	}

}
