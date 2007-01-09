/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.pog.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPOFile;
import org.eventb.core.IPOSource;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pog.util.POGPredicate;
import org.eventb.core.pog.util.POGSource;
import org.eventb.core.pog.util.POGTraceablePredicate;

/**
 * @author Stefan Hallerstede
 *
 */
public class MachineEventPreserveInvariantModule extends MachineEventInvariantModule {

	/* (non-Javadoc)
	 * @see org.eventb.internal.core.pog.modules.MachineEventInvariantModule#isApplicable()
	 */
	@Override
	protected boolean isApplicable() {
//		 this POG module applies to refined events
		return abstractEvent != null;
	}
	
	@Override
	protected void createInvariantProofObligation(
			IPOFile target, 
			ISCInvariant invariant, 
			String invariantLabel, 
			Predicate invPredicate, 
			Set<FreeIdentifier> freeIdents,
			IProgressMonitor monitor) throws CoreException {
		
		LinkedList<BecomesEqualTo> substitution = new LinkedList<BecomesEqualTo>();
		if (concreteEventActionTable.getDeltaPrime() != null)
			substitution.add(concreteEventActionTable.getDeltaPrime());
		substitution.addAll(abstractEventActionTable.getDisappearingWitnesses());
		Predicate predicate = invPredicate.applyAssignments(substitution, factory);
		substitution.clear();		
		substitution.addAll(witnessTable.getEventDetAssignments());
		substitution.addAll(witnessTable.getMachineDetAssignments());
		if (witnessTable.getPrimeSubstitution() != null)
			substitution.add(witnessTable.getPrimeSubstitution());
		predicate = predicate.applyAssignments(substitution, factory);
		substitution.clear();
		if (concreteEventActionTable.getXiUnprime() != null)
			substitution.add(concreteEventActionTable.getXiUnprime());
		substitution.addAll(concreteEventActionTable.getPrimedDetAssignments());
		predicate = predicate.applyAssignments(substitution, factory);
		
// TODO: remove following:
//		LinkedList<POGPredicate> bighyp = new LinkedList<POGPredicate>();
//		bighyp.addAll(makeActionHypothesis());
//		bighyp.addAll(makeWitnessHypothesis());
		
		ArrayList<POGPredicate> bighyp = makeActionAndWitnessHypothesis(predicate);
		
		String sequentName = concreteEventLabel + "/" + invariantLabel + "/INV";
		createPO(
				target, 
				sequentName, 
				"Invariant " + (isInitialisation ? " establishment" : " preservation"),
				fullHypothesis,
				bighyp,
				new POGTraceablePredicate(predicate, invariant),
				sources(
						new POGSource(IPOSource.ABSTRACT_ROLE, abstractEvent),
						new POGSource(IPOSource.CONCRETE_ROLE, concreteEvent), 
						new POGSource(IPOSource.DEFAULT_ROLE, invariant)),
				hints(
						getLocalHypothesisSelectionHint(target, sequentName),
						getInvariantPredicateSelectionHint(target, invariant)),
				monitor);
	}

}
