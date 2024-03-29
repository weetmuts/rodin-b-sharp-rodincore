/*******************************************************************************
 * Copyright (c) 2010, 2022 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.seqprover.eventbExtensionTests;

import static org.eventb.core.ast.FormulaFactory.makePosition;

import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IReasonerInput;
import org.eventb.core.seqprover.UntranslatableException;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.core.seqprover.tests.TestLib;
import org.eventb.internal.core.seqprover.eventbExtensions.AbstractManualInference;
import org.junit.Test;

/**
 * @author Nicolas Beauger
 *
 */
public class DTDistinctCaseTests extends AbstractManualReasonerTests {

	public DTDistinctCaseTests() {
		super(DT_FAC);
	}
	
	@Override
	public String getReasonerID() {
		return "org.eventb.core.seqprover.dtDistinctCase";
	}

	// Make an input from a position (in the goal)
	protected IReasonerInput input(String position) {
		return new AbstractManualInference.Input(null, makePosition(position));
	}

	// Make an input from a position in a given hypothesis
	protected IReasonerInput input(String hypothesis, String position) {
		return new AbstractManualInference.Input(TestLib.genPred(ff.makeTypeEnvironment(), hypothesis),
				makePosition(position));
	}

	public void assertReasonerSuccess(String sequent, IReasonerInput input, String... newSequentImages)
			throws UntranslatableException {
		assertReasonerSuccess(TestLib.genSeq(sequent, ff), input, newSequentImages);
	}

	@Override
	protected List<IPosition> getPositions(Predicate predicate) {
		return Tactics.dtDCInducGetPositions(predicate);
	}

	@Override
	protected String[] getTestGetPositions() {
		return new String [] {
				"∀ l⦂SD · l=l1", "1.1",
				"∀ l ⦂ SD · destr1(l) = 0", "",
		};
	}

	@Test
	public void success() throws Exception {
		successGeneric();
		successSpecific();
	}

	/*
	 * Generic test cases that behave the same way for the distinct case and
	 * induction tactics.
	 */
	protected void successGeneric() throws Exception {
		// Applied to the goal
		assertReasonerSuccess("|- ∀ l⦂SD · l=l1", input("1.1"),
				"{l1=SD}[][][l1=cons0] |- ∀ l⦂SD · l=l1",
				"{l1=SD; p_destr1=ℤ}[][][l1=cons1(p_destr1)] |- ∀ l⦂SD · l=l1",
				"{l1=SD; p_destr2_0=ℤ; p_destr2_1=ℤ}[][][l1=cons2(p_destr2_0, p_destr2_1)] |- ∀ l⦂SD · l=l1");
		// Applied to an hypothesis
		assertReasonerSuccess("l1∈SD |- ⊥", input("l1∈SD", "0"),
				"{l1=SD}[][][l1∈SD;;l1=cons0] |- ⊥",
				"{l1=SD; p_destr1=ℤ}[][][l1∈SD;;l1=cons1(p_destr1)] |- ⊥",
				"{l1=SD; p_destr2_0=ℤ; p_destr2_1=ℤ}[][][l1∈SD;;l1=cons2(p_destr2_0, p_destr2_1)] |- ⊥");
	}

	/*
	 * Specific test cases that behave differently for the distinct case and
	 * induction tactics.
	 */
	protected void successSpecific() throws Exception {
		// Applied to an inductive datatype
		assertReasonerSuccess("|- ∀ l⦂Induc(ℤ) · l=l1", input("1.1"),
				"{l1=Induc(ℤ)}[][][l1=ind0] |- ∀ l⦂Induc(ℤ) · l=l1",
				"{l1=Induc(ℤ); p_ind1_0=Induc(ℤ)}[][][l1=ind1(p_ind1_0)] |- ∀ l⦂Induc(ℤ) · l=l1",
				"{l1=Induc(ℤ); p_ind2_0=Induc(ℤ); p_ind2_1=Induc(ℤ)}[][][l1=ind2(p_ind2_0, p_ind2_1)] |- ∀ l⦂Induc(ℤ) · l=l1");
		// Applied to an inductive datatype in an hypothesis
		assertReasonerSuccess("l1∈Induc(ℕ) |- ⊥", input("l1∈Induc(ℕ)", "0"),
				"{l1=Induc(ℤ)}[][][l1∈Induc(ℕ);;l1=ind0] |- ⊥",
				"{l1=Induc(ℤ); p_ind1_0=Induc(ℤ)}[][][l1∈Induc(ℕ);;p_ind1_0∈Induc(ℕ);;l1=ind1(p_ind1_0)] |- ⊥",
				"{l1=Induc(ℤ); p_ind2_0=Induc(ℤ); p_ind2_1=Induc(ℤ)}[][][l1∈Induc(ℕ);;p_ind2_0∈Induc(ℕ);;"
						+ "p_ind2_1∈Induc(ℕ);;l1=ind2(p_ind2_0, p_ind2_1)] |- ⊥");
	}

	@Test
	public void failure() throws Exception {
		assertReasonerFailure("∀ l⦂SD · l=l1 |- ⊤", input("∀ l⦂SD · l=l1", "1.0"),
				"Inference " + getReasonerID() + " is not applicable for ∀l·l=l1 at position 1.0");
		assertReasonerFailure("|- ∀ l⦂SD · l=l1", input("1.0"),
				"Inference " + getReasonerID() + " is not applicable for ∀l·l=l1 at position 1.0");
		assertReasonerFailure("|- ∀ l ⦂ SD · destr1(l) = 0", input("1.0.0"),
				"Inference " + getReasonerID() + " is not applicable for ∀l·destr1(l)=0 at position 1.0.0");
	}

}
