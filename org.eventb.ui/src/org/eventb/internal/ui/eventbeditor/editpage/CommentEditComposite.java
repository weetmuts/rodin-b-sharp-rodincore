package org.eventb.internal.ui.eventbeditor.editpage;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eventb.core.ICommentedElement;
import org.rodinp.core.RodinDBException;

public class CommentEditComposite extends TextEditComposite {

	public String getValue() throws RodinDBException {
		assert element instanceof ICommentedElement;
		final ICommentedElement cElement = (ICommentedElement) element;
		return cElement.getComment();
	}

	@Override
	public void createMainComposite(FormToolkit toolkit, Composite parent) {
		createMainComposite(toolkit, parent, SWT.MULTI);
	}

	public void setValue() {
		assert element instanceof ICommentedElement;
		final ICommentedElement cElement = (ICommentedElement) element;
		Text text = (Text) control;
		String str = text.getText();
		String value;
		try {
			value = getValue();
		} catch (RodinDBException e) {
			value = null;
		}
		if (value == null || !value.equals(str)) {
			try {
				cElement.setComment(str, new NullProgressMonitor());
			} catch (RodinDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
