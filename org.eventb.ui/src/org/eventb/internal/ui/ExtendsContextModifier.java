package org.eventb.internal.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IExtendsContext;
import org.eventb.ui.IElementModifier;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

public class ExtendsContextModifier implements IElementModifier {

	public void modify(IRodinElement element, String text)
			throws RodinDBException {
		if (element instanceof IExtendsContext) {
			IExtendsContext extendsContext = (IExtendsContext) element;
			if (!extendsContext.getAbstractContextName().equals(text))
				extendsContext.setAbstractContextName(text,
						new NullProgressMonitor());
		}
		return;
	}

}
