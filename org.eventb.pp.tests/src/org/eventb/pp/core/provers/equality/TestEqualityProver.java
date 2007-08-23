package org.eventb.pp.core.provers.equality;

import static org.eventb.internal.pp.core.elements.terms.Util.cClause;
import static org.eventb.internal.pp.core.elements.terms.Util.cEqual;
import static org.eventb.internal.pp.core.elements.terms.Util.cNEqual;
import static org.eventb.internal.pp.core.elements.terms.Util.cProp;
import static org.eventb.internal.pp.core.elements.terms.Util.mSet;

import org.eventb.internal.pp.core.ProverResult;
import org.eventb.internal.pp.core.elements.Clause;
import org.eventb.internal.pp.core.elements.terms.AbstractPPTest;
import org.eventb.internal.pp.core.elements.terms.VariableContext;
import org.eventb.internal.pp.core.provers.equality.EqualityProver;

public class TestEqualityProver extends AbstractPPTest {

	private EqualityProver prover;
	
	@Override
	public void setUp() {
		prover = new EqualityProver(new VariableContext());
	}
	
	public void testProverInstantiation() {
		prover.addClauseAndDetectContradiction(cClause(cEqual(a, x), cProp(0)));
		prover.addClauseAndDetectContradiction(cClause(cNEqual(a, b)));
		
		ProverResult result = prover.next(false);
		assertEquals(result.getGeneratedClauses(), mSet(cClause(cProp(0))));
	}
	
	public void testProverInstantiation2() {
		Clause c1 = cClause(cEqual(a, x), cProp(0));
		Clause u1 = cClause(cEqual(a, b));
		prover.addClauseAndDetectContradiction(c1);
		prover.addClauseAndDetectContradiction(u1);
		
		ProverResult result = prover.next(false);
		assertEquals(result, ProverResult.EMPTY_RESULT);
	}
	
	public void testEmptyResult() {
		ProverResult result = prover.addClauseAndDetectContradiction(cClause(cProp(0)));
		assertTrue(result.isEmpty());
	}
	
	public void testContradiction() {
		prover.addClauseAndDetectContradiction(cClause(cEqual(a, b)));
		ProverResult result = prover.addClauseAndDetectContradiction(cClause(cNEqual(a, b)));
		
		assertFalse(result);
	}
	
	public void testSubsumption() {
		Clause c1 = cClause(cEqual(a,b),cProp(0));
		prover.addClauseAndDetectContradiction(c1);
		ProverResult result = prover.addClauseAndDetectContradiction(cClause(cEqual(a,b)));
		
		assertTrue(result);
		assertEquals(result.getSubsumedClauses(),mSet(c1));
	}
	
	public void testSubsumptionWithDifferentLevels() {
		Clause c1 = cClause(ONE,cEqual(a,b),cProp(0));
		prover.addClauseAndDetectContradiction(c1);
		ProverResult result = prover.addClauseAndDetectContradiction(cClause(cEqual(a,b)));
		
		assertTrue(result);
		assertEquals(result.getSubsumedClauses(),mSet(c1));
	}
	
	public void testNoSubsumption() {
		Clause c1 = cClause(cEqual(a,b),cProp(0));
		prover.addClauseAndDetectContradiction(c1);
		ProverResult result = prover.addClauseAndDetectContradiction(cClause(ONE,cEqual(a,b)));
		
		assertTrue(result);
		assertTrue(result.getSubsumedClauses().isEmpty());
	}
}
