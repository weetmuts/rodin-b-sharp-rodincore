/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.core.tests.builder;

import org.eclipse.core.runtime.CoreException;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;

/**
 * @author Stefan Hallerstede
 *
 */
public class MBuilderTest extends AbstractBuilderTest {

	private IRodinProject project;
	
	public MBuilderTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		project = createRodinProject("P");
		ToolTrace.flush();
	}
	
	protected void tearDown() throws Exception {
		project.getProject().delete(true, true, null);
	}

	private void runBuilder(String expectedTrace) throws CoreException {
		super.runBuilder(project, expectedTrace);
	}
	
	/**
	 * Ensures that extractors and tools are run when a file is created.
	 */
	public void testOneBuild() throws Exception {
		IRodinFile mch = createRodinFile("P/x.mch");
		createData(mch, "one");
		mch.save(null, true);
		
		SCTool.RUN_SC = true;
		SCTool.SHOW_CLEAN = true;
		SCTool.SHOW_EXTRACT = true;
		SCTool.SHOW_RUN = true;
		
		POTool.RUN_PO = true;
		POTool.SHOW_CLEAN = true;
		POTool.SHOW_EXTRACT = true;
		POTool.SHOW_RUN = true;
		
		runBuilder(
				"MSC extract /P/x.mch\n" + 
				"MSC run /P/x.msc\n" + 
				"MPO extract /P/x.msc\n" + 
				"MPO run /P/x.po"
		);
		
		IRodinFile scMch = getRodinFile("P/x.msc");
		assertContents("Invalid contents of checked machine",
				"x.msc\n" +
				"  data: one",
				scMch);
	}
	
	/**
	 * Ensures that extractors and tools are run when a file is created.
	 */
	public void testGraphBuild() throws Exception {
		IRodinFile ctx = createRodinFile("P/x.ctx");
		createData(ctx, "cone");
		ctx.save(null, true);
		
		IRodinFile cty = createRodinFile("P/y.ctx");
		createDependency(cty, "x");
		createData(cty, "ctwo");
		cty.save(null, true);		
		
		IRodinFile ctz = createRodinFile("P/z.ctx");
		createData(ctz, "cthree");
		ctz.save(null, true);
	
		IRodinFile mca = createRodinFile("P/a.mch");
		createDependency(mca, "x");
		createData(mca, "mone");
		mca.save(null, true);
		
		IRodinFile mcb = createRodinFile("P/b.mch");
		createDependency(mcb, "y");
		createReference(mcb, "a");
		createData(mcb, "mtwo");
		mcb.save(null, true);
		
		IRodinFile mcc = createRodinFile("P/c.mch");
		createDependency(mcc, "y");
		createReference(mcc, "b");
		createData(mcc, "mthree");
		mcc.save(null, true);
		
		IRodinFile mcd = createRodinFile("P/d.mch");
		createDependency(mcd, "y");
		createDependency(mcd, "z");
		createReference(mcd, "c");
		createData(mcd, "mfour");
		mcd.save(null, true);
		
		SCTool.RUN_SC = true;
		SCTool.SHOW_CLEAN = false;
		SCTool.SHOW_EXTRACT = false;
		SCTool.SHOW_RUN = true;
		
		POTool.RUN_PO = true;
		POTool.SHOW_CLEAN = false;
		POTool.SHOW_EXTRACT = false;
		POTool.SHOW_RUN = true;	
		
		runBuilder(
				"CSC run /P/z.csc\n" + 
				"CPO run /P/z.po\n" + 
				"CSC run /P/x.csc\n" + 
				"CPO run /P/x.po\n" + 
				"CSC run /P/y.csc\n" + 
				"CPO run /P/y.po\n" + 
				"MSC run /P/a.msc\n" + 
				"MPO run /P/a.po\n" + 
				"MSC run /P/b.msc\n" + 
				"MPO run /P/b.po\n" + 
				"MSC run /P/c.msc\n" + 
				"MPO run /P/c.po\n" + 
				"MSC run /P/d.msc\n" + 
				"MPO run /P/d.po"
		);
		
		IRodinFile scMch = getRodinFile("P/d.msc");
		assertContents("Invalid contents of checked machine",
				"d.msc\n" + 
				"  data: mfour\n" + 
				"  data: mthree\n" + 
				"  data: mtwo\n" + 
				"  data: mone\n" + 
				"  data: cone\n" + 
				"  data: ctwo\n" + 
				"  data: cone\n" + 
				"  data: ctwo\n" + 
				"  data: cone\n" + 
				"  data: ctwo\n" + 
				"  data: cone\n" + 
				"  data: cthree",
				scMch);
	}	

}
