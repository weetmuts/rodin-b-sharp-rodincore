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
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eventb.core.prover.sequent.Hypothesis;
import org.eventb.internal.ui.EventBUIPlugin;

public abstract class HypothesesSection
	extends SectionPart
{
    protected ProofsPage page;
    private Composite comp;
    private ScrolledForm scrolledForm;
    private String title;
    private String description;
    
    protected Collection<HypothesisRow> rows;
    
    // Contructor
	public HypothesesSection(ProofsPage page, Composite parent, int style, String title, String description) {
		super(parent, page.getManagedForm().getToolkit(), style);
		this.page = page;
		this.title = title;
		this.description = description;
		FormToolkit toolkit = page.getManagedForm().getToolkit();
		createClient(getSection(), toolkit);
		rows = new HashSet<HypothesisRow>();
	}
	
	protected abstract void createTopFormText(FormToolkit toolkit, Composite comp);

	public void createClient(Section section, FormToolkit toolkit) {
        section.setText(title);
        section.setDescription(description);

		Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        createTopFormText(toolkit, composite);

        scrolledForm = toolkit.createScrolledForm(composite);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledForm.setLayoutData(gd);
        
		comp = scrolledForm.getBody();
        layout = new GridLayout();
        layout.numColumns  = 3;
        layout.verticalSpacing = 5;
		comp.setLayout(layout);
		
		section.setClient(composite);
	}
	
	protected void update(Collection<Hypothesis> added, Collection<Hypothesis> removed) {
		if (removed != null) {
			Collection<HypothesisRow> deletedRows = new HashSet<HypothesisRow>();
		
			for (Iterator<HypothesisRow> it = rows.iterator(); it.hasNext();) {
				HypothesisRow hr = it.next();
				if (removed.contains(hr.getHypothesis())) {
//					if (UIUtils.DEBUG) System.outprintln("Removed " + hr.getHypothesis());
					deletedRows.add(hr);
					hr.dispose();
				}
			}
			rows.removeAll(deletedRows);
		}
		
		if (added != null) {
			for (Iterator<Hypothesis> it = added.iterator(); it.hasNext();) {
				Hypothesis hp = it.next();
//				if (UIUtils.DEBUG) System.outprintln("Added " + hp);
				HypothesisRow row = new HypothesisRow(this.getManagedForm().getToolkit(), comp, hp, ((ProverUI) page.getEditor()).getUserSupport());
				rows.add(row);
			}
		}

		Display display = EventBUIPlugin.getDefault().getWorkbench().getDisplay();
		final SectionPart part = this;
		display.syncExec (new Runnable () {
			public void run () {
				if (!scrolledForm.isDisposed()) {
					scrolledForm.reflow(true); // Important for refresh
					part.refresh();
				}
			}
		});
		
	}
}