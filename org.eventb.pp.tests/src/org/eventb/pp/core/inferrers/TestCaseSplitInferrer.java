package org.eventb.pp.core.inferrers;

import static org.eventb.internal.pp.core.elements.terms.Util.cClause;
import static org.eventb.internal.pp.core.elements.terms.Util.cEqClause;
import static org.eventb.internal.pp.core.elements.terms.Util.cEqual;
import static org.eventb.internal.pp.core.elements.terms.Util.cNotPred;
import static org.eventb.internal.pp.core.elements.terms.Util.cNotProp;
import static org.eventb.internal.pp.core.elements.terms.Util.cPred;
import static org.eventb.internal.pp.core.elements.terms.Util.cProp;
import static org.eventb.internal.pp.core.elements.terms.Util.mSet;

import java.util.Set;

import org.eventb.internal.pp.core.elements.Clause;
import org.eventb.internal.pp.core.elements.terms.AbstractPPTest;
import org.eventb.internal.pp.core.elements.terms.VariableContext;
import org.eventb.internal.pp.core.inferrers.CaseSplitInferrer;

/**
 * This class tests the case split inferrer. Case split inferrer behaves
 * correctly if a clause is split in two clauses that contain two disjoint
 * sets of variables. 
 * <p>
 * For now, the casesplit inferrer only splits on constant only clauses.
 *
 * @author François Terrier
 *
 */
public class TestCaseSplitInferrer extends AbstractPPTest {

	public void testCaseSplitInferrer () {
			doTest(
					cClause(BASE, cProp(0),cProp(1)),
					mSet(cClause(ONE, cProp(0))),
					mSet(cClause(TWO,cProp(1)))
			);
			doTest(
					cEqClause(BASE, cProp(0),cProp(1)),
					mSet(cClause(ONE, cProp(0)),cClause(ONE,cProp(1))),
					mSet(cClause(TWO, cNotProp(0)),cClause(TWO, cNotProp(1)))
			);
			doTest(
					cClause(BASE, cEqual(a,b),cPred(0,a)),
					mSet(cClause(ONE, cPred(0,a))),
					mSet(cClause(TWO, cEqual(a,b)))
			);
			doTest(
					cClause(BASE, cEqual(a,b),cEqual(b,c)),
					mSet(cClause(ONE, cEqual(a,b))),
					mSet(cClause(TWO,cEqual(b,c)))
			);
			doTest(
					cEqClause(BASE, cPred(0,fvar0), cPred(0,a)),
					mSet(cClause(ONE, cPred(0,x)), cClause(ONE, cPred(0,a))),
					mSet(cClause(TWO, cNotPred(0,evar0)), cClause(cNotPred(0,a)))
			);
			doTest(
					cClause(BASE, cPred(0,evar0), cPred(0,a)),
					mSet(cClause(ONE, cPred(0,evar0))),
					mSet(cClause(TWO, cPred(0,a)))
			);
			doTest(
					cEqClause(BASE, cPred(0,evar0), cPred(0,a)),
					mSet(cClause(ONE, cPred(0,evar0)),cClause(ONE, cPred(0,a))),
					mSet(cClause(TWO, cNotPred(0,x)),cClause(TWO, cNotPred(0,a)))
			);
			doTest(
					cEqClause(BASE, cProp(0), cProp(1), cProp(2)),
					mSet(cClause(ONE, cProp(0)), cEqClause(cProp(1),cProp(2))),
					mSet(cClause(TWO, cNotProp(0)),cEqClause(cNotProp(1),cProp(2)))
			);
			
	}

	public void doTest(Clause original, Set<Clause> left, Set<Clause> right) {
		CaseSplitInferrer inferrer = new CaseSplitInferrer(new VariableContext());

		inferrer.setLevel(original.getLevel());
		original.infer(inferrer);
		assertTrue("Expected: "+left+", was: "+inferrer.getLeftCase()+", from :"+original,inferrer.getLeftCase().equals(left));
		assertTrue("Expected: "+right+", was: "+inferrer.getRightCase()+", from :"+original,inferrer.getRightCase().equals(right));
	}
	
	public void testIllegal() {
		CaseSplitInferrer inferrer = new CaseSplitInferrer(new VariableContext());

		Clause clause = cClause(cProp(0),cProp(1));
		try {
			clause.infer(inferrer);
			fail();
		}
		catch (IllegalStateException e) {
			// normal case
		}
	}	
	
	public void testCanInfer() {
		CaseSplitInferrer inferrer = new CaseSplitInferrer(new VariableContext());
		
		Clause[] canInfer = new Clause[]{
				cClause(cProp(0),cProp(1)),
				cClause(cPred(0,a),cPred(1,b)),
				cClause(cPred(0,evar0),cPred(1,evar1)),
				cClause(cEqual(a,b),cPred(0,a)),
				cClause(cPred(0,evar0),cPred(1,var1)),
				cEqClause(cProp(0),cProp(1)),
				cEqClause(cPred(0,a),cPred(1,b)),
				cEqClause(cPred(0,evar0),cPred(1,evar1)),
				cEqClause(cEqual(a,b),cPred(0,a)),
				cEqClause(cPred(0,evar0),cPred(1,var1)),
		};

		for (Clause clause : canInfer) {
			assertTrue(inferrer.canInfer(clause));
		}
	}
	
	public void testCannotInfer() {
		CaseSplitInferrer inferrer = new CaseSplitInferrer(new VariableContext());
		
		Clause[] cannotInfer = new Clause[]{
				cClause(cProp(0)),
				cClause(cPred(0,x),cPred(0,x)),
				cClause(cPred(0,x),cPred(1,x,y)),
				cClause(cEqual(x,b),cPred(0,x)),
				cEqClause(cPred(0,x),cPred(0,x)),
				cEqClause(cPred(0,x),cPred(1,x,y)),
		};

		for (Clause clause : cannotInfer) {
			assertFalse(inferrer.canInfer(clause));
		}
	}
	
}
