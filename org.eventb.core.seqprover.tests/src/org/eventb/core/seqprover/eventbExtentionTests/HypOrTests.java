/*******************************************************************************
 * Copyright (c) 2007, 2012 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *******************************************************************************/
package org.eventb.core.seqprover.eventbExtentionTests;

import org.eventb.core.seqprover.IReasonerInput;
//import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.reasonerExtentionTests.AbstractReasonerTests;
import org.eventb.core.seqprover.reasonerInputs.EmptyInput;
import org.eventb.core.seqprover.tests.TestLib;

//import com.b4free.rodin.core.B4freeCore;

public class HypOrTests extends AbstractReasonerTests {

	private static final IReasonerInput input = new EmptyInput();

	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.hypOr";
	}

	@Override
	public SuccessfullReasonerApplication[] getSuccessfulReasonerApplications() {
		return new SuccessfullReasonerApplication[] {
				new SuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2 ∨ x = 1 ∨ x = 3 "), input),
				new SuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 1 ∨ x = 2 ∨ x = 3 "), input),
				new SuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2 ∨ x = 3 ∨ x = 1 "), input)				
		};
	}

	@Override
	public UnsuccessfullReasonerApplication[] getUnsuccessfullReasonerApplications() {
		return new UnsuccessfullReasonerApplication[] {
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2"), input),
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2 "), input,
						"Goal is not a disjunctive predicate"),			
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2 ∨ x = 4 ∨ x = 3"), input),
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2 ∨ x = 4 ∨ x = 3 "), input,
						"Hypotheses contain no disjunct in goal")
		};
	}

//	@Override
//	public ITactic getJustDischTactic() {
//		return B4freeCore.externalPP(false);
//	}

}
