/*******************************************************************************
 * Copyright (c) 2011 Systerel and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Systerel - Initial API and implementation
 *******************************************************************************/
package fr.systerel.editor.internal.operations;

import static org.eventb.internal.ui.EventBUtils.isReadOnly;
import static org.eventb.internal.ui.UIUtils.showInfo;
import static org.eventb.internal.ui.utils.Messages.dialogs_pasteNotAllowed;
import static org.eventb.internal.ui.utils.Messages.dialogs_readOnlyElement;
import static org.eventb.internal.ui.utils.Messages.title_canNotPaste;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IIdentifierElement;
import org.eventb.core.ILabeledElement;
import org.eventb.internal.ui.UIUtils;
import org.eventb.internal.ui.eventbeditor.EventBEditorUtils;
import org.eventb.internal.ui.eventbeditor.elementdesc.ElementDescRegistry;
import org.eventb.ui.EventBUIPlugin;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author "Thomas Muller"
 *
 */
public class OperationUtils {
	
	/**
	 * Perform a copy operation through the undo history.
	 * <p>
	 * If the element type of elements to copy and the target are equals, the
	 * new elements is placed after target. Else the new element is placed at
	 * the end of the children list.
	 * 
	 * @param target
	 *            The selected element.
	 * @param elements
	 *            the elements to copy
	 */
	public static void copyElements(IInternalElement target,
			IRodinElement[] elements) {
		if (checkAndShowReadOnly(target)) {
			return;
		}

		final IElementType<?> typeNotAllowed = elementTypeNotAllowed(elements,
				target);
		if (typeNotAllowed == null) {
			copyElements(elements, target, null);
		} else if (haveSameType(elements, target)) {
			try {
				copyElements(elements, target.getParent(),
						target.getNextSibling());
			} catch (RodinDBException e) {
				e.printStackTrace();
			}
		} else {
			UIUtils.showError(
					title_canNotPaste,
					dialogs_pasteNotAllowed(typeNotAllowed.getName(), target
							.getElementType().getName()));
			return;
		}
		if (EventBEditorUtils.DEBUG)
			EventBEditorUtils.debug("PASTE SUCCESSFULLY");
	}
	
	/**
	 * Perform a copy operation through the undo history.
	 */
	private static void copyElements(IRodinElement[] handleData,
			IRodinElement target, IRodinElement nextSibling) {
		History.getInstance().addOperation(
				OperationFactory.copyElements((IInternalElement) target,
						handleData, (IInternalElement) nextSibling));
	}
	
	/**
	 * Returns whether the given element is read only. Additionally, if the
	 * given element is read only, this method informs the user through an info
	 * window.
	 * 
	 * @param element
	 *            an element to check
	 * @return true iff the given element is read only
	 */
	public static boolean checkAndShowReadOnly(IRodinElement element) {
		if (!(element instanceof IInternalElement)) {
			return false;
		}
		final boolean readOnly = isReadOnly((IInternalElement) element);
		if (readOnly) {
			showInfo(dialogs_readOnlyElement(getDisplayName(element)));
		}
		return readOnly;
	}

	private static String getDisplayName(IRodinElement element) {
		try {
			if(element instanceof ILabeledElement) {
				return ((ILabeledElement)element).getLabel();
			} else if (element instanceof IIdentifierElement) {
				return ((IIdentifierElement)element).getIdentifierString();
			} else if (element instanceof IEventBRoot) {
				return element.getElementName();
			}
		} catch (RodinDBException e) {
			UIUtils.log(e, "when checking for read-only element");
		}
		return "";
	}
	
	/**
	 * Returns the type of an element that is not allowed to be pasted as child
	 * of target.
	 * 
	 * @return the type that is not allowed to be pasted or <code>null</code> if
	 *         all elements to paste can become valid children
	 * */
	private static IElementType<?> elementTypeNotAllowed(
			IRodinElement[] toPaste, IRodinElement target) {
		final Set<IElementType<?>> allowedTypes = getAllowedChildTypes(target);
		for (IRodinElement e : toPaste) {
			final IElementType<?> type = e.getElementType();
			if (!allowedTypes.contains(type)) {
				return type;
			}
		}
		return null;
	}
	
	private static Set<IElementType<?>> getAllowedChildTypes(
			IRodinElement target) {
		final IElementType<?> targetType = target.getElementType();
		final IElementType<?>[] childTypes = ElementDescRegistry.getInstance()
				.getChildTypes(targetType);
		final Set<IElementType<?>> allowedTypes = new HashSet<IElementType<?>>(
				Arrays.asList(childTypes));
		return allowedTypes;
	}

	private static boolean haveSameType(IRodinElement[] toPaste,
			IRodinElement target) {
		final IElementType<?> targetType = target.getElementType();
		for (IRodinElement e : toPaste) {
			if (targetType != e.getElementType()) {
				return false;
			}
		}
		return true;
	}
	
	private static void syncExec(Runnable runnable) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(runnable);
	}
	
	private static Shell getShell() {
		return EventBUIPlugin.getActiveWorkbenchWindow().getShell();
	}
	
	/**
	 * Opens an error dialog to the user displaying the given message.
	 * 
	 * @param message
	 *            The dialog message displayed
	 * @param title 
	 */
	public static void showError(final String title, final String message) {
		syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(getShell(), title, message);
			}
		});
	}

}