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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eventb.core.IAxiom;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEvent;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IPORoot;
import org.eventb.core.IPSRoot;
import org.eventb.core.IPSStatus;
import org.eventb.core.ITheorem;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import fr.systerel.explorer.ExplorerUtils;
import fr.systerel.explorer.RodinNavigator;

/**
 * The Model is used to present the structure of the machines and contexts and
 * the proof obligations. The ModelController controls the model (e.g. updates
 * it, when the database changes) and grants access to its element such as
 * Projects, Machines, Invariants etc.
 * 
 */
public class ModelController implements IElementChangedListener {
	
	
	private static HashMap<IRodinProject, ModelProject> projects = new HashMap<IRodinProject, ModelProject>();
	RodinNavigator navigator;

	/**
	 * Create this controller and register it in the DataBase for changes.
	 * 
	 * @param navigator
	 */
	public ModelController(RodinNavigator navigator){
		RodinCore.addElementChangedListener(this);
		this.navigator = navigator;
	}

	/**
	 * No arguments constructor for testing purpose
	 */
	public ModelController() {
		// do nothing
	}

	/**
	 * Processes a RodinProject. Creates a model for this project (Machines,
	 * Contexts, Invariants etc.). Proof Obligations are not included in
	 * processing.
	 * 
	 * @param project
	 *            The Project to process.
	 */
	public static void processProject(IRodinProject project){
		try {
			ModelProject prj;
			if (!projects.containsKey(project)) {
				prj =  new ModelProject(project);
				projects.put(project, prj);
			}	
			prj =  projects.get(project);
			//only process if really needed
			if (prj.needsProcessing) {
				IContextRoot[] contexts = ExplorerUtils.getContextRootChildren(project);
				for (IContextRoot context : contexts) {
					prj.processContext(context);
				}
				prj.calculateContextBranches();
				IMachineRoot[] machines = ExplorerUtils.getMachineRootChildren(project);
				for (IMachineRoot machine : machines) {
					prj.processMachine(machine);
				}
				prj.calculateMachineBranches();
				prj.needsProcessing = false;
			}
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Gets the ModelInvariant for a given Invariant
	 * 
	 * @param invariant
	 *            The Invariant to look for
	 * @return The corresponding ModelInvariant, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelInvariant getInvariant(IInvariant invariant){
		ModelProject project = projects.get(invariant.getRodinProject());
		if (project != null) {
			return project.getInvariant(invariant);
		}
		return null;
	}
	
	/**
	 * Gets the ModelEvent for a given Event
	 * 
	 * @param event
	 *            The Event to look for
	 * @return The corresponding ModelEvent, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelEvent getEvent(IEvent event){
		ModelProject project = projects.get(event.getRodinProject());
		if (project != null) {
			return project.getEvent(event);
		}
		return null;
	}
	
	/**
	 * Gets the ModelAxiom for a given Axiom
	 * 
	 * @param axiom
	 *            The Axiom to look for
	 * @return The corresponding ModelAxiom, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelAxiom getAxiom(IAxiom axiom){
		ModelProject project = projects.get(axiom.getRodinProject());
		if (project != null) {
			return project.getAxiom(axiom);
		}
		return null;
	}

	/**
	 * Gets the ModelTheorem for a given Theorem
	 * 
	 * @param theorem
	 *            The Theorem to look for
	 * @return The corresponding ModelTheorem, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelTheorem getTheorem(ITheorem theorem){
		ModelProject project = projects.get(theorem.getRodinProject());
		if (project != null) {
			return project.getTheorem(theorem);
		}
		return null;
	}
	
	/**
	 * Gets the ModelMachine for a given MachineRoot
	 * 
	 * @param machineRoot
	 *            The MachineRoot to look for
	 * @return The corresponding ModelMachine, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelMachine getMachine(IMachineRoot machineRoot){
		ModelProject project = projects.get(machineRoot.getRodinProject());
		if (project != null) {
				return project.getMachine(machineRoot);
		}
		return null;
	}
	
	/**
	 * Removes a ModelMachine from the Model for a given MachineRoot. Also
	 * removes dependencies
	 * 
	 * @param machineRoot
	 *            The MachineRoot to look for
	 */
	public static void removeMachine(IMachineRoot machineRoot){
		ModelProject project = projects.get(machineRoot.getRodinProject());
		if (project != null ) {
				project.removeMachine(machineRoot);
		}
	}
	

	/**
	 * Gets the ModelProofObligation for a given IPSStatus
	 * 
	 * @param status
	 *            The IPSStatus to look for
	 * @return The corresponding ModelProofObligation, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelProofObligation getModelPO(IPSStatus status){
		ModelProject project = projects.get(status.getRodinProject());
		if (project != null) {
				return project.getProofObligation(status);
		}
		return null;
	}
	
	
	/**
	 * Gets the ModelContext for a given ContextRoot
	 * 
	 * @param contextRoot
	 *            The ContextRoot to look for
	 * @return The corresponding ModelContext, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelContext getContext(IContextRoot contextRoot){
		ModelProject project = projects.get(contextRoot.getRodinProject());
		if (project != null) {
				return project.getContext(contextRoot);
		}
		return null;
	}
	
	/**
	 * Removes a ModelContext from the Model for a given ContextRoot
	 * 
	 * @param contextRoot
	 *            The ContextRoot to remove
	 */
	public static void removeContext(IContextRoot contextRoot){
		ModelProject project = projects.get(contextRoot.getRodinProject());
		if (project != null) {
				project.removeContext(contextRoot);
		}
	}
	/**
	 * Gets the ModelProject for a given RodinProject
	 * 
	 * @param project
	 *            The RodinProjecct to look for
	 * @return The corresponding ModelProject, if there exists one,
	 *         <code>null</code> otherwise
	 */
	public static ModelProject getProject(IRodinProject project) {
		return projects.get(project);
	}
	
	/**
	 * Removes the corresponding ModelProject from the Model if it was present.
	 * 
	 * @param project
	 *            The Project to remove.
	 */
	public static void removeProject(IRodinProject project){
		projects.remove(project);
	}
	
	/**
	 * Gets the corresponding IMachineRoots for a set of ModelMachines
	 * 
	 * @param machs
	 *            The ModelMachines to convert
	 * @return The corresponding IMachineRoots
	 */
	public static IMachineRoot[] convertToIMachine(ModelMachine[] machs) {
		IMachineRoot[] results = new IMachineRoot[machs.length];
		for (int i = 0; i < machs.length; i++) {
			results[i] = machs[i].getInternalMachine();
			
		}
		return results;
	}
	
	/**
	 * Gets the corresponding IMachineRoots for a set of ModelMachines
	 * 
	 * @param machs
	 *            The ModelMachines to convert
	 * @return The corresponding IMachineRoots
	 */
	public static List<IMachineRoot> convertToIMachine(List<ModelMachine> machs) {
		List<IMachineRoot> results = new LinkedList<IMachineRoot>();
		for (Iterator<ModelMachine> iterator = machs.iterator(); iterator.hasNext();) {
			 results.add(iterator.next().getInternalMachine());
		}
		return results;
	}

	/**
	 * Gets the corresponding IContextRoots for a set of ModelContexts
	 * 
	 * @param conts
	 *            The ModelContexts to convert
	 * @return The corresponding IContextRoots
	 */
	public static IContextRoot[] convertToIContext(ModelContext[] conts) {
		IContextRoot[] results = new IContextRoot[conts.length];
		for (int i = 0; i < conts.length; i++) {
			results[i] = conts[i].getInternalContext();
			
		}
		return results;
	}

	/**
	 * Gets the corresponding IContextRoots for a set of ModelContexts
	 * 
	 * @param conts
	 *            The ModelContexts to convert
	 * @return The corresponding IContextRoots
	 */
	public static List<IContextRoot> convertToIContext(List<ModelContext> conts) {
		List<IContextRoot> results = new LinkedList<IContextRoot>();
		for (Iterator<ModelContext> iterator = conts.iterator(); iterator.hasNext();) {
			 results.add(iterator.next().getInternalContext());
		}
		return results;
	}
	
	/**
	 * React to changes in the database.
	 *
	 */
	public void elementChanged(ElementChangedEvent event) {	
		
		DeltaProcessor processor =  new DeltaProcessor(event.getDelta());
		final ArrayList<IRodinElement> toRefresh = processor.getToRefresh();
		final ArrayList<IRodinElement> toRemove = processor.getToRemove();
		
		navigator.getViewSite().getShell().getDisplay().asyncExec(new Runnable(){
			public void run() {
				cleanUpModel(toRemove);
				//refresh the model
				for (IRodinElement elem : toRefresh) {
					refreshModel(elem);
					
				}
				refreshViewer(toRefresh);
		}});
	}

	
	/**
	 * Refreshes the model
	 * 
	 * @param element
	 *            The element to refresh
	 */
	public void refreshModel(IRodinElement element) {
		if (!(element instanceof IRodinDB)) {
			ModelProject project = projects.get(element.getRodinProject());
			if (element instanceof IRodinProject) {
				project.needsProcessing = true;
				processProject((IRodinProject)element);
			}
			
			if (element instanceof IMachineRoot) {
				project.processMachine((IMachineRoot)element);
			}
			if (element instanceof IContextRoot) {
				project.processContext((IContextRoot)element);
			}
			if (element instanceof IPORoot) {
				IPORoot root = (IPORoot) element;
				//get corresponding machine or context
				if (root.getMachineRoot().exists()) {
					ModelMachine machine = getMachine(root.getMachineRoot());
					machine.poNeedsProcessing = true;
					machine.processPORoot();
					//process the statuses as well
					machine.psNeedsProcessing = true;
					machine.processPSRoot();
				}
				if (root.getContextRoot().exists()) {
					ModelContext context = getContext(root.getContextRoot());
					context.poNeedsProcessing = true;
					context.processPORoot();
					//process the statuses as well
					context.psNeedsProcessing = true;
					context.processPSRoot();
				}
			}
			if (element instanceof IPSRoot) {
				IPSRoot root = (IPSRoot) element;
				//get corresponding machine or context
				if (root.getMachineRoot().exists()) {
					ModelMachine machine = getMachine(root.getMachineRoot());
					machine.psNeedsProcessing = true;
					machine.processPSRoot();
				}
				if (root.getContextRoot().exists()) {
					ModelContext context = getContext(root.getContextRoot());
					context.psNeedsProcessing = true;
					context.processPSRoot();
				}
			}
		}
	}
	
	public void refreshViewer(ArrayList<IRodinElement> toRefresh) {
		TreeViewer viewer = navigator.getCommonViewer();
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			// refresh everything
			if (toRefresh.contains(RodinCore.getRodinDB())) {
				viewer.refresh();
			} else {
				for (Object elem : toRefresh) {
					if (elem instanceof IRodinProject) {
						viewer.refresh(((IRodinProject)elem).getProject());
					} else {
						viewer.refresh(elem);
					}
				}
			}
		}
		
	}

	/**
	 * Removes the corresponding elements from the model
	 * @param toRemove
	 */
	public void cleanUpModel(ArrayList<IRodinElement> toRemove){
		for (IRodinElement element : toRemove) {
			if (element instanceof IContextRoot) {
				removeContext((IContextRoot) element);
			}
			if (element instanceof IMachineRoot) {
				removeMachine((IMachineRoot) element);
			}
			if (element instanceof IRodinProject) {
				removeProject((IRodinProject) element);
			}
		}
	}
}
