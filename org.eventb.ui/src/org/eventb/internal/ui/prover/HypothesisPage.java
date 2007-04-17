/*******************************************************************************
 * Copyright (c) 2007 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui.prover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.Page;
import org.eventb.core.EventBPlugin;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IProofState;
import org.eventb.core.pm.IProofStateDelta;
import org.eventb.core.pm.IUserSupport;
import org.eventb.core.pm.IUserSupportDelta;
import org.eventb.core.pm.IUserSupportManagerChangedListener;
import org.eventb.core.pm.IUserSupportManagerDelta;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.internal.ui.HypothesisRow;
import org.eventb.ui.EventBUIPlugin;

/**
 * @author htson
 *         <p>
 *         This class is an abstract implementation of a Hypothesis 'page'.
 */
public abstract class HypothesisPage extends Page implements
		IHypothesisPage, IUserSupportManagerChangedListener {

	IUserSupport userSupport;

	ScrolledForm scrolledForm;

	protected Collection<HypothesisRow> rows;

	private FormToolkit toolkit;

	int flag;

	/**
	 * Constructor.
	 * <p>
	 * 
	 * @param userSupport
	 *            the User Support associated with this Hypothesis Page.
	 * @param flag
	 *            the IProofStateDelta flags that this page should respond to
	 *            when changes occur in the proof state. For example,
	 *            <code>IProofStateDelta.F_NODE | IProofStateDelta.F_SEARCH</code>
	 *            specify that the page is refresh when the current node or the
	 *            search hypothesis has been changed
	 */
	public HypothesisPage(IUserSupport userSupport, int flag) {
		this.userSupport = userSupport;
		this.flag = flag;
		rows = new ArrayList<HypothesisRow>();
		EventBPlugin.getDefault().getUserSupportManager().addChangeListener(
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	@Override
	public void dispose() {
		// Deregister with the UserSupport
		EventBPlugin.getDefault().getUserSupportManager().removeChangeListener(
				this);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 * <p>
	 * 
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

		scrolledForm = toolkit.createScrolledForm(parent);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledForm.setLayoutData(gd);

		Composite comp = scrolledForm.getBody();
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 0;
		comp.setLayout(layout);

		contributeToActionBars();
		init();
	}

	private IProverSequent getProverSequent(IProofState ps) {
		IProverSequent sequent = null;
		if (ps != null) {
			IProofTreeNode node = ps.getCurrentNode();
			if (node != null) {
				sequent = node.getSequent();
			}
		}
		return sequent;
	}

	private boolean isEnable(IProofState ps) {
		boolean enable = false;
		if (ps != null) {
			IProofTreeNode node = ps.getCurrentNode();
			if (node != null) {
				if (node.isOpen())
					enable = true;
			}
		}
		return enable;
	}

	void init() {
		IProofState ps = userSupport.getCurrentPO();

		IProverSequent sequent = getProverSequent(ps);
		Collection<Predicate> hyps = getHypotheses(ps);
		boolean enable = isEnable(ps);
		init(hyps, sequent, enable);
	}

	public abstract Collection<Predicate> getHypotheses(IProofState ps);

	private void init(Collection<Predicate> hyps, IProverSequent sequent,
			boolean enable) {
		// Remove everything
		for (HypothesisRow row : rows) {
			row.dispose();
		}
		rows.clear();

		int i = 0;
		for (Predicate hyp : hyps) {
			HypothesisRow row = new HypothesisRow(toolkit, scrolledForm
					.getBody(), hyp, userSupport, sequent
					.isSelected(hyp), enable);
			rows.add(row);
			i++;
		}

		scrolledForm.reflow(true);
	}

	/**
	 * Setup the action bars
	 */
	private void contributeToActionBars() {
		IActionBars bars = getSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill the local pull down.
	 * <p>
	 * 
	 * @param manager
	 *            the menu manager
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
	}

	/**
	 * Fill the context menu.
	 * <p>
	 * 
	 * @param manager
	 *            the menu manager
	 */
	void fillContextMenu(IMenuManager manager) {
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Fill the local toolbar.
	 * <p>
	 * 
	 * @param manager
	 *            the toolbar manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		// Do nothing
	}

	/**
	 * Passing the focus request to the button bar.
	 * <p>
	 * 
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	@Override
	public void setFocus() {
		scrolledForm.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		return scrolledForm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eventb.core.pm.IProofStateChangedListener#proofStateChanged(org.eventb.core.pm.IProofStateDelta)
	 */
	public void userSupportManagerChanged(IUserSupportManagerDelta delta) {

		// Do nothing if the form is disposed.
		if (scrolledForm.isDisposed())
			return;

		// Trying to get the changes for the current user support.
		final IUserSupportDelta affectedUserSupport = ProverUIUtils
				.getUserSupportDelta(delta, userSupport);

		// Do nothing if there is no change for this current user support.
		if (affectedUserSupport == null)
			return;

		// If the user support has been removed, do nothing. This will be handle
		// by the main proof editor.
		final int kind = affectedUserSupport.getKind();
		if (kind == IUserSupportDelta.REMOVED) {
			return; // Do nothing
		}

		// This case should NOT happened.
		if (kind == IUserSupportDelta.ADDED) {
			if (ProverUIUtils.DEBUG)
				ProverUIUtils
						.debug("Error: Delta said that the user Support is added");
			return; // Do nothing
		}

		Display display = EventBUIPlugin.getDefault().getWorkbench()
				.getDisplay();

		display.syncExec(new Runnable() {
			public void run() {
				// Handle the case where the user support has changed.
				if (kind == IUserSupportDelta.CHANGED) {
					int flags = affectedUserSupport.getFlags();
					if ((flags & IUserSupportDelta.F_CURRENT) != 0) {
						// The current proof state is changed, reinitialise the
						// view.
						init();
						scrolledForm.reflow(true);
						return;
					}
					if ((flags & IUserSupportDelta.F_STATE) != 0) {
						// If the changes occurs in some proof states.	
						IProofState proofState = userSupport.getCurrentPO();
						// Trying to get the change for the current proof state. 
						final IProofStateDelta affectedProofState = ProverUIUtils
								.getProofStateDelta(affectedUserSupport,
										proofState);
						if (affectedProofState != null) {
							// If there are some changes
							int psKind = affectedProofState.getKind();
							if (psKind == IProofStateDelta.ADDED) {
								// This case should not happened
								if (ProverUIUtils.DEBUG)
									ProverUIUtils
											.debug("Error: Delta said that the proof state is added");
								return;
							}

							if (psKind == IProofStateDelta.REMOVED) {
								// Do nothing in this case, this will be handled
								// by the main proof editor.
								return;
							}
							
							if (psKind == IProofStateDelta.CHANGED) {
								// If there are some changes to the proof state.
								int psFlags = affectedProofState.getFlags();
								if ((psFlags & flag) != 0) {
									// Update the view if the corresponding flag
									// has been changed
									init();
									scrolledForm.reflow(true);
									return;
								}

							}
						}
					}
				}
			}
		});

	}

	public IUserSupport getUserSupport() {
		return userSupport;
	}

	public Set<Predicate> getSelectedHyps() {
		Set<Predicate> selected = new HashSet<Predicate>();
		for (HypothesisRow hr : rows) {
			if (hr.isSelected()) {
				selected.add(hr.getHypothesis());
			}
		}
		return selected;
	}

}
