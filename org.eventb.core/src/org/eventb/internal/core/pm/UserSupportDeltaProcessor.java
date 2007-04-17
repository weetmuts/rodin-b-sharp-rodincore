package org.eventb.internal.core.pm;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPSFile;
import org.eventb.core.IPSStatus;
import org.eventb.core.pm.IProofState;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

public class UserSupportDeltaProcessor {
	Set<IPSStatus> toBeTrashed;

	Set<IPSStatus> toBeReloaded;

	Set<IPSStatus> toBeReused;

	Set<IPSStatus> toBeRebuilt;

	Set<IPSStatus> toBeDeleted;

	Set<IPSStatus> toBeAdded;

	boolean needRefreshed;

	private UserSupport userSupport;
	
	public UserSupportDeltaProcessor(UserSupport userSupport) {
		this.userSupport = userSupport;
		toBeTrashed = new HashSet<IPSStatus>();
		toBeReloaded = new HashSet<IPSStatus>();
		toBeReused = new HashSet<IPSStatus>();
		toBeRebuilt = new HashSet<IPSStatus>();
		toBeAdded = new HashSet<IPSStatus>();
		toBeDeleted = new HashSet<IPSStatus>();
		needRefreshed = false;
	}

	public Set<IPSStatus> getToBeDeleted() {
		return toBeDeleted;
	}

	public boolean needRefreshed() {
		return needRefreshed;
	}

	public Set<IPSStatus> getToBeReloaded() {
		return toBeReloaded;
	}

	public Set<IPSStatus> getToBeReused() {
		return toBeReused;
	}

	public Set<IPSStatus> getToBeRebuilt() {
		return toBeRebuilt;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("** Delta Processor Status **");
		buffer.append("Need refresh? ");
		buffer.append(needRefreshed);
		buffer.append("\n");
		buffer.append("To be reloaded:");
		buffer.append(toBeReloaded);
		buffer.append("\n");
		buffer.append("To be trashed: ");
		buffer.append(toBeTrashed);
		buffer.append("\n");
		buffer.append("To be rebuilt: ");
		buffer.append(toBeRebuilt);
		buffer.append("\n");
		buffer.append("To be reused: ");
		buffer.append(toBeReused);
		buffer.append("\n");
		buffer.append("To be added: ");
		buffer.append(toBeAdded);
		buffer.append("\n");
		buffer.append("To be deleted: ");
		buffer.append(toBeDeleted);
		buffer.append("\n");
		return buffer.toString();
	}
	
	public void processDelta(IRodinElementDelta elementChangedDelta,
			IProgressMonitor monitor) {
		IRodinElement element = elementChangedDelta.getElement();
		IPSFile input = userSupport.getInput();
		
		// Root DB
		if (element instanceof IRodinDB) {
			for (IRodinElementDelta d : elementChangedDelta
					.getAffectedChildren()) {
				processDelta(d, monitor);
			}			
		}

		// IRodinProject
		if (element instanceof IRodinProject) {
			IRodinElement parent = input.getParent();
			if (parent.equals(element)) {
				for (IRodinElementDelta d : elementChangedDelta
						.getAffectedChildren()) {
					processDelta(d, monitor);
				}
			}
			return;
		}

		// IPSFile
		if (element instanceof IPSFile) {
			if (input.equals(element)) {
				for (IRodinElementDelta d : elementChangedDelta
						.getAffectedChildren()) {
					processDelta(d, monitor);
				}
			}
			return;
		}

		// IPSStatus has been changed
		if (element instanceof IPSStatus) {
			int kind = elementChangedDelta.getKind();

			if (kind == IRodinElementDelta.ADDED) {
				if (UserSupportUtils.DEBUG)
					UserSupportUtils.debug("IPSStatus changed: "
							+ element.getElementName() + " is added");
				IPSStatus psStatus = (IPSStatus) element;
				toBeAdded.add(psStatus);
				needRefreshed = true;
				return;
			}

			if (kind == IRodinElementDelta.REMOVED) {
				if (UserSupportUtils.DEBUG)
					UserSupportUtils.debug("IPSStatus changed: "
							+ element.getElementName() + " is removed");
				// Try to reuse
				IPSStatus psStatus = (IPSStatus) element;
				toBeDeleted.add(psStatus);
				toBeTrashed.add(psStatus);
				needRefreshed = true;
				return;
			}

			if (kind == IRodinElementDelta.CHANGED) {

				int flags = elementChangedDelta.getFlags();

				if ((flags & IRodinElementDelta.F_REORDERED) != 0) {
					needRefreshed = true;
				}

				if (((flags & IRodinElementDelta.F_CONTENT) != 0)
						|| ((flags & IRodinElementDelta.F_ATTRIBUTE) != 0)) {
					// Try to reuse
					IPSStatus psStatus = (IPSStatus) element;
					IProofState state = userSupport.proofStates.get(psStatus);

					// Do nothing if the state is uninitialised
					if (state.isUninitialised())
						return;

					// If the state is not modified, reload the proof from the
					// DB
					if (!state.isDirty()) {
						toBeReloaded.add(psStatus);
						return;
					}

					// if the state is dischared automatically, trash the
					// current proof and reload the proof from the DB
					try {
						if (state.isSequentDischarged()) {
							if (UserSupportUtils.DEBUG)
								UserSupportUtils
										.debug("Proof Discharged in file, to be trashed then reloaded");
							toBeTrashed.add(psStatus);
							toBeReloaded.add(psStatus);
							return;
						}
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						if (state.isProofReusable()) {
							if (UserSupportUtils.DEBUG)
								UserSupportUtils
										.debug("Proof is reusable, to be reused");
							toBeReused.add(psStatus);
							return;
						}

						// Trash the current proof tree and then re-build
						else {
							if (UserSupportUtils.DEBUG)
								UserSupportUtils
										.debug("Proof is NOT reusable, to be trashed and rebuilt");
							toBeTrashed.add(psStatus);
							toBeRebuilt.add(psStatus);
						}
					} catch (RodinDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return;
			}
		}
	}

}
