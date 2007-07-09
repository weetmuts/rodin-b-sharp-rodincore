package org.eventb.internal.pp.core.elements;

import java.util.ArrayList;

import org.eventb.internal.pp.core.inferrers.IInferrer;
import org.eventb.internal.pp.core.simplifiers.ISimplifier;
import org.eventb.internal.pp.core.tracing.IOrigin;

public class FalseClause extends Clause {

	private static final int BASE_HASHCODE = 11;
	
	public FalseClause(IOrigin origin) {
		super(origin, new ArrayList<PredicateLiteral>(), new ArrayList<EqualityLiteral>(), new ArrayList<ArithmeticLiteral>(), BASE_HASHCODE);
	}

	@Override
	protected void computeBitSets() {
		// nothing
	}

	@Override
	public void infer(IInferrer inferrer) {
		// nothing
	}

	@Override
	public Clause simplify(ISimplifier simplifier) {
		return this;
	}

	@Override
	public boolean isFalse() {
		return true;
	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public String toString() {
		return "FALSE";
	}

	@Override
	public boolean isEquivalence() {
		return false;
	}

	@Override
	public boolean matches(PredicateDescriptor predicate) {
		return false;
	}

	@Override
	public boolean matchesAtPosition(PredicateDescriptor predicate, int position) {
		return false;
	}

}
