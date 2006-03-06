/*******************************************************************************
 * Copyright (c) 2005 ETH-Zurich
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH RODIN Group
 *******************************************************************************/

package org.eventb.internal.ui.prover;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eventb.core.pm.IHypothesisChangeEvent;
import org.eventb.core.pm.IHypothesisChangedListener;
import org.eventb.core.pm.IHypothesisDelta;
import org.eventb.core.pm.IProofStatusChangedListener;
import org.eventb.core.prover.sequent.Hypothesis;
import org.eventb.internal.ui.EventBUIPlugin;

public class ProofsPage
	extends FormPage 
	implements	IHypothesisChangedListener,
				IProofStatusChangedListener
{
	
	public static final String PAGE_ID = "Proof State"; //$NON-NLS-1$
	public static final String PAGE_TITLE = "Proof State";
	public static final String PAGE_TAB_TITLE = "Proof State";
	
	private GoalSection goalSection;
	private HypothesesSection selectedSection;
	private HypothesesSection cachedSection;
	private HypothesesSection searchedSection;
	
	private Collection<Hypothesis> selected;
	private Collection<Hypothesis> cached;
	private Collection<Hypothesis> searched;
	
	public ProofsPage(ProverUI editor) {
		super(editor, PAGE_ID, PAGE_TAB_TITLE);  //$NON-NLS-1$
		editor.getUserSupport().addHypothesisChangedListener(this);
		editor.getUserSupport().addProofStatusChangedListener(this);
		selected = new HashSet<Hypothesis>();
		cached = new HashSet<Hypothesis>();
		searched = new HashSet<Hypothesis>();
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText(PAGE_TITLE); //$NON-NLS-1$
		Composite body = form.getBody();
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		body.setLayoutData(gd);
		
		searchedSection = new SearchHypothesesSection(this, body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | Section.COMPACT);
		managedForm.addPart(searchedSection);
		
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		gd.heightHint = 0;
		gd.widthHint = 200;
		searchedSection.getSection().setLayoutData(gd);

		cachedSection = new CacheHypothesesSection(this, body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | Section.EXPANDED);
		managedForm.addPart(cachedSection);

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		gd.minimumHeight = 100;
		gd.widthHint = 200;
		cachedSection.getSection().setLayoutData(gd);
		
		selectedSection = new SelectedHypothesesSection(this, body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | Section.EXPANDED);
		managedForm.addPart(selectedSection);

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 150;
		gd.minimumHeight = 100;
		gd.widthHint = 200;
		selectedSection.getSection().setLayoutData(gd);
		
		goalSection = new GoalSection(this, body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | Section.EXPANDED);
		managedForm.addPart(goalSection);

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 75;
		gd.minimumHeight = 50;
		gd.widthHint = 200;
		goalSection.getSection().setLayoutData(gd);
		
	}

	public void hypothesisChanged(IHypothesisChangeEvent e) {
		Collection<IHypothesisDelta> delta = e.getDelta();
		
		final Collection<Hypothesis> addedToSelected = new HashSet<Hypothesis>();
		final Collection<Hypothesis> removedFromSelected = new HashSet<Hypothesis>();
		final Collection<Hypothesis> addedToCached = new HashSet<Hypothesis>();
		final Collection<Hypothesis> removedFromCached = new HashSet<Hypothesis>();
		final Collection<Hypothesis> addedToSearched = new HashSet<Hypothesis>();
		final Collection<Hypothesis> removedFromSearched = new HashSet<Hypothesis>();

		for (Iterator<IHypothesisDelta> it = delta.iterator(); it.hasNext();) {
			IHypothesisDelta d = it.next();
			Hypothesis hyp = d.getHypothesis();
			if ((d.getFlags() & IHypothesisDelta.F_ADDED_TO_SELECTED) != 0) {
				addedToSelected.add(hyp);
				if (cached.contains(hyp)) removedFromCached.add(hyp);
				else if (searched.contains(hyp)) removedFromSearched.add(hyp);
				selected.add(hyp);
			}
			if ((d.getFlags() & IHypothesisDelta.F_REMOVED_FROM_SELECTED) != 0) {
				removedFromSelected.add(hyp);
				if (cached.contains(hyp)) addedToCached.add(hyp);
				else if (searched.contains(hyp)) addedToSearched.add(hyp);
				selected.remove(hyp);
			}
			if ((d.getFlags() & IHypothesisDelta.F_ADDED_TO_CACHED) != 0) {
				if (!selected.contains(hyp)) {
					addedToCached.add(hyp);
					if (searched.contains(hyp)) removedFromSearched.add(hyp);
				}
				cached.add(hyp);
			}
			if ((d.getFlags() & IHypothesisDelta.F_REMOVED_FROM_CACHED) != 0) {
				if (!selected.contains(hyp)) {
					removedFromCached.add(hyp);
					if (searched.contains(hyp)) addedToSearched.add(hyp);
				}
				cached.remove(hyp);
			}
			if ((d.getFlags() & IHypothesisDelta.F_ADDED_TO_SEARCHED) != 0) {
				if (!selected.contains(hyp) && !cached.contains(hyp)) 
					addedToSearched.add(hyp);
				searched.add(hyp);
			}
			if ((d.getFlags() & IHypothesisDelta.F_REMOVED_FROM_SEARCHED) != 0) {
				if (!selected.contains(hyp) && !cached.contains(hyp)) 
					removedFromSearched.add(d.getHypothesis());
				searched.remove(hyp);
			}

		}
		
		//		if (UIUtils.DEBUG) System.out.println("Update selectedSection");
		Display display = EventBUIPlugin.getDefault().getWorkbench().getDisplay();
		display.syncExec (new Runnable () {
			public void run () {
				selectedSection.update(addedToSelected, removedFromSelected);
				cachedSection.update(addedToCached, removedFromCached);
				searchedSection.update(addedToSearched, removedFromSearched);
			}
		});
//		if (UIUtils.DEBUG) System.out.println("***************");
		
//		if (UIUtils.DEBUG) System.out.println("Update cached");
//		if (UIUtils.DEBUG) System.out.println("*************");
		
//		if (UIUtils.DEBUG) System.out.println("Update searched");
//		if (UIUtils.DEBUG) System.out.println("*************");	
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.pm.IProofStatusChangedListener#proofStatusChanged()
	 */
	public void proofStatusChanged(final boolean complete) {
//		final PenguinDanceDialog dialog = new PenguinDanceDialog(EventBUIPlugin.getActiveWorkbenchShell());
//		
		Display display = EventBUIPlugin.getDefault().getWorkbench().getDisplay();
		display.syncExec (new Runnable () {
			public void run () {
				goalSection.markDirty();
			}
		});
	}
	
}