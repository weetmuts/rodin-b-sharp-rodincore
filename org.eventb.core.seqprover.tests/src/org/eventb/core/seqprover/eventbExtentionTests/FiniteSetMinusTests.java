package org.eventb.core.seqprover.eventbExtentionTests;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Tactics;

/**
 * Unit tests for the Finite of intersection reasoner {@link FiniteInter}
 * 
 * @author htson
 */
public class FiniteSetMinusTests extends AbstractEmptyInputReasonerTests {

	String P1 = "(x = 2) ⇒ finite(S ∖ {0 ↦ 3})";

	String P2 = "∀x· x = 2 ⇒ finite(S ∖ {0 ↦ 3})";

	String P3 = "finite(S ∖ {0 ↦ 3})";

	String resultP3Goal = "{S=ℙ(ℤ×ℤ)}[][][⊤] |- finite(S)";
	
	protected List<IPosition> getPositions(Predicate predicate) {
		return Tactics.finiteSetMinusGetPositions(predicate);
	}

	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.finiteSetMinus";
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
				P3, "",
		};
	}

}
