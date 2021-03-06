/*******************************************************************************
 * Copyright (c) 2009, 2014 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.rodinp.core.tests.indexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.rodinp.core.RodinCore;
import org.rodinp.core.indexer.IOccurrenceKind;

/**
 * Unit tests for occurrence kinds
 * 
 * @author Laurent Voisin
 */
public class OccurrenceKindTests extends IndexTests {

	// Namespace for plugin.xml
	public static final String NAMESPACE = PLUGIN_ID;

	// Ids declared in plugin.xml
	public static final String TEST_KIND = "testKind";
	public static final String INVALID_KIND = "invalid.kind";

	// Id not declared in plugin.xml
	public static final String UNDECLARED_KIND = "undeclared";

	private static void assertExists(String id) {
		IOccurrenceKind kind = RodinCore.getOccurrenceKind(id);
		assertNotNull(kind);
		assertEquals(id, kind.getId());
	}

	private static void assertNotExists(String id) {
		IOccurrenceKind kind = RodinCore.getOccurrenceKind(id);
		assertNull(kind);
	}

	@Test
	public void testValidKind() {
		assertExists(NAMESPACE + "." + TEST_KIND);
	}

	@Test
	public void testInValidKind() {
		assertNotExists(TEST_KIND);

		assertNotExists(INVALID_KIND);
		assertNotExists(NAMESPACE + "." + INVALID_KIND);

		assertNotExists(UNDECLARED_KIND);
		assertNotExists(NAMESPACE + "." + UNDECLARED_KIND);
	}

}
