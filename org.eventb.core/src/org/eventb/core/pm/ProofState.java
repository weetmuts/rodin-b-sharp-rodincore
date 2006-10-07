/*******************************************************************************
 * Copyright (c) 2005-2006 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.core.pm;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPRProofTree;
import org.eventb.core.IPRProofTreeNode;
import org.eventb.core.IPRSequent;
import org.eventb.core.seqprover.Hypothesis;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.Lib;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.proofBuilder.IProofSkeleton;
import org.eventb.core.seqprover.proofBuilder.ProofBuilder;
import org.eventb.core.seqprover.tactics.BasicTactics;
import org.eventb.internal.core.pm.UserSupportUtils;
import org.eventb.internal.core.pom.POLoader;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class keep the proof state for one proof obligation including
 *         the proof tree, the current proof node, the set of cached and
 *         searched hypotheses.
 */
public class ProofState {

	// The PR sequent associated with this proof obligation.
	private IPRSequent prSequent;

	// The current proof tree, this might be different from the proof tree in
	// the disk, can be null when it is not initialised.
	private IProofTree pt;

	// The current proof node, can be null when the proof tree is uninitialised.
	private IProofTreeNode current;

	// The set of cached hypotheses.
	private Collection<Hypothesis> cached;

	// The set of searched hypotheses.
	private Collection<Hypothesis> searched;

	// The dirty flag to indicate if there are some unsaved changes with this
	// proof obligation.
	private boolean dirty;

	public ProofState(IPRSequent ps) {
		this.prSequent = ps;
		// loadProofTree();
	}

	public void loadProofTree() throws RodinDBException {
		//pt = prSequent.rebuildProofTree();

		// Construct the proof tree from the
		// file.
		
		// load the PO from the po file and create a proof tree with it
		IProverSequent seq = POLoader.readPO(prSequent.getPOSequent());
		pt = ProverFactory.makeProofTree(seq);
		
		// If a proof exists in the PR file rebuild it.
		final IPRProofTree prProofTree = prSequent.getProofTree();
		if (prProofTree != null)
		{
			final IPRProofTreeNode root = prProofTree.getRoot();
			if (root != null){
				IProofSkeleton skel = root.getSkeleton(null);
				ProofBuilder.rebuild(pt.getRoot(),skel);
			}
		}
		
		// Current node is the next pending subgoal or the root of the proof
		// tree if there are no pending subgoal.
		current = getNextPendingSubgoal();
		if (current == null) {
			current = pt.getRoot();
		}

		// if the proof tree was previously broken then the rebuild would
		// fix the proof, making it dirty.
		dirty = prSequent.isProofBroken();
		cached = new HashSet<Hypothesis>();
		searched = new HashSet<Hypothesis>();
	}

	public boolean isClosed() throws RodinDBException {
		if (pt != null)
			return pt.isClosed();
		
		final IPRProofTree prProofTree = prSequent.getProofTree();
		return (prProofTree != null && prProofTree.isClosed());
	}

	public IPRSequent getPRSequent() {
		return prSequent;
	}

	public IProofTree getProofTree() {
		return pt;
	}

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

	public void addAllToCached(Collection<Hypothesis> hyps) {
		cached.addAll(hyps);
	}

	public void removeAllFromCached(Collection<Hypothesis> hyps) {
		cached.removeAll(hyps);
	}

	public Collection<Hypothesis> getCached() {
		return cached;
	}

	public void removeAllFromSearched(Collection<Hypothesis> hyps) {
		searched.removeAll(hyps);
	}

	public Collection<Hypothesis> getSearched() {
		return searched;
	}

	public void setSearched(Collection<Hypothesis> searched) {
		this.searched = searched;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void doSave() throws CoreException {
		UserSupportUtils.debug("Saving: " + prSequent.getElementName());
		
		// TODO add lock for po and pr file
		
		RodinCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				prSequent.getProofTree().setProofTree(pt);
				prSequent.updateStatus();
				// PRUtil.updateProofTree(PRSequent.this, pt);
			}
		}, prSequent.getSchedulingRule() ,null);
		
		// prSequent.updateProofTree(pt);
		
		
		dirty = false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProofState))
			return false;
		else {
			ProofState proofState = (ProofState) obj;
			return proofState.getPRSequent().equals(prSequent);
		}

	}

	// Pre: Must be initalised and not currently saving.
	public void proofReuse() throws RodinDBException {
		// if (isSavingOrUninitialised()) return false;
		// if (pt == null) return false; // No proof tree, no reusable.
		
		IProverSequent newSeq = POLoader.readPO(prSequent.getPOSequent());
		IProofTree newTree = ProverFactory.makeProofTree(newSeq);
		
		if (Lib.proofReusable(pt.getProofDependencies(), newSeq)) {
			(BasicTactics.pasteTac(pt.getRoot())).apply(newTree.getRoot(), null);
			pt = newTree;
			current = getNextPendingSubgoal();
			if (current == null) {
				current = pt.getRoot();
			}
			dirty = true;
			return;
		}
		// If NOT, then mark the Proof State as dirty. Send delta to the
		// user
	}

	public boolean isUninitialised() {
		return (pt == null);
	}

	public boolean isSequentDischarged() throws RodinDBException {
		final IPRProofTree prProofTree = prSequent.getProofTree();
		return (prProofTree != null && prProofTree.isClosed());
	}

	public boolean isProofReusable() throws RodinDBException {
		IProverSequent seq = POLoader.readPO(prSequent.getPOSequent());
		return Lib.proofReusable(pt.getProofDependencies(), seq);
	}

	public void reloadProofTree() throws RodinDBException {
		
		// Construct the proof tree from the file.
		IProverSequent newSeq = POLoader.readPO(prSequent.getPOSequent());
		pt = ProverFactory.makeProofTree(newSeq);
		
		
		// Current node is the next pending subgoal or the root of the proof
		// tree if there are no pending subgoal.
		current = getNextPendingSubgoal();
		if (current == null) {
			current = pt.getRoot();
		}

		// if the proof tree was previously broken then the rebuild would
		// fix the proof, making it dirty.
		dirty = prSequent.isProofBroken();
	}

	public void unloadProofTree() {
		pt = null;
		current = null;
	}

}
