package org.eventb.core.seqprover.eventbExtentionTests;

import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.reasonerExtentionTests.AbstractReasonerTests;
import org.eventb.core.seqprover.reasonerInputs.EmptyInput;
import org.eventb.core.seqprover.tests.TestLib;

//import com.b4free.rodin.core.B4freeCore;

public class HypTests extends AbstractReasonerTests {

	private static final IReasonerInput input = new EmptyInput();

	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.hyp";
	}

	@Override
	public SuccessfullReasonerApplication[] getSuccessfulReasonerApplications() {
		return new SuccessfullReasonerApplication[] {
				new SuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 1 "), input,
						"[]"),
				new SuccessfullReasonerApplication(TestLib
						.genSeq(" 1∈P |- 1∈P "), input,
						"[]")				
		};
	}

	@Override
	public UnsuccessfullReasonerApplication[] getUnsuccessfullReasonerApplications() {
		return new UnsuccessfullReasonerApplication[] {
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" x = 1 |- x = 2"), input,
						"Goal not in hypotheses"),
				new UnsuccessfullReasonerApplication(TestLib
						.genSeq(" 1∈P |- 2∈P "), input,
						"Goal not in hypotheses")
		};
	}

//	@Override
//	public ITactic getJustDischTactic() {
//		return B4freeCore.externalPP(false);
//	}

}
