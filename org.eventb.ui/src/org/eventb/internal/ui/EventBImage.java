/*******************************************************************************
 * Copyright (c) 2005 ETH Zurich.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodin @ ETH Zurich
 ******************************************************************************/

package org.eventb.internal.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eventb.core.ICommentedElement;
import org.eventb.core.IPSStatus;
import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IProofTreeNode;
import org.eventb.ui.EventBUIPlugin;
import org.eventb.ui.IEventBSharedImages;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.RodinMarkerUtil;

/**
 * @author htson
 *         <p>
 *         This class handles the images that are used in the Event-B UI.
 */
public class EventBImage {

	// Just for fun :-)
	public static final String IMG_PENGUIN = "Penguine";

	/**
	 * Image IDs for buttons, menu, etc.
	 */
	public static final String IMG_NEW_PROJECT = "New Project";

	public static final String IMG_NEW_COMPONENT = "New Component";

	public static final String IMG_DISCHARGED_SMILEY = "Discharged Smiley";
	
	public static final String IMG_PENDING_SMILEY = "Pending Smiley";

	public static final String IMG_REVIEW_SMILEY = "Review Smiley";

	/**
	 * Returns an image descriptor for the image file within the Event-B UI
	 * Plugin at the given plug-in relative path
	 * <p>
	 * 
	 * @param path
	 *            relative path of the image within this Event-B UI Plugin
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return getImageDescriptor(EventBUIPlugin.PLUGIN_ID, path);
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in and
	 * the relative path within the plugin
	 * <p>
	 * 
	 * @param path
	 *            relative path of the image
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String pluginID,
			String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, path);
	}

	/**
	 * Initialialise the image registry. Additional image should be added here
	 * <p>
	 * 
	 * @param registry
	 *            The image registry
	 */
	public static void initializeImageRegistry(ImageRegistry registry) {
		registerImage(registry, IEventBSharedImages.IMG_MACHINE, "icons/full/obj16/mch_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_CONTEXT, "icons/full/obj16/ctx_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_VARIABLES, "icons/full/obj16/vars_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_INVARIANTS, "icons/full/obj16/invs_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_THEOREMS, "icons/full/obj16/thms_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_EVENTS, "icons/full/obj16/evts_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_CARRIER_SETS,
				"icons/full/obj16/sets_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_CONSTANTS, "icons/full/obj16/csts_obj.gif");
		registerImage(registry, IEventBSharedImages.IMG_AXIOMS, "icons/full/obj16/axms_obj.gif");
		registerImage(registry, IMG_NEW_PROJECT,
				"icons/full/clcl16/newprj_wiz.gif");
		registerImage(registry, IMG_NEW_COMPONENT,
				"icons/full/clcl16/newcomp_wiz.gif");

		registerImage(registry, IEventBSharedImages.IMG_PENDING, "icons/pending.gif");
		registerImage(registry, IEventBSharedImages.IMG_PENDING_BROKEN, "icons/pending_broken.gif");
		registerImage(registry, IEventBSharedImages.IMG_APPLIED, "icons/applied.gif");
		registerImage(registry, IEventBSharedImages.IMG_DISCHARGED, "icons/discharged.gif");
		registerImage(registry, IEventBSharedImages.IMG_DISCHARGED_BROKEN,
				"icons/discharged_broken.gif");
		registerImage(registry, IEventBSharedImages.IMG_REVIEWED, "icons/reviewed.gif");
		registerImage(registry, IEventBSharedImages.IMG_REVIEWED_BROKEN,
				"icons/reviewed_broken.gif");
		registerImage(registry, IEventBSharedImages.IMG_UNATTEMPTED, "icons/unattempted.gif");
		registerImage(registry, IEventBSharedImages.IMG_DEFAULT, "icons/sample.gif");
		registerImage(registry, IEventBSharedImages.IMG_REFINES, "icons/full/ctool16/refines.gif");

		registerImage(registry, IMG_PENGUIN, "icons/penguins-dancing.gif");
		registerImage(registry, IEventBSharedImages.IMG_NULL, "icons/full/ctool16/null.gif");
		
		registerImage(registry, IEventBSharedImages.IMG_ADD, "icons/full/ctool16/add.gif");
		registerImage(registry, IEventBSharedImages.IMG_REMOVE, "icons/full/ctool16/remove.gif");

		registerImage(registry, IEventBSharedImages.IMG_COLLAPSED, "icons/full/elcl16/collapsed.gif");
		registerImage(registry, IEventBSharedImages.IMG_COLLAPSED_HOVER, "icons/full/elcl16/collapsedHover.gif");
		registerImage(registry, IEventBSharedImages.IMG_EXPANDED, "icons/full/elcl16/expanded.gif");
		registerImage(registry, IEventBSharedImages.IMG_EXPANDED_HOVER, "icons/full/elcl16/expandedHover.gif");

		registerImage(registry, EventBImage.IMG_DISCHARGED_SMILEY, "icons/full/ctool16/wink-green.gif");
		registerImage(registry, EventBImage.IMG_PENDING_SMILEY, "icons/full/ctool16/sad.gif");
		registerImage(registry, EventBImage.IMG_REVIEW_SMILEY, "icons/full/ctool16/wink-blue.gif");
	}

	/**
	 * Register an image with the image registry
	 * <p>
	 * 
	 * @param registry
	 *            the image registry
	 * @param key
	 *            the key to retrieve the image later
	 * @param path
	 *            the path to the location of the image file within this Event-B
	 *            UI plugin
	 */
	public static void registerImage(ImageRegistry registry, String key,
			String path) {
		ImageDescriptor desc = getImageDescriptor(path);
		registry.put(key, desc);
	}

	/**
	 * Register an image with the image registry
	 * <p>
	 * 
	 * @param registry
	 *            the image reigstry
	 * @param key
	 *            the key to retrieve the image later
	 * @param pluginID
	 *            the id of the plugin where the image can be found
	 * @param path
	 *            the path to the location of the image file within the plugin
	 */
	public static void registerImage(ImageRegistry registry, String key,
			String pluginID, String path) {
		ImageDescriptor desc = getImageDescriptor(pluginID, path);
		registry.put(key, desc);
	}

	/**
	 * Get an image from the image registry with a given key
	 * <p>
	 * 
	 * @param key
	 *            a key (String)
	 * @return an image associated with the input key or null if it does not
	 *         exist
	 */
	public static Image getImage(String key) {
		ImageRegistry registry = EventBUIPlugin.getDefault().getImageRegistry();
		return registry.get(key);
	}

	/**
	 * Getting an impage corresponding to a Rodin element.
	 * <p>
	 * 
	 * @param element
	 *            A Rodin element
	 * @return The image for displaying corresponding to the input element
	 */
	public static Image getRodinImage(IRodinElement element) {
		
		ImageDescriptor desc = ElementUIRegistry.getDefault()
				.getImageDescriptor(element);
		if (desc == null)
			return null;

		int F_COMMENT = 0x00001;
		
		int F_ERROR = 0x00002;
		
		// Compute the key
		// key = desc:Description:overlay
		// overlay = comment + error
		String key = "desc:" + desc;
		int overlay = 0;
		if (element instanceof ICommentedElement) {
			ICommentedElement ce = (ICommentedElement) element;
			try {
				if (ce.hasComment() && ce.getComment().length() != 0)
					overlay = overlay | F_COMMENT;
			} catch (RodinDBException e) {
				// Do nothing
				if (UIUtils.DEBUG) e.printStackTrace();
			}
		}
		
		if (element instanceof IRodinProject) {
			IRodinProject rodinProj = (IRodinProject) element;
			try {
				IMarker[] errorMarkers = rodinProj.getResource().findMarkers(
						RodinMarkerUtil.RODIN_PROBLEM_MARKER, true,
						IResource.DEPTH_INFINITE);
				if (errorMarkers.length != 0) {
					overlay = overlay | F_ERROR;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (element instanceof IRodinFile) {
			IRodinFile rodinFile = (IRodinFile) element;
			try {
				IMarker[] errorMarkers = rodinFile.getResource().findMarkers(
						RodinMarkerUtil.RODIN_PROBLEM_MARKER, true,
						IResource.DEPTH_ZERO);
				if (errorMarkers.length != 0) {
					overlay = overlay | F_ERROR;
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		key += ":" + overlay;

		ImageRegistry registry = EventBUIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			if (UIUtils.DEBUG)
				System.out.println("Create a new image: " + key);
			OverlayIcon icon = new OverlayIcon(desc);
			if ((overlay & F_COMMENT) != 0)
				icon
						.addTopLeft(getImageDescriptor(IEventBSharedImages.IMG_COMMENT_OVERLAY_PATH));
			if ((overlay & F_ERROR) != 0)
				icon
						.addBottomLeft(getImageDescriptor(IEventBSharedImages.IMG_ERROR_OVERLAY_PATH));
			image = icon.createImage();
			registry.put(key, image);
		}
		return image;

	}

	/**
	 * Get the path of base image corresponding to a proof tree node
	 * <p>
	 * 
	 * @param node
	 *            a proof tree node
	 * @return the path of the base image corresponding to the input node
	 */
	private static final String getProofTreeNodeBaseImagePath(
			IProofTreeNode node) {

		int confidence = node.getConfidence();

		if (confidence == IConfidence.PENDING) {
			if (node.hasChildren())
				return IEventBSharedImages.IMG_APPLIED_PATH;
			return IEventBSharedImages.IMG_PENDING_PATH;
		}
		if (confidence <= IConfidence.REVIEWED_MAX)
			return IEventBSharedImages.IMG_REVIEWED_PATH;
		if (confidence <= IConfidence.DISCHARGED_MAX)
			return IEventBSharedImages.IMG_DISCHARGED_PATH;
		return IEventBSharedImages.IMG_NULL_PATH;
	}

	/**
	 * Get the image corresponding to a proof tree node
	 * <p>
	 * 
	 * @param node
	 *            a proof tree node
	 * @return the image corresponding to the input node
	 */
	public static Image getProofTreeNodeImage(IProofTreeNode node) {
		String base_path = getProofTreeNodeBaseImagePath(node);

		// Compute the key
		// key = "node":pluginID:base_path:overlay
		// overlay = comment
		String key = "node:" + base_path;

		String comment = "0";
		if (!node.getComment().equals("")) {
			comment = "1";
		}
		key += ":" + comment;

		// Return the image if it exists, otherwise create a new image and
		// register with the registry.
		ImageRegistry registry = EventBUIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			if (UIUtils.DEBUG)
				System.out.println("Create a new image: " + key);
			OverlayIcon icon = new OverlayIcon(getImageDescriptor(base_path));
			if (comment == "1")
				icon.addTopLeft(getImageDescriptor(IEventBSharedImages.IMG_COMMENT_OVERLAY_PATH));
			image = icon.createImage();
			registry.put(key, image);
		}
		return image;
	}

	/**
	 * Get the image corresponding with a PRSequent
	 * 
	 * @param status
	 *            a PRSequent
	 * @return the image corresponding to the input sequent
	 */
	public static Image getPRSequentImage(IPSStatus status) {
		String base_path = "";

		int F_AUTO = 0x00001;
		
		int F_INACCURATE = 0x00002;
		
		int confidence;
		try {
			confidence = status.getProofConfidence();
		} catch (RodinDBException e) {
			String message = "Cannot get the confidence from the status of"
				+ status.getElementName();
			if (UIUtils.DEBUG) {
				System.out.println(message);
				e.printStackTrace();
			}
			UIUtils.log(e, message);
			return null;
		}

		boolean isAttempted = confidence > IConfidence.UNATTEMPTED;
		if (!isAttempted)
			base_path = IEventBSharedImages.IMG_UNATTEMPTED_PATH;
		else {
			boolean isProofBroken;
			try {
				isProofBroken = status.isBroken();
			} catch (RodinDBException e) {
				String message = "Cannot check if the proof tree of the sequent "
					+ status.getElementName() + " is brocken or not";
				if (UIUtils.DEBUG) {
					System.out.println(message);
					e.printStackTrace();
				}
				UIUtils.log(e, message);
				return null;
			}
			if (isProofBroken) {
				if (confidence == IConfidence.PENDING)
					base_path = IEventBSharedImages.IMG_PENDING_BROKEN_PATH;
				else if (confidence <= IConfidence.REVIEWED_MAX)
					base_path = IEventBSharedImages.IMG_REVIEWED_BROKEN_PATH;
				else if (confidence <= IConfidence.DISCHARGED_MAX)
					base_path = IEventBSharedImages.IMG_DISCHARGED_BROKEN_PATH;
			} else {
				if (confidence == IConfidence.PENDING)
					base_path = IEventBSharedImages.IMG_PENDING_PATH;
				else if (confidence <= IConfidence.REVIEWED_MAX)
					base_path = IEventBSharedImages.IMG_REVIEWED_PATH;
				else if (confidence <= IConfidence.DISCHARGED_MAX)
					base_path = IEventBSharedImages.IMG_DISCHARGED_PATH;
			}
		}

		int overlay = 0;

		boolean isAutomatic = false;
		try {
			isAutomatic = ! status.hasManualProof();
		} catch (RodinDBException e) {
			String message = "Cannot check if the proof tree of the sequent "
				+ status.getElementName()
				+ " is automatically generated or not";
			if (UIUtils.DEBUG) {
				System.out.println(message);
				e.printStackTrace();
			}
		}
		if (isAutomatic) {
			overlay = overlay | F_AUTO;
		}

		boolean isAccurate = false;
		try {
			isAccurate = status.getPOSequent().isAccurate();
		} catch (RodinDBException e) {
			// Do nothing
		}
		if (!isAccurate) {
			overlay = overlay | F_INACCURATE;
		}
		// Compute the key
		// key = "prsequent":pluginID:base_path:overlay
		// overlay = auto
		String key = "prsequent:" + base_path + ":" + overlay;

		// Return the image if it exists, otherwise create a new image and
		// register with the registry.
		ImageRegistry registry = EventBUIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			if (UIUtils.DEBUG)
				System.out.println("Create a new image: " + key);
			OverlayIcon icon = new OverlayIcon(getImageDescriptor(base_path));
			if ((overlay & F_AUTO) != 0)
				icon
						.addTopRight(getImageDescriptor(IEventBSharedImages.IMG_AUTO_OVERLAY_PATH));
			if ((overlay & F_INACCURATE) != 0)
				icon
						.addBottomLeft(getImageDescriptor(IEventBSharedImages.IMG_WARNING_OVERLAY_PATH));
			image = icon.createImage();
			registry.put(key, image);
		}

		return image;
	}
}
