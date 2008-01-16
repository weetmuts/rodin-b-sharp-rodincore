package org.eventb.core.seqprover.eventbExtentionTests;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.internal.core.seqprover.eventbExtensions.FiniteDom;

/**
 * Unit tests for the Finite of domain of a relation reasoner {@link FiniteDom}
 * 
 * @author htson
 */
public class FiniteDomTests extends AbstractEmptyInputReasonerTests {

	String P1 = "(x = 2) ⇒ finite(dom({0 ↦ (3 ↦ 2),1 ↦ (3 ↦ x),1 ↦ (2 ↦ 3)}))";

	String P2 = "∀x· x = 2 ⇒ finite(dom({0 ↦ (3 ↦ 2),1 ↦ (3 ↦ x),1 ↦ (2 ↦ 3)}))";

	String P3 = "finite(dom({0 ↦ (3 ↦ 2),1 ↦ (3 ↦ x),1 ↦ (2 ↦ 3)}))";

	String resultP3Goal = "{x=ℤ}[][][⊤] |- finite({0 ↦ (3 ↦ 2),1 ↦ (3 ↦ x),1 ↦ (2 ↦ 3)})";
	
	protected List<IPosition> getPositions(Predicate predicate) {
		return Tactics.finiteDomGetPositions(predicate);
	}

	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.finiteDom";
	}

	protected SuccessfulTest[] getSuccessfulTests() {
		return new SuccessfulTest[] {
				// P3 in goal
				new SuccessfulTest(" ⊤ |- " + P3, resultP3Goal)
		};
	}

	protected String[] getUnsuccessfulTests() {
		return new String[] {
				// P1 in goal
				" ⊤ |- " + P1,
				// P2 in goal
				" ⊤ |- " + P2
		};
	}

	@Override
	protected String[] getTestGetPositions() {
		return new String[] {
				P1, "",
				P2, "",
				P3, "ROOT",
		};
	}

	// Commented out, makes the tests NOT succeed
	// TODO: Verify with another external prover
//	@Override
//	public ITactic getJustDischTactic() {
//		return B4freeCore.externalPP(false);
//	}

}
