package fr.systerel.explorer.poModel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eventb.core.IPSFile;
import org.eventb.core.IPSStatus;

public class POContainer {

	private List<ProofObligation> proofObligations = new LinkedList<ProofObligation>();

	public ProofObligation[] getProofObligations() {
		ProofObligation[] proofs = new ProofObligation[proofObligations.size()];
		return proofObligations.toArray(proofs);
	}

	public IPSStatus[] getIPSStatuses() {
		List<IPSStatus> statuses = new LinkedList<IPSStatus>();
		for (Iterator<ProofObligation> iterator =proofObligations.iterator(); iterator.hasNext();) {
			ProofObligation po = iterator.next();
			statuses.add(po.getIPSStatus());
		}
		IPSStatus[] results = new IPSStatus[statuses.size()];
		return statuses.toArray(results);
	}
	
	public void addProofObligation(ProofObligation po) {
		proofObligations.add(po);
	}
	
	public void removeProofObligation(ProofObligation po) {
		proofObligations.remove(po);
	}
	
	public static String DISPLAY_NAME = "Proof Obligations";

}
