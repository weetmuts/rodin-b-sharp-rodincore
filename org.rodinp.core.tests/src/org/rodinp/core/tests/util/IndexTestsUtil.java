/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.rodinp.core.tests.util;

import static junit.framework.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.indexer.IDeclaration;
import org.rodinp.core.indexer.IOccurrence;
import org.rodinp.core.indexer.IOccurrenceKind;
import org.rodinp.core.tests.basis.NamedElement;
import org.rodinp.core.tests.basis.NamedElement2;
import org.rodinp.internal.core.indexer.Descriptor;
import org.rodinp.internal.core.indexer.Occurrence;
import org.rodinp.internal.core.indexer.sort.TotalOrder;
import org.rodinp.internal.core.indexer.tables.IExportTable;
import org.rodinp.internal.core.indexer.tables.IFileTable;
import org.rodinp.internal.core.indexer.tables.INameTable;
import org.rodinp.internal.core.indexer.tables.IRodinIndex;
import org.rodinp.internal.core.indexer.tables.RodinIndex;

public class IndexTestsUtil {

	public static final IAttributeType.String TEST_ATTR_TYPE = RodinCore
			.getStringAttrType("org.rodinp.core.tests.testAttributeType");

	public static final IInternalElementType<?> TEST_FILE_TYPE = RodinCore
			.getInternalElementType("org.rodinp.core.tests.test");

	public static final IInternalElementType<?> TEST_FILE_TYPE_2 = RodinCore
			.getInternalElementType("org.rodinp.core.tests.test2");

	public static final IOccurrenceKind TEST_KIND =
			RodinCore.getOccurrenceKind("testKind");

	public static final IOccurrenceKind TEST_KIND_1 =
			RodinCore.getOccurrenceKind("testKind1");

	public static final IOccurrenceKind TEST_KIND_2 =
			RodinCore.getOccurrenceKind("testKind2");

	public static IRodinFile createRodinFile(IRodinProject project,
			String fileName) throws RodinDBException {
		IRodinFile file = project.getRodinFile(fileName);
		file.create(true, null);
		return file;
	}

	public static IOccurrence createDefaultOccurrence(IInternalElement element,
			IDeclaration declaration) {
		return createInternalOccurrence(element, declaration, TEST_KIND);
	}

	public static IOccurrence createInternalOccurrence(IInternalElement element,
			IDeclaration declaration, IOccurrenceKind kind) {
		return new Occurrence(kind, RodinCore.getInternalLocation(element),
				declaration);
	}

	// useful when you want to keep a descriptor in the index
	// (empty descriptors can be removed)
	public static IOccurrence makeDescAndDefaultOcc(RodinIndex rodinIndex,
			IDeclaration declaration, IInternalElement element) {
		rodinIndex.makeDescriptor(declaration);
		return addInternalOccurrence(rodinIndex, declaration, element,
				TEST_KIND);
	}
	
	public static IOccurrence addInternalOccurrence(RodinIndex rodinIndex,
			IDeclaration declaration, IInternalElement element,
			IOccurrenceKind kind) {
		final Descriptor descElt = rodinIndex.getDescriptor(declaration
				.getElement());
		final IOccurrence occ = createInternalOccurrence(element, declaration,
				kind);
		descElt.addOccurrence(occ);
		return occ;
	}
	
	public static NamedElement createNamedElement(IInternalElement parent,
			String elementName) throws CoreException {
		NamedElement el = new NamedElement(elementName, parent);
		el.create(null, null);
		return el;
	}
	
	public static NamedElement createNamedElement(IRodinFile file,
			String elementName) throws CoreException {
		NamedElement el = new NamedElement(elementName, file.getRoot());
		el.create(null, null);
		return el;
	}

	public static NamedElement2 createNamedElement2(IRodinFile file,
			String elementName) throws CoreException {
		NamedElement2 el = new NamedElement2(elementName, file.getRoot());
		el.create(null, null);
		return el;
	}

	public static void assertNoSuchDescriptor(IRodinIndex index,
			IInternalElement element) {
		Descriptor desc = index.getDescriptor(element);
		assertNull("there should not be any descriptor for element "
				+ element.getElementName(), desc);
	}

	public static void assertNotNullDesc(Descriptor desc) {
		assertNotNull("Descriptor " + desc + " should not be null", desc);
	}

	public static void assertDescriptor(Descriptor expected, Descriptor actual) {
		assertDescDeclaration(actual, expected.getDeclaration());
		final IOccurrence[] expOccs = expected.getOccurrences();
		final IOccurrence[] actOccs = actual.getOccurrences();

		assertOccurrences(expOccs, actOccs);
	}

	public static <T> void assertSameElements(T[] expected, T[] actual,
			String arrayDesc) {
		final List<T> expList = Arrays.asList(expected);
		final List<T> actList = Arrays.asList(actual);

		assertSameElements(expList, actList, arrayDesc);
	}

	public static <T> void assertSameElements(List<T> expList, List<T> actList,
			String arrayDesc) {
		assertEquals(arrayDesc
				+ ": bad length in\nact: "
				+ actList
				+ "\nexp: "
				+ expList, expList.size(), actList.size());
		assertTrue(arrayDesc
				+ ": bad elements in\nact: "
				+ actList
				+ "\nexp: "
				+ expList, actList.containsAll(expList));
	}

	public static void assertIndex(IRodinIndex expected, IRodinIndex actual) {

		final Descriptor[] expDescs = expected.getDescriptors();

		for (Descriptor expDesc : expDescs) {
			final IInternalElement elt = expDesc.getDeclaration().getElement();
			final Descriptor actDesc = actual.getDescriptor(elt);
			assertDescriptor(expDesc, actDesc);
		}
	}

	public static void assertOccurrences(IOccurrence[] expected,
			IOccurrence[] actual) {
		assertSameElements(expected, actual, "occurrences");
	}

	public static void assertDescriptor(Descriptor desc,
			IDeclaration declaration, int expectedLength) {
		assertDescDeclaration(desc, declaration);
		assertLength(desc, expectedLength);
	}

	public static void assertContains(Descriptor desc, IOccurrence occ) {
		assertNotNullDesc(desc);
		assertTrue("occurrence not found: " + occ.getLocation().getElement(),
				desc.hasOccurrence(occ));
	}

	public static void assertContainsNot(Descriptor desc, IOccurrence occ) {
		assertNotNullDesc(desc);
		assertFalse("occurrence should not be found: "
				+ occ.getLocation().getElement(), desc.hasOccurrence(occ));
	}

	public static void assertContainsAll(Descriptor desc, IOccurrence... occs) {
		assertNotNullDesc(desc);
		for (IOccurrence occ : occs) {
			assertContains(desc, occ);
		}
	}

	public static void assertLength(Descriptor desc, int expectedLength) {
		assertNotNullDesc(desc);
		assertEquals("bad number of occurrences", expectedLength, desc
				.getOccurrences().length);
	}

	public static void assertDescDeclaration(Descriptor desc,
			IDeclaration declaration) {
		assertNotNullDesc(desc);
		assertEquals("bad declaration for descriptor " + desc, declaration,
				desc.getDeclaration());
	}

	public static <T> void assertLength(T[] elements, int length) {
		assertEquals("incorrect number of elements in: "
				+ elements
				+ "="
				+ Arrays.asList(elements), length, elements.length);
	}

	public static <T> void assertIsEmpty(T[] elements) {
		assertLength(elements, 0);
	}

	private static void assertContains(IInternalElement elem,
			IInternalElement[] actualElements) {

		List<IInternalElement> actList = Arrays.asList(actualElements);

		assertTrue("element " + elem.getElementName() + " is not present",
				actList.contains(elem));
	}

	private static void assertContainsAll(IInternalElement[] expectedElements,
			IInternalElement[] actualElements) {

		for (IInternalElement elem : expectedElements) {
			assertContains(elem, actualElements);
		}
	}

	public static void assertSameElements(IInternalElement[] expectedElements,
			IInternalElement[] actualElements) {

		assertContainsAll(expectedElements, actualElements);

		assertLength(actualElements, expectedElements.length);
	}

	public static void assertOrder(TotalOrder<IRodinFile> expected,
			TotalOrder<IRodinFile> actual, List<IRodinFile> files) {
		// assert initially marked files
		assertMarkedOrder(expected, actual);

		// assert that files not marked are present and well sorted
		for (IRodinFile file : files) {
			final List<IRodinFile> expPreds = expected.getPredecessors(file);
			final List<IRodinFile> actPreds = actual.getPredecessors(file);
			assertSameElements(expPreds, actPreds, "predecessors");
		}
	}

	private static void assertMarkedOrder(TotalOrder<IRodinFile> expected,
			TotalOrder<IRodinFile> actual) {
		// nodes must already be marked
		while (expected.hasNext()) {
			final IRodinFile expFile = expected.next();
			assertTrue("should have next: " + expFile, actual.hasNext());
			final IRodinFile actFile = actual.next();
			assertEquals("Bad file", expFile, actFile);
		}
	}

	public static void assertExportTable(IExportTable expected,
			IExportTable actual, List<IRodinFile> files) {
		for (IRodinFile file : files) {
			assertExports(expected.get(file), actual.get(file));
		}
	}

	public static void assertExports(Set<IDeclaration> expected,
			Set<IDeclaration> actual) {

		assertEquals("Bad exports.", expected, actual);
	}

	public static void assertFileTable(IFileTable expected, IFileTable actual,
			List<IRodinFile> files) {
		for (IRodinFile file : files) {
			assertSameElements(expected.get(file), actual.get(file),
					"file table");
		}
	}

	public static void assertNameTable(INameTable expected, INameTable actual,
			List<String> names) {
		for (String name : names) {
			assertSameElements(expected.getDeclarations(name), actual
					.getDeclarations(name), "name table");
		}
	}

	public static <T> void assertPredecessors(final List<T> predecessors,
			T... preds) {
		assertEquals("Bad predecessors length", preds.length, predecessors
				.size());
		for (T pred : preds) {
			assertTrue("Predecessors should contain " + pred, predecessors
					.contains(pred));
		}
	}

	public static <T> T[] makeArray(T... elements) {
		return elements;
	}
}
