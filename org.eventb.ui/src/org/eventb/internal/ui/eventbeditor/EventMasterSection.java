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

package org.eventb.internal.ui.eventbeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eventb.core.IAction;
import org.eventb.core.IEvent;
import org.eventb.core.IGuard;
import org.eventb.core.IMachine;
import org.eventb.core.IVariable;
import org.eventb.eventBKeyboard.preferences.PreferenceConstants;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

/**
 * @author htson
 * <p>
 * An implementation of the Event-B Table part with buttons
 * for displaying constants (used as master section in Master-Detail block).
 */
public class EventMasterSection 
	extends NewEventBTreePartWithButtons
{
	// The indexes for different buttons.
	private static final int ADD_EVT_INDEX = 0;
	private static final int ADD_VAR_INDEX = 1;
	private static final int ADD_GRD_INDEX = 2;
	private static final int ADD_ACT_INDEX = 3;
	private static final int UP_INDEX = 4;
	private static final int DOWN_INDEX = 5;

	// Title and description of the section.
	private final static String SECTION_TITLE = "Events";
	private final static String SECTION_DESCRIPTION = "The list contains events from the model whose details are editable on the right";
	
	private static final String [] buttonLabels =
		{"Add Event", "Add Var.", "Add Guard", "Add Action", "Up", "Down"};

	// The group of actions for the tree part.
	private ActionGroup groupActionSet;
	

	/**
	 * The content provider class. 
	 */
	class EventContentProvider
	implements IStructuredContentProvider, ITreeContentProvider
	{
		private IMachine invisibleRoot = null;
		
		public Object getParent(Object child) {
			if (child instanceof IRodinElement) return ((IRodinElement) child).getParent();
			return null;
		}
		
		public Object[] getChildren(Object parent) {
			if (parent instanceof IMachine) {
//				ArrayList<TreeNode> list = new ArrayList<TreeNode>();
				try {
					return ((IMachine) parent).getChildrenOfType(IEvent.ELEMENT_TYPE);
//					IRodinElement [] events =   ((IMachine) parent).getChildrenOfType(IEvent.ELEMENT_TYPE);
//					for (IRodinElement event : events) {
//						UIUtils.debug("Event: " + event.getElementName());
//						TreeNode node = new TreeNode(event);
//						list.add(node);
//					}
				}
				catch (RodinDBException e) {
					// TODO Exception handle
					e.printStackTrace();
				}
//				return list.toArray();
			}
			
//			if (parent instanceof TreeNode) return ((TreeNode) parent).getChildren();
			
			if (parent instanceof IParent) {
				try {
					return ((IParent) parent).getChildren();
				}
				catch (RodinDBException e) {
					// TODO Exception handle
					e.printStackTrace();
				}
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}
		
		public Object[] getElements(Object parent) {
			if (parent instanceof IRodinFile) {
				if (invisibleRoot == null) {
					invisibleRoot = (IMachine) parent;
					return getChildren(invisibleRoot);
				}
			}
			return getChildren(parent);
		}
		
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			invisibleRoot = null;
		}
	}
	
	
	/**
	 * @author htson
	 * This class provides the label for different elements in the tree.
	 */
	class EventLabelProvider 
		implements  ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object rodinElement, int columnIndex) {
//			IRodinElement rodinElement = ((TreeLeaf) element).getElement();
			if (columnIndex != 0) return null;
			return UIUtils.getImage(rodinElement);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object rodinElement, int columnIndex) {
//			IRodinElement rodinElement = ((TreeLeaf) element).getElement();
			
			if (columnIndex == 1) {
				if (rodinElement instanceof IUnnamedInternalElement) return "";
				if (rodinElement instanceof IInternalElement) return ((IInternalElement) rodinElement).getElementName();
				return rodinElement.toString();
			}
			
			if (columnIndex == 2) {
				try {
					if (rodinElement instanceof IInternalElement) return ((IInternalElement) rodinElement).getContents();
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				return rodinElement.toString();
			}
			
			if (columnIndex == 0) {
				try {
					if (rodinElement instanceof IUnnamedInternalElement) return ((IUnnamedInternalElement) rodinElement).getContents();
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				if (rodinElement instanceof IInternalElement) return ((IInternalElement) rodinElement).getElementName();
				else return rodinElement.toString();
			}
			return rodinElement.toString();

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		public Color getBackground(Object element, int columnIndex) {
			 Display display = Display.getCurrent();
             return display.getSystemColor(SWT.COLOR_WHITE);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		public Color getForeground(Object element, int columnIndex) {
			Display display = Display.getCurrent();
            return display.getSystemColor(SWT.COLOR_BLACK);
       }

//		public String getText(Object obj) {
//			if (obj instanceof IAction) {
//				try {
//					return ((IAction) obj).getContents();
//				}
//				catch (RodinDBException e) {
//					// TODO Handle Exception
//					e.printStackTrace();
//					return "";
//				}
//			}
//			if (obj instanceof IInternalElement) return ((IInternalElement) obj).getElementName();
//			return obj.toString();
//		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		public Font getFont(Object element, int columnIndex) {
//			UIUtils.debug("Get fonts");
			return JFaceResources.getFont(PreferenceConstants.EVENTB_MATH_FONT);
		}
		
		
//		public Image getImage(Object obj) {
//			return UIUtils.getImage(obj);
//		}
	
	
	
	}
	
	
	/**
	 * @author htson
	 * This class sorts the elements by types.
	 */
	private class ElementsSorter extends ViewerSorter {
		
		public int compare(Viewer viewer, Object e1, Object e2) {
	        int cat1 = category(e1);
	        int cat2 = category(e2);
	        return cat1 - cat2;
		}
		
		public int category(Object obj) {
			if (obj instanceof IVariable) return 1;
			if (obj instanceof IGuard) return 2;
			if (obj instanceof IAction) return 3;
			
			return 0;
		}
	}
	

	/**
	 * Contructor.
	 * <p>
	 * @param managedForm The form to create this master section
	 * @param parent The composite parent
	 * @param toolkit The Form Toolkit used to create this master section
	 * @param style The style
	 * @param block The master detail block which this master section belong to
	 */
	public EventMasterSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit, 
			int style, EventBEditor editor) {
		super(managedForm, parent, toolkit, style, editor, buttonLabels, SECTION_TITLE, SECTION_DESCRIPTION);
		
		makeActions();
		hookContextMenu();
		((StructuredViewer) getViewer()).setSorter(new ElementsSorter());
	}
	
	
	/*
	 * Create the actions that can be used in the tree.
	 */
	private void makeActions() {
		groupActionSet = new EventMasterSectionActionGroup(editor, (TreeViewer) this.getViewer());
	}
	
	
	/**
	 * Hook the actions to the menu
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				groupActionSet.setContext(new ActionContext(((StructuredViewer) getViewer()).getSelection()));
				groupActionSet.fillContextMenu(manager);
				groupActionSet.setContext(null);
			}
		});
		Viewer viewer = getViewer();
		Menu menu = menuMgr.createContextMenu(((Viewer) viewer).getControl());
		((Viewer) viewer).getControl().setMenu(menu);
		this.editor.getSite().registerContextMenu(menuMgr, (ISelectionProvider) viewer);
	}

	
	/*
	 * Handle add (new element) action.
	 */
	private void handleAddEvent() {
		UIUtils.newEvent(editor.getRodinInput());
		editor.editorDirtyStateChanged();
	}
	

	/*
	 * Handle up action.
	 */
	private void handleUp() {
		UIUtils.debug("Up: To be implemented");
		return;
	}
	
	
	/*
	 * Handle down action. 
	 */
	private void handleDown() {
		UIUtils.debug("Down: To be implemented");
		return;
	}
	
	
	/**
	 * Update the expanded of buttons.
	 */
	protected void updateButtons() {
		ISelection sel = ((ISelectionProvider) getViewer()).getSelection();
		Object [] selections = ((IStructuredSelection) sel).toArray();
		
		boolean hasOneSelection = selections.length == 1;
		
		boolean initSelected = false;
		
		if (hasOneSelection) {
			IRodinElement event;
			if (selections[0] instanceof IEvent) {
				event = (IRodinElement) selections[0];
			}
			else if (selections[0] instanceof IInternalElement) {
				event = ((IInternalElement) selections[0]).getParent();
			}
			else { // Should not happen
				event = null;
			}
			initSelected = (event.getElementName().equals("INITIALISATION")) ? true : false;
			
		}

		setButtonEnabled(ADD_EVT_INDEX, true);
		setButtonEnabled(ADD_VAR_INDEX, hasOneSelection && !initSelected);
		setButtonEnabled(ADD_GRD_INDEX, hasOneSelection && !initSelected);
		setButtonEnabled(ADD_ACT_INDEX, hasOneSelection);
		setButtonEnabled(UP_INDEX, hasOneSelection);
		setButtonEnabled(DOWN_INDEX, hasOneSelection);
	}
	

	/**
	 * Method to response to button selection.
	 * <p>
	 * @param index The index of selected button
	 */
	protected void buttonSelected(int index) {
		switch (index) {
			case ADD_EVT_INDEX:
				handleAddEvent();
				break;
			case ADD_VAR_INDEX:
				handleAddVar();
				break;
			case ADD_GRD_INDEX:
				handleAddGuard();
				break;
			case ADD_ACT_INDEX:
				handleAddAction();
//				EventMasterSectionActionGroup.newAction.run();
				break;
			case UP_INDEX:
				handleUp();
				break;
			case DOWN_INDEX:
				handleDown();
				break;
		}
	}
	

	private void handleAddVar() {
		try {
			TreeViewer viewer = (TreeViewer) this.getViewer();
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			if (ssel.size() == 1) {
				Object obj = ssel.getFirstElement();
				IInternalElement event = getEvent(obj);
				int counter = ((IInternalElement) event).getChildrenOfType(IVariable.ELEMENT_TYPE).length;
				IInternalElement element = event.createInternalElement(IVariable.ELEMENT_TYPE, "var"+(counter+1), null, null);
				editor.editorDirtyStateChanged();
				viewer.refresh();
				viewer.reveal(element);
				select(element, 1);
			}
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}
	}

	private void handleAddGuard() {
		try {
			TreeViewer viewer = (TreeViewer) this.getViewer();
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			if (ssel.size() == 1) {
				Object obj = ssel.getFirstElement();
				IInternalElement event = getEvent(obj);
				int counter = ((IInternalElement) event).getChildrenOfType(IGuard.ELEMENT_TYPE).length;
				IInternalElement element = event.createInternalElement(IGuard.ELEMENT_TYPE, "grd"+(counter+1), null, null);
				editor.editorDirtyStateChanged();
				viewer.refresh();
				viewer.reveal(element);
				select(element, 2);
			}
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}
	}

	private void handleAddAction() {
		try {
			TreeViewer viewer = (TreeViewer) this.getViewer();
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			if (ssel.size() == 1) {
				Object obj = ssel.getFirstElement();
				IInternalElement event = getEvent(obj);
				int counter = ((IInternalElement) event).getChildrenOfType(IAction.ELEMENT_TYPE).length;
				IInternalElement element = event.createInternalElement(IAction.ELEMENT_TYPE, null, null, null);
				element.setContents("act"+(counter+1));
				editor.editorDirtyStateChanged();
				viewer.refresh();
				viewer.reveal(element);
				select(element, 2);
			}
		}
		catch (RodinDBException e) {
			e.printStackTrace();
		}
	}
	
	private IInternalElement getEvent(Object obj) {
//		IRodinElement obj = ((TreeLeaf) obj).getElement();
		if (obj instanceof IEvent) {
			return (IEvent) obj;
		}
		else if (obj instanceof IInternalElement) {
			return (IInternalElement) ((IInternalElement) obj).getParent();
		}
		else return null; // should not happen
	}
	
	private void select(Object obj, int column) throws RodinDBException {
		UIUtils.debug("Element: " + obj);
		if (obj instanceof IAction) {
			UIUtils.debug("Action: " + ((IAction) obj).getContents());
		}
		TreeViewer viewer = (TreeViewer) this.getViewer();
		TreeItem item = (TreeItem) viewer.testFindItem(obj);
		
//		UIUtils.debug("Item: " + item);
//		
//		Rectangle rec = item.getBounds(0);
//		
//		UIUtils.debug("Bound: " + rec.toString());
//		Point pt = new Point(rec.x, rec.y);
//		Tree tree = viewer.getTree();
//		TreeItem item1 = tree.getItem(pt);	
//		UIUtils.debug("Found: " + item1);
		((EventBEditableTreeViewer) viewer).selectItem(item, column); // try to select the second column to edit name
	}
	
	/**
	 * Setting the input for the (table) viewer.
	 */
	protected void setProvider() {
		TreeViewer viewer = (TreeViewer) this.getViewer();
		viewer.setContentProvider(new EventContentProvider());
		viewer.setLabelProvider(new EventLabelProvider());
	}

	/*
	 * Create the table view part.
	 * <p>
	 * @param managedForm The Form used to create the viewer.
	 * @param toolkit The Form Toolkit used to create the viewer
	 * @param parent The composite parent
	 */
	protected EventBEditableTreeViewer createTreeViewer(IManagedForm managedForm, FormToolkit toolkit, Composite parent) {
		return new EventEditableTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION, editor.getRodinInput());
	}
	
	
	/**
	 * Set the selection in the tree viewer.
	 * <p>
	 * @param element A Rodin element
	 */
	public void setSelection(IRodinElement element) {
		TreeViewer viewer = (TreeViewer) this.getViewer();
		viewer.setSelection(new StructuredSelection(element));
	}

	
	// List of elements need to be refresh (when processing Delta of changes).
	private Collection<Object> toRefresh;
	
	private Collection<StatusObject> newStatus;

    private class StatusObject {
    	Object object;
    	boolean expanded;
		boolean selected;
    	
    	StatusObject(Object object, boolean expanded, boolean selected) {
    		this.object = object;
    		this.expanded = expanded;
    		this.selected = selected;
    	}

    	Object getObject() {return object;}
    	boolean getExpandedStatus() {return expanded;}
    	boolean getSelectedStatus() {return selected;}
    }
    
	/* (non-Javadoc)
	 * @see org.rodinp.core.IElementChangedListener#elementChanged(org.rodinp.core.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		toRefresh = new HashSet<Object>();
		newStatus = new HashSet<StatusObject>();
		processDelta(event.getDelta());
		postRefresh(toRefresh, true);
	}
	
	private void processDelta(IRodinElementDelta delta) {
		int kind= delta.getKind();
		IRodinElement element= delta.getElement();
		if (kind == IRodinElementDelta.ADDED) {
			UIUtils.debug("Added: " + element.getElementName());
			// Handle move operation
			if ((delta.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0) {
				IRodinElement oldElement = delta.getMovedFromElement();
				TreeViewer viewer = (TreeViewer) getViewer();
				IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
				boolean selected = ssel.toList().contains(oldElement);
				newStatus.add(new StatusObject(element, viewer.getExpandedState(oldElement), selected));
			}
			Object parent = element.getParent();
			toRefresh.add(parent);
			return;
		}
		
		if (kind == IRodinElementDelta.REMOVED) {
			// Ignore the move operation
			UIUtils.debug("Removed: " + element.getElementName());			
			Object parent = element.getParent();
			toRefresh.add(parent);
			return;
		}
		
		if (kind == IRodinElementDelta.CHANGED) {
			int flags = delta.getFlags();
			UIUtils.debug("Changed: " + element.getElementName());
			
			if ((flags & IRodinElementDelta.F_CHILDREN) != 0) {
				UIUtils.debug("CHILDREN");
				IRodinElementDelta [] deltas = delta.getAffectedChildren();
				for (int i = 0; i < deltas.length; i++) {
					processDelta(deltas[i]);
				}
				return;
			}
			
			if ((flags & IRodinElementDelta.F_REORDERED) != 0) {
				UIUtils.debug("REORDERED");
				toRefresh.add(element.getParent());
				return;
			}
			
			if ((flags & IRodinElementDelta.F_CONTENT) != 0) {
				UIUtils.debug("CONTENT");

				toRefresh.add(element);
				return;
			}
		}

	}
	
	/**
	 * Refresh the nodes.
	 * <p>
	 * @param toRefresh List of node to refresh
	 * @param updateLabels <code>true</code> if the label need to be updated as well
	 */
	private void postRefresh(final Collection toRefresh, final boolean updateLabels) {
		postRunnable(new Runnable() {
			public void run() {
				TreeViewer viewer = (TreeViewer) getViewer();
				Control ctrl= viewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					
					ISelection sel = viewer.getSelection();
					Object [] objects = viewer.getExpandedElements();
					for (Iterator iter = toRefresh.iterator(); iter.hasNext();) {
						IRodinElement element = (IRodinElement) iter.next();
						UIUtils.debug("Event Refresh element " + element.getElementName());
						viewer.refresh(element, updateLabels);
					}
					viewer.setExpandedElements(objects);
					viewer.setSelection(sel);

					for (Iterator iter = newStatus.iterator(); iter.hasNext();) {
						StatusObject state = (StatusObject) iter.next();
						UIUtils.debug("Object: " + state.getObject() + " expanded: " + state.getExpandedStatus());
						viewer.setExpandedState(state.getObject(), state.getExpandedStatus());
						
						if (state.getSelectedStatus()) {
							IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
							ArrayList<Object> list = new ArrayList<Object>(ssel.size() + 1);
							for (Iterator it = ssel.iterator(); it.hasNext();) {
								list.add(it.next());
							}
							list.add(state.getObject());
							viewer.setSelection(new StructuredSelection(list));
						}
					}
//					if (lastMouseEvent != null) mouseAdapter.mouseDown(lastMouseEvent);
				}
			}
		});
	}
	
	private void postRunnable(final Runnable r) {
		Viewer viewer = getViewer();
		Control ctrl= viewer.getControl();
		final Runnable trackedRunnable= new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					//removePendingChange();
					//if (UIUtils.DEBUG) System.out.println("Runned");
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
	
	/* (non-Javadoc)
	 * @see org.eventb.internal.ui.eventbeditor.EventBPartWithButtons#edit(org.rodinp.core.IRodinElement)
	 */
	@Override
	protected void edit(IRodinElement element) {
		TreeViewer viewer = (TreeViewer) this.getViewer();
		viewer.reveal(element);
		TreeItem item  = (TreeItem) viewer.testFindItem(element);
//		Rectangle rec = item.getBounds();
//		Point pt = new Point(rec.x + rec.width/2, rec.y + rec.height/2);
		selectItem(item, 1);
	}

}
