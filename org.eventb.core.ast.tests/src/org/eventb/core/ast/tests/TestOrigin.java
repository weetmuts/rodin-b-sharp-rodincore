/*******************************************************************************
 * Copyright (c) 2008, 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.ast.tests;

import static org.junit.Assert.assertSame;

import org.eventb.core.ast.Formula;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.SourceLocation;
import org.junit.Test;

public class TestOrigin extends AbstractTests {

	private static abstract class TestAllOrigins {
		private String image;

		TestAllOrigins(String image) {
			this.image = image;
		}

		final void verify() {
			final Formula<?> formula = parse(image);
			final SourceLocation loc = formula.getSourceLocation();
			assertSame("Unexpected parser result", this, loc.getOrigin());
			formula.accept(new SourceLocationOriginChecker(this));
		}

		abstract Formula<?> parse(String stringToParse);
	}

	private static class ExprTestOrigin extends TestAllOrigins {

		ExprTestOrigin(String image) {
			super(image);
		}

		@Override
		Formula<?> parse(String image) {
			final IParseResult result = ff.parseExpression(image, this);
			assertSuccess("Parse failed for " + image, result);
			return result.getParsedExpression();
		}
	}
	
	private static class ExprPatternTestOrigin extends ExprTestOrigin {

		ExprPatternTestOrigin(String image) {
			super(image);
		}
		
		@Override
		Formula<?> parse(String image) {
			final IParseResult result = ff.parseExpressionPattern(image, this);
			assertSuccess("Parse failed for " + image, result);
			return result.getParsedExpression();
		}
	}
	
	private static class PredTestOrigin extends TestAllOrigins {

		PredTestOrigin(String image) {
			super(image);
		}

		@Override
		Formula<?> parse(String image) {
			final IParseResult result = ff.parsePredicate(image, this);
			assertSuccess("Parse failed for " + image, result);
			return result.getParsedPredicate();
		}
	}

	private static class PredPatternTestOrigin extends TestAllOrigins {

		PredPatternTestOrigin(String image) {
			super(image);
		}

		@Override
		Formula<?> parse(String image) {
			final IParseResult result = ff.parsePredicatePattern(image, this);
			assertSuccess("Parse failed for " + image, result);
			return result.getParsedPredicate();
		}
	}

	private static class AssignmentTestOrigin extends TestAllOrigins {

		AssignmentTestOrigin(String image) {
			super(image);
		}

		@Override
		Formula<?> parse(String image) {
			final IParseResult result = ff.parseAssignment(image, this);
			assertSuccess("Parse failed for " + image, result);
			return result.getParsedAssignment();
		}
	}

	private static void verifyPredicate(String image) {
		new PredTestOrigin(image).verify();
	}

	private static void verifyPredicatePattern(String image) {
		new PredPatternTestOrigin(image).verify();
	}

	private static void verifyExpression(String image) {
		new ExprTestOrigin(image).verify();
	}

	private static void verifyExpressionPattern(String image) {
		new ExprPatternTestOrigin(image).verify();
	}

	private static void verifyAssignment(String image) {
		new AssignmentTestOrigin(image).verify();
	}

	/**
	 * Main test routine.
	 */
	@Test 
	public void testParserOrigin() {
		verifyPredicate("\u22a5");
		verifyPredicate("\u00ac\u00ac\u22a5");
		verifyPredicate("∀x·x ∈ S ∧ (∀x·x ∈ T)");
		
		verifyPredicatePattern("$P");
		verifyExpressionPattern("bool($P)");

		verifyExpression("bool(\u22a5)");
		verifyExpression("−x+y+z");
		verifyExpression("(f(x))∼[y]");

		verifyAssignment("x ≔ y");
		verifyAssignment("x :∈ S");
		verifyAssignment("x,y,z :\u2223 x' = y ∧ y' = z ∧ z' = x");
	}

}
