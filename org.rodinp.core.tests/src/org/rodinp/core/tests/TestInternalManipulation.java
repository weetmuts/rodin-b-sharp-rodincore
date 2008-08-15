/*******************************************************************************
 * Copyright (c) 2006, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - removed test on pseudo-attribute "contents"
 *******************************************************************************/
package org.rodinp.core.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinDBStatusConstants;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.RodinFile;
import org.rodinp.core.tests.basis.NamedElement;

public class TestInternalManipulation extends ModifyingResourceTests {

	public TestInternalManipulation() {
		super("org.rodinp.core.tests.TestInternalManipulation");
	}

	private IRodinProject rodinProject;
	private IRodinFile rodinFile;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		rodinProject = createRodinProject("P");
		rodinFile = createRodinFile("P/x.test");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		rodinFile.getResource().delete(true, null);
		rodinProject.getProject().delete(true, true, null);
		rodinProject.getRodinDB().close();
	}

	private void checkChildren(IParent parent,
			IInternalElement... expChildren) throws RodinDBException {

		final IRodinElement[] children = parent.getChildren();
		assertEquals(expChildren.length, children.length);
		for (int i = 0; i < children.length; ++i) {
			assertEquals(expChildren[i], children[i]);
		}
		assertEquals(expChildren.length != 0, parent.hasChildren());
	}
	
	private void checkEmpty(IInternalElement... children)
			throws RodinDBException {
		for (IInternalElement child : children) {
			assertFalse(child.hasChildren());
		}
	}
	
	private void checkEmptyChildren(IParent parent,
			IInternalElement... expChildren) throws RodinDBException {
		checkChildren(parent, expChildren);
		checkEmpty(expChildren);
	}
	
	// Test creation of some top-level internal elements
	public void testCreateInternalElement() throws Exception {
		// File exists and is empty
		assertExists("File should exist", rodinFile);
		checkEmptyChildren(rodinFile);
		
		IInternalElement e1 = createNEPositive(rodinFile, "e1", null);
		checkEmptyChildren(rodinFile, e1);

		IInternalElement e3 = createNEPositive(rodinFile, "e3", null);
		checkEmptyChildren(rodinFile, e1, e3);

		IInternalElement e2 = createNEPositive(rodinFile, "e2", e3);
		checkEmptyChildren(rodinFile, e1, e2, e3);

		IInternalElement e0 = createNEPositive(rodinFile, "e0", e1);
		checkEmptyChildren(rodinFile, e0, e1, e2, e3);

		rodinFile.save(null, true);
		rodinFile.close();
		
		//showFile(rodinFile.getResource());
		
		assertExists("Internal element should exist", e0);
		assertExists("Internal element should exist", e1);
		assertExists("Internal element should exist", e2);
		assertExists("Internal element should exist", e3);
		checkEmptyChildren(rodinFile, e0, e1, e2, e3);
		
		// Create sub-elements
		IInternalElement e12 = createNEPositive(e1, "e12", null);
		assertExists("Internal element should exist", e12);
		checkChildren(rodinFile, e0, e1, e2, e3);
		checkEmpty(e0, e2, e3);
		checkEmptyChildren(e1, e12);
		
		IInternalElement e11 = createNEPositive(e1, "e11", e12);
		assertExists("Internal element should exist", e11);
		checkChildren(rodinFile, e0, e1, e2, e3);
		checkEmpty(e0, e2, e3);
		checkEmptyChildren(e1, e11, e12);

		assertEquals("Unexpected file contents",
				"x.test [in P]\n"
				+ "  e0[org.rodinp.core.tests.namedElement]\n"
				+ "  e1[org.rodinp.core.tests.namedElement]\n"
				+ "    e11[org.rodinp.core.tests.namedElement]\n"
				+ "    e12[org.rodinp.core.tests.namedElement]\n"
				+ "  e2[org.rodinp.core.tests.namedElement]\n"
				+ "  e3[org.rodinp.core.tests.namedElement]",
				((RodinFile) rodinFile).toDebugString());
		
		// rodinFile.save(null, true);
		// showFile(rodinFile.getResource());
		
		// Cleanup
		rodinFile.getResource().delete(true, null);
		assertNotExists("File should not exist", rodinFile);
		createRodinFile("P/x.test");
		assertNotExists("Internal element should not exist", e0);
		assertNotExists("Internal element should not exist", e1);
		assertNotExists("Internal element should not exist", e11);
		assertNotExists("Internal element should not exist", e12);
		assertNotExists("Internal element should not exist", e2);
		assertNotExists("Internal element should not exist", e3);
		checkChildren(rodinFile);
	}

	/**
	 * Ensures that attempting to create a new top-level internal element with
	 * the same type and name as an existing one fails.
	 */
	public void testCreateTopDuplicate() throws Exception {
		// File exists and is empty
		assertExists("File should exist", rodinFile);
		checkEmptyChildren(rodinFile);
		
		NamedElement e1 = createNEPositive(rodinFile, "foo", null);
		checkEmptyChildren(rodinFile, e1);

		// Attempt to create a duplicate element
		createNENegative(rodinFile, "foo", null,
				IRodinDBStatusConstants.NAME_COLLISION);
		
		// File has not changed
		checkEmptyChildren(rodinFile, e1);
	}

	/**
	 * Ensures that attempting to create a new non top-level internal element
	 * with the same type and name as an existing one fails.
	 */
	public void testCreateNonTopDuplicate() throws Exception {
		// File exists and is empty
		assertExists("File should exist", rodinFile);
		checkEmptyChildren(rodinFile);
		
		NamedElement e1 = createNEPositive(rodinFile, "foo", null);
		NamedElement e11 = createNEPositive(e1, "bar", null);
		checkChildren(rodinFile, e1);
		checkEmptyChildren(e1, e11);

		// Attempt to create a duplicate element
		createNENegative(e1, "bar", null,
				IRodinDBStatusConstants.NAME_COLLISION);
		
		// File has not changed
		checkChildren(rodinFile, e1);
		checkEmptyChildren(e1, e11);
	}

	// Test creation of a Rodin file and an internal element as an atomic action
	public void testCreateInternalElementAtomic() throws Exception {
		
		// Start with a fresh file
		final String fileName = "titi.test";
		final IRodinFile rf = rodinProject.getRodinFile(fileName);
		assertNotExists("Target file should not exist", rf);
		
		try {
			RodinCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					rf.create(false, null);
					IInternalElement e1 = createNEPositive(rf, "e1", null);
					assertExists("New file should exist", rf);
					checkEmptyChildren(rf, e1);
				}
			}, null);

			assertExists("New file should exist", rf);

			IInternalElement e1 =
				rf.getInternalElement(NamedElement.ELEMENT_TYPE, "e1");
			checkEmptyChildren(rf, e1);
		} finally {
			// Ensure the new Rodin file is destroyed.
			rf.delete(true, null);
		}
	}

	// Test modification of the in-memory copy of a Rodin file and then
	// reverting the changes
	public void testCreateInternalElementRevert() throws Exception {
		
		// Start with a fresh file
		final String fileName = "titi.test";
		final IRodinFile rf = rodinProject.getRodinFile(fileName);
		assertNotExists("Target file should not exist", rf);
		
		try {
			rf.create(false, null);
			IInternalElement e1 = createNEPositive(rf, "e1", null);
			assertExists("Internal element should exist", e1);
			checkEmptyChildren(rf, e1);

			// Revert the changes
			rf.makeConsistent(null);
			assertNotExists("Internal element should not exist", e1);
			checkEmptyChildren(rf);
		} finally {
			// Ensure the new Rodin file is destroyed.
			rf.delete(true, null);
		}
	}

	// Test deletion of an internal element bypassing the Rodin database
	public void testDeleteInternalElementBypass() throws Exception {

		// First create an internal element
		final IInternalElement e1 = createNEPositive(rodinFile, "e1", null);
		rodinFile.save(null, false);
		checkEmptyChildren(rodinFile, e1);

		startDeltas();
		
		// Then, inside a workable, empty the Rodin file bypassing the database
		// The database is not updated!
		RodinCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IFile file = rodinFile.getResource();
				file.setContents(new ByteArrayInputStream(emptyBytes),
						false, true, null);
				
				// Inside the runnable, the Rodin database doesn't get the
				// resource change delta, so the element is still considered to
				// exist.
				assertExists("Internal element should exist", e1);
				checkEmptyChildren(rodinFile, e1);
			}
		}, null);

		// Once the workable is finished, the database gets the resource delta
		assertDeltas("Should report an unknown change to the file",
				"P[*]: {CHILDREN}\n" + 
				"	x.test[*]: {CONTENT}"
		);

		checkEmptyChildren(rodinFile);
		assertNotExists("Internal element should not exist", e1);
	}

	
	@SuppressWarnings("unused")
	private void showFile(IFile file) throws Exception {
		InputStream contents = file.getContents();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(contents, "UTF-8")
		);
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
	}

	/*
	 * Ensures that creating again an existing file empties it.
	 */
	public void testRecreateFile() throws Exception {
		checkEmptyChildren(rodinFile);
		IInternalElement ne = createNEPositive(rodinFile, "foo", null);
		rodinFile.save(null, false);
		checkEmptyChildren(rodinFile, ne);
		
		// Recreate the file
		rodinFile.create(true, null);
		checkEmptyChildren(rodinFile);
		createNEPositive(rodinFile, ne.getElementName(), null);
		checkEmptyChildren(rodinFile, ne);
	}
	
	/*
	 * Ensures that creating again an existing file empties it, even when done
	 * inside a runnable.
	 */
	public void testRecreateFileInRunnable() throws Exception {
		checkEmptyChildren(rodinFile);
		final IInternalElement ne = createNEPositive(rodinFile, "foo", null);
		rodinFile.save(null, false);
		checkEmptyChildren(rodinFile, ne);
		
		// Recreate the file
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				rodinFile.create(true, null);
				checkEmptyChildren(rodinFile);
				createNEPositive(rodinFile, ne.getElementName(), null);
				checkEmptyChildren(rodinFile, ne);
			}
		};
		getWorkspace().run(runnable, null);
		checkEmptyChildren(rodinFile, ne);
	}
	
	public void testGetChildrenOfType() throws Exception {
		IRodinElement[] children;
		
		checkEmptyChildren(rodinFile);
		children = rodinFile.getChildrenOfType(NamedElement.ELEMENT_TYPE);
		assertTrue(children instanceof NamedElement[]);
		assertEquals("Array should be empty", 0, children.length);
		
		final NamedElement ne = createNEPositive(rodinFile, "foo", null);
		children = rodinFile.getChildrenOfType(NamedElement.ELEMENT_TYPE);
		assertTrue(children instanceof NamedElement[]);
		assertEquals("Array should contain one element", 1, children.length);
		assertEquals("Wrong element", ne, children[0]);
	}

	/**
	 * Ensures that a similar element for a Rodin file element is constructed
	 * correctly.
	 */
	public void testSimilarFile() {
		final IRodinFile rf1 = getRodinFile("P/X.test");
		final IRodinFile rf2 = getRodinFile("P/Y.test");
		assertEquals(rf1, rf1.getSimilarElement(rf1));
		assertEquals(rf2, rf1.getSimilarElement(rf2));
	}

	/**
	 * Ensures that a similar element for a top-level element is constructed
	 * correctly.
	 */
	public void testSimilarTop() {
		final IRodinFile rf1 = getRodinFile("P/X.test");
		final NamedElement ie1 = getNamedElement(rf1, "foo");
		final IRodinFile rf2 = getRodinFile("P/Y.test");
		final NamedElement ie2 = getNamedElement(rf2, ie1.getElementName());
		assertEquals(ie1, ie1.getSimilarElement(rf1));
		assertEquals(ie2, ie1.getSimilarElement(rf2));
	}

	/**
	 * Ensures that a similar element for a non top-level element is constructed
	 * correctly.
	 */
	public void testSimilarInt() {
		final IRodinFile rf1 = getRodinFile("P/X.test");
		final NamedElement ie1 = getNamedElement(rf1, "foo");
		final NamedElement ie11 = getNamedElement(ie1, "bar");
		final IRodinFile rf2 = getRodinFile("P/Y.test");
		final NamedElement ie2 = getNamedElement(rf2, ie1.getElementName());
		final NamedElement ie21 = getNamedElement(ie2, ie11.getElementName());
		assertEquals(ie11, ie11.getSimilarElement(rf1));
		assertEquals(ie21, ie11.getSimilarElement(rf2));
	}
	
}
