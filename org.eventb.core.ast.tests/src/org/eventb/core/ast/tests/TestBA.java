package org.eventb.core.ast.tests;

import static org.eventb.core.ast.tests.FastFactory.mList;
import static org.eventb.core.ast.tests.FastFactory.mTypeEnvironment;
import junit.framework.TestCase;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BooleanType;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ITypeCheckResult;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.IntegerType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.Type;

public class TestBA extends TestCase {

	public static FormulaFactory ff = FormulaFactory.getDefault(); 
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private static IntegerType INTEGER = ff.makeIntegerType();
	private static BooleanType BOOL = ff.makeBooleanType();
	
	private static Type POW(Type base) {
		return ff.makePowerSetType(base);
	}
	
	private static Type CPROD(Type left, Type right) {
		return ff.makeProductType(left, right);
	}
	
	private ITypeEnvironment defaultTEnv = mTypeEnvironment(
			mList(
					"x",
					"y",
					"A",
					"B",
					"f",
					"Y"
			),
			mList(
					INTEGER,
					INTEGER,
					POW(INTEGER),
					POW(INTEGER),
					POW(CPROD(INTEGER,INTEGER)),
					POW(BOOL)
			)
	);
	
	private class TestItem {
		String input;
		String expected;
		ITypeEnvironment tenv;
		TestItem(String input, String expected, ITypeEnvironment tenv) {
			this.input = input;
			this.expected = expected;
			this.tenv = tenv;
		}
		
		public void doTest() throws Exception {
			IParseResult resIn = ff.parseAssignment(input);
			assertTrue(resIn.isSuccess());
			Assignment inA = resIn.getParsedAssignment();
			ITypeCheckResult result = inA.typeCheck(tenv);
			assertTrue(input, result.isSuccess());
			
			ITypeEnvironment newEnv = tenv.clone();
			newEnv.addAll(result.getInferredEnvironment());
			
			Predicate inBA = inA.getBAPredicate(ff);
			ITypeCheckResult intResult = inBA.typeCheck(newEnv);
			assertTrue(input + "\n" + inBA.toString() + "\n" + inBA.getSyntaxTree() + "\n", intResult.isSuccess());
			
			IParseResult resExp = ff.parsePredicate(expected);
			assertTrue(input, resExp.isSuccess());
			Predicate exP = resExp.getParsedPredicate().flatten(ff);
			ITypeCheckResult newResult = exP.typeCheck(newEnv);
			assertTrue(newResult.isSuccess());
			
			assertEquals(input, exP, inBA);
		}
	}

	private TestItem[] testItems = new TestItem[] {
			new TestItem("x≔x+y", "x'=x+y", defaultTEnv),
			new TestItem("x:∈A", "x'∈A", defaultTEnv),
			new TestItem("x:|x'∈A", "x'∈A", defaultTEnv)
	};
	
	public void testBA() throws Exception {
		for (TestItem item : testItems)
			item.doTest();
	}

	
}