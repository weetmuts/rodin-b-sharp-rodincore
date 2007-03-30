package org.eventb.core.ast.tests;

import junit.framework.TestCase;

import org.eventb.core.ast.ASTProblem;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ProblemKind;
import org.eventb.core.ast.ProblemSeverities;
import org.eventb.core.ast.SourceLocation;


/**
 * Unit test of error messages.
 * 
 * @author franz
 */
public class TestErrors extends TestCase {
	private FormulaFactory formulaFactory;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		formulaFactory = FormulaFactory.getDefault();
	}
	

	private Object lexTestPairs[] = new Object[]{
			"\ueeee\u22a5",
			new ASTProblem(new SourceLocation(0,0), ProblemKind.LexerError, ProblemSeverities.Warning, "\ueeee"),
			"\u22a5\ueeee",
			new ASTProblem(new SourceLocation(1,1), ProblemKind.LexerError, ProblemSeverities.Warning, "\ueeee"),
			"finite(\u03bb x\u21a6(\ueeeey\u21a6s)\u00b7\u22a5\u2223z)",
			new ASTProblem(new SourceLocation(12,12), ProblemKind.LexerError, ProblemSeverities.Warning, "\ueeee"),
			
	};
	
	private Object parseTestPairs[] = new Object[]{
			"finite(\u03bb x\u21a6(y\u21a6s)\u00b7\u22a5\u2223z",
			new ASTProblem(new SourceLocation(20,20), ProblemKind.SyntaxError, ProblemSeverities.Error, "RPAR expected"),
			"\u03bb x\u21a6(y\u21a6s)\u00b7\u22a5\u2223z",
			new ASTProblem(new SourceLocation(0,1), ProblemKind.SyntaxError, ProblemSeverities.Error, "invalid SimpleExpr"),
			"finite(\u03bb x\u21a6y\u21a6s)\u00b7\u22a5\u2223z)",
			new ASTProblem(new SourceLocation(14,15), ProblemKind.SyntaxError, ProblemSeverities.Error, "QDOT expected"),
			"∀(x)·x∈ℤ",
			new ASTProblem(new SourceLocation(1,1), ProblemKind.UnexpectedLPARInDeclList, ProblemSeverities.Error),
			"∀(x,y)·x∈ℤ ∧ y∈ℤ",
			new ASTProblem(new SourceLocation(1,1), ProblemKind.UnexpectedLPARInDeclList, ProblemSeverities.Error),
// TODO check how it could be extended to quantified expressions
//			"finite(⋃(x)·(x⊆ℤ ∣ x))",
//			new ASTProblem(new SourceLocation(5,5), ProblemKind.UnexpectedLPARInDeclList, ProblemSeverities.Error),
//			"finite(⋃(x,y)·(x⊆ℤ ∧ y⊆ℤ ∣ x∩y))",
//			new ASTProblem(new SourceLocation(5,5), ProblemKind.UnexpectedLPARInDeclList, ProblemSeverities.Error),
	};
	
	
	/**
	 * Test of lexical errors
	 */
	public void testLexErrors() {
		for (int i = 0; i < lexTestPairs.length; i = i + 2) {
			IParseResult result = formulaFactory.parsePredicate((String) lexTestPairs[i]);
			// Lexer errors are only warnings, so parsing is a success.
			assertTrue(result.isSuccess());
			assertEquals(result.getProblems().size(), 1);
			assertEquals(result.getProblems().get(0), lexTestPairs[i + 1]);
			assertNotNull(result.getParsedPredicate());
		}
	}
	
	/**
	 * Test of syntactic errors
	 */
	public void testParseErrors() {
		for (int i = 0; i < parseTestPairs.length; i = i + 2) {
			final String input = (String) parseTestPairs[i];
			final ASTProblem problem = (ASTProblem) parseTestPairs[i + 1];
			final IParseResult result = formulaFactory.parsePredicate(input);
			assertFalse(result.isSuccess());
			assertEquals(1, result.getProblems().size());
			assertNull(result.getParsedPredicate());
			assertEquals(problem, result.getProblems().get(0));
		}
	}
	
	/* TODO: Add well-formedness and type-check errors. */
}
