/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.tests.sc;

import org.eventb.core.IContextFile;
import org.eventb.core.ISCContextFile;
import org.eventb.core.ISCInternalContext;


/**
 * @author Stefan Hallerstede
 *
 */
public class TestExtendsContext extends BasicTest {

	public void testFetchCarrierSet_1() throws Exception {
		IContextFile abs = createContext("abs");
		addCarrierSets(abs, makeSList("S"));
		
		abs.save(null, true);
		
		runSC(abs);
		
		IContextFile con = createContext("con");
		addContextExtends(con, "abs");
		
		con.save(null, true);
		
		runSC(con);
		
		ISCContextFile file = con.getSCContextFile();
		
		ISCInternalContext[] contexts = getInternalContexts(file, 1);
		
		containsCarrierSets(contexts[0], "S");
	}

	public void testFetchCarrierSet_2() throws Exception {
		IContextFile abs = createContext("abs");
		addCarrierSets(abs, makeSList("S1", "S2"));
		
		abs.save(null, true);
		
		runSC(abs);
		
		IContextFile con = createContext("con");
		addContextExtends(con, "abs");
		
		con.save(null, true);
		
		runSC(con);
		
		ISCContextFile file = con.getSCContextFile();
		
		ISCInternalContext[] contexts = getInternalContexts(file, 1);
		
		containsCarrierSets(contexts[0], "S1", "S2");
	}
	
	public void testFetchCarrierSet_3() throws Exception {
		IContextFile abs1 = createContext("abs1");
		addCarrierSets(abs1, makeSList("S11", "S12"));
		
		abs1.save(null, true);
		
		runSC(abs1);
		
		IContextFile abs2 = createContext("abs2");
		addCarrierSets(abs2, makeSList("S11", "S22"));
		
		abs2.save(null, true);
		
		runSC(abs2);
		
		IContextFile con = createContext("con");
		addContextExtends(con, "abs1");
		addContextExtends(con, "abs2");
		
		con.save(null, true);
		
		runSC(con);
		
		ISCContextFile file = con.getSCContextFile();
		
		getInternalContexts(file, 0);
		
	}

	public void testFetchCarrierSet_4() throws Exception {
		IContextFile abs1 = createContext("abs1");
		addCarrierSets(abs1, makeSList("S11", "S12"));
		
		abs1.save(null, true);
		
		runSC(abs1);
		
		IContextFile abs2 = createContext("abs2");
		addCarrierSets(abs2, makeSList("S21", "S22"));
		
		abs2.save(null, true);
		
		runSC(abs2);
		
		IContextFile con = createContext("con");
		addContextExtends(con, "abs1");
		addContextExtends(con, "abs2");
		
		con.save(null, true);
		
		runSC(con);
		
		ISCContextFile file = con.getSCContextFile();
		
		ISCInternalContext[] contexts = getInternalContexts(file, 2);
		
		containsCarrierSets(contexts[0], "S11", "S12");

		containsCarrierSets(contexts[1], "S21", "S22");
	}
	
	public void testFetchCarrierSet_5() throws Exception {
		IContextFile abs1 = createContext("abs1");
		addCarrierSets(abs1, makeSList("S11", "S12"));
		
		abs1.save(null, true);
		
		runSC(abs1);
		
		IContextFile abs2 = createContext("abs2");
		addCarrierSets(abs2, makeSList("S11", "S22"));
		
		abs2.save(null, true);
		
		IContextFile abs3 = createContext("abs3");
		addCarrierSets(abs3, makeSList("S31", "S32"));
		
		abs3.save(null, true);
		
		runSC(abs3);
		
		IContextFile con = createContext("con");
		addContextExtends(con, "abs1");
		addContextExtends(con, "abs2");
		addContextExtends(con, "abs3");
		
		con.save(null, true);
		
		runSC(con);
		
		ISCContextFile file = con.getSCContextFile();
		
		ISCInternalContext[] contexts = getInternalContexts(file, 1);
		
		containsCarrierSets(contexts[0], "S31", "S32");
		
	}

}
