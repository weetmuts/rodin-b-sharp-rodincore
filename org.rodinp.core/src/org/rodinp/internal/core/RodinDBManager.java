/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * Strongly inspired by org.eclipse.jdt.internal.core.JavaModelManager.java which is
 * 
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.rodinp.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.Openable;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.RodinElement;
import org.rodinp.core.RodinFile;
import org.rodinp.internal.core.builder.RodinBuilder;
import org.rodinp.internal.core.util.Messages;
import org.rodinp.internal.core.util.Util;

/**
 * The <code>RodinDBManager</code> manages instances of <code>IRodinDB</code>.
 * <code>IElementChangedListener</code>s register with the <code>RodinDBManager</code>,
 * and receive <code>ElementChangedEvent</code>s for all <code>IRodinDB</code>s.
 * <p>
 * The single instance of <code>RodinDBManager</code> is available from
 * the static method <code>RodinDBManager.getRodinDBManager()</code>.
 */
// TODO add the notion of "focused" FileElement which will be used as a hint by the builder. 
public class RodinDBManager implements ISaveParticipant { 	
 
	/**
	 * Unique handle onto the RodinDB
	 */
	final RodinDB rodinDB = new RodinDB();
	
	/**
	 * Value of the content-type for Rodin source files
	 */
	public static final String RODIN_CONTENT_TYPE = RodinCore.PLUGIN_ID + ".rodin" ; //$NON-NLS-1$

	public static final IContentType rodinContentType =
		Platform.getContentTypeManager().getContentType(RODIN_CONTENT_TYPE);
	
	
//	private static final String INDEX_MANAGER_DEBUG = RodinCore.PLUGIN_ID + "/debug/indexmanager" ; //$NON-NLS-1$
//	private static final String COMPILER_DEBUG = RodinCore.PLUGIN_ID + "/debug/compiler" ; //$NON-NLS-1$
//	private static final String RODINDB_DEBUG = RodinCore.PLUGIN_ID + "/debug/rodindatabse" ; //$NON-NLS-1$
//	private static final String DELTA_DEBUG =RodinCore.PLUGIN_ID + "/debug/rodindelta" ; //$NON-NLS-1$
//	private static final String DELTA_DEBUG_VERBOSE =RodinCore.PLUGIN_ID + "/debug/rodindelta/verbose" ; //$NON-NLS-1$
//	private static final String BUILDER_DEBUG = RodinCore.PLUGIN_ID + "/debug/builder" ; //$NON-NLS-1$
//	private static final String COMPLETION_DEBUG = RodinCore.PLUGIN_ID + "/debug/completion" ; //$NON-NLS-1$
//	private static final String RESOLUTION_DEBUG = RodinCore.PLUGIN_ID + "/debug/resolution" ; //$NON-NLS-1$
//	private static final String SELECTION_DEBUG = RodinCore.PLUGIN_ID + "/debug/selection" ; //$NON-NLS-1$
//	private static final String SEARCH_DEBUG = RodinCore.PLUGIN_ID + "/debug/search" ; //$NON-NLS-1$

//	public static final String COMPLETION_PERF = RodinCore.PLUGIN_ID + "/perf/completion" ; //$NON-NLS-1$
//	public static final String SELECTION_PERF = RodinCore.PLUGIN_ID + "/perf/selection" ; //$NON-NLS-1$
//	public static final String DELTA_LISTENER_PERF = RodinCore.PLUGIN_ID + "/perf/rodindeltalistener" ; //$NON-NLS-1$

	/**
	 * Shortcut class to use instead of the long fully parameterized HashMap.
	 */
	public static final class ElementMap extends HashMap<IRodinElement, RodinElementInfo> {
		private static final long serialVersionUID = -2261316668279408019L;
	}
	
	/**
	 * Returns the Rodin element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Rodin element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IRodinProject</code></li>
	 *	<li>a Rodin file - the element returned is the corresponding <code>RodinFile</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IRodinDB</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Rodin element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IRodinElement create(IResource resource, IRodinProject project) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return RodinCore.create((IProject) resource);
			case IResource.FILE :
				return create((IFile) resource, project);
			case IResource.ROOT :
				return RodinCore.create((IWorkspaceRoot) resource);
			default :
				return null;
		}
	}

	/**
	 * Returns the Rodin element corresponding to the given file, its project
	 * being the given project. Returns <code>null</code> if unable to
	 * associate the given file with a Rodin element.
	 * <p>
	 * The file must have an associated content type which is a subtype of the
	 * Rodin content type.
	 * </p>
	 * <p>
	 * Creating a Rodin element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 * </p>
	 */
	public static RodinElement create(IFile file, IRodinProject project) {
		return createRodinFileFrom(file, project);
	}

	/**
	 * Creates and returns a Rodin file element for the given file, its project
	 * being the given project. Returns <code>null</code> if unable to
	 * recognize the file type.
	 */
	public static RodinFile createRodinFileFrom(IFile file, IRodinProject project) {

		if (file == null) return null;

		if (project == null) {
			project = RodinCore.create(file.getProject());
		}
		return project.getRodinFile(file.getName());
	}
	
	/**
	 * The singleton manager
	 */
	private static RodinDBManager MANAGER = new RodinDBManager();

	/**
	 * Infos cache.
	 */
	public RodinDBCache cache = new RodinDBCache();
	
	/*
	 * Temporary cache of newly opened elements
	 */
	private ThreadLocal<ElementMap> temporaryCache = new ThreadLocal<ElementMap>();

	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map elementsOutOfSynchWithBuffers = new HashMap(11);
	
	/**
	 * Holds the state used for delta processing.
	 */
	public DeltaProcessingState deltaState = new DeltaProcessingState();

	/**
	 * Table from IProject to PerProjectInfo.
	 * NOTE: this object itself is used as a lock to synchronize creation/removal of per project infos
	 */
	protected PerProjectMap perProjectInfos = new PerProjectMap(5);
	
	public static class PerProjectInfo {
		
		public IProject project;
		public Object savedState;
		public boolean triedRead;
		public Map resolvedPathToRawEntries; // reverse map from resolved path to raw entries
		public IPath outputLocation;
		
		public IEclipsePreferences preferences;
		public Hashtable options;
		
		public PerProjectInfo(IProject project) {
			this.triedRead = false;
			this.savedState = null;
			this.project = project;
		}
		
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(this.project.getFullPath());
			buffer.append("\n");
			return buffer.toString();
		}
	}
	
	/**
	 * Shortcut class to use instead of the long fully parameterized HashMap.
	 */
	protected static final class PerProjectMap extends HashMap<IProject, PerProjectInfo> {
		private static final long serialVersionUID = 4034714772753325361L;
		
		public PerProjectMap(int initialCapacity) {
			super(initialCapacity);
		}
	}
	
	public static boolean VERBOSE = false;
	
	/**
	 * Constructs a new RodinDBManager
	 */
	private RodinDBManager() {
		// singleton: prevent others from creating a new instance
	}

	/**
	 * Configure the plugin with respect to option settings defined in ".options" file
	 */
	public void configurePluginDebugOptions(){
		if(RodinCore.getPlugin().isDebugging()){
//			String option = Platform.getDebugOption(BUILDER_DEBUG);
//			if(option != null) RodinBuilder.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//			
//			option = Platform.getDebugOption(COMPILER_DEBUG);
//			if(option != null) Compiler.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(COMPLETION_DEBUG);
//			if(option != null) CompletionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//			
//			option = Platform.getDebugOption(CP_RESOLVE_DEBUG);
//			if(option != null) RodinDBManager.CP_RESOLVE_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(DELTA_DEBUG);
//			if(option != null) DeltaProcessor.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(DELTA_DEBUG_VERBOSE);
//			if(option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(HIERARCHY_DEBUG);
//			if(option != null) TypeHierarchy.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(INDEX_MANAGER_DEBUG);
//			if(option != null) JobManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//			
//			option = Platform.getDebugOption(JAVAMODEL_DEBUG);
//			if(option != null) RodinDBManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(POST_ACTION_DEBUG);
//			if(option != null) RodinDBOperation.POST_ACTION_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(RESOLUTION_DEBUG);
//			if(option != null) NameLookup.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(SEARCH_DEBUG);
//			if(option != null) BasicSearchEngine.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(SELECTION_DEBUG);
//			if(option != null) SelectionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//
//			option = Platform.getDebugOption(ZIP_ACCESS_DEBUG);
//			if(option != null) RodinDBManager.ZIP_ACCESS_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
//			
//			option = Platform.getDebugOption(SOURCE_MAPPER_DEBUG_VERBOSE);
//			if(option != null) SourceMapper.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		}
		
		// configure performance options
		if(PerformanceStats.ENABLED) {
//			CompletionEngine.PERF = PerformanceStats.isEnabled(COMPLETION_PERF);
//			SelectionEngine.PERF = PerformanceStats.isEnabled(SELECTION_PERF);
//			DeltaProcessor.PERF = PerformanceStats.isEnabled(DELTA_LISTENER_PERF);
//			RodinDBManager.PERF_VARIABLE_INITIALIZER = PerformanceStats.isEnabled(VARIABLE_INITIALIZER_PERF);
//			RodinDBManager.PERF_CONTAINER_INITIALIZER = PerformanceStats.isEnabled(CONTAINER_INITIALIZER_PERF);
//			ReconcileWorkingCopyOperation.PERF = PerformanceStats.isEnabled(RECONCILE_PERF);
		}
	}
	
	/**
	 * @see ISaveParticipant
	 */
	public void doneSaving(ISaveContext context){
		// nothing to do for the Rodin platform
	}

	public DeltaProcessor getDeltaProcessor() {
		return this.deltaState.getDeltaProcessor();
	}
	
	/**
	 *  Returns the info for the element.
	 */
	public synchronized RodinElementInfo getInfo(IRodinElement element) {
		ElementMap tempCache = this.temporaryCache.get();
		if (tempCache != null) {
			RodinElementInfo result = tempCache.get(element);
			if (result != null) {
				return result;
			}
		}
		return this.cache.getInfo(element);
	}

	/**
	 * Returns the handle to the active Rodin database.
	 */
	public final RodinDB getRodinDB() {
		return this.rodinDB;
	}

	/**
	 * Returns the singleton RodinDBManager
	 */
	public final static RodinDBManager getRodinDBManager() {
		return MANAGER;
	}

	/**
	 * Returns the last built state for the given project, or null if there is none.
	 * Deserializes the state if necessary.
	 *
	 * For use by image builder and evaluation support only
	 */
	public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
		if (!RodinProject.hasRodinNature(project)) {
			if (RodinBuilder.DEBUG)
				System.out.println(project + " is not a Rodin project"); //$NON-NLS-1$
			return null; // should never be requested on non-Rodin projects
		}
		PerProjectInfo info = getPerProjectInfo(project, true/*create if missing*/);
		if (!info.triedRead) {
			info.triedRead = true;
			try {
				if (monitor != null)
					monitor.subTask(Messages.bind(Messages.build_readStateProgress, project.getName())); 
				info.savedState = readState(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return info.savedState;
	}

	
	/*
	 * Returns the per-project info for the given project. If specified, create the info if the info doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
		synchronized(this.perProjectInfos) { // use the perProjectInfo collection as its own lock
			PerProjectInfo info= this.perProjectInfos.get(project);
			if (info == null && create) {
				info= new PerProjectInfo(project);
				this.perProjectInfos.put(project, info);
			}
			return info;
		}
	}	
	
	/*
	 * Returns  the per-project info for the given project.
	 * If the info doesn't exist, check for the project existence and create the info.
	 * @throws RodinDBException if the project doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfoCheckExistence(IProject project) throws RodinDBException {
		RodinDBManager.PerProjectInfo info = getPerProjectInfo(project, false /* don't create info */);
		if (info == null) {
			if (!RodinProject.hasRodinNature(project)) {
				throw ((RodinProject)RodinCore.create(project)).newNotPresentException();
			}
			info = getPerProjectInfo(project, true /* create info */);
		}
		return info;
	}
	
	/**
	 * Returns the temporary cache for newly opened elements for the current thread.
	 * Creates it if not already created.
	 */
	public ElementMap getTemporaryCache() {
		ElementMap result = this.temporaryCache.get();
		if (result == null) {
			result = new ElementMap();
			this.temporaryCache.set(result);
		}
		return result;
	}
	
	/**
	 * Returns the File to use for saving and restoring the last built state for the given project.
	 */
	private File getSerializationFile(IProject project) {
		if (!project.exists()) return null;
		IPath workingLocation = project.getWorkingLocation(RodinCore.PLUGIN_ID);
		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
	}
	
	/*
	 * Returns whether there is a temporary cache for the current thread.
	 */
	public boolean hasTemporaryCache() {
		return this.temporaryCache.get() != null;
	}
	
//	/**
//	 * Initialize preferences lookups for RodinCore plugin.
//	 */
//	public void initializePreferences() {
//		
//		// Create lookups
//		preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(RodinCore.PLUGIN_ID);
//		preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(RodinCore.PLUGIN_ID);
//
//		// Listen to instance preferences node removal from parent in order to refresh stored one
//		IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
//			public void added(IEclipsePreferences.NodeChangeEvent event) {
//				// do nothing
//			}
//			public void removed(IEclipsePreferences.NodeChangeEvent event) {
//				if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
//					preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(RodinCore.PLUGIN_ID);
//					preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
//				}
//			}
//		};
//		((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
//		preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
//
//		// Listen to default preferences node removal from parent in order to refresh stored one
//		listener = new IEclipsePreferences.INodeChangeListener() {
//			public void added(IEclipsePreferences.NodeChangeEvent event) {
//				// do nothing
//			}
//			public void removed(IEclipsePreferences.NodeChangeEvent event) {
//				if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
//					preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(RodinCore.PLUGIN_ID);
//				}
//			}
//		};
//		((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
//	}

	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	public synchronized RodinElementInfo peekAtInfo(IRodinElement element) {
		HashMap<IRodinElement, RodinElementInfo> tempCache = this.temporaryCache.get();
		if (tempCache != null) {
			RodinElementInfo result = tempCache.get(element);
			if (result != null) {
				return result;
			}
		}
		return this.cache.peekAtInfo(element);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void prepareToSave(ISaveContext context) /*throws CoreException*/ {
		// nothing to do
	}
	/*
	 * Puts the infos in the given map (keys are IRodinElements and values are RodinElementInfos)
	 * in the Rodin database cache in an atomic way.
	 * First checks that the info for the opened element (or one of its ancestors) has not been 
	 * added to the cache. If it is the case, another thread has opened the element (or one of
	 * its ancestors). So returns without updating the cache.
	 */
	public synchronized void putInfos(IRodinElement openedElement, 
			Map<IRodinElement, RodinElementInfo> newElements) {
		// remove children
		RodinElementInfo existingInfo = this.cache.peekAtInfo(openedElement);
		if (openedElement instanceof IParent && existingInfo != null) {
			closeChildren(existingInfo);
		}
		
		for (IRodinElement element: newElements.keySet()) {
			RodinElementInfo info = newElements.get(element);
			this.cache.putInfo(element, info);
		}
	}

	private void closeChildren(RodinElementInfo info) {
		for (RodinElement child: info.getChildren()) {
			if (child instanceof Openable) {
				try {
					((Openable) child).close();
				} catch (RodinDBException e) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * Reads the build state for the relevant project.
	 */
	protected Object readState(IProject project) throws CoreException {
		File file = getSerializationFile(project);
		if (file != null && file.exists()) {
			try {
				DataInputStream in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				try {
					String pluginID= in.readUTF();
					if (!pluginID.equals(RodinCore.PLUGIN_ID))
						throw new IOException(Messages.build_wrongFileFormat); 
					String kind= in.readUTF();
					if (!kind.equals("STATE")) //$NON-NLS-1$
						throw new IOException(Messages.build_wrongFileFormat); 
					if (in.readBoolean())
						return RodinBuilder.readState(project, in);
					if (RodinBuilder.DEBUG)
						System.out.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, RodinCore.PLUGIN_ID, Platform.PLUGIN_ERROR, "Error reading last build state for project "+ project.getName(), e)); //$NON-NLS-1$
			}
		} else if (RodinBuilder.DEBUG) {
			if (file == null)
				System.out.println("Project does not exist: " + project); //$NON-NLS-1$
			else
				System.out.println("Build state file " + file.getPath() + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	/*
	 * Removes all cached info for the given element (including all children)
	 * from the cache.
	 * Returns the info for the given element, or null if it was already closed.
	 */
	public synchronized RodinElementInfo removeInfoAndChildren(RodinElement element) {
		RodinElementInfo info = this.cache.peekAtInfo(element);
		if (info != null) {
			boolean wasVerbose = false;
			try {
				if (VERBOSE) {
					String elementType = element.getElementType();
					System.out.println(Thread.currentThread() + " CLOSING "+ elementType + " " + element.toStringWithAncestors());  //$NON-NLS-1$//$NON-NLS-2$
					wasVerbose = true;
					VERBOSE = false;
				}
				if (element instanceof Openable) {
					((Openable) element).closing(info);
				}
				if (element instanceof IParent && info != null) {
					closeChildren(info);
				}
				this.cache.removeInfo(element);
				if (wasVerbose) {
					System.out.println(this.cache.toStringFillingRation("-> ")); //$NON-NLS-1$
				}
			} finally {
				RodinDBManager.VERBOSE = wasVerbose;
			}
			return info;
		}
		return null;
	}	

	public void removePerProjectInfo(RodinProject javaProject) {
		synchronized(this.perProjectInfos) { // use the perProjectInfo collection as its own lock
			IProject project = javaProject.getProject();
			PerProjectInfo info= this.perProjectInfos.get(project);
			if (info != null) {
				this.perProjectInfos.remove(project);
			}
		}
	}

//	/*
//	 * Reset project options stored in info cache.
//	 */
//	public void resetProjectOptions(RodinProject javaProject) {
//		synchronized(this.perProjectInfos) { // use the perProjectInfo collection as its own lock
//			IProject project = javaProject.getProject();
//			PerProjectInfo info= (PerProjectInfo) this.perProjectInfos.get(project);
//			if (info != null) {
//				info.options = null;
//			}
//		}
//	}
//
//	/*
//	 * Reset project preferences stored in info cache.
//	 */
//	public void resetProjectPreferences(RodinProject javaProject) {
//		synchronized(this.perProjectInfos) { // use the perProjectInfo collection as its own lock
//			IProject project = javaProject.getProject();
//			PerProjectInfo info= (PerProjectInfo) this.perProjectInfos.get(project);
//			if (info != null) {
//				info.preferences = null;
//			}
//		}
//	}
//	
	public static final void doNotUse() {
		// used by tests to simulate a startup
		MANAGER = new RodinDBManager();
	}
	
	/*
	 * Resets the temporary cache for newly created elements to null.
	 */
	public void resetTemporaryCache() {
		this.temporaryCache.set(null);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void rollback(ISaveContext context){
		// nothing to do
	}

	private void saveState(PerProjectInfo info, ISaveContext context) throws CoreException {

		// passed this point, save actions are non trivial
		if (context.getKind() == ISaveContext.SNAPSHOT) return;
		
		// save built state
		if (info.triedRead) saveBuiltState(info);
	}
	
	/**
	 * Saves the built state for the project.
	 */
	private void saveBuiltState(PerProjectInfo info) throws CoreException {
		if (RodinBuilder.DEBUG)
			System.out.println(Messages.bind(Messages.build_saveStateProgress, info.project.getName())); 
		File file = getSerializationFile(info.project);
		if (file == null) return;
		long t = System.currentTimeMillis();
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			try {
				out.writeUTF(RodinCore.PLUGIN_ID);
				out.writeUTF("STATE"); //$NON-NLS-1$
				if (info.savedState == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(true);
					RodinBuilder.writeState(info.savedState, out);
				}
			} finally {
				out.close();
			}
		} catch (RuntimeException e) {
			try {
				file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
			throw new CoreException(
				new Status(IStatus.ERROR, RodinCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Messages.bind(Messages.build_cannotSaveState, info.project.getName()), e)); 
		} catch (IOException e) {
			try {
				file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
			throw new CoreException(
				new Status(IStatus.ERROR, RodinCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Messages.bind(Messages.build_cannotSaveState, info.project.getName()), e)); 
		}
		if (RodinBuilder.DEBUG) {
			t = System.currentTimeMillis() - t;
			System.out.println(Messages.bind(Messages.build_saveStateComplete, String.valueOf(t))); 
		}
	}
	
	/**
	 * @see ISaveParticipant
	 */
	public void saving(ISaveContext context) throws CoreException {
		
		if (context.getKind() == ISaveContext.FULL_SAVE) {
			// will need delta since this save (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658)
			context.needDelta();
			
//			// clean up indexes on workspace full save
//			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52347)
//			IndexManager manager = this.indexManager;
//			if (manager != null 
//					// don't force initialization of workspace scope as we could be shutting down
//					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=93941)
//					&& this.workspaceScope != null) { 
//				manager.cleanUpIndexes();
//			}
		}
	
		IProject savedProject = context.getProject();
		if (savedProject != null) {
			if (!RodinProject.hasRodinNature(savedProject)) return; // ignore
			PerProjectInfo info = getPerProjectInfo(savedProject, true /* create info */);
			saveState(info, context);
			return;
		}
	}


	/**
	 * Sets the last built state for the given project, or null to reset it.
	 */
	public void setLastBuiltState(IProject project, Object state) {
		if (RodinProject.hasRodinNature(project)) {
			// should never be requested on non-Rodin projects
			PerProjectInfo info = getPerProjectInfo(project, true /*create if missing*/);
			info.triedRead = true; // no point trying to re-read once using setter
			info.savedState = state;
		}
		if (state == null) { // delete state file to ensure a full build happens if the workspace crashes
			try {
				File file = getSerializationFile(project);
				if (file != null && file.exists())
					file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
		}
	}
	
//	public void setOptions(Hashtable newOptions) {
//		
//		try {
//			IEclipsePreferences defaultPreferences = getDefaultPreferences();
//			IEclipsePreferences instancePreferences = getInstancePreferences();
//
//			if (newOptions == null){
//				instancePreferences.clear();
//			} else {
//				Enumeration keys = newOptions.keys();
//				while (keys.hasMoreElements()){
//					String key = (String)keys.nextElement();
//					if (!this.optionNames.contains(key)) continue; // unrecognized option
//					// if (key.equals(RodinCore.CORE_ENCODING)) continue; // skipped, contributed by resource prefs
//					String value = (String)newOptions.get(key);
//					String defaultValue = defaultPreferences.get(key, null);
//					if (defaultValue != null && defaultValue.equals(value)) {
//						instancePreferences.remove(key);
//					} else {
//						instancePreferences.put(key, value);
//					}
//				}
//			}
//
//			// persist options
//			instancePreferences.flush();
//			
//			// update cache
//			this.optionsCache = newOptions==null ? null : new Hashtable(newOptions);
//		} catch (BackingStoreException e) {
//			// ignore
//		}
//	}
//		
	public void startup() {
		try {
			configurePluginDebugOptions();

			// request state folder creation (workaround 19885)
			RodinCore.getPlugin().getStateLocation();

//			// Initialize eclipse preferences
//			initializePreferences();
//
//			// Listen to preference changes
//			Preferences.IPropertyChangeListener propertyListener = new Preferences.IPropertyChangeListener() {
//				public void propertyChange(Preferences.PropertyChangeEvent event) {
//					RodinDBManager.this.optionsCache = null;
//				}
//			};
//			RodinCore.getPlugin().getPluginPreferences().addPropertyChangeListener(propertyListener);

			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(
				this.deltaState,
				IResourceChangeEvent.PRE_BUILD
					| IResourceChangeEvent.POST_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);

//			startIndexing();
			
			// process deltas since last activated in indexer thread so that indexes are up-to-date.
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
			Job processSavedState = new Job(Messages.savedState_jobName) { 
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						// add save participant and process delta atomically
						// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
						workspace.run(
							new IWorkspaceRunnable() {
								public void run(IProgressMonitor progress) throws CoreException {
									ISavedState savedState = workspace.addSaveParticipant(RodinCore.getRodinCore(), RodinDBManager.this);
									if (savedState != null) {
										// the event type coming from the saved state is always POST_AUTO_BUILD
										// force it to be POST_CHANGE so that the delta processor can handle it
										RodinDBManager.this.deltaState.getDeltaProcessor().overridenEventType = IResourceChangeEvent.POST_CHANGE;
										savedState.processResourceChangeEvents(RodinDBManager.this.deltaState);
									}
								}
							},
							monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					return Status.OK_STATUS;
				}
			};
			processSavedState.setSystem(true);
			processSavedState.setPriority(Job.SHORT); // process asap
			processSavedState.schedule();
		} catch (RuntimeException e) {
			shutdown();
			throw e;
		}
	}

	public void shutdown () {
		RodinCore javaCore = RodinCore.getRodinCore();
		javaCore.savePluginPreferences();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this.deltaState);
		workspace.removeSaveParticipant(javaCore);
	
		// wait for the initialization job to finish
		try {
			Platform.getJobManager().join(RodinCore.PLUGIN_ID, null);
		} catch (InterruptedException e) {
			// ignore
		}
		
		// Note: no need to close the Rodin database as this just removes Rodin element infos from the Rodin database cache
	}

	public Map getElementsOutOfSynch() {
		return elementsOutOfSynchWithBuffers;
	}

	private DocumentBuilderFactory builderFactory;
	
	public synchronized DocumentBuilder getDocumentBuilder() {
		if (builderFactory == null) {
			builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setCoalescing(true);
			builderFactory.setExpandEntityReferences(false);
			builderFactory.setIgnoringComments(true);
			builderFactory.setIgnoringElementContentWhitespace(false);
			builderFactory.setNamespaceAware(false);
			builderFactory.setSchema(null);
			builderFactory.setValidating(false);
			builderFactory.setXIncludeAware(false);
		}
		try {
			// TODO see how builders could be shared (take care of multi-threading)
			return builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Util.log(e, "Can't get a DOM builder");
			return null;
		}
	}
}
