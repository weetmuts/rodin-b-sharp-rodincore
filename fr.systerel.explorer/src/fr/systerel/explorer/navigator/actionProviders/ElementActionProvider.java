/*******************************************************************************
 * Copyright (c) 2008 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
  *******************************************************************************/
package fr.systerel.explorer.navigator.actionProviders;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionConstants;

/**
 * 
 * An action provider for elements such as Invariants, Theorems, Events etc.
 *
 */
public class ElementActionProvider extends NavigatorActionProvider {


    @Override
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        // forward doubleClick to doubleClickAction
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
              ActionCollection.getOpenAction(site));
    }
	
}
