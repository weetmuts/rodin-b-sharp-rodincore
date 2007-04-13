package org.eventb.pp.core.provers;

import static org.eventb.pp.Util.cClause;
import static org.eventb.pp.Util.cCons;
import static org.eventb.pp.Util.cELocVar;
import static org.eventb.pp.Util.cEqClause;
import static org.eventb.pp.Util.cEqual;
import static org.eventb.pp.Util.cNEqual;
import static org.eventb.pp.Util.cNotPred;
import static org.eventb.pp.Util.cNotProp;
import static org.eventb.pp.Util.cPred;
import static org.eventb.pp.Util.cProp;
import static org.eventb.pp.Util.cVar;
import static org.eventb.pp.Util.mList;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eventb.internal.pp.core.VariableContext;
import org.eventb.internal.pp.core.elements.IClause;
import org.eventb.internal.pp.core.provers.predicate.PredicateProver;
import org.eventb.pp.Util;

/**
 * TODO Comment
 *
 * @author François Terrier
 *
 */
public class TestPredicateProver extends TestCase {

	private class TestPair {
		List<IClause> unit, nonUnit;
		IClause[] result;
		
		TestPair(List<IClause> nonUnit, List<IClause> unit, IClause... result) {
			this.unit = unit;
			this.nonUnit = nonUnit;
			this.result = result;
		}
		
//		TestPair(String input, IClause output) {
//			LoaderResult result = Util.doPhaseOneAndTwo(input);		
//			assert result.getClauses().size() == 1;
//			assert result.getLiterals().size() == 0;
//			this.input = result.getClauses().iterator().next();
//			this.output = output;
//		}
		
	}
	
	
	TestPair[] tests = new TestPair[]{
			// normal case
			new TestPair(
					mList(cClause(cProp(0),cProp(1))),
					mList(cClause(cNotProp(0))),
					cClause(cProp(1))
			),
			// several match case
			new TestPair(
					mList(cClause(cProp(0),cProp(0))),
					mList(cClause(cNotProp(0))),
					cClause(cProp(0)),
					cClause(cProp(0))
			),
			// no match
			new TestPair(
					mList(cClause(cProp(0),cProp(1))),
					mList(cClause(cProp(2)))
			),
			// no match
			new TestPair(
					mList(cClause(cProp(0),cProp(1))),
					mList(cClause(cProp(0)))
			),
			//
			new TestPair(
					mList(cClause(cNotProp(0),cProp(0))),
					mList(cClause(cProp(0))),
					cClause(cProp(0))
			),
			new TestPair(
					mList(cClause(cProp(0),cProp(0),cProp(1))),
					mList(cClause(cNotProp(0))),
					cClause(cProp(0),cProp(1)),
					cClause(cProp(0),cProp(1))
			),
			
			new TestPair(
					mList(	cClause(cProp(0),cProp(1),cProp(2)),
							cClause(cNotProp(0),cProp(1),cProp(2)),
							cClause(cProp(0),cNotProp(1),cProp(2)),
							cClause(cProp(0),cProp(1),cNotProp(2)),
							cClause(cNotProp(0),cNotProp(1),cProp(2)),
							cClause(cNotProp(0),cProp(1),cNotProp(2)),
							cClause(cProp(0),cNotProp(1),cNotProp(2)),
							cClause(cNotProp(0),cNotProp(1),cNotProp(2))
					),
					mList(
							cClause(cProp(0)),
							cClause(cNotProp(0)),
							cClause(cProp(1)),
							cClause(cNotProp(1)),
							cClause(cProp(2)),
							cClause(cNotProp(2))
					),
					cClause(cProp(1),cProp(2)),
					cClause(cNotProp(1),cProp(2)),
					cClause(cProp(1),cNotProp(2)),
					cClause(cNotProp(1),cNotProp(2)),
					cClause(cProp(1),cProp(2)),
					cClause(cNotProp(1),cProp(2)),
					cClause(cProp(1),cNotProp(2)),
					cClause(cNotProp(1),cNotProp(2)),
					
					cClause(cProp(0),cProp(2)),
					cClause(cNotProp(0),cProp(2)),
					cClause(cProp(0),cNotProp(2)),
					cClause(cNotProp(0),cNotProp(2)),
					cClause(cProp(0),cProp(2)),
					cClause(cNotProp(0),cProp(2)),
					cClause(cProp(0),cNotProp(2)),
					cClause(cNotProp(0),cNotProp(2)),
					
					cClause(cProp(0),cProp(1)),
					cClause(cNotProp(0),cProp(1)),
					cClause(cProp(0),cNotProp(1)),
					cClause(cNotProp(0),cNotProp(1)),
					cClause(cProp(0),cProp(1)),
					cClause(cNotProp(0),cProp(1)),
					cClause(cProp(0),cNotProp(1)),
					cClause(cNotProp(0),cNotProp(1))
			),
//			
			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cNotProp(0)),cClause(cProp(0)))
			),
			
			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cProp(0)),cClause(cProp(0)))
			),
			
			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cProp(0)),cClause(cProp(0)))
			),
			
	};

	TestPair[] testEq = new TestPair[]{
			// normal case
			new TestPair(
					mList(cEqClause(cProp(0),cProp(1))),
					mList(cClause(cProp(0))),
					cClause(cProp(1))
			),
			new TestPair(
					mList(cEqClause(cProp(0),cProp(1))),
					mList(cClause(cNotProp(0))),
					cClause(cNotProp(1))
			),
			// several match case
			new TestPair(
					mList(cEqClause(cProp(0),cProp(0))),
					mList(cClause(cProp(0))),
					cClause(cProp(0)),
					cClause(cProp(0))
			),
			new TestPair(
					mList(cEqClause(cProp(0),cProp(0))),
					mList(cClause(cNotProp(0))),
					cClause(cNotProp(0)),
					cClause(cNotProp(0))
			),
			// no match
			new TestPair(
					mList(cEqClause(cProp(0),cProp(1))),
					mList(cClause(cProp(2)))
			),
			//
			new TestPair(
					mList(cEqClause(cNotProp(0),cProp(0))),
					mList(cClause(cProp(0))),
					cClause(cNotProp(0)),
					cClause(cNotProp(0))
			),
			new TestPair(
					mList(cEqClause(cProp(0),cProp(0))),
					mList(cClause(cProp(0))),
					cClause(cProp(0)),
					cClause(cProp(0))
			),
			new TestPair(
					mList(cClause(cProp(0),cProp(0),cProp(1))),
					mList(cClause(cNotProp(0))),
					cClause(cProp(0),cProp(1)),
					cClause(cProp(0),cProp(1))
			),
			
			new TestPair(
					mList(	cClause(cProp(0),cProp(1),cProp(2)),
							cClause(cNotProp(0),cProp(1),cProp(2)),
							cClause(cProp(0),cNotProp(1),cProp(2)),
							cClause(cProp(0),cProp(1),cNotProp(2)),
							cClause(cNotProp(0),cNotProp(1),cProp(2)),
							cClause(cNotProp(0),cProp(1),cNotProp(2)),
							cClause(cProp(0),cNotProp(1),cNotProp(2)),
							cClause(cNotProp(0),cNotProp(1),cNotProp(2))
					),
					mList(
							cClause(cProp(0)),
							cClause(cNotProp(0)),
							cClause(cProp(1)),
							cClause(cNotProp(1)),
							cClause(cProp(2)),
							cClause(cNotProp(2))
					),
					cClause(cProp(1),cProp(2)),
					cClause(cNotProp(1),cProp(2)),
					cClause(cProp(1),cNotProp(2)),
					cClause(cNotProp(1),cNotProp(2)),
					cClause(cProp(1),cProp(2)),
					cClause(cNotProp(1),cProp(2)),
					cClause(cProp(1),cNotProp(2)),
					cClause(cNotProp(1),cNotProp(2)),
					
					cClause(cProp(0),cProp(2)),
					cClause(cNotProp(0),cProp(2)),
					cClause(cProp(0),cNotProp(2)),
					cClause(cNotProp(0),cNotProp(2)),
					cClause(cProp(0),cProp(2)),
					cClause(cNotProp(0),cProp(2)),
					cClause(cProp(0),cNotProp(2)),
					cClause(cNotProp(0),cNotProp(2)),
					
					cClause(cProp(0),cProp(1)),
					cClause(cNotProp(0),cProp(1)),
					cClause(cProp(0),cNotProp(1)),
					cClause(cNotProp(0),cNotProp(1)),
					cClause(cProp(0),cProp(1)),
					cClause(cNotProp(0),cProp(1)),
					cClause(cProp(0),cNotProp(1)),
					cClause(cNotProp(0),cNotProp(1))
			),

			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cNotProp(0)),cClause(cProp(0)))
			),
			
			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cProp(0)),cClause(cProp(0)))
			),
			
			new TestPair(
					new ArrayList<IClause>(),
					mList(cClause(cProp(0)),cClause(cProp(0)))
			),
			
	};

	public void testDisj() {
		for (TestPair test : tests) {
			doTest(test);
		}
	}
	
	public void testEq() {
		for (TestPair test : testEq) {
			doTest(test);
		}
	}
	
	
	public void testHiddenInferrence() {
		doTest(new TestPair(mList(cClause(cPred(0,cELocVar(1)),cProp(1)),cClause(cPred(0,cCons("a")),cProp(1))),
				mList(cClause(cNotPred(0,cCons("a")))),
				cClause(cProp(1),cNEqual(cCons("a"), cCons("a")))));
	}
	
	public void doTest(TestPair test) {
			PredicateProver prover = new PredicateProver(new VariableContext());
			
			for (IClause clause : test.nonUnit) {
				prover.newClause(clause);
			}
			for (IClause clause : test.unit) {
				prover.newClause(clause);
			}
			
			int i=0;
			for (IClause clause : test.result) {
				assertEquals(clause, prover.next());
				i++;
			}
			assertNull("\nUnit: " + test.unit + "NonUnit: " + test.nonUnit, prover.next());
			assertEquals(test.result.length, i);
	}
}
