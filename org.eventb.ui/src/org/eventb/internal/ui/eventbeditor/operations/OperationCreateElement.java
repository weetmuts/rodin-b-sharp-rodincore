package org.eventb.internal.ui.eventbeditor.operations;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.rodinp.core.IInternalElement;

/**
 * Operation Node avec une operation parent pour creer un element Les operations
 * fils dependent de l'element cree.
 * <p>
 * Lors de execute, l'element creer de vient le pere de chaque fils.
 * <p>
 * la methode setParent de OperationCreateElement ne s'applique que sur
 * operationCreate
 */
public class OperationCreateElement extends AbstractOperation implements
		OperationTree {

	final private CreateElementGeneric<?> operationCreate;

	final private OperationNode operationChildren;

	public OperationCreateElement(CreateElementGeneric<?> operationCreate) {
		super("OperationCreatedElement");
		this.operationCreate = operationCreate;
		operationChildren = new OperationNode();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		operationCreate.execute(monitor, info);
		final IInternalElement element = operationCreate.getElement();
		for (OperationTree op : operationChildren) {
			op.setParent(element);
		}
		return operationChildren.execute(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		operationCreate.redo(monitor, info);
		return operationChildren.redo(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return operationCreate.undo(monitor, info);
	}

	/**
	 * Set parent for creation of the element
	 */
	public void setParent(IInternalElement element) {
		operationCreate.setParent(element);
	}

	/**
	 * @return if many element are created, the first created element are the
	 *         first in the resulting collection. For example, in case of an
	 *         event with action, getCreatedElements().get(0) is the event.
	 * 
	 */
	public Collection<IInternalElement> getCreatedElements() {
		return operationCreate.getCreatedElements();
	}



	/**
	 * @return if many element are created, return the first created element. For example, in case of an
	 *         event with action, getCreatedElement() is the event.
	 * 
	 */
	public IInternalElement getCreatedElement() {
		return operationCreate.getCreatedElement();
	}


	
	public void addSubCommande(OperationTree cmd) {
		if (cmd != this) {
			operationChildren.addCommande(cmd);
		}
	}
}
