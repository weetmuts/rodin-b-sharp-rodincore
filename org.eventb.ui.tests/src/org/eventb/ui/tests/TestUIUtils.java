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

package org.eventb.ui.tests;

import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.EventBUtils;
import org.eventb.internal.ui.eventbeditor.EventBContextEditor;
import org.eventb.internal.ui.eventbeditor.editpage.EditPage;
import org.eventb.ui.tests.utils.EventBUITest;
import org.eventb.core.IMachineFile;
import org.eventb.core.IEvent;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IAxiom;
import org.eventb.core.IConstant;
import org.eventb.core.EventBAttributes;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.RodinDBException;

/**
 * Tests on UI utils
 * 
 * @author Nicolas Beauger
 * 
 */

public class TestUIUtils extends EventBUITest {

	private static final String eventNamePrefix = "event";
	private static final String eventLabelPrefix = "evt";
	private static final String axiomNamePrefix = "axiom";
	private static final String axiomLabelPrefix = "axm";
	private static final String constantNamePrefix = "constant";
	private static final String constantIdentifierPrefix = "cst";

	protected static IMachineFile m0;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		m0 = createMachine("m0");
		m0.save(null, true);
		assertNotNull("m0 should be created successfully ", m0);
	}

	static void assertFreeIndex(IInternalElementType<?> type,
			IAttributeType.String attributeType, String prefix,
			String freeIndexExpected) throws RodinDBException,
			IllegalStateException {

		final String freeIndexActual = UIUtils.getFreePrefixIndex(m0, type,
				attributeType, prefix);

		if (freeIndexActual != null) {
			final String kindOfIndex;
			if (attributeType == null) {
				kindOfIndex = "child name";
			} else {
				kindOfIndex = "attribute";
			}
			assertEquals("Incorrect free " + kindOfIndex + " index ",
					freeIndexExpected, freeIndexActual);
		}
	}

	public void testGetFreeIndexNameFirst() throws RodinDBException {
		// Currently, there are no elements, so the free index for a name
		// should be 1
		assertFreeIndex(IEvent.ELEMENT_TYPE, null, eventNamePrefix, "1");
	}

	public void testGetFreeIndexNameSecond() throws RodinDBException {
		// Create 1 event with internal name "event1" and label "evt1".
		createNEvents(m0, eventNamePrefix, eventLabelPrefix, 1, 1);
		// There is "event1" so the free index starting from 1 is 2.
		assertFreeIndex(IEvent.ELEMENT_TYPE, null, eventNamePrefix, "2");
	}

	public void testGetFreeIndexNameThird() throws RodinDBException {
		createNEvents(m0, eventNamePrefix, eventLabelPrefix, 2, 1);
		// There are "event1" and "event2" so the free index
		// starting from 1 is 3.
		assertFreeIndex(IEvent.ELEMENT_TYPE, null, eventNamePrefix, "3");
	}

	public void testGetFreeIndexNameWithHoles() throws RodinDBException {
		// create event with internal name "event314" and label "evt314"
		createNEvents(m0, eventNamePrefix, eventLabelPrefix, 1, 314);

		// highest name index is 314 so the free index should now be 315.
		assertFreeIndex(IEvent.ELEMENT_TYPE, null, eventNamePrefix, "315");

	}

	public void testGetFreeIndexAttributeFirst() throws RodinDBException {
		// no axiom has been created yet so it should return index 1
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "1");
	}

	public void testGetFreeIndexAttributeDifferentType()
			throws RodinDBException {
		// create events with a label attribute
		createNEvents(m0, eventNamePrefix, eventLabelPrefix, 10, 314);

		// as axioms are of a different type, the research for axioms
		// with the same label prefix should return index 1
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				eventLabelPrefix, "1");

	}

	public void testGetFreeIndexAttributeSecond() throws RodinDBException {
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, 1);

		// an axiom has been created with label index 1
		// so next available index should be 2
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "2");
	}

	public void testGetFreeIndexAttributeManyExisting() throws RodinDBException {
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 100, 31);

		// many axioms have been created up to label index 130
		// so next available index should be 131
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "131");
	}

	public void testGetFreeIndexAttributeDifferentAttribute()
			throws RodinDBException {
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 100, 31);

		// many axioms have been created up to label index 130
		// but no predicate attribute has been set
		// so next available predicate index
		assertFreeIndex(IAxiom.ELEMENT_TYPE,
				EventBAttributes.PREDICATE_ATTRIBUTE, "this axiom is false",
				"1");
	}

	public void testBigIndexes() throws RodinDBException {
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, Long
				.parseLong("31415926"));
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "31415927");

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, Long
				.parseLong("314159265"));
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "314159266");

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1,
				Integer.MAX_VALUE);
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, Long
						.toString(((long) (Integer.MAX_VALUE)) + 1));

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, Long
				.parseLong("3141592653"));
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "3141592654");

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, Long
				.parseLong("31415926535"));
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "31415926536");

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1, Long
				.parseLong("314159265358979323"));
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, "314159265358979324");

		m0.clear(true, null);
		createNAxioms(m0, axiomNamePrefix, axiomLabelPrefix, 1,
				Long.MAX_VALUE - 1);
		assertFreeIndex(IAxiom.ELEMENT_TYPE, EventBAttributes.LABEL_ATTRIBUTE,
				axiomLabelPrefix, Long.toString(Long.MAX_VALUE));
	}

	public void testIndexesMaxLong() throws RodinDBException {
		final IConstant cst = createInternalElement(m0, IConstant.ELEMENT_TYPE,
				constantNamePrefix);
		cst.setIdentifierString(constantIdentifierPrefix
				+ Long.toString(Long.MAX_VALUE), null);

		try {
			UIUtils.getFreePrefixIndex(m0, IConstant.ELEMENT_TYPE,
					EventBAttributes.IDENTIFIER_ATTRIBUTE,
					constantIdentifierPrefix);
			fail("should have raised an exception");
		} catch (IllegalStateException e) {
			assertEquals("bad exception message for max long",
					"max value for index reached", e.getMessage());
		}
	}

	public void testIndexesTooBig() throws RodinDBException {
		final IConstant cst = createInternalElement(m0, IConstant.ELEMENT_TYPE,
				constantNamePrefix);
		cst.setIdentifierString(constantIdentifierPrefix
				+ "314159265358979323846264338327950288419"
				+ "716939937510582097494459230781640628620"
				+ "89986280348253421170679821480865132823", null);
		try {
			UIUtils.getFreePrefixIndex(m0, IConstant.ELEMENT_TYPE,
					EventBAttributes.IDENTIFIER_ATTRIBUTE,
					constantIdentifierPrefix);
			// should raise exception
			fail("should have raised an exception");
		} catch (IllegalStateException e) {
			assertEquals("bad exception message for max long",
					"number to parse is too big", e.getMessage());
		}
	}

	// CALLING THE CALLING METHODS //
	public void testGetFreeIndexCallingMethods() throws RodinDBException {
		String freeIndexFound;
		EditPage editPage = new EditPage();
		EventBContextEditor editor = (EventBContextEditor) editPage.getEditor();

		freeIndexFound = UIUtils.getFreeElementLabelIndex(editor, m0,
				IAxiom.ELEMENT_TYPE, axiomLabelPrefix);
		assertEquals("incorrect free element label index", "1", freeIndexFound);

		freeIndexFound = UIUtils.getFreeElementIdentifierIndex(m0,
				ICarrierSet.ELEMENT_TYPE, "set");
		assertEquals("incorrect free element identifier index", "1",
				freeIndexFound);

		freeIndexFound = EventBUtils.getFreeChildNameIndex(m0,
				ICarrierSet.ELEMENT_TYPE, "internal_element");
		assertEquals("incorrect free element identifier index", "1",
				freeIndexFound);
	}

	/**
	 * Ensures that the given prefix can look like a regular expression, in
	 * which case meta-characters are ignored.
	 */
	public void testRegexPrefix() throws Exception {
		createInternalElement(m0, IConstant.ELEMENT_TYPE, "cst+1");
		assertFreeIndex(IConstant.ELEMENT_TYPE, null, "cst+", "2");
	}

	/**
	 * Ensures that the whole prefix of existing elements is taken into account
	 * (no partial match).
	 */
	public void testLongerPrefix() throws Exception {
		createInternalElement(m0, IConstant.ELEMENT_TYPE, "foo_cst1");
		assertFreeIndex(IConstant.ELEMENT_TYPE, null, "cst", "1");
	}

	/**
	 * Ensures that the whole suffix of existing elements is taken into account
	 * (no partial match).
	 */
	public void testLongerSuffix() throws Exception {
		createInternalElement(m0, IConstant.ELEMENT_TYPE, "cst1a");
		assertFreeIndex(IConstant.ELEMENT_TYPE, null, "cst", "1");
	}

}
