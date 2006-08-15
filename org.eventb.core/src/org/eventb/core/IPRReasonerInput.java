package org.eventb.core;

import org.eventb.core.prover.IReasonerInputSerializer;
import org.rodinp.core.IInternalElement;


/**
 * @author Farhad Mehta
 *
 */

public interface IPRReasonerInput extends IInternalElement, IReasonerInputSerializer {
		
	public String ELEMENT_TYPE = EventBPlugin.PLUGIN_ID + ".prReasonerInput"; //$NON-NLS-1$

}
