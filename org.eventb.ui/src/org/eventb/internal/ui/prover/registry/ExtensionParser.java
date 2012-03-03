/*******************************************************************************
 * Copyright (c) 2005, 2011 ETH Zurich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - refactored to use ITacticProvider2 and ITacticApplication
 *******************************************************************************/
package org.eventb.internal.ui.prover.registry;

import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eventb.internal.ui.prover.registry.ErrorStatuses.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eventb.internal.ui.prover.ProverUIUtils;
import org.eventb.internal.ui.prover.TacticUIRegistry;

/**
 * Utility class for parsing the extensions contributed to extension point
 * <code>org.eventb.ui.proofTactics</code>. This class is not thread-safe, but
 * this doesn't matter as it is only called during class initialization of
 * {@link TacticUIRegistry}.
 * 
 * @author Laurent Voisin
 * @see TacticUIRegistry
 */
public class ExtensionParser {

	private static class ErroneousElement extends Exception {

		private static final long serialVersionUID = 7360620082299355450L;

		private final IStatus status;

		public ErroneousElement(IStatus status) {
			super(status.getMessage(), status.getException());
			this.status = status;
		}

		public IStatus getStatus() {
			return status;
		}

	}

	/**
	 * Implements a set of configuration elements to parse.
	 */
	private abstract class ElementSet {

		// Set of elements: the use of the LinkedHashMap ensures that the
		// order of insertion is retained. Keys are element ids.
		private final Map<String, IConfigurationElement> set = new LinkedHashMap<String, IConfigurationElement>();

		ElementSet() {
			// Do nothing
		}

		/*
		 * Adds a new element, checking that its id is well-formed and unique
		 * among the set. Erroneous elements are ignored (and an exception is
		 * thrown). Returns the id in case of success.
		 */
		public void add(IConfigurationElement element) {
			String id = element.getAttribute("id"); //$NON-NLS-1$

			// Check that the id is present and well-formed
			if (id == null) {
				errors.add(missingId(element));
				return;
			}
			if (id.length() == 0) {
				errors.add(invalidId(element));
				return;
			}

			// Raw id becomes qualified with the name space of the element
			if (id.indexOf('.') == -1) { //$NON-NLS-1$
				id = element.getNamespaceIdentifier() + "." + id; //$NON-NLS-1$
			}

			// Register the element, checking for uniqueness of id
			final IConfigurationElement oldElement = set.put(id, element);
			if (oldElement != null) {
				// Repair and ignore duplicate id
				set.put(id, oldElement);
				errors.add(duplicateId(element));
			}
		}

		/*
		 * Parse the elements of this set and register them appropriately
		 */
		public void parse() {
			for (final Map.Entry<String, IConfigurationElement> entry : set
					.entrySet()) {
				try {
					parse(entry.getKey(), entry.getValue());
				} catch (ErroneousElement e) {
					errors.add(e.getStatus());
				}
			}
		}

		protected abstract void parse(String id, IConfigurationElement element)
				throws ErroneousElement;

	}

	private class TacticParser extends ElementSet {

		TacticParser() {
			// Do nothing
		}

		@Override
		protected void parse(String id, IConfigurationElement element)
				throws ErroneousElement {
			final TacticUILoader loader = new TacticUILoader(id, element);
			final TacticUIInfo info = loader.load();
			if (info != null) {
				final String target = element.getAttribute("target");
				putInRegistry(info, target);
				printDebugRegistration(id, TACTIC_TAG);
			}
		}

	}

	private class DropdownParser extends ElementSet {

		DropdownParser() {
			// Do nothing
		}

		@Override
		protected void parse(String id, IConfigurationElement element)
				throws ErroneousElement {
			dropdownRegistry.put(id, new DropdownInfo(globalRegistry, id,
					element));
			printDebugRegistration(id, DROPDOWN_TAG);
		}

	}

	private class ToolbarParser extends ElementSet {

		ToolbarParser() {
			// Do nothing
		}

		@Override
		protected void parse(String id, IConfigurationElement element)
				throws ErroneousElement {
			toolbarRegistry.put(id, new ToolbarInfo(globalRegistry,
					dropdownRegistry, id));

			printDebugRegistration(id, TOOLBAR_TAG);
		}

	}

	// Possible tags of extensions
	private static final String TACTIC_TAG = "tactic";
	private static final String TOOLBAR_TAG = "toolbar";
	private static final String DROPDOWN_TAG = "dropdown";

	private static final String TARGET_ANY = "any"; //$NON-NLS-0$

	private static final String TARGET_GOAL = "goal"; //$NON-NLS-0$

	private static final String TARGET_HYPOTHESIS = "hypothesis"; //$NON-NLS-0$

	private final Map<String, TacticProviderInfo> goalTacticRegistry = new LinkedHashMap<String, TacticProviderInfo>();
	private final Map<String, ProofCommandInfo> goalCommandRegistry = new LinkedHashMap<String, ProofCommandInfo>();
	private final Map<String, TacticProviderInfo> hypothesisTacticRegistry = new LinkedHashMap<String, TacticProviderInfo>();
	private final Map<String, ProofCommandInfo> hypothesisCommandRegistry = new LinkedHashMap<String, ProofCommandInfo>();
	private final Map<String, TacticProviderInfo> anyTacticRegistry = new LinkedHashMap<String, TacticProviderInfo>();
	private final Map<String, ProofCommandInfo> anyCommandRegistry = new LinkedHashMap<String, ProofCommandInfo>();
	private final Map<String, TacticUIInfo> globalRegistry = new LinkedHashMap<String, TacticUIInfo>();
	private final Map<String, ToolbarInfo> toolbarRegistry = new LinkedHashMap<String, ToolbarInfo>();
	private final Map<String, DropdownInfo> dropdownRegistry = new LinkedHashMap<String, DropdownInfo>();

	private final List<IStatus> errors = new ArrayList<IStatus>();

	/*
	 * Configuration elements are processed in two phases. In the first phase,
	 * they are sorted by type. In the second phase, the final objects are built
	 * and registered in the appropriate data structures.
	 */
	public void parse(IConfigurationElement[] elements) {
		final ElementSet tactics = new TacticParser();
		final ElementSet dropdowns = new DropdownParser();
		final ElementSet toolbars = new ToolbarParser();

		for (final IConfigurationElement element : elements) {
			final String tag = element.getName();
			if (tag.equals(TACTIC_TAG)) {
				tactics.add(element);
			} else if (tag.equals(DROPDOWN_TAG)) {
				dropdowns.add(element);
			} else if (tag.equals(TOOLBAR_TAG)) {
				toolbars.add(element);
			} else {
				errors.add(unknownElement(element));
			}
		}

		tactics.parse();
		dropdowns.parse();
		toolbars.parse();
	}

	public IStatus getStatus() {
		final int length = errors.size();
		if (length == 0) {
			return OK_STATUS;
		} else {
			final IStatus[] array = errors.toArray(new IStatus[length]);
			return loadingErrors(array);
		}
	}

	private void putInRegistry(TacticUIInfo info, String target) {
		final String id = info.getID();
		boolean error = false;
		if (target.equals(TARGET_GOAL)) {
			if (info instanceof TacticProviderInfo) {
				goalTacticRegistry.put(id, (TacticProviderInfo) info);
			} else if (info instanceof ProofCommandInfo) {
				goalCommandRegistry.put(id, (ProofCommandInfo) info);
			} else
				error = true;
		} else if (target.equals(TARGET_HYPOTHESIS)) {
			if (info instanceof TacticProviderInfo) {
				hypothesisTacticRegistry.put(id, (TacticProviderInfo) info);
			} else if (info instanceof ProofCommandInfo) {
				hypothesisCommandRegistry.put(id, (ProofCommandInfo) info);
			} else
				error = true;
		} else if (target.equals(TARGET_ANY)) {
			if (info instanceof TacticProviderInfo) {
				anyTacticRegistry.put(id, (TacticProviderInfo) info);
			} else if (info instanceof ProofCommandInfo) {
				anyCommandRegistry.put(id, (ProofCommandInfo) info);
			} else
				error = true;
		} else {
			globalRegistry.put(id, info);
		}
		if (error) {
			if (ProverUIUtils.DEBUG)
				ProverUIUtils.debug("Error while trying to put info " + id
						+ " in a registry");
		} else {
			printDebugRegistration(id, TACTIC_TAG);
		}
	}

	private static void printDebugRegistration(String id, String kind) {
		if (ProverUIUtils.DEBUG)
			ProverUIUtils.debug("Registered " + kind + " with id " + id);
	}

	public Map<String, TacticProviderInfo> getGoalTacticRegistry() {
		return goalTacticRegistry;
	}

	public Map<String, ProofCommandInfo> getGoalCommandRegistry() {
		return goalCommandRegistry;
	}

	public Map<String, TacticProviderInfo> getHypothesisTacticRegistry() {
		return hypothesisTacticRegistry;
	}

	public Map<String, ProofCommandInfo> getHypothesisCommandRegistry() {
		return hypothesisCommandRegistry;
	}

	public Map<String, TacticProviderInfo> getAnyTacticRegistry() {
		return anyTacticRegistry;
	}

	public Map<String, ProofCommandInfo> getAnyCommandRegistry() {
		return anyCommandRegistry;
	}

	public Map<String, TacticUIInfo> getGlobalRegistry() {
		return globalRegistry;
	}

	public Map<String, ToolbarInfo> getToolbarRegistry() {
		return toolbarRegistry;
	}

	public Map<String, DropdownInfo> getDropdownRegistry() {
		return dropdownRegistry;
	}

}
