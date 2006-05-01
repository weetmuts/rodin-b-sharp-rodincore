package org.eventb.internal.ui.eventbeditor;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachine;
import org.eventb.eventBKeyboard.preferences.PreferenceConstants;
import org.eventb.internal.ui.UIUtils;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IUnnamedInternalElement;
import org.rodinp.core.RodinDBException;

public class InvariantEditableTableViewer extends EventBEditableTableViewer {

	/**
	 * The content provider class. 
	 */
	class InvariantContentProvider
	implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			if (parent instanceof IMachine)
				try {
					return ((IMachine) parent).getInvariants();
				}
				catch (RodinDBException e) {
					// TODO Exception handle
					e.printStackTrace();
				}
			return new Object[0];
		}
    	
    	public void dispose() {return;}

    	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    		return;
    	}
    }
	
	class InvariantLabelProvider 
	implements  ITableLabelProvider, ITableFontProvider, ITableColorProvider
	{
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 0) return null;
			return UIUtils.getImage(element);
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			
			if (columnIndex == 1) {
				try {
					if (element instanceof IInternalElement) return ((IInternalElement) element).getContents();
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				return element.toString();
			}
			
			if (columnIndex == 0) {
				try {
					if (element instanceof IUnnamedInternalElement) return ((IUnnamedInternalElement) element).getContents();
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				if (element instanceof IInternalElement) return ((IInternalElement) element).getElementName();
				else return element.toString();
			}
			return element.toString();
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
	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		public Font getFont(Object element, int columnIndex) {
			return JFaceResources.getFont(PreferenceConstants.EVENTB_MATH_FONT);
		}
	
	}

	public InvariantEditableTableViewer(Composite parent, int style, IRodinFile rodinFile) {
		super(parent, style, rodinFile);
		this.setContentProvider(new InvariantContentProvider());
		this.setLabelProvider(new InvariantLabelProvider());
	}
	
	public void commit(int row, int col, String text) {
		// Determine which row was selected
        TableItem item = this.getTable().getItem(row);
        if (item == null) return; 
        Object itemData = item.getData();
		if (itemData instanceof IInternalElement) {
			switch (col) {
			case 0:  // Commit name
				try {
					UIUtils.debug("Commit : " + ((IInternalElement) itemData).getElementName() + " to be : " + text);
					if (!((IInternalElement) itemData).getElementName().equals(text)) {
						((IInternalElement) itemData).rename(text, false, null);
					}
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				
				break;
			case 1:  // Commit name
				try {
					UIUtils.debug("Commit content: " + ((IInternalElement) itemData).getContents() + " to be : " + text);
					if (!((IInternalElement) itemData).getContents().equals(text)) {
						((IInternalElement) itemData).setContents(text);
					}
				}
				catch (RodinDBException e) {
					e.printStackTrace();
				}
				
				break;
			}
		}
	}

	protected void newElement(Table table, TableItem item, int column) {
		try {
			int counter = rodinFile.getChildrenOfType(IInvariant.ELEMENT_TYPE).length;
			IInternalElement element = rodinFile.createInternalElement(IInvariant.ELEMENT_TYPE, "inv"+(counter+1), null, null);
			refresh();
			reveal(element);
			int row = table.indexOf(item);
			selectRow(row + 1, column);
		}
		catch (RodinDBException exception) {
			exception.printStackTrace();
		}
	}
	
	protected void createTableColumns(Table table) {
		TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setResizable(true);
		nameColumn.setWidth(100);

		
		TableColumn predicateColumn = new TableColumn(table, SWT.LEFT);
		predicateColumn.setText("Predicate");
		predicateColumn.setResizable(true);
		predicateColumn.setWidth(250);
		
		table.setHeaderVisible(true);
	}

}
