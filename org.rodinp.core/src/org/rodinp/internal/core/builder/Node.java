/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.internal.core.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.rodinp.core.RodinCore;
import org.rodinp.core.builder.TempMarkerHelper;
import org.rodinp.internal.core.ElementTypeManager;

/**
 * @author halstefa
 *
 */
public class Node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2715764862822077579L;
	private String name; // name of the resource (full name in workspace!)
	private String fileElementTypeId; // the extension of the resource
	private LinkedList<Link> pred; // the predecessor list
	private String producerId; // producerId to be run to produce the resource of this node
	private boolean dated; // true if the resource of this node needs to be (re-)created
	private boolean phantom; // a node that was created by a dependency requirement
	private boolean cycle; // node is on a cycle
	
	// temporary data for construction of topological order
	private int totalCount; // number of predecessors of this node (for topological sort)
	private ArrayList<Node> succNodes; // successors of this node (for topological sort)
	private ArrayList<Link> succLinks; // successors of this node (for topological sort)
	private HashMap<String, Node> targets; // the set of names of the successors
	
	transient private int succPos; // Position in succ* during graph traversal
	
	transient protected int count; // number of predecessors of this node remaining in the unprocessed top sort
	transient protected boolean done; // nodes with count zero and done are already in the ordered list
	
	transient private IPath path; // the path corresponding to name (cache)
	
	/**
	 * cache for the file resource
	 */
	transient private IFile file;
	
	public Node() {
		name = null;
		fileElementTypeId = null;
		pred = new LinkedList<Link>();
		producerId = null;
		dated = true;
		totalCount = 0;
		succNodes = new ArrayList<Node>(3);
		succLinks = new ArrayList<Link>(3);
		targets = new HashMap<String, Node>(3);
		done = false;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		return name.equals(((Node) o).name);
	}
	
	protected void addLink(Node origin, Node source, String id, Link.Provider prov, Link.Priority prio) throws CoreException {
		Link link = new Link(prov, prio, id, source, origin);
		if(pred.contains(link))
			throw new CoreException(new Status(IStatus.ERROR,
					RodinCore.PLUGIN_ID, 
					Platform.PLUGIN_ERROR, "Duplicate link: " + link.toString(), null)); //$NON-NLS-1$
		source.targets.put(this.name, this);
		pred.add(link);
		totalCount++;
		if(source.succPos <= source.succSize())
			count++;
		
		if(prio == Link.Priority.LOW) {
			source.succNodes.add(this);
			source.succLinks.add(link);
		} else {
			source.succNodes.add(0, this);
			source.succLinks.add(0, link);
		}
	}
	
	protected void removeLinks(String id) {
		LinkedList<Link> predCopy = new LinkedList<Link>(pred);
		for(Link link : predCopy) {
			if(link.id.equals(id)) {
				link.source.targets.remove(this.name);
				pred.remove(link);
				totalCount--;
				count--;
				
				link.source.succNodes.remove(this);
				link.source.succLinks.remove(link);
			}
		}
	}
	
	protected Collection<IPath> getSources(String id) {
		ArrayList<IPath> sources = new  ArrayList<IPath>(pred.size());
		for(Link link : pred) {
			if(link.id.equals(id))
				sources.add(link.source.getPath());
		}
		return sources;
	}
	
	protected IPath getPath() {
		if(path == null)
			path = new Path(name);
		return path;
	}

	protected String getName() {
		return name;
	}

	protected void setPath(IPath path) {
		this.path = path;
		this.name = path.toString();
		
		final ElementTypeManager manager = ElementTypeManager.getElementTypeManager();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		this.file = workspace.getRoot().getFile(path);
		if (file != null)
			this.fileElementTypeId = manager.getFileElementType(file);
		else
			this.fileElementTypeId = null;
	}
	
	protected int getInCount() {
		return totalCount;
	}


	protected void setProducerId(String tool) {
		this.producerId = tool;
	}
	
	protected String getProducerId() {
		return producerId;
	}
	
	protected void markSuccessorsDated() {
		for(Node suc : succNodes) {
			suc.setDated(true);
		}
	}
	
	protected boolean hasSuccessor(Node node) {
		return succNodes.contains(node);
	}
	
	protected void nextSucc() {
		succPos++;
	}
	
	protected int getSuccPos() {
		return succPos;
	}

	protected Node succNode() {
		return (succPos < succNodes.size()) ? succNodes.get(succPos) : null;
	}

	protected Link succLink() {
		return (succPos < succLinks.size()) ? succLinks.get(succPos) : null;
	}

	protected IFile getFile() {
		if (file != null)
			return file;
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(getPath());
		return file;
	}
	
	protected void setDated(boolean value) {
		dated = value;
	}
	
	protected boolean isDated() {
		return dated;
	}
	
	protected void initForSort() {
		count = totalCount;
		done = false;
		succPos = 0;
	}
	
	protected int succSize() {
		return succNodes.size();
	}
	
	protected String printNode() {
		String res = name + " :";
		for(Node node : succNodes) {
			res = res + " " + node.name;
		}
		return res;
	}
	
	protected void unlink() {
		for(Link link : pred) {
			link.source.targets.remove(this.name);
			
			link.source.succNodes.remove(this);
			link.source.succLinks.remove(link);
		}
		int size = succNodes.size();
		for(int pos = 0; pos < size; pos++) {
			Node node = succNodes.get(pos);
			node.dated = true;
			node.pred.remove(succLinks.get(pos));
			node.totalCount--;
			node.count--;
		}
	}
	
	protected void removeSuccessorToolCount() {
		for(int pos = 0; pos < succNodes.size(); pos++) {
			if(succLinks.get(pos).prov == Link.Provider.TOOL) {
				succNodes.get(pos).count--;
			}
		}
	}
	
	protected void markReachable() {
		if(!done)
			return;
		done = false;
		for(Node node : succNodes)
			node.markReachable();
	}
	
	protected void addOriginToCycle() {
		for(Link link : pred) {
			if(link.source.count > 0) {
				file = link.origin.getFile();
				link.origin.dated = true;
				if(file != null)
					TempMarkerHelper.addMarker(file, IMarker.SEVERITY_ERROR, "Resource in dependency cycle"); //$NON-NLS-1$
				else if(RodinBuilder.DEBUG)
					System.out.println(getClass().getName() + ": File not found: " + link.origin.getName()); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @return Returns the phantom.
	 */
	protected boolean isPhantom() {
		return phantom;
	}

	/**
	 * @param phantom The phantom to set.
	 */
	protected void setPhantom(boolean phantom) {
		this.phantom = phantom;
	}

	/**
	 * @return Returns the cycle.
	 */
	protected boolean isCycle() {
		return cycle;
	}

	/**
	 * @param cycle The cycle to set.
	 */
	protected void setCycle(boolean cycle) {
		this.cycle = cycle;
	}

	/**
	 * @return Returns the fileElementTypeId.
	 */
	public String getFileElementTypeId() {
		return fileElementTypeId;
	}

}
