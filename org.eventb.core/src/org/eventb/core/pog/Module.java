/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.core.pog;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPOFile;
import org.eventb.core.IPOPredicate;
import org.eventb.core.IPOPredicateSet;
import org.eventb.core.IPOSequent;
import org.eventb.core.IPOSource;
import org.eventb.core.pog.state.IStatePOG;
import org.eventb.core.state.IStateRepository;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * 
 * @author Stefan Hallerstede
 *
 */
public abstract class Module implements IModule {
	
	private static final String SEQ_HYP_NAME = "SEQHYP";
	private static final String PRD_NAME_PREFIX = "PRD";
	private static final String SRC_NAME_PREFIX = "SRC";
	private static final String HINT_NAME_PREFIX = "HINT";
	private static final String GOAL_NAME = "GOAL";
	
	public void createPO(
			IPOFile file, 
			String name,
			String desc,
			IPOPredicateSet globalHypothesis,
			List<POGPredicate> localHypothesis,
			POGPredicate goal,
			POGSource[] sources,
			POGHint[] hints,
			IProgressMonitor monitor) throws RodinDBException {
		
		IPOSequent sequent = file.getSequent(name);
		sequent.create(null, monitor);
		
		IPOPredicateSet hypothesis = sequent.getHypothesis(SEQ_HYP_NAME);
		hypothesis.create(null, monitor);
		hypothesis.setParentPredicateSet(globalHypothesis, monitor);
		
		putPOGPredicates(hypothesis, localHypothesis, monitor);
		
		IPOPredicate goalPredicate = sequent.getGoal(GOAL_NAME);
		putPredicate(goalPredicate, goal, monitor);
		
		sequent.setDescription(desc, monitor);
		
		putPOGSources(sequent, sources, monitor);
		
//		putPOGHints(sequent, hints, monitor);
	}

	private void putPredicate(IPOPredicate predicate, POGPredicate pogPredicate, IProgressMonitor monitor) throws RodinDBException {
		predicate.create(null, monitor);
		predicate.setPredicate(pogPredicate.getPredicate(), monitor);
		predicate.setSource(pogPredicate.getSource(), monitor);
	}
	
//	private void putPOGHints(
//			IPOSequent sequent, 
//			POGHint[] hints, 
//			IProgressMonitor monitor) throws RodinDBException {
//		
//		if (hints == null)
//			return;
//		
//		for (int idx=0; idx < hints.length; idx++) {
//			
//			IPOHint hint = sequent.getHint(HINT_NAME_PREFIX + idx);
//			hint.create(null, monitor);
//			hint.setHint(hints[idx].getValue(), monitor);
//
//		}
//
//	}
	
	private void putPOGSources(
			IPOSequent sequent, 
			POGSource[] sources, 
			IProgressMonitor monitor) throws RodinDBException {
		
		if (sources == null)
			return;
		
		for (int idx=0; idx < sources.length; idx++) {
			
			IPOSource source = sequent.getSource(SRC_NAME_PREFIX + idx);
			source.create(null, monitor);
			source.setSource(sources[idx].getSource().getSource(monitor), monitor);
			source.setRole(sources[idx].getRoleKey(), monitor);
		}

	}
	
	private void putPOGPredicates(
			IPOPredicateSet hypothesis, 
			List<POGPredicate> localHypothesis, 
			IProgressMonitor monitor) throws RodinDBException {
		
		if (localHypothesis == null)
			return;
		
		int index = 0;
		
		for (POGPredicate predicate : localHypothesis) {
			
			IPOPredicate poPredicate = hypothesis.getPredicate(PRD_NAME_PREFIX + index++);
			putPredicate(poPredicate, predicate, monitor);
		}

	}

	protected void initModules(
			IRodinElement element,
			IPOFile target,
			IModule[] modules,
			IStateRepository<IStatePOG> repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IModule module : modules) {
			module.initModule(element, target, repository, monitor);
		}
	}
	
	protected void processModules(
			IModule[] modules, 
			IRodinElement element, 
			IPOFile target,
			IStateRepository<IStatePOG> repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IModule module : modules) {
			module.process(element, target, repository, monitor);
		}
	}
	
	protected void endModules(
			IRodinElement element,
			IPOFile target,
			IModule[] modules, 
			IStateRepository<IStatePOG> repository, 
			IProgressMonitor monitor) throws CoreException {
		for (IModule module : modules) {
			module.endModule(element, target, repository, monitor);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.core.pog.IProcessorModule#initModule(org.rodinp.core.IRodinElement, org.eventb.core.IPOFile, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initModule(
			IRodinElement element, 
			IPOFile target, 
			IStateRepository<IStatePOG> repository, 
			IProgressMonitor monitor) throws CoreException {

		// by default nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.pog.IProcessorModule#endModule(org.rodinp.core.IRodinElement, org.eventb.core.IPOFile, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void endModule(
			IRodinElement element, 
			IPOFile target, 
			IStateRepository<IStatePOG> repository, 
			IProgressMonitor monitor) throws CoreException {
		// by default nothing to do
		
	}
	
	protected POGSource[] sources(POGSource... sources) {
		return sources;
	}
	
	protected POGPredicate[] hypotheses(POGPredicate... predicates) {
		return predicates;
	}

}
