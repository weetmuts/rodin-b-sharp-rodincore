package org.eventb.core.pm;

import java.util.Collection;
import java.util.HashSet;

import org.eventb.core.IPRSequent;
import org.eventb.core.prover.IProofTree;
import org.eventb.core.prover.IProofTreeNode;
import org.eventb.core.prover.sequent.Hypothesis;
import org.rodinp.core.RodinDBException;

public class ProofState {
	// TODO Add PO handle to replace IProverSequent
	private IPRSequent ps;
	private IProofTree pt;
	private IProofTreeNode current;
	private Collection<Hypothesis> cached;
	private Collection<Hypothesis> searched;
	
	public ProofState(IPRSequent ps) throws RodinDBException {
		this.ps = ps;
		pt = ps.makeProofTree();
		current = null;
		cached = new HashSet<Hypothesis>();
		searched = new HashSet<Hypothesis>();
	}
	
	public boolean isDischarged() {
		return pt.isDischarged();
	}
	
	public IPRSequent getPRSequent() {return ps;}
	
	public IProofTree getProofTree() {return pt;}
	
	public IProofTreeNode getCurrentNode() {
		return current;
	}
	
	public void setCurrentNode(IProofTreeNode newNode) {
		current = newNode;
	}
	
	public IProofTreeNode getNextPendingSubgoal(IProofTreeNode node) {
		IProofTreeNode subGoal = node.getFirstOpenDescendant();
		if (subGoal != null)
			return subGoal;
		return pt.getRoot().getFirstOpenDescendant();
	}

	public IProofTreeNode getNextPendingSubgoal() {
		return pt.getRoot().getFirstOpenDescendant();
	}

	public void addAllToCached(Collection<Hypothesis> hyps) {cached.addAll(hyps);}

	public void removeAllFromCached(Collection<Hypothesis> hyps) {cached.removeAll(hyps);}

	public Collection<Hypothesis> getCached() {return cached;}

//	public void addAllToSearched(Collection<Hypothesis> hyps) {searched.addAll(hyps);}
	
	public void removeAllFromSearched(Collection<Hypothesis> hyps) {searched.removeAll(hyps);}

	public Collection<Hypothesis> getSearched() {return searched;}

	public void setSearched(Collection<Hypothesis> searched) {this.searched = searched;}
	
	public void updateStatus() throws RodinDBException {
		ps.updateStatus(pt);
	}

}
