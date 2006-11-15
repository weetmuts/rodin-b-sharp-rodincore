package org.rodinp.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents an entire Rodin file. File elements need to be opened before they
 * can be navigated or manipulated.
 * 
 * TODO write doc for IRodinFile.
 *
 * @author Laurent Voisin
 */
public interface IRodinFile extends IOpenable, IInternalParent,
		IElementManipulation {

	/**
	 * Creates this file in the database. As a side effect, all ancestors of
	 * this element are open if they were not already.
	 * <p>
	 * It is possible that this file already exists. The value of the
	 * <code>force</code> parameter effects the resolution of such a conflict:
	 * <ul>
	 * <li><code>true</code> - in this case the file is created anew with
	 * empty contents</li>
	 * <li><code>false</code> - in this case a <code>RodinDBException</code>
	 * is thrown</li>
	 * </ul>
	 * </p>
	 * 
	 * @param force
	 *            specify how to handle conflict is this element already exists
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @exception RodinDBException
	 *                if the element could not be created. Reasons include:
	 *                <ul>
	 *                <li> The parent of this element does not exist
	 *                (ELEMENT_DOES_NOT_EXIST)</li>
	 *                <li>This file element exists and force is
	 *                <code>false</code> (NAME_COLLISION)</li>
	 *                <li> A <code>CoreException</code> occurred while
	 *                creating an underlying resource
	 *                </ul>
	 */
	void create(boolean force, IProgressMonitor monitor) throws RodinDBException;
	
	/**
	 * Finds the elements in this file that correspond to the given element. An
	 * element A corresponds to an element B if:
	 * <ul>
	 * <li>A has the same element type and name as B.
	 * <li>The parent of A corresponds to the parent of B recursively up to
	 * their respective files.
	 * <li>A exists.
	 * </ul>
	 * Returns <code>null</code> if no such Rodin elements can be found or if
	 * the given element is not included in a file.
	 * 
	 * @param element
	 *            the given element
	 * @return the found elements in this file that correspond to the given
	 *         element
	 */
	IRodinElement[] findElements(IRodinElement element);

	/* (non-Javadoc)
	 * @see org.rodinp.core.IRodinElement#getElementType()
	 */
	IFileElementType getElementType();
	
	/* (non-Javadoc)
	 * @see org.rodinp.core.IRodinElement#getResource()
	 */
	IFile getResource();

	/* (non-Javadoc)
	 * @see org.rodinp.core.IInternalParent#getSnapshot()
	 */
	IRodinFile getSnapshot();
	
	/* (non-Javadoc)
	 * @see org.rodinp.core.IInternalParent#getMutableCopy()
	 */
	IRodinFile getMutableCopy();

}