/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.ITactic;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.internal.ui.HypothesisRow;
import org.eventb.ui.EventBUIPlugin;
import org.eventb.ui.IEventBSharedImages;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class is an sub-class of Hypotheses Section to show the set of
 *         cache hypotheses in Prover UI editor.
 */
public class CacheHypothesesSection extends HypothesesSection {

	// Title and description
	private static final String SECTION_TITLE = "Cached Hypotheses";

	private static final String SECTION_DESCRIPTION = "The set of cached hypotheses";

	ImageHyperlink ds;

	ImageHyperlink sl;

	/**
	 * @author htson
	 *         <p>
	 *         This class extends HyperlinkAdapter and provide response actions
	 *         when a hyperlink is activated.
	 */
	class CachedHyperlinkAdapter extends HyperlinkAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.events.IHyperlinkListener#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
		 */
		@Override
		public void linkActivated(HyperlinkEvent e) {
			Widget widget = e.widget;
			if (widget.equals(sl)) {
				Set<Predicate> selected = new HashSet<Predicate>();
				for (Iterator<HypothesisRow> it = rows.iterator(); it.hasNext();) {
					HypothesisRow hr = it.next();
					if (hr.isSelected()) {
						selected.add(hr.getHypothesis());
					}
				}
				if (selected.isEmpty())
					return;

				ProverUI editor = (ProverUI) page.getEditor();
				ITactic t = Tactics.mngHyp(ProverFactory.makeSelectHypAction(selected));
				try {
					editor.getUserSupport().applyTacticToHypotheses(t, selected, new NullProgressMonitor());
				} catch (RodinDBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			else if (widget.equals(ds)) {
				Set<Predicate> deselected = new HashSet<Predicate>();
				for (Iterator<HypothesisRow> it = rows.iterator(); it.hasNext();) {
					HypothesisRow hr = it.next();
					if (hr.isSelected())
						deselected.add(hr.getHypothesis());
				}
				if (deselected.isEmpty())
					return;
				ProverUI editor = (ProverUI) page.getEditor();
				editor.getUserSupport().removeCachedHypotheses(deselected);
			}

		}

	}

	/**
	 * Constructor
	 * <p>
	 * 
	 * @param page
	 *            The page that contain this section
	 * @param parent
	 *            the composite parent of the section
	 * @param style
	 *            style to create this section
	 */
	public CacheHypothesesSection(ProofsPage page, Composite parent, int style) {
		super(page, parent, style, SECTION_TITLE, SECTION_DESCRIPTION);
	}

	@Override
	protected void createTextClient(Section section, FormToolkit toolkit) {
		Composite composite = new Composite(section, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);

		toolkit.adapt(composite, true, true);
		composite.setBackground(section.getTitleBarGradientBackground());

		ds = new ImageHyperlink(composite, SWT.CENTER);
		toolkit.adapt(ds, true, true);
		ImageRegistry registry = EventBUIPlugin.getDefault().getImageRegistry();
		ds.setImage(registry.get(IEventBSharedImages.IMG_REMOVE));
		ds.addHyperlinkListener(new CachedHyperlinkAdapter());
		ds.setBackground(section.getTitleBarGradientBackground());
		ds.setToolTipText("Deselect checked hypotheses");
		
		sl = new ImageHyperlink(composite, SWT.CENTER);
		toolkit.adapt(sl, true, true);
		sl.setImage(registry.get(IEventBSharedImages.IMG_ADD));
		sl.addHyperlinkListener(new CachedHyperlinkAdapter());
		sl.setBackground(section.getTitleBarGradientBackground());
		sl.setToolTipText("Select checked hypotheses");
		composite.pack();

		section.setTextClient(composite);
	}

	@Override
	protected void updateTextClientStatus(boolean enable) {
		ds.setEnabled(enable);
		sl.setEnabled(enable);
	}

}