/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.internal.pp.core.provers.casesplit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.eventb.internal.pp.core.ClauseSimplifier;
import org.eventb.internal.pp.core.Dumper;
import org.eventb.internal.pp.core.IDispatcher;
import org.eventb.internal.pp.core.IProverModule;
import org.eventb.internal.pp.core.IVariableContext;
import org.eventb.internal.pp.core.Level;
import org.eventb.internal.pp.core.ProverResult;
import org.eventb.internal.pp.core.elements.Clause;
import org.eventb.internal.pp.core.inferrers.CaseSplitInferrer;

public class CaseSplitter implements IProverModule {
	
	/**
	 * Debug flag for <code>PROVER_CASESPLIT_TRACE</code>
	 */
	public static boolean DEBUG = false;
	public static void debug(String message){
			String prefix = "";
//			for (int i = 0; i < currentLevel.getHeight(); i++) {
//				prefix += " ";
//			}
			System.out.println(prefix+message);
	}
	
	private Stack<SplitPair> splits = new Stack<SplitPair>();
	
	private SplitPair nextCase;
	
	private Vector<Clause> candidates;
	private CaseSplitInferrer inferrer;
	
	private IDispatcher dispatcher;
	private ClauseSimplifier simplifier;
	
	public CaseSplitter(IVariableContext context, IDispatcher dispatcher) {
		this.inferrer = new CaseSplitInferrer(context);
		this.dispatcher = dispatcher;
	}
	
	public void initialize(ClauseSimplifier simplifier) {
		this.simplifier = simplifier;
		candidates = new Stack<Clause>();
	}
	
	public ProverResult contradiction(Level oldLevel, Level newLevel, Set<Level> dependencies) {
		return backtrack(oldLevel, newLevel, dependencies);
	}
	
	private int counter = 0;
	private boolean isNextAvailable() {
		if (counter > 0) {
			counter--;
			return true;
		}
		else {
			counter = candidates.size()*50;
			return false;
		}
	}
	
	// this returns the next clause produced by a case split.
	// if the preceding branch was closed, it returns the next case.
	// it it is not the case it does a new case split
	public ProverResult next(boolean force) {
//		assert splits.size() == dispatcher.getLevel().getHeight();
		if (!force && !isNextAvailable()) return ProverResult.EMPTY_RESULT;
		
		Set<Clause> result;
		Set<Clause> subsumedClauses = new HashSet<Clause>();
		if (nextCase == null) {
			if (candidates.isEmpty()) return ProverResult.EMPTY_RESULT;
			dispatcher.nextLevel();
			result = newCaseSplit();
		}
		else {
			dispatcher.nextLevel();
			result = nextCase();
		}
		simplifier.run(result);
		
//		assert splits.size() == dispatcher.getLevel().getHeight();
		return new ProverResult(result, subsumedClauses);
	}
	
	private Set<Clause> nextCase() {
		if (DEBUG) debug("Following case on "+nextCase.original+", size of split stack: "+splits.size());
		Set<Clause> result = nextCase.right;
		splits.push(nextCase);
		nextCase = null;
		return result;
	}
	
	private Set<Clause> newCaseSplit() {
		Clause clause = nextCandidate();
		candidates.remove(clause);
		
		assert !dispatcher.getLevel().isAncestorOf(clause.getLevel()):"Splitting on clause: "+clause+", but level: "+dispatcher.getLevel();
		
		splits.push(split(clause));
		if (DEBUG) debug("New case split on "+clause+", size of split stack: "+splits.size()+", remaining candidates: "+candidates.size());
		return splits.peek().left;
	}
	
	private Clause nextCandidate() {
		List<Clause> restrictedCandidates = getCandidatesDependingOnGoal();
		if (restrictedCandidates.isEmpty()) restrictedCandidates = candidates;
		int depth = -1;
		Clause currentClause = null;
		for (Clause clause : restrictedCandidates) {
			if (depth == -1 || clause.getOrigin().getDepth() < depth) {
				depth = clause.getOrigin().getDepth();
				currentClause = clause;
			}
		}
		return currentClause;
	}
	
	private List<Clause> getCandidatesDependingOnGoal() {
		List<Clause> result = new ArrayList<Clause>();
		for (Clause clause : candidates) {
			if (clause.getOrigin().dependsOnGoal()) result.add(clause);
		}
		return result;
	}

	private SplitPair split(Clause clause) {
		inferrer.setLevel(dispatcher.getLevel().getParent());
		clause.infer(inferrer);
		return new SplitPair(clause,inferrer.getLeftCase(),inferrer.getRightCase(),dispatcher.getLevel().getParent());
	}
	
	/**
	 * Backtrack from this level up to and inclusive the level specified as a parameter.
	 * 
	 * @param oldLevel the level which must be backtracked
	 */
	private ProverResult backtrack(Level oldLevel, Level newLevel, Set<Level> dependencies) {
		if (DEBUG) debug("CaseSplitter: Backtracking datastructures, size of split stack: "+splits.size());
		
		Set<Clause> putBackList = new LinkedHashSet<Clause>();
		if (	nextCase != null
			&&	!newLevel.isAncestorOf(nextCase.original.getLevel())) {
			// we put the clause as a candidate again
			putBackList.add(nextCase.original);
		}
		
		if (DEBUG) debug("CaseSplitter: Backtracking from: "+oldLevel+", to: "+dispatcher.getLevel());
		Level tmp = oldLevel;

		while (!tmp.getParent().equals(dispatcher.getLevel())) {
			SplitPair pair = splits.pop();
			if (	!dependencies.contains(tmp)
					&& !newLevel.isAncestorOf(pair.original.getLevel())) { 
				// we put it back in the candidate list
				putBackList.add(pair.original);
			}
			tmp = tmp.getParent();
			
			assert tmp.equals(pair.level);
		}
		if (DEBUG) debug("CaseSplitter: Backtracking done, size of split stack: "+splits.size());
		nextCase = splits.pop();
		
//		assert splits.size() == dispatcher.getLevel().getHeight() : "Splits: "+splits.size();
		return new ProverResult(putBackList, new HashSet<Clause>());
	}
	
	public void registerDumper(Dumper dumper) {
		dumper.addObject("CaseSplitter", candidates);
	} 
	
	private boolean accepts(Clause clause) {
		return inferrer.canInfer(clause);
	}

	public ProverResult addClauseAndDetectContradiction(Clause clause) {
		assert !candidates.contains(clause);
		assert !dispatcher.getLevel().isAncestorOf(clause.getLevel());
		
		if (accepts(clause)) candidates.add(clause);
		return ProverResult.EMPTY_RESULT;
	}

	public void removeClause(Clause clause) {
		for (Iterator<Clause> iter = candidates.iterator(); iter.hasNext();) {
			Clause existingClause = iter.next();
			if (existingClause.equals(clause)) {
				assert existingClause.equalsWithLevel(clause);
				iter.remove();
			}
		}
	}

	public boolean isSubsumed(Clause clause) {
		return false;
	}
	
	private static class SplitPair {
		Clause original;
		Set<Clause> left,right;
		Level level;
		
		SplitPair(Clause original, Set<Clause> left, Set<Clause> right, Level level) {
			this.original = original;
			this.left = left;
			this.right = right;
			this.level = level;
		}
	}

	@Override
	public String toString() {
		return "CaseSplitter";
	}
}
