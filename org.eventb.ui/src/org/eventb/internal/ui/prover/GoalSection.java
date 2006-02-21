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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eventb.core.pm.IGoalChangeEvent;
import org.eventb.core.pm.IGoalChangedListener;
import org.eventb.core.pm.IGoalDelta;
import org.eventb.core.pm.ProofState;
import org.eventb.core.prover.IProofTreeNode;
import org.eventb.core.prover.sequent.IProverSequent;
import org.eventb.core.prover.tactics.ITactic;
import org.eventb.core.prover.tactics.Tactics;
import org.eventb.internal.ui.EventBUIPlugin;
import org.eventb.internal.ui.Utils;

public class GoalSection
	extends SectionPart
	implements IGoalChangedListener
{
	private static final String SECTION_TITLE = "Goal";
	private static final String SECTION_DESCRIPTION = "The current goal";	
	
    private FormPage page;
    private Text text;
    private FormText formText;
    private ScrolledForm scrolledForm;
    
    private class GoalITacticHyperlinkAdapter extends HyperlinkAdapter {
		@Override
		public void linkActivated(HyperlinkEvent e) {
			if (e.getHref().equals(Utils.CONJI_SYMBOL)) {
				apply(Tactics.conjI());
				return;
			}
			if (e.getHref().equals(Utils.IMPI_SYMBOL)) {
				apply(Tactics.impI());
				return;
			}
			
			if (e.getHref().equals(Utils.HYP_SYMBOL)) {
				apply(Tactics.hyp());
				return;
			}
			
			if (e.getHref().equals(Utils.ALLI_SYMBOL)) {
				apply(Tactics.allI());
				return;
			}

			if (e.getHref().equals(Utils.TRIVIAL_SYMBOL)) {
				apply(Tactics.trivial());
				return;
			}
		}
    	
    }
    
    
    private void apply(ITactic t) {
    	ProverUI editor = (ProverUI) page.getEditor();
		if (editor != null) {
			TreeViewer viewer = editor.getProofTreeUI().getViewer();
		
			ISelection selection = viewer.getSelection();
			Object obj = ((IStructuredSelection) selection).getFirstElement();

			if (obj instanceof IProofTreeNode) {
				IProofTreeNode proofTree = (IProofTreeNode) obj;
				if (!proofTree.isDischarged()) {
					t.apply(proofTree);
					editor.getProofTreeUI().refresh(proofTree);
					// Expand the node
					viewer.expandToLevel(proofTree, AbstractTreeViewer.ALL_LEVELS);
					//viewer.setExpandedState(proofTree, true);

					// Select the "next" pending "subgoal"
					ProofState ps = editor.getUserSupport().getCurrentPO();
					IProofTreeNode pt = ps.getNextPendingSubgoal(proofTree);
					if (pt != null) 
						editor.getProofTreeUI().getViewer().setSelection(new StructuredSelection(pt));
					else {
						Dialog dialog = new PenguinDanceDialog(EventBUIPlugin.getActiveWorkbenchShell());
						dialog.open();
					}
				}
			}
		}
    }
    
    // Contructor
	public GoalSection(FormPage page, Composite parent, int style) {
		super(parent, page.getManagedForm().getToolkit(), style);
		this.page = page;
		FormToolkit toolkit = page.getManagedForm().getToolkit();
		createClient(getSection(), toolkit);
		((ProverUI) page.getEditor()).getUserSupport().addGoalChangedListener(this);
	}

	public void createClient(Section section, FormToolkit toolkit) {
        section.setText(SECTION_TITLE);
        section.setDescription(SECTION_DESCRIPTION);
        scrolledForm = toolkit.createScrolledForm(section);
        
		Composite comp = scrolledForm.getBody();
        GridLayout layout = new GridLayout();
        layout.numColumns  = 2;
        layout.verticalSpacing = 5;
		comp.setLayout(layout);
		section.setClient(scrolledForm);
        toolkit.paintBordersFor(scrolledForm);
		
        formText = toolkit.createFormText(comp, true);
		HyperlinkSettings hyperlinkSettings = new HyperlinkSettings(EventBUIPlugin.getActiveWorkbenchWindow().getWorkbench().getDisplay());
		hyperlinkSettings.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		formText.setHyperlinkSettings(hyperlinkSettings);
		formText.addHyperlinkListener(new GoalITacticHyperlinkAdapter());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        gd.widthHint = 100;
        formText.setLayoutData(gd);
        toolkit.paintBordersFor(formText);
        
        text = toolkit.createText(comp, "No selection");
        text.setEditable(false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        text.setLayoutData(gd);

	}

	@Override
	protected void expansionStateChanging(boolean expanding) {
		if (expanding) {
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 60;
			gd.minimumHeight = 40;
			gd.widthHint = 200;
			this.getSection().setLayoutData(gd);
		}
		else {
			GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			gd.widthHint = 200;
			this.getSection().setLayoutData(gd);
		}
		super.expansionStateChanging(expanding);
	}

	public void setGoal(IProofTreeNode pt) {
		if (pt == null) {
			clearFormText();
			text.setText("No selected goal");
			return;
		}
		if (!pt.isOpen()) {
			clearFormText();
			text.setText("ITactic(s) applied");
		}
		else {
			text.setText(pt.getSequent().goal().toString());
			setFormText(pt.getSequent());
		}
		return;
	}

	private void clearFormText() {
		formText.setText("<form></form>", true, false);
		scrolledForm.reflow(false);
		return;
	}

	private void setFormText(IProverSequent ps) {
		String formString = "<form><li style=\"text\" value=\"\">";
		List<String> tactics = Utils.getApplicableToGoal(ps);
		
		for (Iterator<String> it = tactics.iterator(); it.hasNext();) {
			String t = it.next();
			formString = formString + "<a href=\"" + t + "\">" + t +"</a> ";
		}
		
		formString = formString + "</li></form>";
		formText.setText(formString, true, false);
		scrolledForm.reflow(false);
		
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.core.pm.IGoalChangedListener#goalChanged(org.eventb.core.pm.IGoalChangeEvent)
	 */
	public void goalChanged(IGoalChangeEvent e) {
		IGoalDelta delta = e.getDelta();
		setGoal(delta.getProofTreeNode());
	}

}