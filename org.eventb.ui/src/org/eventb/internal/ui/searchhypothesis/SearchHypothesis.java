package org.eventb.internal.ui.searchhypothesis;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eventb.core.ast.Predicate;
import org.eventb.core.pm.IUserSupport;
import org.eventb.internal.ui.prover.IHypothesisPage;
import org.eventb.internal.ui.prover.ProverContentOutline;
import org.eventb.ui.EventBUIPlugin;

/**
 * @author htson
 *         <p>
 *         Implementation of the Search Hypothesis View.
 */
public class SearchHypothesis extends ProverContentOutline {

	/**
	 * The identifier of the Search Hypothesis View (value
	 * <code>"org.eventb.ui.views.SearchHypothesis"</code>).
	 */
	public static final String VIEW_ID = EventBUIPlugin.PLUGIN_ID
			+ ".views.SearchHypothesis";

	public HashMap<IPage, IWorkbenchPart> fPagesToParts;

	public SearchHypothesis() {
		super("Search Hypothesis is not available");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		// Try to get a Search Hypothesis Page.
		Object obj = part.getAdapter(ISearchHypothesisPage.class);
		if (obj instanceof ISearchHypothesisPage) {
			ISearchHypothesisPage page = (ISearchHypothesisPage) obj;
			initPage(page);
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		// There is no content outline
		return null;
	}

	public Set<Predicate> getSelectedHyps() {
		IPage currentPage = this.getCurrentPage();
		if (currentPage != null && currentPage instanceof IHypothesisPage)
			return ((IHypothesisPage) currentPage).getSelectedHyps();
		return null;
	}

	public IUserSupport getUserSupport() {
		IPage currentPage = this.getCurrentPage();
		if (currentPage != null && currentPage instanceof IHypothesisPage)
			return ((IHypothesisPage) currentPage).getUserSupport();
		return null;
	}

}