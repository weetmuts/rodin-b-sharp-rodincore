/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.testscpog;


import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eventb.core.IContext;
import org.eventb.core.ISCContext;
import org.eventb.internal.core.protosc.SCCore;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class TestContextSC_1 extends TestCase {
	
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	
	IRodinProject rodinProject;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RodinCore.create(workspace.getRoot()).open(null);  // TODO temporary kludge
		IProject project = workspace.getRoot().getProject("testsc");
		project.create(null);
		project.open(null);
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] {RodinCore.NATURE_ID});
		project.setDescription(description, null);
		rodinProject = RodinCore.create(project);
		rodinProject.open(null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		rodinProject.getProject().delete(true, true, null);
	}

	/*
	 * Test method for 'org.eventb.internal.core.protosc.ContextSC.run()'
	 * This test only checks whether the database works correctly
	 * with the context static checker
	 */
	public void testRun() throws Exception {
		IContext context = createContextOne();
		
		/*IFile scFile =*/ workspace.getRoot().getFile(context.getResource().getFullPath().removeFileExtension().addFileExtension("bcc"));
		/*String scName =*/ context.getPath().removeFileExtension().addFileExtension("bcc").toString();
		
		ISCContext scContext = (ISCContext) rodinProject.createRodinFile("one.bcc", true, null);
		
//		SCContext scContext = (SCContext) RodinCore.create(scName);
//		((IRodinProject) scContext.getRodinProject()).createRodinFile(scContext.getElementName(), true, null);
		scContext.open(null);
		
		SCCore.runContextSC(context, scContext);
		
		scContext.save(null, true);

	}
	
	private IContext createContextOne() throws RodinDBException {
		IRodinFile rodinFile = rodinProject.createRodinFile("one.buc", true, null);
		TestUtil.addCarrierSets(rodinFile, TestUtil.makeList("S1", "S2"));
		TestUtil.addConstants(rodinFile, TestUtil.makeList("C1", "C2", "C3", "F1"));
		TestUtil.addAxioms(rodinFile, TestUtil.makeList("A1", "A2", "A3", "A4"), TestUtil.makeList("C1∈S1", "F1∈S1↔S2", "C2∈F1[{C1}]", "C3=1"), null);
		TestUtil.addTheorems(rodinFile, TestUtil.makeList("T1"), TestUtil.makeList("C3>0 ⇒ (∃ x · x ∈ ran(F1))"), null);
		rodinFile.save(null, true);
		return (IContext) rodinFile;
	}

}
