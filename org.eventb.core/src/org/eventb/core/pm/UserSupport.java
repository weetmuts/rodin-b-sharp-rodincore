package org.eventb.core.pm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eventb.core.IPRFile;
import org.eventb.core.IPRSequent;
import org.eventb.core.prover.IProofTreeNode;
import org.eventb.core.prover.sequent.Hypothesis;
import org.eventb.core.prover.tactics.ITactic;
import org.eventb.internal.core.pm.GoalChangeEvent;
import org.eventb.internal.core.pm.GoalDelta;
import org.eventb.internal.core.pm.HypothesisChangeEvent;
import org.eventb.internal.core.pm.HypothesisDelta;
import org.eventb.internal.core.pm.POChangeEvent;
import org.eventb.internal.core.pm.PODelta;
import org.rodinp.core.RodinDBException;

public class UserSupport
{
	
	// TODO UserSupport needs to listen to the Database
	
	Collection<IHypothesisChangedListener> hypChangedListeners;
	Collection<IGoalChangedListener> goalChangedListeners;
	Collection<IPOChangedListener> poChangedListeners;
	Collection<IProofStatusChangedListener> proofStatusChangedListeners;
	
	private Collection<Hypothesis> displayCached;
	private Collection<Hypothesis> displaySearched;

	private List<ProofState> proofStates;
	private int counter;
	private ProofState proofState;

	public UserSupport() {
		hypChangedListeners = new HashSet<IHypothesisChangedListener>();
		goalChangedListeners = new HashSet<IGoalChangedListener>();
		poChangedListeners = new HashSet<IPOChangedListener>();
		proofStatusChangedListeners = new HashSet<IProofStatusChangedListener>();
		displayCached = new HashSet<Hypothesis>();
		displaySearched = new HashSet<Hypothesis>();
		proofStates = new ArrayList<ProofState>();
	}

	public void setInput(IPRFile prFile) throws RodinDBException {
		try {
			for (int i = 0; i < prFile.getSequents().length; i++) {
				IPRSequent prSequent = prFile.getSequents()[i];
				proofStates.add(new ProofState(prSequent));
			}
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}
		counter = -1;
		nextUndischargedPO();		
	}
	
	public void setCurrentPO(IPRSequent prSequent) {
		for (int i = 1; i <= proofStates.size(); i++) {
			int index = (counter + i) % proofStates.size();
			ProofState ps = proofStates.get(index);
			if (ps.getPRSequent().equals(prSequent)) {
				setProofState(ps, index);
				return;
			}
		}
	}
	
	public Collection<Hypothesis> getDisplayCached() {return displayCached;}

	public void setDisplayCached(Collection<Hypothesis> newDisplayCached) {
		displayCached = newDisplayCached;
	}
	
	public Collection<Hypothesis> getDisplaySearched() {return displaySearched;}

	public void setDisplaySearched(Collection<Hypothesis> newDisplaySearched) {
		displayCached = newDisplaySearched;
	}
	
	public void nextUndischargedPO() {
		for (int i = 1; i <= proofStates.size(); i++) {
			int index = (counter + i) % proofStates.size();
			ProofState ps = proofStates.get(index);
			if (!ps.isDischarged()) {
				setProofState(ps, index);
				return;
			}
		}
		proofState = null;
	}
	
	public void prevUndischargedPO() {
		for (int i = 1; i < proofStates.size(); i++) {
			int index = (counter + proofStates.size() - i) % proofStates.size();
			ProofState ps = proofStates.get(index);
			if (!ps.isDischarged()) {
				setProofState(ps, index);
				return;
			}
		}
		proofState = null;
	}
	
	private void setProofState(ProofState ps, int index) {
		// Calculate delta
		if (ps.getCurrentNode() == null) ps.setCurrentNode(ps.getNextPendingSubgoal());
		
		IProofTreeNode currentNode = ps.getCurrentNode();
		IHypothesisDelta hypDelta = calculateHypDelta(ps, currentNode);
		IHypothesisChangeEvent hypEvent = new HypothesisChangeEvent(hypDelta);
		notifyHypothesisChangedListener(hypEvent);
		
		IGoalDelta goalDelta = new GoalDelta(currentNode);
		IGoalChangeEvent goalEvent = new GoalChangeEvent(goalDelta);
		notifyGoalChangedListener(goalEvent);
		
		IPODelta poDelta = new PODelta(ps);
		IPOChangeEvent poEvent = new POChangeEvent(poDelta);
		notifyPOChangedListener(poEvent);
		
		counter = index;
		proofState = ps;
		
		return;
	}
	
	public void addHypothesisChangedListener(IHypothesisChangedListener listener) {
		hypChangedListeners.add(listener);
	}
	
	public void removeHypothesisChangedListener(IHypothesisChangedListener listener) {
		hypChangedListeners.remove(listener);
	}
	
	private void notifyHypothesisChangedListener(IHypothesisChangeEvent e) {
		for (Iterator<IHypothesisChangedListener> i = hypChangedListeners.iterator(); i.hasNext();) {
			IHypothesisChangedListener listener = i.next();
			listener.hypothesisChanged(e);
		}
		return;
	}
	
	public void addGoalChangedListener(IGoalChangedListener listener) {
		goalChangedListeners.add(listener);
	}
	
	public void removeGoalChangedListener(IGoalChangedListener listener) {
		goalChangedListeners.remove(listener);
	}
	
	private void notifyGoalChangedListener(IGoalChangeEvent e) {
		for (Iterator<IGoalChangedListener> i = goalChangedListeners.iterator(); i.hasNext();) {
			IGoalChangedListener listener = i.next();
			listener.goalChanged(e);
		}
		return;
	}

	public void addPOChangedListener(IPOChangedListener listener) {
		poChangedListeners.add(listener);
	}
	
	public void removePOChangedListener(IPOChangedListener listener) {
		poChangedListeners.remove(listener);
	}
	
	private void notifyPOChangedListener(IPOChangeEvent e) {
		for (Iterator<IPOChangedListener> i = poChangedListeners.iterator(); i.hasNext();) {
			IPOChangedListener listener = i.next();
			listener.poChanged(e);
		}
		return;
	}

	public void addProofStatusChangedListener(IProofStatusChangedListener listener) {
		proofStatusChangedListeners.add(listener);
	}
	
	public void removeProofStatusChangedListener(IProofStatusChangedListener listener) {
		proofStatusChangedListeners.remove(listener);
	}

	private void notifyProofStatusChangedListener(boolean complete) {
		for (Iterator<IProofStatusChangedListener> i = proofStatusChangedListeners.iterator(); i.hasNext();) {
			IProofStatusChangedListener listener = i.next();
			listener.proofStatusChanged(complete);
		}
		return;
	}
	
	private IHypothesisDelta calculateHypDelta(ProofState newProofState, IProofTreeNode newNode) {
		Collection<Hypothesis> newSelectedHypotheses;
		if (newNode == null) newSelectedHypotheses = new HashSet<Hypothesis>(); 
		else newSelectedHypotheses = newNode.getSequent().selectedHypotheses();

		Collection<Hypothesis> addedToSelected = new HashSet<Hypothesis>();
		Collection<Hypothesis> removedFromSelected = new HashSet<Hypothesis>();
		
		if (proofState != null && proofState.getCurrentNode() != null) {
			Collection<Hypothesis> currentSelectedHypotheses = proofState.getCurrentNode().getSequent().selectedHypotheses(); 
			for (Iterator<Hypothesis> it = currentSelectedHypotheses.iterator(); it.hasNext();) {
				Hypothesis hp = it.next();
				if (!newSelectedHypotheses.contains(hp)) removedFromSelected.add(hp); 
			}
		
			for (Iterator<Hypothesis> it = newSelectedHypotheses.iterator(); it.hasNext();) {
				Hypothesis hp = it.next();
				if (!currentSelectedHypotheses.contains(hp)) addedToSelected.add(hp);
			}
		}
		else {
			addedToSelected = newSelectedHypotheses;
		}
		
		Collection<Hypothesis> addedToCached = new HashSet<Hypothesis>();
		Collection<Hypothesis> removedFromCached = new HashSet<Hypothesis>();
		if (proofState != null) {
			Collection<Hypothesis> newDisplayCached = new HashSet<Hypothesis>();
			for (Iterator<Hypothesis> it = newProofState.getCached().iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
//				System.out.print("In cache: " + hyp);
				if (displayCached.contains(hyp)) {   
//					System.out.print(", currently display");
					if (isValid(hyp, newNode) && !isSelected(hyp, newNode)) { // cached, display, valid & not selected
//						System.out.println(", valid");
						newDisplayCached.add(hyp);
					}
					else {                                  // cached, display, (invalid or selected)
//						System.out.println(", invalid");
						removedFromCached.add(hyp);
					}
					displayCached.remove(hyp);
				}
				else {
//					System.out.print(", not currently display");
					if (isValid(hyp, newNode) && !isSelected(hyp, newNode)) { // cached, not(display), valid & not selected
//						System.out.println(", valid");
						newDisplayCached.add(hyp);
						addedToCached.add(hyp);
					}
//					else System.out.println();
				}
			}
			
			for (Iterator<Hypothesis> it = displayCached.iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
//				System.out.println("Currently display but not in cached: " + hyp);
				removedFromCached.add(hyp);                        // display, invalid or selected, not(cached)
			}
			
			displayCached = newDisplayCached;
		}
		
		Collection<Hypothesis> addedToSearched = new HashSet<Hypothesis>();
		Collection<Hypothesis> removedFromSearched = new HashSet<Hypothesis>();
		if (proofState != null) {
			Collection<Hypothesis> newDisplaySearched = new HashSet<Hypothesis>();
			for (Iterator<Hypothesis> it = newProofState.getSearched().iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
//				System.out.print("In cache: " + hyp);
				if (displaySearched.contains(hyp)) {   
//					System.out.print(", currently display");
					if (isValid(hyp, newNode) && !isSelected(hyp, newNode)) { // cached, display, valid & not selected
//						System.out.println(", valid");
						newDisplaySearched.add(hyp);
					}
					else {                                  // cached, display, (invalid or selected)
//						System.out.println(", invalid");
						removedFromSearched.add(hyp);
					}
					displaySearched.remove(hyp);
				}
				else {
//					System.out.print(", not currently display");
					if (isValid(hyp, newNode) && !isSelected(hyp, newNode)) { // cached, not(display), valid & not selected
//						System.out.println(", valid");
						newDisplaySearched.add(hyp);
						addedToSearched.add(hyp);
					}
				}
			}
			
			for (Iterator<Hypothesis> it = displaySearched.iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
//				System.out.println("Currently display but not in cached: " + hyp);
				removedFromSearched.add(hyp);                        // display, invalid or selected, not(cached)
			}
			displaySearched = newDisplaySearched;
		}
		
		return new HypothesisDelta(addedToSelected, removedFromSelected, addedToCached, removedFromCached, addedToSearched, removedFromSearched);
	}
	
	private boolean isValid(Hypothesis hyp, IProofTreeNode pt) {
		return (pt != null && pt.getSequent().hypotheses().contains(hyp));
	}
	
	private boolean isSelected(Hypothesis hyp, IProofTreeNode pt) {
		return pt.getSequent().selectedHypotheses().contains(hyp);
	}
	
	/**
	 * This is the response of the UserSupport for selecting a node in the current
	 * Proof Tree.
	 */ 
	public void selectNode(IProofTreeNode pt) {
		IHypothesisDelta delta = calculateHypDelta(proofState, pt);
		IHypothesisChangeEvent e = new HypothesisChangeEvent(delta);
		notifyHypothesisChangedListener(e);
				
		if (pt != null) {
			notifyGoalChangedListener(new GoalChangeEvent(new GoalDelta(pt)));
		}
		
		if (proofState != null) proofState.setCurrentNode(pt);
		return;
	}

	/**
	 * This method return the current Obligation (Proof State).
	 * This should be called at the initialisation of a listener of the
	 * UserSupport.
	 * After that the listeners will update their states by listen to the
	 * changes from the UserSupport
	 * @return the current ProofState (can be null).
	 */
	public ProofState getCurrentPO() {
		return proofState;
	}

	public void applyTacticToHypotheses(ITactic t, Set<Hypothesis> hyps) {
		t.apply(proofState.getCurrentNode());
		proofState.addAllToCached(hyps);
		notifyProofStatusChangedListener(false);
	}
	
	public void removeHypotheses(int origin, Collection<Hypothesis> hyps) {
		IHypothesisDelta delta;
		IHypothesisChangeEvent e;
		switch (origin) {
		case (IHypothesisDelta.CACHED):
			Collection<Hypothesis> removedFromCached = new HashSet<Hypothesis>();
			for (Iterator<Hypothesis> it = hyps.iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
				if (displayCached.contains(hyp))
					removedFromCached.add(hyp);
			}
			delta = new HypothesisDelta(null, null, null, removedFromCached, null, null);
			e = new HypothesisChangeEvent(delta);
			notifyHypothesisChangedListener(e);
			proofState.removeAllFromCached(hyps);
			break;
		case (IHypothesisDelta.SEARCHED):
			Collection<Hypothesis> removedFromSearched = new HashSet<Hypothesis>();
			for (Iterator<Hypothesis> it = hyps.iterator(); it.hasNext();) {
				Hypothesis hyp = it.next();
				if (displaySearched.contains(hyp))
					removedFromSearched.add(hyp);
			}
			delta = new HypothesisDelta(null, null, null, null, null, removedFromSearched);
			e = new HypothesisChangeEvent(delta);
			notifyHypothesisChangedListener(e);
			proofState.removeAllFromCached(hyps);
		}
			
	}

	public void applyTactic(ITactic t) throws RodinDBException {
		IProofTreeNode currentNode = proofState.getCurrentNode();
		t.apply(currentNode);
		proofState.updateStatus();
		
		notifyProofStatusChangedListener(proofState.isDischarged());
		
		IProofTreeNode newNode = proofState.getNextPendingSubgoal(currentNode);
		if (newNode == null) newNode = currentNode;
		
		IHypothesisDelta hypDelta = calculateHypDelta(proofState, newNode);
		IHypothesisChangeEvent hypEvent = new HypothesisChangeEvent(hypDelta);
		notifyHypothesisChangedListener(hypEvent);
		notifyGoalChangedListener(new GoalChangeEvent(new GoalDelta(newNode)));
		proofState.setCurrentNode(newNode);
		
		IPODelta poDelta = new PODelta(proofState);
		IPOChangeEvent poEvent = new POChangeEvent(poDelta);
		notifyPOChangedListener(poEvent);
	}

}