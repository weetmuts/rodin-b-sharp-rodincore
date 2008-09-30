package org.eventb.core.indexer;

import org.eventb.core.ast.SourceLocation;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.index.IOccurrenceKind;
import org.rodinp.core.index.IRodinLocation;
import org.rodinp.core.index.RodinIndexer;

public class EventBIndexUtil {
	
	public static final IOccurrenceKind 	DECLARATION =
		RodinIndexer.addOccurrenceKind("fr.systerel.eventb.indexer.declaration",
				"declaration");
	public static final IOccurrenceKind 	REFERENCE =
		RodinIndexer.addOccurrenceKind("fr.systerel.eventb.indexer.reference",
				"reference");

	private static int alloc;

	public static String getUniqueName(String identifierString) {
		if (alloc == Integer.MAX_VALUE)
			throw new IndexOutOfBoundsException();
		return "n" + alloc++ + "_" + identifierString;
	}

	/**
	 * When extracting a location from a SourceLocation, using that method is
	 * mandatory, as long as SourceLocation and RodinLocation do not share the
	 * same range convention.
	 * 
	 * @param element
	 * @param attributeType
	 * @param location
	 * @return
	 */
	public static IRodinLocation getRodinLocation(IInternalParent element,
			IAttributeType.String attributeType, SourceLocation location) {
		return RodinIndexer.getRodinLocation(element, attributeType, location
				.getStart(), location.getEnd() + 1);
	}

}
