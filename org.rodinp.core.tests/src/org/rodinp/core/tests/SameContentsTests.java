/*******************************************************************************
 * Copyright (c) 2007 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.core.tests;

import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.tests.basis.NamedElement;
import org.rodinp.core.tests.basis.NamedElement2;

/**
 * Unit tests for:
 *    IInternalParent.hasSameContents().
 *    IInternalParent.hasSameAttributes().
 *    IInternalParent.hasSameChildren().
 * 
 * @author Laurent Voisin
 */
public class SameContentsTests extends ModifyingResourceTests {

	public static void assertSameContents(IInternalParent left,
			IInternalParent right, boolean sameContents,
			boolean sameAttributes, boolean sameChildren)
			throws RodinDBException {

		assertEquals(sameContents, left.hasSameContents(right));
		assertEquals(sameContents, right.hasSameContents(left));

		assertEquals(sameAttributes, left.hasSameAttributes(right));
		assertEquals(sameAttributes, right.hasSameAttributes(left));

		assertEquals(sameChildren, left.hasSameChildren(right));
		assertEquals(sameChildren, right.hasSameChildren(left));
	}
	
	public SameContentsTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createRodinProject("P");
		createRodinProject("P2");
	}
	
	public void tearDown() throws Exception {
		deleteProject("P");
		deleteProject("P2");
		super.tearDown();
	}

	/**
	 * Ensures that element name and types are taken into account when comparing
	 * files.
	 */
	public void testFileNameType() throws Exception {
		IRodinFile rf1, rf2;
		
		rf1 = getRodinFile("P/x.test");
		rf2 = getRodinFile("P2/x.test");
		assertSameContents(rf1, rf2, true, true, true);

		rf1 = getRodinFile("P/x.test");
		rf2 = getRodinFile("P/x.test2");
		assertSameContents(rf1, rf2, false, true, true);
		
		rf1 = getRodinFile("P/x.test");
		rf2 = getRodinFile("P/y.test");
		assertSameContents(rf1, rf2, false, true, true);
	}
	
	/**
	 * Ensures that element existence is taken into account when comparing
	 * files.
	 */
	public void testFileExistence() throws Exception {
		final IRodinFile rf1 = getRodinFile("P/x.test");
		final IRodinFile rf2 = getRodinFile("P2/x.test");
		assertSameContents(rf1, rf2, true, true, true);
		
		rf1.create(false, null);
		assertSameContents(rf1, rf2, false, false, false);

		rf2.create(false, null);
		assertSameContents(rf1, rf2, true, true, true);
	}
	
	/**
	 * Ensures that file attributes are taken into account when comparing
	 * files.
	 */
	public void testFileAttributes() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P2/x.test");

		rf1.setAttributeValue(fBool, true, null);
		assertSameContents(rf1, rf2, false, false, true);
		
		rf2.setAttributeValue(fBool, true, null);
		assertSameContents(rf1, rf2, true, true, true);
		
		rf2.setAttributeValue(fBool, false, null);
		assertSameContents(rf1, rf2, false, false, true);

		rf2.setAttributeValue(fBool, true, null);
		createNEPositive(rf2, "foo", null);
		assertSameContents(rf1, rf2, false, true, false);
	}
	
	/**
	 * Ensures that file children are taken into account when comparing
	 * files.
	 */
	public void testFileChildren() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P2/x.test");

		createNEPositive(rf1, "foo", null);
		assertSameContents(rf1, rf2, false, true, false);
		
		final NamedElement foo2 = createNEPositive(rf2, "foo", null);
		assertSameContents(rf1, rf2, true, true, true);
		
		createNEPositive(rf1, "bar", null);
		final NamedElement bar2 = createNEPositive(rf2, "bar", foo2);
		assertSameContents(rf1, rf2, false, true, false);
		
		foo2.move(rf2, bar2, null, false, null);
		assertSameContents(rf1, rf2, true, true, true);
	}
	
	/**
	 * Ensures that element name and types are taken into account when comparing
	 * internal elements.
	 */
	public void testIntNameType() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P/y.test");
		IInternalElement ne1, ne2;
		
		ne1 = rf1.getInternalElement(NamedElement.ELEMENT_TYPE, "foo");
		ne2 = rf2.getInternalElement(NamedElement.ELEMENT_TYPE, "foo");
		assertSameContents(ne1, ne2, true, true, true);

		ne1 = rf1.getInternalElement(NamedElement.ELEMENT_TYPE, "foo");
		ne2 = rf2.getInternalElement(NamedElement2.ELEMENT_TYPE, "foo");
		assertSameContents(ne1, ne2, false, true, true);
		
		ne1 = rf1.getInternalElement(NamedElement.ELEMENT_TYPE, "foo");
		ne2 = rf2.getInternalElement(NamedElement.ELEMENT_TYPE, "bar");
		assertSameContents(ne1, ne2, false, true, true);
	}
	
	/**
	 * Ensures that element existence is taken into account when comparing
	 * internal elements.
	 */
	public void testIntExistence() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P/y.test");
		final NamedElement ne1 = getNamedElement(rf1, "foo");
		final NamedElement ne2 = getNamedElement(rf2, "foo");

		assertSameContents(ne1, ne2, true, true, true);
		
		ne1.create(null, null);
		assertSameContents(ne1, ne2, false, false, false);

		ne2.create(null, null);
		assertSameContents(ne1, ne2, true, true, true);
	}
	
	/**
	 * Ensures that file attributes are taken into account when comparing
	 * internal elements.
	 */
	public void testIntAttributes() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P/y.test");
		final NamedElement ne1 = createNEPositive(rf1, "foo", null);
		final NamedElement ne2 = createNEPositive(rf2, "foo", null);

		ne1.setAttributeValue(fBool, true, null);
		assertSameContents(ne1, ne2, false, false, true);
		
		ne2.setAttributeValue(fBool, true, null);
		assertSameContents(ne1, ne2, true, true, true);
		
		ne2.setAttributeValue(fBool, false, null);
		assertSameContents(ne1, ne2, false, false, true);

		ne2.setAttributeValue(fBool, true, null);
		createNEPositive(ne2, "foo", null);
		assertSameContents(ne1, ne2, false, true, false);
	}
	
	/**
	 * Ensures that file children are taken into account when comparing
	 * internal elements.
	 */
	public void testIntChildren() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P/y.test");
		final NamedElement ne1 = createNEPositive(rf1, "root", null);
		final NamedElement ne2 = createNEPositive(rf2, "root", null);

		createNEPositive(ne1, "foo", null);
		assertSameContents(ne1, ne2, false, true, false);
		
		final NamedElement foo2 = createNEPositive(ne2, "foo", null);
		assertSameContents(ne1, ne2, true, true, true);
		
		createNEPositive(ne1, "bar", null);
		final NamedElement bar2 = createNEPositive(ne2, "bar", foo2);
		assertSameContents(ne1, ne2, false, true, false);
		
		foo2.move(ne2, bar2, null, false, null);
		assertSameContents(ne1, ne2, true, true, true);
	}
	
	/**
	 * Ensures that children attributes are taken into account when comparing
	 * elements.
	 */
	public void testChildrenAttributes() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P2/x.test");

		final NamedElement foo1 = createNEPositive(rf1, "foo", null);
		final NamedElement foo2 = createNEPositive(rf2, "foo", null);
		foo1.setAttributeValue(fBool, true, null);
		foo2.setAttributeValue(fBool, true, null);
		assertSameContents(rf1, rf2, true, true, true);
		
		foo2.setAttributeValue(fBool, false, null);
		assertSameContents(rf1, rf2, false, true, false);
	}
	
	/**
	 * Ensures that grand-children are taken into account when comparing
	 * elements.
	 */
	public void testGrandChildren() throws Exception {
		final IRodinFile rf1 = createRodinFile("P/x.test");
		final IRodinFile rf2 = createRodinFile("P2/x.test");

		final NamedElement foo1 = createNEPositive(rf1, "foo", null);
		createNEPositive(rf2, "foo", null);
		assertSameContents(rf1, rf2, true, true, true);
		
		createNEPositive(foo1, "bar", null);
		assertSameContents(rf1, rf2, false, true, false);
	}
	
}
