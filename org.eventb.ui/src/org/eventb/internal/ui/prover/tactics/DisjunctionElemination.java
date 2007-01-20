package org.eventb.internal.ui.prover.tactics;

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.ui.prover.ITacticProvider;

public class DisjunctionElemination implements ITacticProvider {

	public ITactic getTactic(IProofTreeNode node, Predicate hyp,
			IPosition position, String[] inputs) {
		return Tactics.disjE(hyp);
	}

	public List<IPosition> getApplicablePositions(IProofTreeNode node,
			Predicate hyp, String input) {
		if (Tactics.disjE_applicable(hyp))
			return new ArrayList<IPosition>();
		return null;
	}

}
