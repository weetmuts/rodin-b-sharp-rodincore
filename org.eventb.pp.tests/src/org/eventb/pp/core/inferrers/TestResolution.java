package org.eventb.pp.core.inferrers;

import static org.eventb.pp.Util.cClause;
import static org.eventb.pp.Util.cEqClause;
import static org.eventb.pp.Util.cNEqual;
import static org.eventb.pp.Util.cNotPred;
import static org.eventb.pp.Util.cNotProp;
import static org.eventb.pp.Util.cPred;
import static org.eventb.pp.Util.cProp;
import static org.eventb.pp.Util.mList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.internal.pp.core.IVariableContext;
import org.eventb.internal.pp.core.elements.Clause;
import org.eventb.internal.pp.core.elements.PredicateDescriptor;
import org.eventb.internal.pp.core.elements.Sort;
import org.eventb.internal.pp.core.elements.terms.LocalVariable;
import org.eventb.internal.pp.core.elements.terms.Variable;
import org.eventb.internal.pp.core.inferrers.ResolutionInferrer;
import org.eventb.internal.pp.core.provers.predicate.IMatchIterator;
import org.eventb.internal.pp.core.provers.predicate.ResolutionResolver;
import org.eventb.internal.pp.loader.clause.VariableContext;
import org.eventb.pp.Util;

/**
 * This class tests the one point rule. There are several tests :
 * <ul>
 * 	<li>one-point rule on non-arithmetic</li>
 * 	<li>one-point rule on arithmetic with single expressions</li>
 * 	<li>one-point rule on arithmetic with complex expressions</li>
 * </ul>
 *
 * @author François Terrier
 *
 */
public class TestResolution extends AbstractInferrerTests {

	private static Sort A = new Sort(FormulaFactory.getDefault().makeIntegerType());

	private static Variable var0 = Util.cVar(A);
	private static Variable var1 = Util.cVar(A);

	private static Variable var00 = Util.cVar(A);
	private static Variable var11 = Util.cVar(A);

	private static LocalVariable fvar0 = Util.cFLocVar(0,A);
//	private static LocalVariable fvar1 = Util.cFLocVar(1,A);
	private static LocalVariable fvar2 = Util.cFLocVar(2,A);
	private static LocalVariable evar0 = Util.cELocVar(0,A);
	private static LocalVariable evar1 = Util.cELocVar(1,A);
	private static LocalVariable evar2 = Util.cELocVar(2,A);


	private Clause[] noClause() {
		return new Clause[0];
	}

	public void testSimple() {
		// normal case
		doTest(
				cClause(cProp(0),cProp(1)),
				cClause(cNotProp(0)),
				cClause(cProp(1))
		);
		// several match case
		doTest(
				cClause(cProp(0),cProp(0)),
				cClause(cNotProp(0)),
				cClause(cProp(0)),
				cClause(cProp(0))
		);
		// no match
		doTest(
				cClause(cProp(0),cProp(1)),
				cClause(cProp(2))
		);
		// no match
		doTest(
				cClause(cProp(0),cProp(1)),
				cClause(cProp(0))
		);
		//
		doTest(
				cClause(cNotProp(0),cProp(0)),
				cClause(cProp(0)),
				cClause(cProp(0))
		);
		doTest(
				cClause(cProp(0),cProp(0),cProp(1)),
				cClause(cNotProp(0)),
				cClause(cProp(0),cProp(1)),
				cClause(cProp(0),cProp(1))
		);

		// EQUIVALENCE
		doTest(
				cEqClause(cProp(0),cProp(1)),
				cClause(cNotProp(0)),
				cClause(cNotProp(1))
		);
		doTest(
				cEqClause(cProp(0),cProp(1)),
				cClause(cProp(0)),
				cClause(cProp(1))
		);
		// several match case
		doTest(
				cEqClause(cProp(0),cProp(0)),
				cClause(cNotProp(0)),
				cClause(cNotProp(0)),
				cClause(cNotProp(0))
		);
		doTest(
				cEqClause(cProp(0),cNotProp(0)),
				cClause(cNotProp(0)),
				cClause(cProp(0)),
				cClause(cProp(0))
		);
	}

	public void testRule1() {
		// PREDICATE LOGIC
		doTest(
				cClause(cPred(0,var0),cPred(1,var0)),
				cClause(cNotPred(0,var0)),
				cClause(mList(cPred(1,var0)),cNEqual(var0,var0))
		);
		doTest(
				cClause(cPred(0,a),cPred(1,a)),
				cClause(cNotPred(0,b)),
				cClause(mList(cPred(1,a)),cNEqual(a, b))
		);
	}

	public void testRule2NewWithPredicatePreparation() {
		doTest(
				cClause(cPred(0,evar1),cProp(1)),
				cClause(cNotPred(0,var0)),
				cClause(mList(cProp(1)),cNEqual(evar1, evar1))
		);
	}

	public void testRule2() {
		doTest(
				cClause(cPred(0,evar1),cProp(1)),
				cClause(cNotPred(0,var0)),
				cClause(mList(cProp(1)),cNEqual(evar1, evar1))
		);
		doTest(
				cClause(cPred(0,evar0,var0),cPred(1,var0)),
				cClause(cNotPred(0,var1,var1)),
				cClause(mList(cPred(1,var0)),cNEqual(evar0, evar0), cNEqual(evar0, var0))
		);
		doTest(
				cClause(cPred(0,evar0,evar1),cPred(1,var0)),
				cClause(cNotPred(0,var1,var0)),
				cClause(mList(cPred(1,var0)),cNEqual(evar0, evar0), cNEqual(evar1, evar1))
		);
		doTest(
				cClause(cPred(0,evar0,evar1),cPred(1,var0)),
				cClause(cNotPred(0,var1,var1)),
				cClause(mList(cPred(1,var0)),cNEqual(evar1, evar1), cNEqual(evar0, evar1))
		);
		
	}

	public void testRule3() {
		doTest(
				cClause(cPred(0,var0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar1, var00))
		);
	}

	public void testRule4and5() {
		// PREDICATE LOGIC WITH EQUIVALENCE
		doTest(
				cEqClause(cPred(0,var0),cPred(1,var1),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cEqClause(mList(cPred(1,var1),cPred(1,var0)),cNEqual(var0, var0))
		);
		doTest(
				cEqClause(cPred(0,a),cPred(0,b),cNotPred(0,c)),
				cClause(cPred(0,b)),
				cEqClause(mList(cPred(0,b),cNotPred(0,c)),cNEqual(a, b)),
				cEqClause(mList(cPred(0,a),cNotPred(0,c)),cNEqual(b, b)),
				cEqClause(mList(cNotPred(0,a),cPred(0,b)),cNEqual(c, b))
		);
	}

	public void testRule4and5WithTransformation() {
		// Tests transformation eqclause->disjclause
		doTest(
				cEqClause(cPred(0,a),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cPred(1,var0)),cNEqual(a,a))
		);
		doTest(
				cEqClause(cNotPred(0,a),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cNotPred(1,var0)),cNEqual(a,a))
		);
		doTest(
				cEqClause(cPred(0,a),cPred(1,evar0)),
				cClause(cPred(0,var0)),
				cClause(mList(cPred(1,evar0)),cNEqual(a,a))
		);
		doTest(
				cEqClause(cPred(0,a),cPred(1,fvar2)),
				cClause(cPred(0,var0)),
				cClause(mList(cPred(1,var11)),cNEqual(a,a))
		);
		doTest(
				cEqClause(cPred(0,a),cPred(1,evar2)),
				cClause(cNotPred(0,var0)),
				cClause(mList(cNotPred(1,var11)),cNEqual(a, a))
		);
		doTest(
				cEqClause(cPred(0,a),cPred(1,fvar2)),
				cClause(cNotPred(0,var0)),
				cClause(mList(cNotPred(1,evar2)),cNEqual(a, a))
		);
	}

	public void testRule6and7() {
		// 4 TESTS with local variables + transformation
		doTest(
				cEqClause(cNotPred(0,evar2),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cNotPred(1,var0)),cNEqual(evar2,evar2))
		);
		doTest(
				cEqClause(cPred(0,fvar2),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cPred(1,var0)),cNEqual(evar2,evar2))
		);
		doTest(
				cEqClause(cNotPred(0,evar2),cPred(1,var1),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cEqClause(mList(cNotPred(1,var1),cPred(1,var0)),cNEqual(evar2,evar2))
		);
		doTest(
				cEqClause(cPred(0,fvar2),cPred(1,var1),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cEqClause(mList(cPred(1,var1),cPred(1,var0)),cNEqual(evar2,evar2))
		);
		// 
		
//		doTest(
//				cEqClause(cNotPred(0,cPlus(evar2,evar2)),cPred(1,var1),cPred(1,var0)),
//				cClause(cPred(0,var0)),
//				cEqClause(mList(cNotPred(1,var1),cPred(1,var0)),cNEqual(cPlus(evar2,evar2),cPlus(evar2,evar2)))
//		);
//		doTest(
//				cEqClause(cPred(0,cPlus(fvar2,fvar2)),cPred(1,var1),cPred(1,var0)),
//				cClause(cPred(0,var0)),
//				cEqClause(mList(cPred(1,var1),cPred(1,var0)),cNEqual(cPlus(evar2,evar2),cPlus(evar2,evar2)))
//		);
	}

	public void testRule8And9() {
		doTest(
				cEqClause(cPred(0,evar2),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cPred(1,var0)),cNEqual(var00,var00))
		);
		doTest(
				cEqClause(cNotPred(0,fvar2),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cClause(mList(cNotPred(1,var0)),cNEqual(var00,var00))
		);
		// same tests with constants
		doTest(
				cEqClause(cPred(0,evar2),cPred(1,var0)),
				cClause(cPred(0,a)),
				cClause(mList(cPred(1,var0)),cNEqual(a,var11))
		);
		doTest(
				cEqClause(cNotPred(0,fvar2),cPred(1,var0)),
				cClause(cPred(0,a)),
				cClause(mList(cNotPred(1,var0)),cNEqual(a,var11))
		);
		doTest(
				cEqClause(cNotPred(0,evar2),cPred(1,var0)),
				cClause(cNotPred(0,a)),
				cClause(mList(cPred(1,var0)),cNEqual(a,var11))
		);
		doTest(
				cEqClause(cPred(0,fvar2),cPred(1,var0)),
				cClause(cNotPred(0,a)),
				cClause(mList(cNotPred(1,var0)),cNEqual(a,var11))
		);
		doTest(
				cEqClause(cPred(0,evar2),cPred(1,var1),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cEqClause(mList(cPred(1,var1),cPred(1,var0)),cNEqual(var00,var00))
		);
		doTest(
				cEqClause(cNotPred(0,fvar2),cPred(1,var1),cPred(1,var0)),
				cClause(cPred(0,var0)),
				cEqClause(mList(cNotPred(1,var1),cPred(1,var0)),cNEqual(var00,var00))
		);

//		doTest(
//				cEqClause(cPred(0,cPlus(evar2,evar2)),cPred(1,var1),cPred(1,var0)),
//				cClause(cPred(0,var0)),
//				cEqClause(mList(cPred(1,var1),cPred(1,var0)),cNEqual(var11,cPlus(var00,var00)))
//		);
//		doTest(
//				cEqClause(cNotPred(0,cPlus(fvar2,fvar2)),cPred(1,var1),cPred(1,var0)),
//				cClause(cPred(0,var0)),
//				cEqClause(mList(cNotPred(1,var1),cPred(1,var0)),cNEqual(var11,cPlus(var00,var00)))
//		);
	}

	public void testOtherRule() {
		// TODO check this
		doTest(
				cEqClause(cPred(0,var0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cNotProp(1)),cNEqual(evar1,var0))
		);
		doTest(
				cEqClause(cNotPred(0,var0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar1,var0))
		);
	}
	
	// constants + pseudo constant
//	doTest(
//	cClause(cPred(0,evar1),cProp(1)),
//	cClause(cNotPred(0,a)),
//	cClause(mList(cProp(1)),cNEqual(evar1, a))
//	);
//	doTest(
//	cClause(cNotPred(0,evar1),cProp(1)),
//	cClause(cPred(0,a)),
//	cClause(mList(cProp(1)),cNEqual(evar1, a))
//	);

	public void testRuleDoNotApply() {
		// disjunctive clauses
		doTest(
				cClause(cPred(0,evar1),cProp(1)),
				cClause(cNotPred(0,a)),
				cClause(mList(cProp(1)),cNEqual(evar1, a))
		);
		doTest(
				cClause(cPred(0,a),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar1, a))
		);
		doTest(
				cClause(cPred(0,evar0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar1, evar0))
		);

		// equivalence clauses
		doTest(
				cEqClause(cPred(0,evar0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cNotProp(1)),cNEqual(evar0, evar1))
		);
		doTest(
				cEqClause(cNotPred(0,evar0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar0, var0))
		);
		doTest(
				cEqClause(cPred(0,fvar0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cNotProp(1)),cNEqual(evar0, var0))
		);
		doTest(
				cEqClause(cNotPred(0,fvar0),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar0, evar1))
		);
		
		doTest(
				cEqClause(cPred(0,a),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cNotProp(1)),cNEqual(evar1, a))
		);
		doTest(
				cEqClause(cNotPred(0,a),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(evar1, a))
		);
		doTest(
				cEqClause(cPred(0,a),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cNotProp(1)),cNEqual(evar1, a))
		);
		doTest(
				cEqClause(cNotPred(0,a),cProp(1)),
				cClause(cNotPred(0,evar1)),
				cClause(mList(cProp(1)),cNEqual(a, evar1))
		);

		// TODO variables mixed with local quantifiers
	}

	public void testInitialization() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		Clause clause = cClause(cPred(0));
		try {
			clause.infer(inferrer);
			fail();
		}
		catch (IllegalStateException e) {
			//nothing
		}
	}
	
	private static class MyMatcher implements IMatchIterator {
		private List<Clause> list = new ArrayList<Clause>();
		
		MyMatcher(Clause clause) {
			this.list.add(clause);
		}
		
		public Iterator<Clause> iterator(PredicateDescriptor predicate) {
			return list.iterator();
		}
		
	}

	public void doTest(Clause nonUnit, Clause unit, Clause... result) {
		IVariableContext context = new VariableContext();
		ResolutionInferrer inferrer = new ResolutionInferrer(context);
		ResolutionResolver resolution = new ResolutionResolver(inferrer, new MyMatcher(nonUnit));
		resolution.initialize(unit);
//		cleanVariables();

		for (Clause clause : result) {
			Clause inferredClause = resolution.next().getClause();
			assertEquals(clause, inferredClause);
			disjointVariables(inferredClause, unit);
			disjointVariables(inferredClause, nonUnit);
		}
		assertNull("\nUnit: " + unit + "NonUnit: " + nonUnit, resolution.next());
	}
	
	public void testSubsumption() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cProp(0)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotProp(0), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().contains(cClause(cNotProp(0), cProp(1))));
	}

	public void testNoSubsumptionWithConstants() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cPred(0,a)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotPred(0,b), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().isEmpty());
	}
	
	public void testSubsumptionWithConstants() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cPred(0,a)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotPred(0,a), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().contains(cClause(cNotPred(0,a), cProp(1))));
	}
	
	public void testNoSubsumptionWithVariables() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cPred(0,a)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotPred(0,x), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().isEmpty());
	}
	
	public void testSubsumptionWithVariables() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cPred(0,x)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotPred(0,a), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().contains(cClause(cNotPred(0,a), cProp(1))));
	}
	
	public void testSubsumptionWithVariables2() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(cPred(0,x)));
		inferrer.setPosition(0);
		Clause clause = cClause(cNotPred(0,x), cPred(1,x,y));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().contains( cClause(cNotPred(0,x), cPred(1,x,y))));
	}
	
	
	public void testSubsumptionWithLevels() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(BASE,cProp(0)));
		inferrer.setPosition(0);
		Clause clause = cClause(ONE,cNotProp(0), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().contains(cClause(ONE,cNotProp(0), cProp(1))));
	}

	public void testNoSubsumptionWithLevels() {
		ResolutionInferrer inferrer = new ResolutionInferrer(new VariableContext());
		inferrer.setUnitClause(cClause(ONE,cProp(0)));
		inferrer.setPosition(0);
		Clause clause = cClause(BASE,cNotProp(0), cProp(1));
		clause.infer(inferrer);
		assertTrue(inferrer.getResult().getSubsumedClauses().isEmpty());
	}
	
}
