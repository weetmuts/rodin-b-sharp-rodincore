package org.rodinp.internal.core.index.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Node<T> {

	private final T label;
	private final List<Node<T>> predecessors;
	private final List<Node<T>> successors; // FIXME maybe not needed (but for
	// dependents)
	private boolean mark;

	public Node(T label) {
		this.label = label;
		this.predecessors = new ArrayList<Node<T>>();
		this.successors = new ArrayList<Node<T>>();
		this.mark = false;
	}

	public T getLabel() {
		return label;
	}

	public List<Node<T>> getPredecessors() {
		return new ArrayList<Node<T>>(predecessors);
	}

	public List<Node<T>> getSuccessors() {
		return new ArrayList<Node<T>>(successors);
	}

	public void addPredecessor(Node<T> tail) {
		if (this.equals(tail)) {
			throw new IllegalArgumentException(
					"Setting a node as self-predecessor: " + this);
		}
		if (!predecessors.contains(tail)) {
			this.predecessors.add(tail);
			tail.successors.add(this);
		}
	}

	public void removePredecessor(Node<T> tail) {
		this.predecessors.remove(tail);
		tail.successors.remove(this);
	}

	public void clear() {
		final Iterator<Node<T>> iterPred = predecessors.iterator();
		while (iterPred.hasNext()) {
			final Node<T> pred = iterPred.next();
			pred.successors.remove(this);
			iterPred.remove();
		}
		final Iterator<Node<T>> iterSucc = successors.iterator();
		while (iterSucc.hasNext()) {
			final Node<T> succ = iterSucc.next();
			succ.predecessors.remove(this);
			iterSucc.remove();
		}
	}

	public void setMark(boolean value) {
		mark = value;
	}

	public boolean isMarked() {
		return mark;
	}

	public int degree() {
		return predecessors.size();
	}

	public void markSuccessors() {
		for (Node<T> succ : successors) {
			succ.mark = true;
		}
	}

	@Override
	public String toString() {
		// return label.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(label);
		sb.append("; preds: ");
		for (Node<T> n : predecessors) {
			sb.append(n.label + " ");
		}
		sb.append("; succs: ");
		for (Node<T> n : successors) {
			sb.append(n.label + " ");
		}
		sb.append(")");
		return sb.toString();
	}
}
