/*******************************************************************************
 * Copyright (c) 2005, 2012 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - refactored to use ITacticProvider2 and ITacticApplication
 *******************************************************************************/
package org.eventb.internal.ui.prover;

import static java.util.Collections.unmodifiableList;
import static org.eventb.ui.EventBUIPlugin.PLUGIN_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IUserSupport;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.prover.registry.AbstractInfo;
import org.eventb.internal.ui.prover.registry.ExtensionParser;
import org.eventb.internal.ui.prover.registry.TacticProviderInfo;
import org.eventb.internal.ui.prover.registry.TacticUIInfo;
import org.eventb.internal.ui.prover.registry.ToolbarInfo;
import org.eventb.ui.prover.ITacticApplication;

/**
 * Registry of all tactic and proof command contributions to the prover UI.
 * <p>
 * This registry is implemented as a singleton immutable class, which ensures
 * thread-safety. The extension point is analyzed when this class gets loaded by
 * the JVM, which happens the first time that {{@link #getDefault()} is called.
 * </p>
 * 
 * @author Thai Son Hoang
 */
public class TacticUIRegistry {

	// The identifier of the extension point (value
	// <code>"org.eventb.ui.proofTactics"</code>).
	public static final String PROOFTACTICS_ID = PLUGIN_ID + ".proofTactics"; //$NON-NLS-1$

	// The static instance of this singleton class
	private static final TacticUIRegistry instance = new TacticUIRegistry();

	// The registry stored Element UI information
	private final List<TacticProviderInfo> goalTactics;

	private final List<TacticProviderInfo> hypothesisTactics;

	// Temporary registry of all tactics created during refactoring.
	// This maps contains the union of all other tactic maps.
	private final Map<String, TacticUIInfo> allTacticRegistry;

	private final List<ToolbarInfo> toolbars;

	/**
	 * The unique instance of this class is created when initializing the static
	 * final field "instance", thus in a thread-safe manner.
	 */
	private TacticUIRegistry() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = reg.getExtensionPoint(PROOFTACTICS_ID);
		IConfigurationElement[] configurations = extensionPoint
				.getConfigurationElements();

		final ExtensionParser parser = new ExtensionParser();
		parser.parse(configurations);
		final IStatus status = parser.getStatus();
		if (!status.isOK()) {
			UIUtils.log(status);
		}

		goalTactics = parser.getGoalTactics();
		hypothesisTactics= parser.getHypothesisTactics();
		allTacticRegistry = parser.getAllTacticRegistry();
		toolbars = parser.getToolbars();

		if (ProverUIUtils.DEBUG) {
			show(goalTactics, "goalTactics");
			show(hypothesisTactics, "hypothesisTactics");
			show(allTacticRegistry, "allTacticRegistry");
			show(toolbars, "toolbars");
		}
	}

	private void show(Map<String, ?> registry, String name) {
		System.out.println("Contents of registry : " + name + ":");
		for (final String id : registry.keySet()) {
			System.out.println("\t" + id);
		}
	}

	private void show(Collection<? extends AbstractInfo> list, String name) {
		System.out.println("Contents of registry : " + name + ":");
		for (final AbstractInfo info : list) {
			System.out.println("\t" + info.getID());
		}
	}

	/**
	 * Returns the unique instance of this registry. The instance of this class
	 * is lazily constructed at class loading time.
	 * 
	 * @return the unique instance of this registry
	 */
	public static TacticUIRegistry getDefault() {
		return instance;
	}

	public List<ITacticApplication> getTacticApplicationsToGoal(IUserSupport us) {
		final List<ITacticApplication> result = new ArrayList<ITacticApplication>();
		for (TacticProviderInfo info : goalTactics) {
			final List<ITacticApplication> applications = info
					.getApplicationsToGoal(us);
			result.addAll(applications);
		}
		return result;
	}

	public List<ITacticApplication> getTacticApplicationsToHypothesis(
			IUserSupport us, Predicate hyp) {
		final List<ITacticApplication> result = new ArrayList<ITacticApplication>();
		for (TacticProviderInfo info : hypothesisTactics) {
			final List<ITacticApplication> applications = info
					.getApplicationsToHypothesis(us, hyp);
			result.addAll(applications);
		}
		return result;
	}

	public Image getIcon(String tacticID) {
		final TacticUIInfo info = allTacticRegistry.get(tacticID);
		if (info != null)
			return info.getIcon();

		return null;
	}

	public String getTip(String tacticID) {
		final TacticUIInfo info = allTacticRegistry.get(tacticID);

		if (info != null)
			return info.getTooltip();

		return null;
	}

	public boolean isSkipPostTactic(String tacticID) {
		final TacticUIInfo info = allTacticRegistry.get(tacticID);
		if (info != null)
			return info.isSkipPostTactic();

		return false;
	}

	public List<ToolbarInfo> getToolbars() {
		return unmodifiableList(toolbars);
	}

}
