/*******************************************************************************
 * Copyright (c) 2013, 2017 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.ui.preferences.tactics;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.preferences.autotactics.ITacticProfileCache;
import org.eventb.core.seqprover.IParamTacticDescriptor;
import org.eventb.core.seqprover.ITacticDescriptor;

/**
 * @author Nicolas Beauger
 * TODO split into simple parameterized combined
 */
public class TacticDetailsProvider implements IDetailsProvider {

	private final ITacticProfileCache cache;
	private Composite parent;

	private final ParamTacticViewer paramViewer = new ParamTacticViewer();
	private final CombinedTacticViewer combViewer = new CombinedTacticViewer();
	private IPrefMapEntry<ITacticDescriptor> currentProfile = null;
	
	public TacticDetailsProvider(ITacticProfileCache cache) {
		this.cache = cache;
	}

	@Override
	public void setParentComposite(Composite parent) {
		if (parent == this.parent) {
			return;
		}
		this.parent = parent;
		disposeAll();
		paramViewer.createContents(parent);
		combViewer.createContents(parent);
	}
	
	private void disposeAll() {
		paramViewer.dispose();
		combViewer.dispose();
	}

	public ITacticDescriptor getEditResult() {
		if (currentProfile == null) {
			// not editing
			return null;
		}
		// only parameterized tactics can be edited through details
		if (paramViewer.getInput() != currentProfile.getValue()) {
			// not editing a parameterized tactic
			return null;
		}
		return paramViewer.getEditResult();
	}
	
	private void updateDetails(Control topControl) {
		final Layout layout = parent.getLayout();
		assert layout instanceof StackLayout;
		final StackLayout sl = (StackLayout) layout;
		sl.topControl = topControl;
		parent.layout();
	}
	
	@Override
	public void putDetails(String element) {
		currentProfile = cache.getEntry(element);
		if (currentProfile == null) return;
		final ITacticDescriptor desc = currentProfile.getValue();
		if (desc instanceof IParamTacticDescriptor) {
			paramViewer.setReadOnly(cache.isDefaultEntry(element));
			paramViewer.setInput((IParamTacticDescriptor) desc);
			updateDetails(paramViewer.getControl());
		} else {
			combViewer.setInput(desc);
			updateDetails(combViewer.getControl());
		}
	}

	@Override
	public boolean hasChanges() {
		final ITacticDescriptor currentEditResult = getEditResult();
		return (currentEditResult != null && currentEditResult != currentProfile
				.getValue());
	}
	
	@Override
	public void save() {
		if (currentProfile == null) {
			return;
		}
		final ITacticDescriptor currentEditResult = getEditResult();
		currentProfile.setValue(currentEditResult);
	}
	
	@Override
	public void clear() {
		updateDetails(null);
	}

}
