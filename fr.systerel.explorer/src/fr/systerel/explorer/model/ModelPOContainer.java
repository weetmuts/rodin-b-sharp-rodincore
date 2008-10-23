/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
  *******************************************************************************/


package fr.systerel.explorer.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eventb.core.IAxiom;
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IPOSequent;
import org.eventb.core.IPSStatus;
import org.eventb.core.ITheorem;
import org.rodinp.core.IInternalElementType;

/**
 * An abstract class for any elements that may contain ProofObligations (Machines, Invariants...)
 *
 */
public abstract class ModelPOContainer implements IModelElement{
	
	// name for Label Provider
	public static final String DISPLAY_NAME = "Proof Obligations";
	
	protected IModelElement parent;
	
	protected HashMap<IPOSequent, ModelProofObligation> proofObligations = new HashMap<IPOSequent, ModelProofObligation>();

	public ModelProofObligation[] getProofObligations() {
		ModelProofObligation[] proofs = new ModelProofObligation[proofObligations.values().size()];
		return proofObligations.values().toArray(proofs);
	}
	
	public void addProofObligation(ModelProofObligation po){
		proofObligations.put(po.getIPOSequent(), po);
	}

	
	public ModelProofObligation getProofObligation(IPSStatus status){
		return proofObligations.get(status.getPOSequent());
	}
	
	/**
	 * 
	 * @return 	The IPSStatuses of the ProofObligations in this container 
	 * 			It is possible that some ProofObligatiosn don't have a status
	 */
	public IPSStatus[] getIPSStatuses() {
		List<IPSStatus> statuses = new LinkedList<IPSStatus>();
		for (ModelProofObligation po : proofObligations.values()) {
			if (po.getIPSStatus() != null) {
				statuses.add(po.getIPSStatus());
			}
		}
		IPSStatus[] results = new IPSStatus[statuses.size()];
		return statuses.toArray(results);
	}

	public IModelElement getModelParent() {
		return parent;
	}
	
	/**
	 * 
	 * @return True, if there's an undischarged ProofObligation in this container.
	 * False otherwise.
	 */
	public boolean hasUndischargedPOs(){
		for (ModelProofObligation po : proofObligations.values()) {
			if (!po.isDischarged()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return the total number of Proof Obligations
	 */
	public int getPOcount(){
		return proofObligations.size();
		
	}

	/**
	 * Gets the total number of proof obligations that belong to a certain element type (e.g invariants)
	 * @param aType The type of the element (invariant, theorem, event...)
	 * @return the total number of proof obligations that have an element of the given type as source
	 */
	public int getPOcount(IInternalElementType<?> aType){
		int result = 0;
		if (aType == IInvariant.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.getInvariants().length > 0) {
					result++;
				}
			}
		}
		if (aType == ITheorem.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.getTheorems().length > 0) {
					result++;
				}
			}
		}
		if (aType == IAxiom.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.getAxioms().length > 0) {
					result++;
				}
			}
		}
		if (aType == IEvent.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.getEvents().length > 0) {
					result++;
				}
			}
		}
		//return all proof obligations.
		if (aType == IPSStatus.ELEMENT_TYPE){
			result = getPOcount();
		}

		return result;
	}

	
	/**
	 * Gets the number of undischarged proof obligations that belong to a certain element type (e.g invariants)
	 * @param aType The type of the element (invariant, theorem, event...)
	 * @return The number of undischarged Proof Obligations (including Reviewed POs)
	 */
	public int getUndischargedPOcount(IInternalElementType<?> aType) {
		int result = 0;
		if (aType == IInvariant.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (!po.isDischarged() && po.getInvariants().length > 0) {
					result++;
				}
			}
		}
		if (aType == ITheorem.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (!po.isDischarged() && po.getTheorems().length > 0) {
					result++;
				}
			}
		}
		if (aType == IAxiom.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (!po.isDischarged() && po.getAxioms().length > 0) {
					result++;
				}
			}
		}
		if (aType == IEvent.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (!po.isDischarged() && po.getEvents().length > 0) {
					result++;
				}
			}
		}
		//return all undischarged proof obligations.
		if (aType == IPSStatus.ELEMENT_TYPE){
			result = getUndischargedPOcount();
		}

		return result;
	}
	
	/**
	 * 
	 * @return The number of undischarged Proof Obligations (including Reviewed POs)
	 */
	public int getUndischargedPOcount() {
		int result = 0;
		for (ModelProofObligation po : proofObligations.values()) {
			if (!po.isDischarged()) {
				result++;
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @return The number of broken Proof Obligations
	 */
	public int getBrokenPOcount() {
		int result = 0;
		for (ModelProofObligation po : proofObligations.values()) {
			if (po.isBroken()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * 
	 * @return The number of manually discharged Proof Obligations (not including reviewed POs)
	 */
	public int getManuallyDischargedPOcount() {
		int result = 0;
		for (ModelProofObligation po : proofObligations.values()) {
			if (po.isManual() &&po.isDischarged()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Gets the number of manually discharged proof obligations that belong to a certain element type (e.g invariants)
	 * @param aType The type of the element (invariant, theorem, event...)
	 * @return The number of manually discharged Proof Obligations (not including reviewed POs)
	 */
	public int getManuallyDischargedPOcount(IInternalElementType<?> aType) {
		int result = 0;
		if (aType == IInvariant.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isManual() &&po.isDischarged() && po.getInvariants().length > 0) {
					result++;
				}
			}
		}
		if (aType == ITheorem.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isManual() &&po.isDischarged() && po.getTheorems().length > 0) {
					result++;
				}
			}
		}
		if (aType == IAxiom.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isManual() &&po.isDischarged() && po.getAxioms().length > 0) {
					result++;
				}
			}
		}
		if (aType == IEvent.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isManual() &&po.isDischarged() && po.getEvents().length > 0) {
					result++;
				}
			}
		}
		//return all manually discharged proof obligations.
		if (aType == IPSStatus.ELEMENT_TYPE){
			result = getManuallyDischargedPOcount();
		}

		return result;
	}
	
	
	/**
	 * 
	 * @return The number of reviewed Proof Obligations
	 */
	public int getReviewedPOcount() {
		int result = 0;
		for (ModelProofObligation po : proofObligations.values()) {
			if (po.isReviewed()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Gets the number of reviewed proof obligations that belong to a certain element type (e.g invariants)
	 * @param aType The type of the element (invariant, theorem, event...)
	 * @return The number of reviewed Proof Obligations
	 */
	public int getReviewedPOcount(IInternalElementType<?> aType) {
		int result = 0;
		if (aType == IInvariant.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isReviewed() && po.getInvariants().length > 0) {
					result++;
				}
			}
		}
		if (aType == ITheorem.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isReviewed() && po.getTheorems().length > 0) {
					result++;
				}
			}
		}
		if (aType == IAxiom.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isReviewed() && po.getAxioms().length > 0) {
					result++;
				}
			}
		}
		if (aType == IEvent.ELEMENT_TYPE){
			for (ModelProofObligation po : proofObligations.values()) {
				if (po.isReviewed() && po.getEvents().length > 0) {
					result++;
				}
			}
		}
		//return all reviewed proof obligations.
		if (aType == IPSStatus.ELEMENT_TYPE){
			result = getReviewedPOcount();
		}

		return result;
	}
	
	
	public abstract String getLabel();
	
}
