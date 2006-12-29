/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.pog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.ISCWitness;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.pog.state.IWitnessTable;
import org.eventb.internal.core.tool.state.ToolState;

/**
 * @author Stefan Hallerstede
 *
 */
public class WitnessTable extends ToolState implements IWitnessTable {

	private final List<ISCWitness> witnesses;
	private final BecomesEqualTo primeSubstitution;
	
	private final ArrayList<ISCWitness> machineDetWitnesses;
	private final ArrayList<BecomesEqualTo> machineDetermist;
	private final ArrayList<BecomesEqualTo> machinePrimedDetermist;
	private final ArrayList<ISCWitness> eventDetWitnesses;
	private final ArrayList<BecomesEqualTo> eventDetermist;
	
	private final ArrayList<ISCWitness> nondetWitnesses;
	private final ArrayList<Predicate> nondetPredicates;
	
	private final HashSet<FreeIdentifier> witnessedVars;

	public WitnessTable(
			ISCWitness[] witnesses, 
			ITypeEnvironment typeEnvironment, 
			FormulaFactory factory, 
			IProgressMonitor monitor) throws CoreException {
		this.witnesses = Arrays.asList(witnesses);
		machineDetWitnesses = new ArrayList<ISCWitness>(witnesses.length);
		machineDetermist = new ArrayList<BecomesEqualTo>(witnesses.length);
		machinePrimedDetermist = new ArrayList<BecomesEqualTo>(witnesses.length);
		eventDetWitnesses = new ArrayList<ISCWitness>(witnesses.length);
		eventDetermist = new ArrayList<BecomesEqualTo>(witnesses.length);
		nondetWitnesses = new ArrayList<ISCWitness>(witnesses.length);
		nondetPredicates = new ArrayList<Predicate>(witnesses.length);
		witnessedVars = new HashSet<FreeIdentifier>(witnesses.length * 4 / 3 + 1);
	
		final LinkedList<FreeIdentifier> left = new LinkedList<FreeIdentifier>();
		final LinkedList<Expression> right = new LinkedList<Expression>();
	
		for (int i=0; i<witnesses.length; i++) {
			final Predicate predicate = witnesses[i].getPredicate(factory, typeEnvironment);
			final String name = witnesses[i].getLabel();
			final FreeIdentifier identifier = factory.makeFreeIdentifier(name, null);
			identifier.typeCheck(typeEnvironment);
			final FreeIdentifier unprimed = 
				identifier.isPrimed() ? 
						identifier.withoutPrime(factory) : 
						identifier;
			witnessedVars.add(identifier);
			boolean nondet = categorize(identifier, unprimed, predicate, witnesses[i], factory);
			if (nondet && identifier != unprimed) {
				left.add(unprimed);
				right.add(identifier);
			}	
		}
		
		if (left.size() == 0) {
			primeSubstitution = null;
		} else {
			primeSubstitution = factory.makeBecomesEqualTo(left, right, null);
			primeSubstitution.typeCheck(typeEnvironment);
		}
		
		machineDetWitnesses.trimToSize();
		machineDetermist.trimToSize();
		machinePrimedDetermist.trimToSize();
		eventDetWitnesses.trimToSize();
		eventDetermist.trimToSize();
		nondetWitnesses.trimToSize();
		nondetPredicates.trimToSize();
	}
	
	private boolean categorize(
			FreeIdentifier identifier, 
			FreeIdentifier unprimed, 
			Predicate predicate,
			ISCWitness witness,
			FormulaFactory factory) {
		
		// is it a deterministic witness?
		if (predicate instanceof RelationalPredicate) {
			RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;
			if (relationalPredicate.getTag() == Formula.EQUAL)
				if (relationalPredicate.getLeft().equals(identifier) 
						&& !Arrays.asList(relationalPredicate.getRight().getFreeIdentifiers()).contains(identifier)) {
					final BecomesEqualTo becomesEqualTo =
						factory.makeBecomesEqualTo(unprimed, relationalPredicate.getRight(), null);
					if (identifier == unprimed) {
						eventDetermist.add(becomesEqualTo);
						eventDetWitnesses.add(witness);
					} else {
						machineDetermist.add(becomesEqualTo);
						machineDetWitnesses.add(witness);
						machinePrimedDetermist.add(
								factory.makeBecomesEqualTo(
										identifier, 
										relationalPredicate.getRight(), null));
					}
					// it's deterministic
					return false;
				}
		}

		// or a nondeterministic witness?
		nondetWitnesses.add(witness);
		nondetPredicates.add(predicate);
		
		// it's nondeterministic
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.core.sc.IState#getStateType()
	 */
	public String getStateType() {
		return STATE_TYPE;
	}

	public BecomesEqualTo getPrimeSubstitution() {
		return primeSubstitution;
	}

	public List<ISCWitness> getWitnesses() {
		return witnesses;
	}

	public ArrayList<ISCWitness> getMachineDetWitnesses() {
		return machineDetWitnesses;
	}

	public ArrayList<BecomesEqualTo> getMachineDetAssignments() {
		return machineDetermist;
	}

	public ArrayList<BecomesEqualTo> getMachinePrimedDetAssignments() {
		return machinePrimedDetermist;
	}

	public ArrayList<ISCWitness> getEventDetWitnesses() {
		return eventDetWitnesses;
	}

	public ArrayList<BecomesEqualTo> getEventDetAssignments() {
		return eventDetermist;
	}

	public ArrayList<Predicate> getNondetPredicates() {
		return nondetPredicates;
	}

	public Set<FreeIdentifier> getWitnessedVariables() {
		return witnessedVars;
	}

	public ArrayList<ISCWitness> getNondetWitnesses() {
		return nondetWitnesses;
	}

}
