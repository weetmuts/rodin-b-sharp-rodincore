/*******************************************************************************
 * Copyright (c) 2006, 2008 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - refactored for using the Proof Manager API
 *     Systerel - added missing cleanup in dispose() and refresh()
 ******************************************************************************/
package org.eventb.internal.core.pm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.EventBPlugin;
import org.eventb.core.IPSFile;
import org.eventb.core.IPSStatus;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IProofComponent;
import org.eventb.core.pm.IProofState;
import org.eventb.core.pm.IUserSupport;
import org.eventb.core.pm.IUserSupportInformation;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IProofTreeNodeFilter;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.ProverLib;
import org.eventb.internal.core.ProofMonitor;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class UserSupport implements IElementChangedListener, IUserSupport {

	private static final class ProofStateLoader implements Runnable {

		private final UserSupport us;

		private RodinDBException exc;

		public ProofStateLoader(UserSupport us) {
			this.us = us;
		}

		public void run() {
			try {
				for (IPSStatus psStatus : us.getStatuses()) {
					final ProofState state = new ProofState(us, psStatus);
					us.proofStates.add(state);
					us.deltaProcessor.newProofState(us, state);
				}
			} catch (RodinDBException e) {
				exc = e;
			}
		}

		public void checkNestedException() throws RodinDBException {
			if (exc != null)
				throw exc;
		}
	}

	private static final IProofState[] NO_PROOF_STATES = new IProofState[0];
	
	protected LinkedHashSet<IProofState> proofStates;

	protected ProofState currentPS;

	protected UserSupportManager manager;

	protected DeltaProcessor deltaProcessor;

	protected IProofComponent pc;

	public UserSupport() {
		RodinCore.addElementChangedListener(this);
		proofStates = null;
		manager = UserSupportManager.getDefault();
		deltaProcessor = manager.getDeltaProcessor();
		manager.addUserSupport(this);
		deltaProcessor.newUserSupport(this);
	}

	public void setInput(final IPSFile psFile) {
		pc = EventBPlugin.getProofManager().getProofComponent(psFile);
	}

	private void loadProofStatesIfNeeded() throws RodinDBException {
		if (proofStates == null) {
			loadProofStates();
		}
	}

	public void loadProofStates() throws RodinDBException {
		final ProofStateLoader loader = new ProofStateLoader(this);
		proofStates = new LinkedHashSet<IProofState>();
		manager.run(loader);
		loader.checkNestedException();
	}

	public void dispose() {
		RodinCore.removeElementChangedListener(this);
		manager.removeUserSupport(this);
		deltaProcessor.removeUserSupport(this);
		if (proofStates != null) {
			for (IProofState pss : proofStates) {
				pss.unloadProofTree();
			}
		}
	}
	public IPSFile getInput() {
		if (pc != null)
			return pc.getPSFile();
		return null;
	}

	public void nextUndischargedPO(final boolean force,
			final IProgressMonitor monitor) throws RodinDBException {
		loadProofStatesIfNeeded();
		boolean found = false;
		IProofState newProofState = null;
		IProofState firstOpenedProofState = null;
		for (IProofState proofState : proofStates) {
			if (firstOpenedProofState == null && !proofState.isClosed()) {
				firstOpenedProofState = proofState;
			}
			if (found) {
				if (!proofState.isClosed()) {
					newProofState = proofState;
					break;
				}
			}
			else {
				if (proofState.equals(currentPS)) {
					found = true;
				}
			}
		}
		
		if (found && newProofState == null)  {// Have not found new proof State yet
			newProofState = firstOpenedProofState;
		}
		else if (!found) {
			newProofState = firstOpenedProofState;
		}
		
		final IProofState proofState = newProofState;

		manager.run(new Runnable() {

			public void run() {
				try {
					if (proofState != null)
						setProofState(proofState, monitor);
					else if (force) {
						setProofState(null, monitor);
						deltaProcessor
								.informationChanged(
										UserSupport.this,
										new UserSupportInformation(
												"No un-discharged proof obligation found",
												IUserSupportInformation.MAX_PRIORITY));
					}
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	public void prevUndischargedPO(final boolean force,
			final IProgressMonitor monitor) throws RodinDBException {
		loadProofStatesIfNeeded();
		boolean found = false;
		IProofState newProofState = null;
		IProofState lastOpenedProofState = null;
		for (IProofState proofState : proofStates) {
			if (!found) {
				if (proofState.equals(currentPS)) {
					if (lastOpenedProofState != null) {
						newProofState = lastOpenedProofState;
						break;
					}
					found = true;
				}
			}
			if (!proofState.isClosed()) {
				lastOpenedProofState = proofState;
			}
		}
		
		if (found && newProofState == null)  {// Have not found new proof State yet
			newProofState = lastOpenedProofState;
		}
		else if (!found) {
			newProofState = lastOpenedProofState;
		}
		
		final IProofState proofState = newProofState;

		manager.run(new Runnable() {

			public void run() {
				try {
					if (proofState != null)
						setProofState(proofState, monitor);
					else if (force) {
						setProofState(null, monitor);
						deltaProcessor
								.informationChanged(
										UserSupport.this,
										new UserSupportInformation(
												"No un-discharged proof obligation found",
												IUserSupportInformation.MAX_PRIORITY));
					}
				} catch (RodinDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#getCurrentPO()
	 */
	public IProofState getCurrentPO() {
		return currentPS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#setCurrentPO(org.eventb.core.IPSstatus,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setCurrentPO(IPSStatus psStatus, IProgressMonitor monitor)
			throws RodinDBException {
		loadProofStatesIfNeeded();
		if (psStatus == null) {
			setProofState(null, monitor);
			return;
		}
		for (IProofState proofState : proofStates) {
			if (proofState.getPSStatus().equals(psStatus))
				setProofState(proofState, monitor);			
		}
	}

	void setProofState(final IProofState proofState, final IProgressMonitor monitor)
			throws RodinDBException {
		if (currentPS == null && proofState == null) {
			// Try to fire the remaining delta
			deltaProcessor.informationChanged(this, new UserSupportInformation(
					"No new obligation", IUserSupportInformation.MIN_PRIORITY));
			return;
		}

		if (currentPS != null && currentPS.equals(proofState)) {
			// Try to fire the remaining delta
			deltaProcessor.informationChanged(this, new UserSupportInformation(
					"No new obligation", IUserSupportInformation.MIN_PRIORITY));
			return;			
		}
		
		manager.run(new Runnable() {

			public void run() {
				if (UserSupportUtils.DEBUG)
					UserSupportUtils.debug("New Proof Sequent: " + proofState);
				if (proofState == null) {
					currentPS = null;
				} else {
					currentPS = (ProofState) proofState;
					// Load the proof tree if it is not there already
					if (proofState.getProofTree() == null) {
						try {
							proofState.loadProofTree(monitor);
						} catch (RodinDBException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				deltaProcessor.currentProofStateChange(UserSupport.this);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#getPOs()
	 */
	public IProofState[] getPOs() {
		if (proofStates == null) {
			return NO_PROOF_STATES;
		}
		return proofStates.toArray(new IProofState[proofStates.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() {
		if (proofStates == null) {
			return false;
		}
		for (IProofState proofState : proofStates) {
			if (proofState.isDirty())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#getUnsavedPOs()
	 */
	public IProofState[] getUnsavedPOs() {
		if (proofStates == null) {
			return NO_PROOF_STATES;
		}
		Collection<IProofState> unsaved = new HashSet<IProofState>();
		for (IProofState proofState : proofStates) {
			if (proofState.isDirty())
				unsaved.add(proofState);
		}
		return unsaved.toArray(new IProofState[unsaved.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.pm.IUserSupport#getInformation()
	 */
	@Deprecated
	public Object[] getInformation() {
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IUserSupport#removeCachedHypotheses(java.util.Collection)
	 */
	public void removeCachedHypotheses(final Collection<Predicate> hyps) {
		checkCurrentPS();
		manager.run(new Runnable() {

			public void run() {
				currentPS.removeAllFromCached(hyps);
			}

		});
		return;
	}

	private void checkCurrentPS() throws IllegalStateException {
		if (currentPS == null) {
			throw new IllegalStateException("No current PO");
		}
	}

	public void searchHyps(String token) {
		checkCurrentPS();
		token = token.trim();
		final Set<Predicate> hyps = ProverLib.hypsTextSearch(currentPS
				.getCurrentNode().getSequent(), token);
		manager.run(new Runnable() {
			public void run() {
				currentPS.setSearched(hyps);
				deltaProcessor.informationChanged(UserSupport.this,
						new UserSupportInformation("Search hypotheses",
								IUserSupportInformation.MAX_PRIORITY));
			}
		});
	}

	public void removeSearchedHypotheses(final Collection<Predicate> hyps) {
		checkCurrentPS();
		manager.run(new Runnable() {
			public void run() {
				currentPS.removeAllFromSearched(hyps);
			}
		});
	}

	public void selectNode(IProofTreeNode node) throws RodinDBException {
		checkCurrentPS();
		currentPS.setCurrentNode(node);
	}

	protected void addAllToCached(Set<Predicate> hyps) {
		currentPS.addAllToCached(hyps);
	}

	@Deprecated
	public void applyTactic(final ITactic t, final IProgressMonitor monitor)
			throws RodinDBException {
		applyTactic(t, true, monitor);
	}

	public void applyTactic(ITactic t, boolean applyPostTactic,
			IProgressMonitor monitor) throws RodinDBException {
		checkCurrentPS();
		IProofTreeNode node = currentPS.getCurrentNode();
		currentPS.applyTactic(t, node, applyPostTactic, monitor);
	}
	
	@Deprecated
	public void applyTacticToHypotheses(ITactic t, Set<Predicate> hyps,
			IProgressMonitor monitor) throws RodinDBException {
		applyTacticToHypotheses(t, hyps, true, monitor);
	}

	public void applyTacticToHypotheses(ITactic t, Set<Predicate> hyps,
			boolean applyPostTactic, IProgressMonitor monitor)
			throws RodinDBException {
		checkCurrentPS();
		currentPS.applyTacticToHypotheses(t, currentPS.getCurrentNode(), hyps,
				applyPostTactic, monitor);
	}

	void refresh() {
		assert proofStates != null;
		manager.run(new Runnable() {

			public void run() {

				LinkedHashSet<IProofState> newProofStates;
				// Remove the deleted ones first
				for (IProofState proofState : usDeltaProcessor.getToBeDeleted()) {
					deltaProcessor.removeProofState(UserSupport.this,
							proofState);
					proofState.unloadProofTree();
					proofStates.remove(proofState);
				}
				
				// Construct the Proof States
				IPSStatus[] psStatuses;
				try {
					psStatuses = getStatuses();
				} catch (RodinDBException e) {
					e.printStackTrace();
					return;
				}

				newProofStates = new LinkedHashSet<IProofState>(
						psStatuses.length);

				for (IPSStatus psStatus : psStatuses) {
					IProofState proofState = UserSupport.this.getProofState(psStatus);

					if (proofState == null) { // A new PS Status
						proofState = new ProofState(UserSupport.this,
								psStatus);
						deltaProcessor.newProofState(UserSupport.this, proofState);
					}
					newProofStates.add(proofState);
				}

				proofStates = newProofStates;
			}
		});
	}

	public void back(IProgressMonitor monitor) throws RodinDBException {
		checkCurrentPS();
		currentPS.back(currentPS.getCurrentNode(), monitor);
	}

	public void setComment(String text, IProofTreeNode node)
			throws RodinDBException {
		checkCurrentPS();
		currentPS.setComment(text, node);
	}

	UserSupportDeltaProcessor usDeltaProcessor;
	
	public void elementChanged(final ElementChangedEvent event) {
		final IProgressMonitor monitor = new NullProgressMonitor();
		usDeltaProcessor = new UserSupportDeltaProcessor(this);
		IRodinElementDelta delta = event.getDelta();
		if (UserSupportUtils.DEBUG) {
			UserSupportUtils.debug("Delta: " + delta);
		}
		usDeltaProcessor.processDelta(delta, monitor);
		if (UserSupportUtils.DEBUG) {
			UserSupportUtils.debug(usDeltaProcessor.toString());
		}
		
		manager.run(new Runnable() {

			public void run() {
				// Process trashed proofs first

				// Then refresh to get all the proof states
				if (usDeltaProcessor.needRefreshed()) {
					refresh();
				}

				// Process reloaded
				for (IProofState proofState : usDeltaProcessor.getToBeReloaded()) {
					try {
						proofState.loadProofTree(monitor);
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}

				// Process Reused
				for (IProofState proofState : usDeltaProcessor.getToBeReused()) {
					try {
						proofState.proofReuse(new ProofMonitor(
								monitor));
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// Process Rebuilt
				for (IProofState proofState : usDeltaProcessor.getToBeRebuilt()) {
					try {
						proofState.proofRebuilt(new ProofMonitor(
								new NullProgressMonitor()));
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		});

	}

	public void doSave(IProofState[] states, IProgressMonitor monitor)
			throws RodinDBException {
		if (proofStates == null) {
			return;
		}
		for (IProofState state : states) {
			state.setProofTree(monitor);
			// state.getPSStatus().setManualProof(true, monitor);
		}
		pc.save(monitor, true);
		for (IProofState state : states) {
			state.setDirty(false);
		}
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("****** User Support for: ");
		buffer.append(this.getInput().getBareName() + " ******\n");
		buffer.append("** Proof States **\n");
		for (IProofState proofState : getPOs()) {
			buffer.append(proofState.toString());
			buffer.append("\n");
		}
		buffer.append("Current psSatus: ");
		buffer.append(currentPS.getPSStatus());
		buffer.append("\n");
		buffer.append("********************************************************\n");
		return buffer.toString();
	}

	public IProofState getProofState(IPSStatus psStatus) {
		if (proofStates == null) {
			return null;
		}
		for (IProofState proofState : proofStates) {
			if (proofState.getPSStatus().equals(psStatus))
				return proofState;
		}
		return null;
	}

	public boolean selectNextSubgoal(boolean rootIncluded,
			IProofTreeNodeFilter filter) throws RodinDBException {
		if (currentPS == null)
			return false;
		return currentPS.selectNextSubGoal(currentPS.getCurrentNode(),
				rootIncluded, filter);
	}
	
	public IPSStatus[] getStatuses() throws RodinDBException {
		return pc.getPSFile().getStatuses();
	}

	public IProofComponent getProofComponent() {
		return pc;
	}
}