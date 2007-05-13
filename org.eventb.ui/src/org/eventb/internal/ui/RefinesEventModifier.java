package org.eventb.internal.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eventb.core.IRefinesEvent;
import org.eventb.ui.IElementModifier;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

public class RefinesEventModifier implements IElementModifier {

	public void modify(IRodinElement element, String text)
			throws RodinDBException {
		if (element instanceof IRefinesEvent) {
			IRefinesEvent refinesEvent = (IRefinesEvent) element;
			String abstractEventLabel = null;
			try {
				abstractEventLabel = refinesEvent.getAbstractEventLabel();
			}
			catch (RodinDBException e) {
				// Do nothing
			}
			if (abstractEventLabel == null || !abstractEventLabel.equals(text))
				refinesEvent.setAbstractEventLabel(text,
						new NullProgressMonitor());
		}
		return;
	}

}
