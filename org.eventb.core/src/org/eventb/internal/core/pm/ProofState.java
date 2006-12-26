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

package org.eventb.internal.core.pm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IPRProof;
import org.eventb.core.IPSStatus;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IProofState;
import org.eventb.core.pm.IUserSupportManager;
import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProofTreeDelta;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.ProverLib;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.core.seqprover.proofBuilder.IProofSkeleton;
import org.eventb.core.seqprover.proofBuilder.ProofBuilder;
import org.eventb.core.seqprover.tactics.BasicTactics;
import org.eventb.internal.core.ProofMonitor;
import org.eventb.internal.core.pom.POLoader;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class keep the proof state for one proof obligation including
 *         the proof tree, the current proof node, the set of cached and
 *         searched hypotheses.
 */
public class ProofState implements IProofState {

	// The PR sequent associated with this proof obligation.
	IPSStatus status;

	// The current proof tree, this might be different from the proof tree in
	// the disk, can be null when it is not initialised.
	IProofTree pt;

	// The current proof node, can be null when the proof tree is uninitialised.
	IProofTreeNode current;

	// The set of cached hypotheses.
	private Collection<Predicate> cached;

	// The set of searched hypotheses.
	private Collection<Predicate> searched;

	// The dirty flag to indicate if there are some unsaved changes with this
	// proof obligation.
	private boolean dirty;

	DeltaProcessor deltaProcessor;

	UserSupport userSupport;

	public ProofState(UserSupport userSupport, IPSStatus ps) {
		this.userSupport = userSupport;
		this.status = ps;
		cached = new ArrayList<Predicate>();
		searched = new ArrayList<Predicate>();
		deltaProcessor = ((UserSupportManager) UserSupportManager.getDefault())
				.getDeltaProcessor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#loadProofTree(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void loadProofTree(IProgressMonitor monitor) throws RodinDBException {

		if (pt != null)
			pt.removeChangeListener(this);
		// Construct the proof tree from the PO file.
		pt = userSupport.getProofManager().getProofTree(status);
		pt.addChangeListener(this);

		// If a proof exists in the PR file rebuild it.
		final IPRProof prProof = status.getProof();
		if (prProof.exists()) {
			final IProofSkeleton proofSkeleton = prProof.getSkeleton(
					FormulaFactory.getDefault(), monitor);
			if (proofSkeleton != null) {
				ProofBuilder.rebuild(pt.getRoot(), proofSkeleton);
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
		dirty = status.isBroken();
		cached = new HashSet<Predicate>();
		searched = new HashSet<Predicate>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#isClosed()
	 */
	public boolean isClosed() throws RodinDBException {
		if (pt != null)
			return pt.isClosed();

		return isSequentDischarged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getPRSequent()
	 */
	public IPSStatus getPRSequent() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getProofTree()
	 */
	public IProofTree getProofTree() {
		return pt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getCurrentNode()
	 */
	public IProofTreeNode getCurrentNode() {
		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#setCurrentNode(org.eventb.core.seqprover.IProofTreeNode)
	 */
	public void setCurrentNode(final IProofTreeNode newNode)
			throws RodinDBException {
		userSupport.startInformation();
		UserSupportManager.getDefault().run(new Runnable() {
			public void run() {
				if (current != newNode) {
					current = newNode;
					// Fire delta
					deltaProcessor.setNewCurrentNode(userSupport,
							ProofState.this);
					userSupport.addInformation("Select a new proof node");

				} else {
					userSupport.addInformation("Not a new proof node");
				}
				deltaProcessor.informationChanged(userSupport);
			}
		});

		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getNextPendingSubgoal(org.eventb.core.seqprover.IProofTreeNode)
	 */
	public IProofTreeNode getNextPendingSubgoal(IProofTreeNode node) {
		IProofTreeNode subGoal = node.getFirstOpenDescendant();
		if (subGoal != null)
			return subGoal;
		return pt.getRoot().getFirstOpenDescendant();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getNextPendingSubgoal()
	 */
	public IProofTreeNode getNextPendingSubgoal() {
		return pt.getRoot().getFirstOpenDescendant();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#addAllToCached(java.util.Collection)
	 */
	public void addAllToCached(Collection<Predicate> hyps) {
		cached.addAll(hyps);
		deltaProcessor.cacheChanged(userSupport, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#removeAllFromCached(java.util.Collection)
	 */
	public void removeAllFromCached(Collection<Predicate> hyps) {
		cached.removeAll(hyps);
		deltaProcessor.cacheChanged(userSupport, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getCached()
	 */
	public Collection<Predicate> getCached() {
		return cached;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#removeAllFromSearched(java.util.Collection)
	 */
	public void removeAllFromSearched(Collection<Predicate> hyps) {
		searched.removeAll(hyps);
		deltaProcessor.searchChanged(userSupport, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#getSearched()
	 */
	public Collection<Predicate> getSearched() {
		return searched;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#setSearched(java.util.Collection)
	 */
	public void setSearched(Collection<Predicate> searched) {
		this.searched = searched;
		deltaProcessor.searchChanged(userSupport, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) throws CoreException {
		UserSupportUtils.debug("Saving: " + status.getElementName());

		userSupport.getProofManager().saveProofTree(status, pt, monitor);

		dirty = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#setDirty(boolean)
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProofState))
			return false;
		else {
			IProofState proofState = (IProofState) obj;
			return proofState.getPRSequent().equals(status);
		}

	}

	// Pre: Must be initalised and not currently saving.
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#proofReuse(org.eventb.core.seqprover.IProofMonitor)
	 */
	public void proofReuse(IProofMonitor monitor) throws RodinDBException {
		// if (isSavingOrUninitialised()) return false;
		// if (pt == null) return false; // No proof tree, no reusable.

		IProofTree newTree = userSupport.getProofManager().getProofTree(status);
		IProverSequent newSeq = newTree.getSequent();
		if (ProverLib.proofReusable(pt.getProofDependencies(), newSeq)) {
			(BasicTactics.pasteTac(pt.getRoot())).apply(newTree.getRoot(),
					monitor);
			if (pt != null)
				pt.removeChangeListener(this);
			pt = newTree;
			newTree.addChangeListener(this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#isUninitialised()
	 */
	public boolean isUninitialised() {
		return (pt == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#isSequentDischarged()
	 */
	public boolean isSequentDischarged() throws RodinDBException {
		final IPRProof prProof = status.getProof();
		return (prProof.exists() && (prProof.getConfidence() > IConfidence.PENDING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#isProofReusable()
	 */
	public boolean isProofReusable() throws RodinDBException {
		IProverSequent seq = POLoader.readPO(status.getPOSequent());
		return ProverLib.proofReusable(pt.getProofDependencies(), seq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#reloadProofTree()
	 */
	public void reloadProofTree() throws RodinDBException {

		// Construct the proof tree from the file.
		if (pt != null)
			pt.removeChangeListener(this);
		pt = userSupport.getProofManager().getProofTree(status);
		pt.addChangeListener(this);

		// Current node is the next pending subgoal or the root of the proof
		// tree if there are no pending subgoal.
		current = getNextPendingSubgoal();
		if (current == null) {
			current = pt.getRoot();
		}

		// if the proof tree was previously broken then the rebuild would
		// fix the proof, making it dirty.
		dirty = status.isBroken();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofState#unloadProofTree()
	 */
	public void unloadProofTree() {
		pt = null;
		current = null;
	}

	@Override
	public String toString() {
		return status.toString(); // Return the psStatus identify this Proof
		// State
	}

	public void applyTactic(final ITactic t, final IProofTreeNode node,
			final IProofMonitor pm) throws RodinDBException {
		userSupport.startInformation();
		UserSupportManager.getDefault().run(new Runnable() {

			public void run() {
				internalApplyTactic(t, node, pm);
				selectNextPendingSubGoal(node);
			}

		});

	}

	public void applyTacticToHypotheses(final ITactic t,
			final IProofTreeNode node, final Set<Predicate> hyps,
			final IProgressMonitor monitor) throws RodinDBException {
		userSupport.startInformation();
		UserSupportManager.getDefault().run(new Runnable() {

			public void run() {
				ProofState.this.addAllToCached(hyps);
				internalApplyTactic(t, node, new ProofMonitor(monitor));
				selectNextPendingSubGoal(node);
			}

		});

	}

	protected void selectNextPendingSubGoal(IProofTreeNode node) {
		IProofTreeNode newNode = this.getNextPendingSubgoal(node);
		if (newNode != null) {
			try {
				setCurrentNode(newNode);
			} catch (RodinDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void internalApplyTactic(ITactic t, IProofTreeNode node,
			IProofMonitor pm) {
		Object info = t.apply(node, pm);
		if (!t.equals(Tactics.prune())) {
			IUserSupportManager usManager = EventBPlugin.getDefault()
					.getUserSupportManager();
			if (usManager.getProvingMode().isExpertMode()) {
				Tactics.postProcessExpert().apply(node, pm);
			} else {
				Tactics.postProcessBeginner().apply(node, pm);
			}
		}
		if (info == null) {
			info = "Tactic applied successfully";
			this.setDirty(true);
		}
		userSupport.addInformation(info);
		deltaProcessor.informationChanged(userSupport);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#proofTreeChanged(org.eventb.core.seqprover.IProofTreeDelta)
	 */
	public void proofTreeChanged(IProofTreeDelta proofTreeDelta) {
		UserSupportUtils.debug("UserSupport - Proof Tree Changed: "
				+ proofTreeDelta);
		deltaProcessor.proofTreeChanged(userSupport, this, proofTreeDelta);
	}

	public void back(IProofTreeNode node, final IProgressMonitor monitor)
			throws RodinDBException {
		if (node == null)
			return;

		final IProofTreeNode parent = node.getParent();
		if (node.isOpen() && parent != null) {
			UserSupportManager.getDefault().run(new Runnable() {

				public void run() {
					try {
						applyTactic(Tactics.prune(), parent, new ProofMonitor(
								monitor));
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			});
		}
	}

	public void setComment(final String text, final IProofTreeNode node) throws RodinDBException {
		UserSupportManager.getDefault().run(new Runnable() {

			public void run() {
				node.setComment(text);
				ProofState.this.setDirty(true);
			}
			
		});
	}

}
