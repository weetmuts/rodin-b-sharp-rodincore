package org.eventb.core.seqprover.eventbExtentionTests;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.internal.core.seqprover.eventbExtensions.rewriters.RanDistLeftRewrites;

/**
 * Unit tests for the Range distribution left Rewrites reasoner
 * {@link RanDistLeftRewrites}
 * 
 * @author htson
 */
public class RanDistLeftTests extends AbstractManualRewriterTests {

	// (p \/ ... \/ q) |> s == (p |> s) \/ ... \/ (q |> s)
	String P1 = "(p ∪ q ∪ r) ▷ {1, x} = {x ↦ x}";

	String resultP1 = "(p ▷ {1,x})∪(q ▷ {1,x})∪(r ▷ {1,x})={x ↦ x}";

	String P2 = "(1 = x) ⇒ {x ↦ x} = (p ∪ q ∪ r) ▷ {1, x}";

	String resultP2 = "1=x⇒{x ↦ x}=(p ▷ {1,x})∪(q ▷ {1,x})∪(r ▷ {1,x})";

	String P3 = "∀x·x = 0 ⇒ (p ∪ q ∪ r) ▷ {1, x} = {x ↦ x}";

	String resultP3 = "∀x·x=0⇒(p ▷ {1,x})∪(q ▷ {1,x})∪(r ▷ {1,x})={x ↦ x}";


	// (p /\ ... /\ q) |> s == (p |> s) /\ ... /\ (q |> s)
	String P4 = "(p ∩ q ∩ r) ▷ {1, x} = {x ↦ x}";

	String resultP4 = "(p ▷ {1,x})∩(q ▷ {1,x})∩(r ▷ {1,x})={x ↦ x}";

	String P5 = "(1 = x) ⇒ {x ↦ x} = (p ∩ q ∩ r) ▷ {1, x}";

	String resultP5 = "1=x⇒{x ↦ x}=(p ▷ {1,x})∩(q ▷ {1,x})∩(r ▷ {1,x})";

	String P6 = "∀x·x = 0 ⇒ (p ∩ q ∩ r) ▷ {1, x} = {x ↦ x}";

	String resultP6 = "∀x·x=0⇒(p ▷ {1,x})∩(q ▷ {1,x})∩(r ▷ {1,x})={x ↦ x}";


	// (p \/ ... \/ q) |>> s == (p |>> s) \/ ... \/ (q |>> s)
	String P7 = "(p ∪ q ∪ r) ⩥ {1, x} = {x ↦ x}";

	String resultP7 = "(p ⩥ {1,x})∪(q ⩥ {1,x})∪(r ⩥ {1,x})={x ↦ x}";

	String P8 = "(1 = x) ⇒ {x ↦ x} = (p ∪ q ∪ r) ⩥ {1, x}";

	String resultP8 = "1=x⇒{x ↦ x}=(p ⩥ {1,x})∪(q ⩥ {1,x})∪(r ⩥ {1,x})";

	String P9 = "∀x·x = 0 ⇒ (p ∪ q ∪ r) ⩥ {1, x} = {x ↦ x}";

	String resultP9 = "∀x·x=0⇒(p ⩥ {1,x})∪(q ⩥ {1,x})∪(r ⩥ {1,x})={x ↦ x}";


	// (p /\ ... /\ q) |>> s == (p |>> s) /\ ... /\ (q |>> s)
	String P10 = "(p ∩ q ∩ r) ⩥ {1, x} = {x ↦ x}";

	String resultP10 = "(p ⩥ {1,x})∩(q ⩥ {1,x})∩(r ⩥ {1,x})={x ↦ x}";

	String P11 = "(1 = x) ⇒ {x ↦ x} = (p ∩ q ∩ r) ⩥ {1, x}";

	String resultP11 = "1=x⇒{x ↦ x}=(p ⩥ {1,x})∩(q ⩥ {1,x})∩(r ⩥ {1,x})";

	String P12 = "∀x·x = 0 ⇒ (p ∩ q ∩ r) ⩥ {1, x} = {x ↦ x}";

	String resultP12 = "∀x·x=0⇒(p ⩥ {1,x})∩(q ⩥ {1,x})∩(r ⩥ {1,x})={x ↦ x}";

	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.ranDistLeftRewrites";
	}

	protected List<IPosition> getPositions(Predicate predicate) {
		return Tactics.ranDistLeftGetPositions(predicate);
	}

	@Override
	protected SuccessfulTest[] getSuccessfulTests() {
		return new SuccessfulTest[] {
				new SuccessfulTest(P1, "0", resultP1),
				new SuccessfulTest(P2, "1.1", resultP2),
				new SuccessfulTest(P3, "1.1.0", resultP3),
				new SuccessfulTest(P4, "0", resultP4),
				new SuccessfulTest(P5, "1.1", resultP5),
				new SuccessfulTest(P6, "1.1.0", resultP6),
				new SuccessfulTest(P7, "0", resultP7),
				new SuccessfulTest(P8, "1.1", resultP8),
				new SuccessfulTest(P9, "1.1.0", resultP9),
				new SuccessfulTest(P10, "0", resultP10),
				new SuccessfulTest(P11, "1.1", resultP11),
				new SuccessfulTest(P12, "1.1.0", resultP12)
		};
	}

	@Override
	protected String[] getUnsuccessfulTests() {
		return new String[] {
				P1, "1",
				P2, "1.0",
				P3, "1.0.1",
				P4, "1",
				P5, "1.0",
				P6, "1.0.1",
				P7, "1",
				P8, "1.0",
				P9, "1.0.1",
				P10, "1",
				P11, "1.0",
				P12, "1.0.1"
		};
	}

	@Override
	protected String[] getTestGetPositions() {
		return new String[] {
				P1, "0",
				P2, "1.1",
				P3, "1.1.0",
				P4, "0",
				P5, "1.1",
				P6, "1.1.0",
				P7, "0",
				P8, "1.1",
				P9, "1.1.0",
				P10, "0",
				P11, "1.1",
				P12, "1.1.0"
		};
	}

}
