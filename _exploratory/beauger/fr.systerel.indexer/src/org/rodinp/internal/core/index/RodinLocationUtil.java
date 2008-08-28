package org.rodinp.internal.core.index;

import static org.rodinp.core.IRodinDBStatusConstants.ATTRIBUTE_DOES_NOT_EXIST;
import static org.rodinp.core.IRodinDBStatusConstants.ELEMENT_DOES_NOT_EXIST;
import static org.rodinp.core.IRodinDBStatusConstants.INVALID_ATTRIBUTE_KIND;

import org.rodinp.core.IAttributeType;
import org.rodinp.core.IAttributedElement;
import org.rodinp.core.IRodinDBStatus;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.index.RodinIndexer;
import org.rodinp.internal.core.RodinDBStatus;

public class RodinLocationUtil {

	public static IRodinDBStatus verifyRodinLocation(IRodinElement element,
			IAttributeType attributeType, int charStart, int charEnd) {
		if (!element.exists()) {
			return new RodinDBStatus(ELEMENT_DOES_NOT_EXIST, element);
		}
		if (attributeType == null) {
			return verifyNoLocation(charStart, charEnd);
		} else {
			return verifyWithLocation(element, attributeType, charStart,
					charEnd);
		}
	}

	private static IRodinDBStatus verifyWithLocation(IRodinElement element,
			IAttributeType attributeType, int charStart, int charEnd) {

		IRodinDBStatus status = verifyAttributeId(element, attributeType,
				charStart >= 0);
		if (!status.isOK()) {
			return status;
		}
		if (charStart >= 0) {
			if (charStart >= charEnd) { // charEnd must be EXclusive
				return errorStatus("End position is before start position");
			}
		} else if (charEnd >= 0) {
			return errorStatus("End position without a start position");
		}
		return RodinDBStatus.VERIFIED_OK;
	}

	private static IRodinDBStatus verifyNoLocation(int charStart, int charEnd) {
		if (charStart >= 0) {
			return errorStatus("Start position without an attribute id");
		}
		if (charEnd >= 0) {
			return errorStatus("End position without an attribute id");
		}
		return RodinDBStatus.VERIFIED_OK;
	}

	private static IRodinDBStatus verifyAttributeId(IRodinElement element,
			IAttributeType attributeType, boolean withCharPos) {
		if (element instanceof IAttributedElement) {
			IAttributedElement ie = (IAttributedElement) element;
			// Check that attribute exists
			try {
				if (withCharPos && !ie.hasAttribute(attributeType)) {
					return new RodinDBStatus(ATTRIBUTE_DOES_NOT_EXIST, ie,
							attributeType.getId());
				}
			} catch (RodinDBException rde) {
				return rde.getRodinDBStatus();
			}
			if (withCharPos) {
				// Check that it's an attribute of kind String
				if (!(attributeType instanceof IAttributeType.String)) {
					return new RodinDBStatus(INVALID_ATTRIBUTE_KIND,
							attributeType.getId());
					/** To be moved to {@link RodinCore} */

				}
			}
		}
		return RodinDBStatus.VERIFIED_OK;
	}

	/** **************************************************************************** */

	// public static IRodinDBStatus verifyRodinLocation(IRodinLocation location)
	// {
	// final IRodinElement element = location.getElement();
	// if (!element.exists()) {
	// return new RodinDBStatus(ELEMENT_DOES_NOT_EXIST, element);
	// }
	// IAttributeType attributeType = location.getAttributeType();
	// if (attributeType != null) {
	// return verifyWithLocation(location);
	// }
	// return verifyWithoutLocation(location);
	// }
	//
	// private static IRodinDBStatus verifyWithLocation(IRodinLocation location)
	// {
	// int charStart = location.getCharStart();
	// int charEnd = location.getCharEnd();
	//
	// IRodinDBStatus status = verifyAttributeId(location, charStart >= 0);
	// if (!status.isOK()) {
	// return status;
	// }
	// if (charStart >= 0) {
	// if (charStart >= charEnd) {
	// return errorStatus("End position is before start position");
	// }
	// } else if (charEnd >= 0) {
	// return errorStatus("End position without a start position");
	// }
	// return RodinDBStatus.VERIFIED_OK;
	// }
	//
	// private static IRodinDBStatus verifyAttributeId(IRodinLocation location,
	// boolean withCharPos) {
	// final IRodinElement element = location.getElement();
	// if (element instanceof IAttributedElement) {
	// IAttributedElement ie = (IAttributedElement) element;
	// IAttributeType attrType = location.getAttributeType();
	// // Check that attribute exists
	// try {
	// if (withCharPos && !ie.hasAttribute(attrType)) {
	// return new RodinDBStatus(ATTRIBUTE_DOES_NOT_EXIST, ie,
	// attrType.getId());
	// }
	// } catch (RodinDBException rde) {
	// return rde.getRodinDBStatus();
	// }
	// if (withCharPos) {
	// // Check that it's an attribute of kind String
	// if (!(attrType instanceof IAttributeType.String)) {
	// return new RodinDBStatus(INVALID_ATTRIBUTE_KIND, attrType
	// .getId()); /** To be moved to {@link RodinCore} */
	//
	// }
	// }
	// }
	// return RodinDBStatus.VERIFIED_OK;
	// }
	//
	// private static IRodinDBStatus verifyWithoutLocation(IRodinLocation
	// location) {
	// if (location.getCharStart() >= 0) {
	// return errorStatus("Start position without an attribute id");
	// }
	// if (location.getCharEnd() >= 0) {
	// return errorStatus("End position without an attribute id");
	// }
	// return RodinDBStatus.VERIFIED_OK;
	// }
	private static IRodinDBStatus errorStatus(String message) {
		final Exception exc = new IllegalArgumentException(message);
		return new RodinDBStatus(RodinIndexer.INVALID_LOCATION, exc);
	}

}
