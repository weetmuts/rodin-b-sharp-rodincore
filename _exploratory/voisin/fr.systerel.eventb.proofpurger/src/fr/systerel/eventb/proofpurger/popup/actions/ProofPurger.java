/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package fr.systerel.eventb.proofpurger.popup.actions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eventb.core.IEventBFile;
import org.eventb.core.IPOFile;
import org.eventb.core.IPRFile;
import org.eventb.core.IPRProof;
//import org.eventb.core.pm.IProofPurger;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * Provides proof purging facilities. Being given projects or files, it allows
 * to determine potentially unused proofs (some of them might be intended to be
 * used in the future). It can also perform actual deletion of unused proofs.
 * 
 * @author Nicolas Beauger
 * 
 */
public class ProofPurger implements IProofPurger {

	private static final boolean DEBUG = false;
	
	private static ProofPurger instance;
	
	private ProofPurger() {
		// Singleton: Private default constructor
	}

	public static ProofPurger getDefault() {
		if (instance == null)
			instance = new ProofPurger();
		return instance;
	}

	/**
	 * Computes an array of potentially unused proofs. Actually, it filters on
	 * proofs which have no associated PO.
	 * 
	 * @param projectsOrFiles
	 *            Selection of projects or files to be searched in. Its elements
	 *            types should be either IRodinProject or IEventBFile.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that
	 *            no progress should be reported and that the operation cannot
	 *            be canceled.
	 * @return An array containing potentially unused proofs.
	 * @throws RodinDBException
	 */
	public IPRProof[] computeUnusedProofs(IRodinElement[] projectsOrFiles,
			IProgressMonitor monitor) throws RodinDBException {
		final IPRProof[] cancelReturnValue = new IPRProof[0];
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.beginTask(Messages.proofpurger_computingunusedproofs, 100);
		progress.subTask(Messages.proofpurger_extractingprooffiles);
		debugHook();
		Set<IPRFile> prFilesToProcess = extractProofFiles(projectsOrFiles,
				progress.newChild(20));
		if (progress.isCanceled() || prFilesToProcess == null)
			return cancelReturnValue;

		progress.subTask(Messages.proofpurger_extractingunusedproofs);
		debugHook();
		List<IPRProof> unusedProofs = extractUnusedProofs(prFilesToProcess,
				progress.newChild(80));
		if (progress.isCanceled() || unusedProofs == null)
			return cancelReturnValue;
		debugHook();
		
		return unusedProofs.toArray(new IPRProof[unusedProofs.size()]);
	}

	/**
	 * Deletes all given unused proofs. If any of these proofs are actually
	 * used, throws IllegalArgumentException.
	 * 
	 * @param proofs
	 *            An array containing proofs to delete.
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts <code>null</code>, indicating that
	 *            no progress should be reported and that the operation cannot
	 *            be canceled.
	 * @throws IllegalArgumentException
	 * @throws RodinDBException
	 */
	public void purgeUnusedProofs(IPRProof[] proofs, IProgressMonitor monitor)
			throws IllegalArgumentException, RodinDBException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		progress.beginTask(Messages.proofpurger_deletingselectedproofs, 100);
		progress.subTask(Messages.proofpurger_verifyingselectedproofs);
		debugHook();
		if (!areAllUnused(proofs)) {
			throw new IllegalArgumentException(
					Messages.proofpurger_tryingtodeleteusedproofs);
		}
		progress.worked(20);
		if (progress.isCanceled()) return;

		monitor.subTask(Messages.proofpurger_deleting);
		debugHook();
		Set<IPRFile> openProofFiles = new LinkedHashSet<IPRFile>();
		deleteProofs(proofs, openProofFiles, progress.newChild(60));
		progress.setWorkRemaining(20);
		if (progress.isCanceled()) return;
		
		monitor.subTask(Messages.proofpurger_savingchanges);
		debugHook();
		saveProofFiles(openProofFiles, progress.newChild(20));
		if (progress.isCanceled()) return;
		debugHook();
	}
	
	private static void debugHook() {
		if (DEBUG) {
			for (int i=0;i<80000000;i++){
				Integer g=new Integer(i);
				g = g + 1;
			}
		}
	}

	private static boolean isUsed(IPRProof pr) {
		final String name = pr.getElementName();
		final IPOFile poFile = ((IPRFile) pr.getRodinFile()).getPOFile();
		return poFile.getSequent(name).exists();
	}

	private static boolean areAllUnused(IPRProof[] proofs) {
		for (IPRProof pr : proofs) {
			if (isUsed(pr)) {
				return false;
			}
		}
		return true;
	}

	private static Set<IPRFile> extractProofFiles(
			IRodinElement[] projectsOrFiles, IProgressMonitor monitor) throws RodinDBException {
		Set<IPRFile> prFilesToProcess = new LinkedHashSet<IPRFile>();
		SubMonitor progress = SubMonitor.convert(monitor);

		if (projectsOrFiles.length > 0) {
			progress.setWorkRemaining(projectsOrFiles.length);
			for (IRodinElement elem : projectsOrFiles) {
				if (elem instanceof IEventBFile) {
					final IPRFile prFile = ((IEventBFile) elem).getPRFile();
					prFilesToProcess.add(prFile);
				} else if (elem instanceof IRodinProject) {
					addProject(prFilesToProcess, (IRodinProject) elem);
				}
				progress.worked(1);
			}
		} else {
			progress.done();
		}
		return prFilesToProcess;
	}

	private static List<IPRProof> extractUnusedProofs(
			Set<IPRFile> prFilesToProcess, IProgressMonitor monitor) throws RodinDBException {
		List<IPRProof> unusedProofs = new ArrayList<IPRProof>();
		final int size = prFilesToProcess.size();
		SubMonitor progress = SubMonitor.convert(monitor);

		if (size > 0) {
			progress.setWorkRemaining(size);
			for (IPRFile currentFile : prFilesToProcess) {
				IPRProof[] proofs = currentFile.getProofs();
				for (IPRProof pr : proofs) {
					if (!isUsed(pr)) {
						unusedProofs.add(pr);
					}
				}
				progress.worked(1);
				debugHook();
				if (progress.isCanceled()) {
					return null;
				}
			}
		} else {
			progress.done();
		}
		return unusedProofs;
	}

	private static void addProject(Set<IPRFile> prFilesToProcess,
			IRodinProject rodinProjectToAdd) throws RodinDBException {
		for (IRodinFile rf : rodinProjectToAdd.getRodinFiles()) {
			if (rf.getElementType() == IPRFile.ELEMENT_TYPE) {
				prFilesToProcess.add((IPRFile) rf);
			}
		}
	}

	private static Set<IPRFile> deleteProofs(IPRProof[] proofs,
			Set<IPRFile> prOpenFiles, IProgressMonitor monitor)
			throws RodinDBException {
		SubMonitor progress = SubMonitor.convert(monitor);
		if (proofs.length > 0) {
			progress.setWorkRemaining(proofs.length);
			for (IPRProof pr : proofs) {
				if (pr.exists()) {
					prOpenFiles.add((IPRFile) pr.getRodinFile());
					pr.delete(false, null);
				}
				progress.worked(1);
				debugHook();
				if (progress.isCanceled()) {
					return null;
				}
			}
		} else {
			progress.done();
		}
		return prOpenFiles;
	}

	private static void saveProofFiles(Set<IPRFile> prFiles,
			IProgressMonitor monitor) throws RodinDBException {
		final int size = prFiles.size();
		SubMonitor progress = SubMonitor.convert(monitor);

		if (size > 0) {
			progress.setWorkRemaining(size);
			for (IPRFile prFile : prFiles) {
				if (prFile.exists()) {
					if (prFile.hasUnsavedChanges()) {
						if (prFile.getProofs().length == 0
								&& !prFile.getPSFile().exists()) {
							prFile.delete(false, null);
						} else {
							prFile.save(null, false);
						}
					}
				}
				progress.worked(1);
				if (progress.isCanceled()) {
					return;
				}
			}
		} else {
			progress.done();
		}
	}
}
