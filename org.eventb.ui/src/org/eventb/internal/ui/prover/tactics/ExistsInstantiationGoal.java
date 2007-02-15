package org.eventb.internal.ui.prover.tactics;

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.ast.IPosition;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.eventbExtensions.Lib;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.ui.prover.DefaultTacticProvider;

public class ExistsInstantiationGoal extends DefaultTacticProvider {

	private List<IPosition> positions;

	@Override
	public ITactic getTactic(IProofTreeNode node, Predicate hyp,
			IPosition position, String[] inputs) {
		return Tactics.exI(inputs);
	}

	@Override
	public List<IPosition> getApplicablePositions(IProofTreeNode node,
			Predicate hyp, String input) {
		if (node != null && Tactics.exI_applicable(node.getSequent().goal())) {
			internalGetPositions(node.getSequent().goal());
			return positions;
		}
		return null;
	}

	private void internalGetPositions(Predicate goal) {
		positions = new ArrayList<IPosition>();
		if (Lib.isExQuant(goal))
			positions.add(IPosition.ROOT);
	}

}
