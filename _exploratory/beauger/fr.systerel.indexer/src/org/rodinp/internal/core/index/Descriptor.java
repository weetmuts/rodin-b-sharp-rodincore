package org.rodinp.internal.core.index;

import java.util.HashSet;
import java.util.Set;

import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.index.IDescriptor;
import org.rodinp.core.index.Occurrence;

public final class Descriptor implements IDescriptor {

	/**
	 * Name of the described element. It is intended to be used as a public
	 * name, known by the user, as opposed to the name returned by
	 * {@link IRodinElement#getElementName()}, which is of a rather internal
	 * scope.
	 */
	private String name;
	private IInternalElement element;
	private Set<Occurrence> occurrences;

	public Descriptor(String name, IInternalElement element) {
		this.name = name;
		this.element = element;
		this.occurrences = new HashSet<Occurrence>();
	}

	public String getName() {
		return name;
	}

	public IInternalElement getElement() {
		return element;
	}

	public Occurrence[] getOccurrences() {
		return occurrences.toArray(new Occurrence[occurrences.size()]);
	}

	public boolean hasOccurrence(Occurrence occurrence) {
		return occurrences.contains(occurrence);
	}

	public void addOccurrence(Occurrence occurrence) {
		if (verifyOccurrence(occurrence)) {
			occurrences.add(occurrence);
		} else {
			throw new IllegalArgumentException(
					"trying to add an alien occurrence:\n"
							+ occurrence.toString() + "for element: " + name);
		}
	}

	public void removeOccurrence(Occurrence occurrence) {
		occurrences.remove(occurrence);
	}

	public void clearOccurrences() {
		occurrences.clear();
	}

	private boolean verifyOccurrence(Occurrence occ) {
		final IRodinElement locElem = occ.getLocation().getElement();
		final IRodinFile rodinFile = element.getRodinFile();
		if (locElem instanceof IRodinFile) {
			return locElem == rodinFile;
		}
		if (locElem instanceof IInternalElement) {
			return ((IInternalElement) locElem).getRodinFile() == rodinFile;
		}
		return false;
	}

	// DEBUG
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("*** descriptor: ");
		sb.append(element.getElementName() + "\n");
		sb.append("Name: " + name + "\n");
		
		for (Occurrence ref : occurrences) {
			sb.append(ref.toString() + "\n");
		}
		return sb.toString();
	}

}
