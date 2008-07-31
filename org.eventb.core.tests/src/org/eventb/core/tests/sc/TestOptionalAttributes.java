/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.tests.sc;

import org.eventb.core.EventBAttributes;
import org.eventb.core.IContextFile;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineFile;
import org.rodinp.core.IRodinFile;

/**
 * @author Stefan Hallerstede
 *
 */
public class TestOptionalAttributes extends BasicSCTestWithFwdConfig {
	
	private abstract class OptAttrTest<F extends IRodinFile> {
		protected F f;
		public abstract void createFile() throws Exception;
		public abstract void removeAttr() throws Exception;
		public void saveFile() throws Exception {
			f.save(null, true);
		}
		public abstract void checkAttr() throws Exception;
	}
	
	private abstract class MachineOptAttrTest extends OptAttrTest<IMachineFile> {

		@Override
		public void createFile() throws Exception {
			f = createMachine();
		}
		
		protected IEvent e() throws Exception {
			return f.getEvents()[0];
		}
		
	}
	
	
	private abstract class ContextOptAttrTest extends OptAttrTest<IContextFile> {

		@Override
		public void createFile() throws Exception {
			f = createContext();
		}
		
	}

	private IMachineFile createMachine() throws Exception {
		IMachineFile a = createMachine("abs");
		addInitialisation(a);
		addEvent(a, "e", 
				makeSList("a"), 
				makeSList("G"), makeSList("a∈ℤ"), 
				makeSList(), makeSList());
		IContextFile c = createContext("con");
		IMachineFile m = createMachine("mch");
		addMachineRefines(m, "abs");
		addMachineSees(m, "con");
		addVariables(m, "v");
		addInvariants(m, makeSList("I"), makeSList("v∈ℤ"));
		addTheorems(m, makeSList("T"), makeSList("⊤"));
		addVariant(m, "1");
		IEvent e = addEvent(m, "e", 
				makeSList("b"), 
				makeSList("G"), makeSList("b∈ℤ"), 
				makeSList("A"), makeSList("v≔b"));
		addEventRefines(e, "e");
		addEventWitnesses(e, makeSList("a"), makeSList("⊤"));
		addInitialisation(m, "v");
		
		a.save(null, true);
		c.save(null, true);
		m.save(null, true);
		
		return m;
	}
	
	private IContextFile createContext() throws Exception {
		IContextFile a = createContext("abs");
		IContextFile c = createContext("con");
		addContextExtends(c, "abs");
		addCarrierSets(c, "S");
		addConstants(c, "C");
		addAxioms(c, makeSList("A"), makeSList("C∈S"));
		addTheorems(c, makeSList("T"), makeSList("⊤"));
		
		a.save(null, true);
		c.save(null, true);
		
		return c;
	}
	
	/**
	 * precondition of proper test:
	 * check if machine is ok
	 */
	public void testMachine() throws Exception {
		IMachineFile m = createMachine();
		
		runBuilder();
		
		containsMarkers(m, false);
	}
	
	/**
	 * precondition of proper test:
	 * check if context is ok
	 */
	public void testContext() throws Exception {
		IContextFile c = createContext();
		
		runBuilder();
		
		containsMarkers(c, false);
	}
	
	private OptAttrTest<?>[] tests = new OptAttrTest[] {
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getRefinesClauses()[0],EventBAttributes.TARGET_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getRefinesClauses()[0].hasAttribute(EventBAttributes.TARGET_ATTRIBUTE));
					f.getRefinesClauses()[0].removeAttribute(EventBAttributes.TARGET_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getSeesClauses()[0], EventBAttributes.TARGET_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getSeesClauses()[0].hasAttribute(EventBAttributes.TARGET_ATTRIBUTE));
					f.getSeesClauses()[0].removeAttribute(EventBAttributes.TARGET_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getVariables()[0], EventBAttributes.IDENTIFIER_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getVariables()[0].hasAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE));
					f.getVariables()[0].removeAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getInvariants()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getInvariants()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					f.getInvariants()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getInvariants()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getInvariants()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					f.getInvariants()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getTheorems()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getTheorems()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					f.getTheorems()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getTheorems()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getTheorems()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					f.getTheorems()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getVariants()[0], EventBAttributes.EXPRESSION_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getVariants()[0].hasAttribute(EventBAttributes.EXPRESSION_ATTRIBUTE));
					f.getVariants()[0].removeAttribute(EventBAttributes.EXPRESSION_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getEvents()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getEvents()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					f.getEvents()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getEvents()[0], EventBAttributes.CONVERGENCE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getEvents()[0].hasAttribute(EventBAttributes.CONVERGENCE_ATTRIBUTE));
					f.getEvents()[0].removeAttribute(EventBAttributes.CONVERGENCE_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getEvents()[0], EventBAttributes.INHERITED_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getEvents()[0].hasAttribute(EventBAttributes.INHERITED_ATTRIBUTE));
					f.getEvents()[0].removeAttribute(EventBAttributes.INHERITED_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getParameters()[0], EventBAttributes.IDENTIFIER_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getParameters()[0].hasAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE));
					e().getParameters()[0].removeAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getGuards()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getGuards()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					e().getGuards()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getGuards()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getGuards()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					e().getGuards()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getActions()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getActions()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					e().getActions()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getActions()[0], EventBAttributes.ASSIGNMENT_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getActions()[0].hasAttribute(EventBAttributes.ASSIGNMENT_ATTRIBUTE));
					e().getActions()[0].removeAttribute(EventBAttributes.ASSIGNMENT_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getWitnesses()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getWitnesses()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					e().getWitnesses()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getWitnesses()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getWitnesses()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					e().getWitnesses()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			},
			new MachineOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(e().getRefinesClauses()[0], EventBAttributes.TARGET_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(e().getRefinesClauses()[0].hasAttribute(EventBAttributes.TARGET_ATTRIBUTE));
					e().getRefinesClauses()[0].removeAttribute(EventBAttributes.TARGET_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getExtendsClauses()[0], EventBAttributes.TARGET_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getExtendsClauses()[0].hasAttribute(EventBAttributes.TARGET_ATTRIBUTE));
					f.getExtendsClauses()[0].removeAttribute(EventBAttributes.TARGET_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getCarrierSets()[0], EventBAttributes.IDENTIFIER_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getCarrierSets()[0].hasAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE));
					f.getCarrierSets()[0].removeAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getConstants()[0], EventBAttributes.IDENTIFIER_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getConstants()[0].hasAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE));
					f.getConstants()[0].removeAttribute(EventBAttributes.IDENTIFIER_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getAxioms()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getAxioms()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					f.getAxioms()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getAxioms()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getAxioms()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					f.getAxioms()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getTheorems()[0], EventBAttributes.LABEL_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getTheorems()[0].hasAttribute(EventBAttributes.LABEL_ATTRIBUTE));
					f.getTheorems()[0].removeAttribute(EventBAttributes.LABEL_ATTRIBUTE, null);
				}
				
			},
			new ContextOptAttrTest() {

				@Override
				public void checkAttr() throws Exception {
					hasMarker(f.getTheorems()[0], EventBAttributes.PREDICATE_ATTRIBUTE);
				}

				@Override
				public void removeAttr() throws Exception {
					assertTrue(f.getTheorems()[0].hasAttribute(EventBAttributes.PREDICATE_ATTRIBUTE));
					f.getTheorems()[0].removeAttribute(EventBAttributes.PREDICATE_ATTRIBUTE, null);
				}
				
			}
	};
	
	public void test() throws Exception {
		
		for (OptAttrTest<?> test : tests) {
			test.createFile();
			test.removeAttr();
			test.saveFile();
			runBuilder();
			test.checkAttr();
		}
	}

}
