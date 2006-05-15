/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eventb.internal.core.pom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IPOFile;
import org.eventb.core.IPOSequent;
import org.eventb.core.IPRFile;
import org.eventb.core.IPRSequent;
import org.eventb.core.IProof;
import org.eventb.core.IProof.Status;
import org.eventb.core.prover.Lib;
import org.eventb.core.prover.sequent.IProverSequent;
import org.eventb.internal.core.protosc.ContextSC;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.basis.InternalElement;
import org.rodinp.core.basis.RodinElement;
import org.rodinp.core.builder.IAutomaticTool;
import org.rodinp.core.builder.IExtractor;
import org.rodinp.core.builder.IGraph;

/**
 * @author halstefa
 *
 */
public class AutoPOM implements IAutomaticTool, IExtractor {

//	private IInterrupt interrupt;
	private IProgressMonitor monitor;

	private IPOFile poFile;
	private IPRFile prFile;
	
	
	public void init(
			@SuppressWarnings("hiding") IPOFile poFile, 
			@SuppressWarnings("hiding") IPRFile prFile, 
			@SuppressWarnings("hiding") IProgressMonitor monitor) {
		this.monitor = monitor;
		this.poFile = poFile;
		this.prFile = prFile;
	}
	
	
	public boolean run(IFile file, 
			@SuppressWarnings("hiding") IProgressMonitor monitor) throws CoreException {

		IPRFile newPRFile = (IPRFile) RodinCore.create(file);
		IPOFile poIn = newPRFile.getPOFile();
		
		if (! poIn.exists()) {
			ContextSC.makeError("Source PO file does not exist.");
		}
		
		init(poIn, newPRFile, monitor);

		// Create the resulting PR file atomically.
		RodinCore.run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor saveMonitor) throws CoreException {
						Map<String, Status> newStatus = computeNewStatus();
						createFreshPRFile(newStatus);
					}
				}, monitor);
		
		new AutoProver().run(newPRFile);
		return true;
	}

	Map<String, Status> computeNewStatus() throws RodinDBException
	{
		IPRFile oldPRFile = prFile;
		IPOFile newPOFile = poFile;
		
		Map<String, IProverSequent> newPOs = POUtil.readPOs(newPOFile);		
		Map<String, Status> newStatus = new HashMap<String, Status>();
		
		if (oldPRFile.exists()){
			Map<String, IProverSequent> oldPOs = PRUtil.readPOs(oldPRFile);
			Map<String, Status> oldStatus = PRUtil.readStatus(oldPRFile);
			for (Map.Entry<String, IProverSequent> newPO : newPOs.entrySet()){
				String newPOname = newPO.getKey();
				IProverSequent newPOseq = newPO.getValue();
				IProverSequent oldPOseq = oldPOs.get(newPOname);
				if  ((oldPOseq != null) &&
						(Lib.sufficient(newPOseq,oldPOseq)))
					newStatus.put(newPOname,oldStatus.get(newPOname));
				else
					newStatus.put(newPOname,Status.PENDING);	
			}
		}
		else
		{
			for (String name : newPOs.keySet())
				newStatus.put(name,Status.PENDING);
		}
		return newStatus;
	}
	
	
	public void clean(IFile file, 
			@SuppressWarnings("hiding") IProgressMonitor monitor) throws CoreException {
		file.delete(true, monitor);
	}

	public void extract(IFile file, IGraph graph) throws CoreException {
		
		IPOFile in = (IPOFile) RodinCore.create(file);
		IPRFile target = in.getPRFile();
		
		IPath inPath = in.getPath();
		IPath targetPath = target.getPath();
		
		graph.addNode(targetPath, POMCore.AUTO_POM_TOOL_ID);
		graph.putToolDependency(inPath, targetPath, POMCore.AUTO_POM_TOOL_ID, true);
		graph.updateGraph();
	}

	void createFreshPRFile(Map<String, Status> newStatus) throws CoreException {

		if (prFile.exists()) 
		{
			for (IRodinElement child : prFile.getChildren()){
				((InternalElement)child).delete(true,null);
			}
			
		}
		else
		{
			IRodinProject project = prFile.getRodinProject();
			project.createRodinFile(prFile.getElementName(), true, null);
		}
		
		copyGlobalInfo();
		copySequents(newStatus);
		prFile.save(monitor, true);
	}
	
	private void copySequents(Map<String, Status> newStatus) throws RodinDBException{
		IPOSequent[] poSequents = poFile.getSequents();
		
		for (IPOSequent poSeq : poSequents)
		{
			IPRSequent prSeq = 
				(IPRSequent) prFile.createInternalElement(
						IPRSequent.ELEMENT_TYPE, poSeq.getName(), null, monitor);
			IRodinElement[] children = poSeq.getChildren();
		
			for (IRodinElement child : children){
				((IInternalElement)child).copy(prSeq,null,null,false,monitor);
			}
			IProof proof =
				(IProof) prSeq.createInternalElement(
						IProof.ELEMENT_TYPE, "", null, monitor);
			proof.setStatus(newStatus.get(prSeq.getName()));
		}
		
	}
	
	private void copyGlobalInfo() throws RodinDBException{
		
		IRodinElement[] children = ((RodinElement)poFile).getChildren();
		for (IRodinElement child : children){
			if (!(child.getElementType().equals(IPOSequent.ELEMENT_TYPE))){
				((IInternalElement)child).copy(prFile,null,null,false,monitor);
			}
		}
	}
}
