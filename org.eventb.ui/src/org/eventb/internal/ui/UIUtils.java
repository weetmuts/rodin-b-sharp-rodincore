/*******************************************************************************
 * Copyright (c) 2005, 2008 ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ETH Zurich - initial API and implementation
 *     Systerel - added getFreeIndex method to factorize several methods 
 *******************************************************************************/

package org.eventb.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eventb.core.EventBAttributes;
import org.eventb.core.IContextFile;
import org.eventb.core.IEventBFile;
import org.eventb.core.IMachineFile;
import org.eventb.core.IPSFile;
import org.eventb.core.IPSStatus;
import org.eventb.internal.ui.eventbeditor.EventBContextEditor;
import org.eventb.internal.ui.eventbeditor.EventBMachineEditor;
import org.eventb.ui.projectexplorer.ExplorerUtilities;
import org.eventb.ui.projectexplorer.TreeNode;
import org.eventb.internal.ui.prover.ProverUI;
import org.eventb.ui.EventBUIPlugin;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IAttributedElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IOpenable;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 *         <p>
 *         This class contains some utility static methods that are used in this
 *         Event-B User interface plug-in.
 */
public class UIUtils {
	
	public static abstract class MaxFinder {

		/**
		 * Inserts a number given in string format.
		 * 
		 * @param number
		 *            a string of decimal digits.
		 */
		public abstract void insert(String number);

		/**
		 * Returns a number which was not inserted previously as a string of
		 * decimal digits. Currently, returns the maximum of the inserted
		 * numbers plus one (or one if no number was inserted).
		 * 
		 * @return an available number
		 */
		public abstract String getAvailable();
	}

	public static class BigMaxFinder extends MaxFinder {

		BigInteger max = BigInteger.ZERO;

		@Override
		public void insert(String number) {
			final BigInteger n = new BigInteger(number);
			if (max.compareTo(n) < 0) {
				max = n;
			}
		}

		@Override
		public String getAvailable() {
			return max.add(BigInteger.ONE).toString();
		}

	}

	/**
	 * The debug flag. This is set by the option when the platform is launched.
	 * Client should not try to reset this flag.
	 */
	public static boolean DEBUG = false;

	/**
	 * Returns a non already used String index intended to be concatenated to
	 * the name or the attribute of an element.
	 * 
	 * @param parent
	 *            the parent node to be looked for
	 * @param type
	 *            the type of the element to be looked for
	 * @param attributeType
	 *            the type of the attribute to be looked for set it to null if
	 *            the index is intended to the name of the element
	 * @param prefix
	 *            the prefix of the element to be looked for
	 * @return a non already used integer index intended to be concatenated to
	 *         the name or the attribute of an element (if no element of that
	 *         type already exists, returns beginIndex)
	 * @throws RodinDBException
	 */
	public static String getFreePrefixIndex(IInternalParent parent,
			IInternalElementType<?> type, IAttributeType.String attributeType,
			String prefix) throws RodinDBException, IllegalStateException {

		final String regex = Pattern.quote(prefix) + "(\\d+)";
		final Pattern prefixDigits = Pattern.compile(regex);
		final MaxFinder maxFinder = new BigMaxFinder();
		for (IInternalElement element : parent.getChildrenOfType(type)) {
			final String elementString;
			if (attributeType == null) {
				// name research
				elementString = element.getElementName();
			} else if (element.hasAttribute(attributeType)) {
				// attribute research
				elementString = element.getAttributeValue(attributeType);
			} else {
				elementString = null;
			}

			if (elementString != null) {
				final Matcher matcher = prefixDigits.matcher(elementString);
				if (matcher.matches()) {
					maxFinder.insert(matcher.group(1));
				}
			}
		}
		return maxFinder.getAvailable();
	}

	public static void log(Throwable exc, String message) {
		if (exc instanceof RodinDBException) {
			final Throwable nestedExc = ((RodinDBException) exc).getException();
			if (nestedExc != null) {
				exc = nestedExc;
			}
		}
		if (message == null) {
			message = "Unknown context"; //$NON-NLS-1$
		}
		IStatus status = new Status(IStatus.ERROR, EventBUIPlugin.PLUGIN_ID,
				IStatus.ERROR, message, exc);
		EventBUIPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Method to return the openable for an object (IRodinElement or TreeNode).
	 * <p>
	 * 
	 * @param node
	 *            A Rodin Element or a tree node
	 * @return The IRodinFile corresponding to the input object
	 */
	public static IOpenable getOpenable(Object node) {
		if (node instanceof TreeNode)
			return ((IRodinElement) ((TreeNode<?>) node).getParent())
					.getOpenable();
		else if (node instanceof IRodinElement)
			return ((IRodinElement) node).getOpenable();

		return null;
	}

	/**
	 * Link the current object to a Prover UI editor.
	 * <p>
	 * 
	 * @param obj
	 *            the object (e.g. a proof obligation or a Rodin file)
	 */
	public static void linkToProverUI(final Object obj) {
		final IPSFile psFile = getPSFileFor(obj);
		if (psFile == null) {
			// Not a PS file element
			// TODO log error here ?
			return;
		}
		try {
			IEditorInput fileInput = new FileEditorInput(psFile.getResource());
			final ProverUI editor = (ProverUI) EventBUIPlugin.getActivePage()
					.openEditor(fileInput, ProverUI.EDITOR_ID);
			if (obj instanceof IPSStatus)
				UIUtils.runWithProgressDialog(editor.getSite().getShell(),
						new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException,
									InterruptedException {
								editor.setCurrentPO((IPSStatus) obj, monitor);
							}

						});
		} catch (PartInitException e) {
			MessageDialog.openError(null, null,
					"Error opening the proving editor");
			e.printStackTrace();
			// TODO EventBImage.logException(e);
		}
	}

	private static IPSFile getPSFileFor(Object obj) {
		if (!(obj instanceof IRodinElement)) {
			return null;
		}

		final IOpenable file = ((IRodinElement) obj).getOpenable();
		if (file instanceof IPSFile) {
			return (IPSFile) file;
		}
		if (file instanceof IEventBFile) {
			return ((IEventBFile) file).getPSFile();
		}
		return null;
	}

	/**
	 * Link the current object to an Event-B editor.
	 * <p>
	 * 
	 * @param obj
	 *            the object (e.g. an internal element or a Rodin file)
	 */
	public static void linkToEventBEditor(Object obj) {

//		IRodinFile component;
//
//		if (!(obj instanceof IRodinProject)) {
//			component = (IRodinFile) UIUtils.getOpenable(obj);
//			try {
//				IEditorInput fileInput = new FileEditorInput(component
//						.getResource());
//				String editorId = "";
//				if (component instanceof IMachineFile) {
//					editorId = EventBMachineEditor.EDITOR_ID;
//				} else if (component instanceof IContextFile) {
//					editorId = EventBContextEditor.EDITOR_ID;
//				}
//				IEventBEditor<?> editor = (IEventBEditor<?>) EventBUIPlugin
//						.getActivePage().openEditor(fileInput, editorId);
//				editor.getSite().getSelectionProvider().setSelection(
//						new StructuredSelection(obj));
//			} catch (PartInitException e) {
//				MessageDialog.openError(null, null,
//						"Error open the Event-B Editor");
//				e.printStackTrace();
//			}
//		}
//		return;
		
		ExplorerUtilities.linkToEditor(obj);
		
	}

	/**
	 * Convert a string input to XML format by replacing special characters (&, <,
	 * >).
	 * <p>
	 * 
	 * @param input
	 *            the input string
	 * @return a string corresponding to the input in XML format
	 */
	public static String XMLWrapUp(String input) {
		String output = input;
		output = output.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return output;
	}

	/**
	 * Convert a string input to HTML format by replacing special characters (&, <, >,
	 * space, tab).
	 * <p>
	 * 
	 * @param input
	 *            the input string
	 * @return a string corresponding to the input in XML format
	 */
	public static String HTMLWrapUp(String input) {
		String output = input;
		output = output.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.replaceAll(" ", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
		return output;
	}

	/**
	 * Utitlity method to create a text and link with the same label
	 * <p>
	 * 
	 * @param link
	 *            a String
	 * @return XML formatted string represents the link
	 */
	public static String makeHyperlink(String link) {
		return "<a href=\"" + UIUtils.XMLWrapUp(link) + "\">" //$NON-NLS-1$ //$NON-NLS-2$
				+ UIUtils.XMLWrapUp(link) + "</a>"; //$NON-NLS-1$
	}

	/**
	 * Utitlity method to create a text and link with the same label
	 * <p>
	 * 
	 * @param link
	 *            a String
	 * @return XML formatted string represents the link
	 */
	public static String makeHyperlink(String link, String text) {
		return "<a href=\"" + UIUtils.XMLWrapUp(link) + "\">" //$NON-NLS-1$ //$NON-NLS-2$
				+ UIUtils.XMLWrapUp(text) + "</a>"; //$NON-NLS-1$
	}

	/**
	 * Activate a particular view given the ID.
	 * <p>
	 * 
	 * @param View_ID
	 *            the ID of the view which will be activate
	 */
	public static void activateView(String View_ID) {
		IViewPart aView = EventBUIPlugin.getActivePage().findView(View_ID);
		if (aView != null) {
			EventBUIPlugin.getActivePage().activate(aView);
		}
		return;
	}

	/**
	 * Activate a particular view given the ID.
	 * <p>
	 * 
	 * @param view_ID
	 *            the ID of the view which will be activate
	 */
	public static void showView(String view_ID) {
		try {
			EventBUIPlugin.getActivePage().showView(view_ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Running a runnable asynchronously.
	 * <p>
	 * 
	 * @param r
	 *            the runnable
	 * @param ctrl
	 *            the control that the runnable attached to
	 */
	public static void asyncPostRunnable(final Runnable r, Control ctrl) {
		final Runnable trackedRunnable = new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					// removePendingChange();
				}
			}
		};
		if (ctrl != null && !ctrl.isDisposed()) {
			try {
				ctrl.getDisplay().asyncExec(trackedRunnable);
			} catch (RuntimeException e) {
				throw e;
			} catch (Error e) {
				throw e;
			}
		}
	}

	/**
	 * Running a runnable synchronously.
	 * <p>
	 * 
	 * @param r
	 *            the runnable
	 * @param ctrl
	 *            the control that the runnable attached to
	 */
	public static void syncPostRunnable(final Runnable r, Control ctrl) {
		final Runnable trackedRunnable = new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					// removePendingChange();
				}
			}
		};
		if (ctrl != null && !ctrl.isDisposed()) {
			try {
				ctrl.getDisplay().syncExec(trackedRunnable);
			} catch (RuntimeException e) {
				throw e;
			} catch (Error e) {
				throw e;
			}
		}
	}

	public static String getNamePrefix(IEventBEditor<?> editor,
			IInternalElementType<?> type, String defaultPrefix) {
		return "internal_" + getPrefix(editor, type, defaultPrefix); //$NON-NLS-1$
	}

	public static String getPrefix(IEventBEditor<?> editor,
			IInternalElementType<?> type, String defaultPrefix) {
		IRodinFile inputFile = editor.getRodinInput();
		String prefix = null;
		try {
			prefix = inputFile.getResource().getPersistentProperty(
					getQualifiedName(type));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (prefix == null)
			prefix = defaultPrefix;
		return prefix;
	}

	public static <T extends IInternalElement> String getFreeElementName(
			IEventBEditor<?> editor, IInternalParent parent,
			IInternalElementType<T> type, String defaultPrefix)
			throws RodinDBException {
		String prefix = getNamePrefix(editor, type, defaultPrefix);
		return prefix + EventBUtils.getFreeChildNameIndex(parent, type, prefix);
	}

	public static String getFreeElementLabel(IEventBEditor<?> editor,
			IInternalParent parent,
			IInternalElementType<? extends IInternalElement> type,
			String defaultPrefix) throws RodinDBException {
		String prefix = getPrefix(editor, type, defaultPrefix);
		return prefix + getFreeElementLabelIndex(editor, parent, type, prefix);
	}


	/**
	 * Returns an integer that can be used as a label index for a newly created
	 * element.
	 * 
	 * @param editor
	 *            unused parameter
	 * @param parent
	 *            the parent node to be looked for
	 * @param type
	 *            the type of the element to be looked for
	 * @param prefix
	 *            the prefix of the element to be looked for
	 * @return a non already used integer identifier intended for a new element
	 *         of that type (if no element of that type already exists, returns
	 *         beginIndex)
	 * @throws RodinDBException
	 */
	public static String getFreeElementLabelIndex(IEventBEditor<?> editor,
			IInternalParent parent,
			IInternalElementType<? extends IInternalElement> type,
			String prefix) throws RodinDBException {
		return getFreePrefixIndex(parent, type,
				EventBAttributes.LABEL_ATTRIBUTE, prefix);
	}

	public static String getFreeElementIdentifier(IEventBEditor<?> editor,
			IInternalParent parent,
			IInternalElementType<? extends IInternalElement> type,
			String defaultPrefix) throws RodinDBException {
		String prefix = getPrefix(editor, type, defaultPrefix);
		return prefix + getFreeElementIdentifierIndex(parent, type, prefix);
	}

	public static String getFreeElementIdentifierIndex(IInternalParent parent,
			IInternalElementType<?> type, String prefix)
			throws RodinDBException {
		return getFreePrefixIndex(parent, type, 
				EventBAttributes.IDENTIFIER_ATTRIBUTE, prefix);
	}



	public static Collection<IRodinElement> addToTreeSet(
			Collection<IRodinElement> set, IRodinElement element) {
		Collection<IRodinElement> delete = new ArrayList<IRodinElement>();
		for (IRodinElement member : set) {
			if (element.isAncestorOf(member))
				return set;
			else if (member.isAncestorOf(element)) {
				delete.add(member);
			}
		}
		set.removeAll(delete);
		set.add(element);
		return set;
	}

	/**
	 * Sets the given string attribute of the given Rodin element to the given
	 * new value, if it is not already set. In case the new value is empty, the
	 * attribute is removed.
	 * 
	 * @param element
	 * @param attrType
	 * @param newValue
	 * @param pm
	 */
	public static void setStringAttribute(IAttributedElement element,
			IAttributeType.String attrType, String newValue, IProgressMonitor pm) {
		try {
			if (newValue.length() == 0) {
				if (element.hasAttribute(attrType)) {
					element.removeAttribute(attrType, pm);
				}
			} else if (!element.hasAttribute(attrType)
					|| !newValue.equals(element.getAttributeValue(attrType))) {
				element.setAttributeValue(attrType, newValue, pm);
			}
		} catch (RodinDBException e) {
			UIUtils.log(e, "Error changing attribute " + attrType.getId() //$NON-NLS-1$
					+ " for element " + element.getElementName()); //$NON-NLS-1$
			if (UIUtils.DEBUG)
				e.printStackTrace();
		}
	}

	public static <T extends IInternalElement> String getFreeChildName(
			IEventBEditor<?> editor, IInternalParent parent,
			IInternalElementType<T> type) throws RodinDBException {
		String defaultPrefix = "element"; // TODO Get this from extensions
		String prefix = getNamePrefix(editor, type, defaultPrefix);
		return prefix + EventBUtils.getFreeChildNameIndex(parent, type, prefix);
	}

	public static QualifiedName getQualifiedName(IInternalElementType<?> type) {
		return new QualifiedName(EventBUIPlugin.PLUGIN_ID, type.getId());
	}

	public static void runWithProgressDialog(Shell shell,
			final IRunnableWithProgress op) {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, op);
		} catch (InterruptedException exception) {
			if (UIUtils.DEBUG)
				System.out.println("Interrupt");
			return;
		} catch (InvocationTargetException exception) {
			final Throwable realException = exception.getTargetException();
			if (UIUtils.DEBUG)
				System.out.println("Interrupt");
			realException.printStackTrace();
			final String message = realException.getMessage();
			MessageDialog.openError(shell, "Unexpected Error", message);
			return;
		}
	}

	/**
	 * Print the debug message in multiple line, each line with the same given
	 * prefix.
	 * 
	 * @param debugPrefix
	 *            the default prefix for the debug message
	 * @param message
	 *            the debug message
	 */
	public static void printDebugMessage(String debugPrefix, String message) {
		StringTokenizer tokenizer = new StringTokenizer(message, "\n"); //$NON-NLS-1$
		while (tokenizer.hasMoreElements()) {
			System.out.println(debugPrefix + tokenizer.nextToken());
		}
	}

	/**
	 * Return a comma separated representation of a list of input objects.
	 * 
	 * @param objects
	 *            a list of objects.
	 * @return the comma separated string representation of input objects.
	 */
	public static String toCommaSeparatedList(ArrayList<Object> objects) {
		StringBuffer buffer = new StringBuffer();
		boolean sep = false;
		for (Object item : objects) {
			if (sep) {
				sep = true;
			} else {
				buffer.append(","); //$NON-NLS-1$
			}
			buffer.append(item);
		}
		return buffer.toString();
	}

	/**
	 * Return a comma separated representation of an array of input objects.
	 * 
	 * @param objects
	 *            an array of objects.
	 * @return the comma separated string representation of input objects.
	 */
	public static String toCommaSeparatedList(String[] objects) {
		StringBuffer buffer = new StringBuffer();
		boolean sep = false;
		for (Object item : objects) {
			if (sep) {
				sep = true;
			} else {
				buffer.append(","); //$NON-NLS-1$
			}
			buffer.append(item);
		}
		return buffer.toString();
	}

	/**
	 * Parse a comma separated string to a list of string.
	 * 
	 * @param stringList
	 *            the comma separated string.
	 * @return an array of strings that make up the comma separted input string.
	 */
	public static String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, ",");//$NON-NLS-1$
		ArrayList<String> result = new ArrayList<String>();
		while (st.hasMoreElements()) {
			result.add((String) st.nextElement());
		}
		return result.toArray(new String[result.size()]);
	}

}
