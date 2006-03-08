package org.eventb.core.ast.tests;

import static org.eventb.core.ast.Formula.EQUAL;
import static org.eventb.core.ast.Formula.LAND;
import static org.eventb.core.ast.tests.FastFactory.mAssociativeExpression;
import static org.eventb.core.ast.tests.FastFactory.mAssociativePredicate;
import static org.eventb.core.ast.tests.FastFactory.mAtomicExpression;
import static org.eventb.core.ast.tests.FastFactory.mBecomesEqualTo;
import static org.eventb.core.ast.tests.FastFactory.mBecomesMemberOf;
import static org.eventb.core.ast.tests.FastFactory.mBecomesSuchThat;
import static org.eventb.core.ast.tests.FastFactory.mBinaryExpression;
import static org.eventb.core.ast.tests.FastFactory.mBinaryPredicate;
import static org.eventb.core.ast.tests.FastFactory.mBoolExpression;
import static org.eventb.core.ast.tests.FastFactory.mBoundIdentDecl;
import static org.eventb.core.ast.tests.FastFactory.mBoundIdentifier;
import static org.eventb.core.ast.tests.FastFactory.mIntegerLiteral;
import static org.eventb.core.ast.tests.FastFactory.mList;
import static org.eventb.core.ast.tests.FastFactory.mLiteralPredicate;
import static org.eventb.core.ast.tests.FastFactory.mQuantifiedExpression;
import static org.eventb.core.ast.tests.FastFactory.mQuantifiedPredicate;
import static org.eventb.core.ast.tests.FastFactory.mRelationalPredicate;
import static org.eventb.core.ast.tests.FastFactory.mSetExtension;
import static org.eventb.core.ast.tests.FastFactory.mTypeEnvironment;
import static org.eventb.core.ast.tests.FastFactory.mUnaryExpression;
import static org.eventb.core.ast.tests.FastFactory.mUnaryPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.SourceLocation;
import org.eventb.core.ast.Type;


/**
 * Unit test for the following methods:
 * <ul>
 * <li><code>org.eventb.core.ast.Formula.getFreeIdentifiers()</code></li>
 * <li><code>org.eventb.core.ast.Formula.bindAllFreeIdents()</code></li>
 * <li><code>org.eventb.core.ast.Formula.bindTheseIdents()</code></li>
 * </ul> 
 * 
 * @author Laurent Voisin
 */
public class TestFreeIdents extends TestCase {
	
	private static FormulaFactory ff = FormulaFactory.getDefault();

	private static final FreeIdentifier[] NO_FREE_IDENT = new FreeIdentifier[0];
	
	private static class TestItem {
		Formula<? extends Formula> formula;
		FreeIdentifier[] freeIdents;
		Formula<? extends Formula> boundFormula;
		
		TestItem(Formula<? extends Formula> formula,
				FreeIdentifier[] freeIdents,
				Formula<? extends Formula> boundFormula) {
			this.formula = formula;
			this.freeIdents = freeIdents;
			this.boundFormula = boundFormula;
		}
	}

	// Ident names used here.
	private static final String[] names = new String[] { "x", "y", "z", };
	private static final int x = 0;
	private static final int y = 1;
	private static final int z = 2;

	// FreeIdentifiers to use, with different source locations
	private FreeIdentifier[][] ids = new FreeIdentifier[names.length][10];
	{
		for (int i = 0; i < names.length; i++) {
			for (int j = 0; j < ids[i].length; j++) {
				ids[i][j] = ff.makeFreeIdentifier(names[i], new SourceLocation(j, j+1));
			}
		}
	}
	
	private BoundIdentDecl[][] bds = new BoundIdentDecl[names.length][10];
	{
		for (int i = 0; i < names.length; i++) {
			for (int j = 0; j < bds[i].length; j++) {
				bds[i][j] = ff.makeBoundIdentDecl(names[i], new SourceLocation(j, j+1));
			}
		}
	}
	
	// BoundIdentifiers to use
	private BoundIdentifier[][] bs = new BoundIdentifier[5][10];
	{
		for (int i = 0; i < bs.length; i++) {
			for (int j = 0; j < bs[i].length; j++) {
				bs[i][j] = ff.makeBoundIdentifier(i, new SourceLocation(j, j+1));
			}
		}
	}
	
	private TestItem[] testItemsBindAll = new TestItem[] {
			// Basic test "x"
			new TestItem(
					ids[x][0],
					mList(ids[x][0]),
					bs[0][0]
			),
			// Basic test "x + y"
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1]),
					mList(ids[x][0], ids[y][1]),
					mAssociativeExpression(bs[1][0], bs[0][1])
			),
			// AssociativeExpression
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1], ids[z][2]),
					mList(ids[x][0], ids[y][1], ids[z][2]),
					mAssociativeExpression(bs[2][0], bs[1][1], bs[0][2])
			),
			// AssociativeExpression with same ident twice
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1], ids[x][2]),
					mList(ids[x][0], ids[y][1]),
					mAssociativeExpression(bs[1][0], bs[0][1], bs[1][2])
			),
			// AssociativePredicate
			new TestItem(
					mAssociativePredicate(mpred(ids[x][0]), mpred(ids[y][1]), mpred(ids[z][2])),
					mList(ids[x][0], ids[y][1], ids[z][2]),
					mAssociativePredicate(mpred(bs[2][0]), mpred(bs[1][1]), mpred(bs[0][2]))
			),
			// AssociativePredicate with same ident twice
			new TestItem(
					mAssociativePredicate(mpred(ids[x][0]), mpred(ids[y][1]), mpred(ids[x][2])),
					mList(ids[x][0], ids[y][1]),
					mAssociativePredicate(mpred(bs[1][0]), mpred(bs[0][1]), mpred(bs[1][2]))
			),
			// AtomicExpression
			new TestItem(
					mAtomicExpression(),
					NO_FREE_IDENT,
					mAtomicExpression()
			),
			// BinaryExpression
			new TestItem(
					mBinaryExpression(ids[x][0], ids[y][1]),
					mList(ids[x][0], ids[y][1]),
					mBinaryExpression(bs[1][0], bs[0][1])
			),
			// BinaryExpression with same ident twice
			new TestItem(
					mBinaryExpression(ids[x][0], ids[x][1]),
					mList(ids[x][0]),
					mBinaryExpression(bs[0][0], bs[0][1])
			),
			// BinaryPredicate
			new TestItem(
					mBinaryPredicate(mpred(ids[x][0]), mpred(ids[y][1])),
					mList(ids[x][0], ids[y][1]),
					mBinaryPredicate(mpred(bs[1][0]), mpred(bs[0][1]))
			),
			// BinaryPredicate with same ident twice
			new TestItem(
					mBinaryPredicate(mpred(ids[x][0]), mpred(ids[x][1])),
					mList(ids[x][0]),
					mBinaryPredicate(mpred(bs[0][0]), mpred(bs[0][1]))
			),
			// BoolExpression
			new TestItem(
					mBoolExpression(mpred(ids[x][0])),
					mList(ids[x][0]),
					mBoolExpression(mpred(bs[0][0]))
			),
			// BoundIdentifier
			new TestItem(
					bs[0][0],
					NO_FREE_IDENT,
					bs[0][0]
			),
			// FreeIdentifier
			new TestItem(
					ids[x][0],
					mList(ids[x][0]),
					bs[0][0]
			),
			// IntegerLiteral
			new TestItem(
					mIntegerLiteral(),
					NO_FREE_IDENT,
					mIntegerLiteral()
			),
			// LiteralPredicate
			new TestItem(
					mLiteralPredicate(),
					NO_FREE_IDENT,
					mLiteralPredicate()
			),
			// QuantifiedExpression
			new TestItem(
					mQuantifiedExpression(
							mList(bds[x][0]),
							mRelationalPredicate(ids[y][1], bs[0][2]), 
							mBinaryExpression(ids[z][3], bs[0][4])),
					mList(ids[y][1], ids[z][3]),
					mQuantifiedExpression(
							mList(bds[x][0]),
							mRelationalPredicate(bs[2][1], bs[0][2]), 
							mBinaryExpression(bs[1][3], bs[0][4]))
			),
			// QuantifiedExpression with same ident twice
			new TestItem(
					mQuantifiedExpression(
							mList(bds[x][0]),
							mRelationalPredicate(ids[y][1], bs[0][2]), 
							mBinaryExpression(ids[y][3], bs[0][4])),
					mList(ids[y][1]),
					mQuantifiedExpression(
							mList(bds[x][0]),
							mRelationalPredicate(bs[1][1], bs[0][2]), 
							mBinaryExpression(bs[1][3], bs[0][4]))
			),
			// QuantifiedPredicate
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(ids[y][1], bs[0][2])), 
					mList(ids[y][1]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[1][1], bs[0][2]))
			),
			// RelationalPredicate
			new TestItem(
					mRelationalPredicate(ids[x][0], ids[y][1]),
					mList(ids[x][0], ids[y][1]),
					mRelationalPredicate(bs[1][0], bs[0][1])
			),
			// RelationalPredicate with same ident twice
			new TestItem(
					mRelationalPredicate(ids[x][0], ids[x][1]),
					mList(ids[x][0]),
					mRelationalPredicate(bs[0][0], bs[0][1])
			),
			// SetExtension
			new TestItem(
					mSetExtension(ids[x][0], ids[y][1], ids[z][2]),
					mList(ids[x][0], ids[y][1], ids[z][2]),
					mSetExtension(bs[2][0], bs[1][1], bs[0][2])
			),
			// SetExtension with same ident twice
			new TestItem(
					mSetExtension(ids[x][0], ids[y][1], ids[x][2]),
					mList(ids[x][0], ids[y][1]),
					mSetExtension(bs[1][0], bs[0][1], bs[1][2])
			),
			// SimplePredicate
			new TestItem(
					mpred(ids[x][0]),
					mList(ids[x][0]),
					mpred(bs[0][0])
			),
			// UnaryExpression
			new TestItem(
					mUnaryExpression(ids[x][0]),
					mList(ids[x][0]),
					mUnaryExpression(bs[0][0])
			),
			// UnaryPredicate
			new TestItem(
					mUnaryPredicate(mpred(ids[x][0])),
					mList(ids[x][0]),
					mUnaryPredicate(mpred(bs[0][0]))
			),
			// Test with already bound ident
			new TestItem(
					mSetExtension(ids[x][0], bs[0][1]),
					mList(ids[x][0]),
					mSetExtension(bs[0][0], bs[1][1])
			),
			
			// Test from the JavaDoc of Formula#bindAllFreeIdentifiers.
			// !x.x = y &amp; y = z &amp; y = { z | z : y)
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mAssociativePredicate(
									mRelationalPredicate(bs[0][1], ids[y][2]),
									mRelationalPredicate(ids[y][3], ids[z][4]),
									mRelationalPredicate(
											ids[y][5], 
											mQuantifiedExpression(
													mList(bds[z][6]),
													mRelationalPredicate(bs[0][7], ids[y][8]),
													bs[0][9])))),
					mList(ids[y][2], ids[z][4]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mAssociativePredicate(
									mRelationalPredicate(bs[0][1], bs[2][2]),
									mRelationalPredicate(bs[2][3], bs[1][4]),
									mRelationalPredicate(
											bs[2][5], 
											mQuantifiedExpression(
													mList(bds[z][6]),
													mRelationalPredicate(bs[0][7], bs[3][8]),
													bs[0][9]))))
			),
	};

	private TestItem[] testItemsBindPartial = new TestItem[] {
			// Formula "x", bind "x"
			new TestItem(
					ids[x][0],
					mList(ids[x][0]),
					bs[0][0]
			),
			// Formula "x + y", bind "x, y"
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1]),
					mList(ids[x][0], ids[y][1]),
					mAssociativeExpression(bs[1][0], bs[0][1])
			),
			// Formula "x + y", bind "x"
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1]),
					mList(ids[x][0]),
					mAssociativeExpression(bs[0][0], ids[y][1])
			),
			// Formula "x + y", bind "y"
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1]),
					mList(ids[y][1]),
					mAssociativeExpression(ids[x][0], bs[0][1])
			),
			// Formula "x", bind "y"
			new TestItem(
					ids[x][0],
					mList(ids[y][0]),
					ids[x][0]
			),
			// Formula "x + y", bind "x, z"
			new TestItem(
					mAssociativeExpression(ids[x][0], ids[y][1]),
					mList(ids[x][0], ids[z][1]),
					mAssociativeExpression(bs[1][0], ids[y][1])
			),
			// Formula "!x.x = y", bind "x"
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					),
					mList(ids[x][0]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					)
			),
			// Formula "!x.x = y", bind "y"
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					),
					mList(ids[y][0]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], bs[1][2])
					)
			),
			// Formula "!x.x = y", bind "z"
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					),
					mList(ids[z][0]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					)
			),
			// Formula "!x.x = y", bind "x, z"
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					),
					mList(ids[x][0], ids[z][0]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					)
			),
			// Formula "!x.x = y", bind "x, y, z"
			new TestItem(
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], ids[y][2])
					),
					mList(ids[x][0], ids[y][0], ids[z][0]),
					mQuantifiedPredicate(
							mList(bds[x][0]),
							mRelationalPredicate(bs[0][1], bs[2][2])
					)
			),
	};
			
	public static SimplePredicate mpred(Expression expr) {
		return FastFactory.mSimplePredicate(expr);
	}

	// Check equality of arrays of identifiers, including source locations.
	private void assertEquals(String msg, FreeIdentifier[] expected, FreeIdentifier[] actual) {
		assertEquals(msg, expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			// Same identifier
			assertEquals(msg, expected[i], actual[i]);
			// Same location
			assertEquals(msg, expected[i].getSourceLocation(), actual[i].getSourceLocation());
		}
	}
	
	// Check equality of arrays of identifiers, including source locations.
	private void assertEquals(String msg, FreeIdentifier[] expected, List<BoundIdentDecl> actual) {
		assertEquals(msg, expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			// Same name
			assertEquals(msg, expected[i].getName(), actual.get(i).getName());
			// Same location
			assertEquals(msg, expected[i].getSourceLocation(), actual.get(i).getSourceLocation());
			// Same type
			assertEquals(msg, expected[i].getType(), actual.get(i).getType());
		}
	}
	
	
	/**
	 * Test method for 'org.eventb.core.ast.Formula.getFreeIdentifiers()'
	 */
	public final void testGetFreeIdentifiers() {
		for (TestItem testItem : testItemsBindAll) {
			String msg = testItem.formula.toString();
			FreeIdentifier[] result = testItem.formula.getFreeIdentifiers();
			assertEquals(msg, testItem.freeIdents, result);
		}
	}
	
	/**
	 * Test method for 'org.eventb.core.ast.Formula.bindAllFreeIdents()'
	 */
	public final void testBindAllFreeIdents() {
		for (TestItem testItem : testItemsBindAll) {
			String msg = testItem.formula.toString();
			List<BoundIdentDecl> actualIdents = new ArrayList<BoundIdentDecl>();
			Formula result = testItem.formula.bindAllFreeIdents(actualIdents, ff);
			assertEquals(msg, testItem.freeIdents, actualIdents);
			assertEquals(msg, testItem.boundFormula, result);
		}
	}

	/**
	 * Test method for 'org.eventb.core.ast.Formula.bindTheseIdents()'
	 */
	public final void testBindTheseIdents() {
		for (TestItem testItem : testItemsBindPartial) {
			List<FreeIdentifier> identsToBind = Arrays.asList(testItem.freeIdents);
			String msg = "binding " + identsToBind + " in " + testItem.formula.toString();
			Formula result = testItem.formula.bindTheseIdents(identsToBind, ff);
			assertEquals(msg, testItem.boundFormula, result);
		}
	}
	
	private final List<String> freeToString(FreeIdentifier[] free) {
		LinkedList<String> list = new LinkedList<String>();
		for(int i=0; i<free.length; i++)
			list.add(free[i].getName());
		return list;
	}
	
	private void isWellFormedSpecialCases() {
		//"{x,y\u00b7\u2200s,t,u\u00b7\u22a5\u2223u}=a"
		
		final FreeIdentifier id_a = ff.makeFreeIdentifier("a", null);
		
		final BoundIdentDecl bd_x = ff.makeBoundIdentDecl("x", null);
		final BoundIdentDecl bd_y = ff.makeBoundIdentDecl("y", null);
		final BoundIdentDecl bd_s = ff.makeBoundIdentDecl("s", null);
		final BoundIdentDecl bd_t = ff.makeBoundIdentDecl("t", null);
		final BoundIdentDecl bd_u = ff.makeBoundIdentDecl("u", null);
		
		final LiteralPredicate bfalse = ff.makeLiteralPredicate(Formula.BFALSE, null);

		Predicate pp = ff.makeRelationalPredicate(Formula.EQUAL,ff.makeQuantifiedExpression(Formula.CSET,mList(bd_x, bd_y),
						ff.makeQuantifiedPredicate(Formula.FORALL,mList(bd_s, bd_t, bd_u),bfalse,null),ff.makeBoundIdentifier(4,null),null, QuantifiedExpression.Form.Explicit),id_a,null);

		assertFalse("Formula has dangling bound index", pp.isWellFormed());
	}
	
	/**
	 * Test method for 'org.eventb.core.ast.Formula.isWellFormed()'
	 */
	public void testIsWellFormed() {
		for (TestItem testItem : testItemsBindPartial) {
			assertTrue("Should be well-formed: " + testItem.formula, testItem.formula.isWellFormed());
			FreeIdentifier[] result = testItem.formula.getFreeIdentifiers();
			TreeSet<String> freeIds = new TreeSet<String>(freeToString(result));
			freeIds.removeAll(freeToString(testItem.freeIdents));
			boolean varBound = freeIds.size() < result.length;
			if(varBound)
				assertFalse("Should not be well-formed: " + testItem.boundFormula, testItem.boundFormula.isWellFormed());
			else
				assertTrue("Should be well-formed: " + testItem.boundFormula, testItem.boundFormula.isWellFormed());
		}
		
		isWellFormedSpecialCases();
	}
	
	private void checkFreeIdent(FreeIdentifier ident, String name, Type type) {
		assertEquals(name, ident.getName());
		assertEquals(type, ident.getType());
	}
	

	/**
	 * Test method for 'org.eventb.core.ast.FormulaFactory.makeFreshIdentifiers()'
	 */
	public void testMakeFreshIdentifiers() {
		final ITypeEnvironment tenv = ff.makeTypeEnvironment();
		tenv.addGivenSet("S");
		
		BoundIdentDecl bd_x1 = mBoundIdentDecl("x");
		BoundIdentDecl bd_x2 = mBoundIdentDecl("x");
		BoundIdentDecl bd_y = mBoundIdentDecl("y");
		BoundIdentDecl bd_z = mBoundIdentDecl("z");
		
		BoundIdentifier b0 = mBoundIdentifier(0);
		BoundIdentifier b1 = mBoundIdentifier(1);
		BoundIdentifier b2 = mBoundIdentifier(2);
		BoundIdentifier b3 = mBoundIdentifier(3);

		final Expression INT = mAtomicExpression(Formula.INTEGER);
		final Expression BOOL = mAtomicExpression(Formula.BOOL);
		final Expression S = ff.makeFreeIdentifier("S", null);
		
		Predicate pred = mQuantifiedPredicate(
				mList(bd_x1, bd_x2, bd_y, bd_z),
				mAssociativePredicate(Formula.LAND,
						mRelationalPredicate(Formula.IN, b3, INT),
						mRelationalPredicate(Formula.IN, b2, BOOL),
						mRelationalPredicate(Formula.IN, b1, BOOL),
						mRelationalPredicate(Formula.IN, b0, S))
		);
		pred.typeCheck(tenv);
		assertTrue("Initial type-check failed", pred.isTypeChecked());
		
		// Create one fresh identifier 
		FreeIdentifier[] idents = ff.makeFreshIdentifiers(mList(bd_x1), tenv);
		assertEquals(idents.length, 1);
		checkFreeIdent(idents[0], "x", bd_x1.getType());
		
		// Create two fresh identifiers 
		idents = ff.makeFreshIdentifiers(mList(bd_x1, bd_y), tenv);
		assertEquals(idents.length, 2);
		checkFreeIdent(idents[0], "x0", bd_x1.getType());
		checkFreeIdent(idents[1], "y", bd_y.getType());
		
		// Create three fresh identifiers 
		idents = ff.makeFreshIdentifiers(mList(bd_x1, bd_y, bd_z), tenv);
		assertEquals(idents.length, 3);
		checkFreeIdent(idents[0], "x1", bd_x1.getType());
		checkFreeIdent(idents[1], "y0", bd_y.getType());
		checkFreeIdent(idents[2], "z", bd_z.getType());
		
		// Create two fresh identifiers with similar bound decl 
		idents = ff.makeFreshIdentifiers(mList(bd_x1, bd_x2), tenv);
		assertEquals(idents.length, 2);
		checkFreeIdent(idents[0], "x2", bd_x1.getType());
		checkFreeIdent(idents[1], "x3", bd_x2.getType());
	}
	
	private void typeCheck(Formula formula, ITypeEnvironment te) {
		formula.typeCheck(te);
		assertTrue("Formula " + formula + " should typecheck.", formula.isTypeChecked());
	}
	
	private void typeCheck(Formula[] formulas, ITypeEnvironment te) {
		for (Formula formula: formulas) {
			typeCheck(formula, te);
		}
	}
	
	/**
	 * Test method for 'org.eventb.core.ast.Assignment.getUsedIdentifiers()'
	 */
	public void testGetUsedIdentifiers() {
		final Type INT = ff.makeIntegerType();
		final ITypeEnvironment te = 
			mTypeEnvironment(mList("x", "y", "z"), mList(INT, INT, INT));

		/* Becomes equal to with two identifiers, one being used. */
		Assignment assignment = mBecomesEqualTo(mList(ids[x][0], ids[y][1]), mList(ids[y][2], ids[z][3]));
		typeCheck(assignment, te);
		FreeIdentifier[] expected = mList(ids[y][2], ids[z][3]);
		typeCheck(expected, te);
		FreeIdentifier[] actual = assignment.getUsedIdentifiers();
		assertEquals(assignment.toString(), expected, actual);
		
		/* Becomes member of with lhs identifier unused. */
		assignment = mBecomesMemberOf(ids[x][0], mSetExtension(ids[y][1]));
		typeCheck(assignment, te);
		expected = mList(ids[y][1]);
		typeCheck(expected, te);
		actual = assignment.getUsedIdentifiers();
		assertEquals(assignment.toString(), expected, actual);
		
		/* Becomes member of with lhs identifier used. */
		assignment = mBecomesMemberOf(ids[x][0], mSetExtension(ids[x][1]));
		typeCheck(assignment, te);
		expected = mList(ids[x][1]);
		typeCheck(expected, te);
		actual = assignment.getUsedIdentifiers();
		assertEquals(assignment.toString(), expected, actual);
		
		/* Becomes such that with two identifiers, one being used. */
		assignment = mBecomesSuchThat(mList(ids[x][0], ids[y][1]),
				mAssociativePredicate(LAND,
						mRelationalPredicate(EQUAL, bs[0][2], ids[y][3]),
						mRelationalPredicate(EQUAL, bs[1][4], ids[z][5])
				)
		);
		typeCheck(assignment, te);
		expected = mList(ids[y][3], ids[z][5]);
		typeCheck(expected, te);
		actual = assignment.getUsedIdentifiers();
		assertEquals(assignment.toString(), expected, actual);
	}

}
