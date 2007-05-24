package org.eventb.core.seqprover;

import org.eventb.core.seqprover.tactics.BasicTactics;


/**
 * Common protocol for a proof tree node.
 * 
 * <p>
 * Each proof tree node contains a proof sequent (see {@link IProofSequent}). A node has 
 * children proof tree nodes iff a rule has been applied to it. Nodes that have no 
 * rule applied to them are termed "open". Note that there is a difference between a 
 * proof tree node having no children (no rule applied, open), and a proof tree node 
 * having 0 children (rule with 0 anticidents applied, closed).
 * 
 * Proof Nodes are either "pending" or "closed" :
 * <ul>
 * <li>Pending - This node is either open, or has at least one open descendent. The 
 * proof attempt for its sequent is not yet complete</li>
 * <li>Closed - This node has no open descendents. The proof attempt for its sequent 
 * is complete.
 * </ul>
 * </p>
 * <p>
 * Each node in addition has a confidence level associated to it 
 * (see {@see org.eventb.core.prover.IConfidence}):
 * <ul>
 * <li>Pending nodes have their confidence levels set to PENDING, which is the 
 * minimum confidence level and is reserved for this purpose. </li>
 * <li>The confidence level for a closed node is the minimum of the confidence level 
 * of the rule associated to it, and the confidence levels of its children.
 * </li>
 * <ul>
 * 
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @author Farhad Mehta
 * @author Laurent Voisin
 */
public interface IProofTreeNode extends IProofSkeleton {

	/**
	 * Returns the children of this node.
	 * <p>
	 * This method always returns an array, even if this node is a leaf node
	 * (that is with no rule applied to it). It never returns <code>null</code>.
	 * </p>
	 * 
	 * @return an array of the children of this node
	 * @see #hasChildren()
	 */
	IProofTreeNode[] getChildNodes();

	/**
	 * Returns the first open descendant of this node.
	 * <p>
	 * The first open descendant is selected quite arbitrarily by traversing the
	 * proof subtree rooted at this node depth-first.
	 * </p>
	 * <p>
	 * This node is considered a descendant of itself. Hence, calling this
	 * method on an open node returns the open node itself.
	 * </p>
	 * 
	 * @return an open descendant of this node or <code>null</code> if this
	 *         node has been discharged
	 * @see #getOpenDescendants()
	 */
	IProofTreeNode getFirstOpenDescendant();

	/**
	 * Returns the next open node encountered when traversing the proof tree of
	 * this node in preorder.
	 * <p>
	 * If this node is already open, it will be returned. Otherwise, this method
	 * will traverse the proof tree to which this node belongs, in pre-order and
	 * return the first open node encountered after traversing this node.
	 * </p>
	 * 
	 * @return the next open node after this node in pre-order or
	 *         <code>null</code> if this node has been discharged
	 * @see #getFirstOpenDescendant()
	 */
	IProofTreeNode getNextOpenNode();
	
	/**
	 * Returns all open descendants of this node.
	 * <p>
	 * This method always returns an array, even if this node has been
	 * discharged (that is has no open descendants). It never returns
	 * <code>null</code>.
	 * </p>
	 * <p>
	 * This node is considered a descendant of itself. Hence, calling this
	 * method on an open node returns an array containg one element: the open
	 * node itself.
	 * </p>
	 * <p>
	 * Clients should not use this method if they're interested only in an
	 * arbitrary open descendant. In that latter case, clients should call
	 * {@link #getFirstOpenDescendant()}.
	 * </p>
	 * 
	 * @return an array of all open descendants of this node
	 * @see #getFirstOpenDescendant()
	 */
	IProofTreeNode[] getOpenDescendants();

	/**
	 * Returns the parent of this proof tree node, or <code>null</code> if
	 * this node is the root of its proof tree.
	 * 
	 * @return the parent of this node or <code>null</code>
	 */
	IProofTreeNode getParent();

	/**
	 * Returns the proof tree to which this node belongs.
	 * <p>
	 * Note that a node that has been pruned out of the proof tree doesn't
	 * belong anymore to it. Hence, this method will return <code>null</code>
	 * if this node is not connected anymore to a proof tree.
	 * </p>
	 * 
	 * @return the proof tree of this node or <code>null</code>
	 */
	IProofTree getProofTree();

	/**
	 * Returns the rule applied to this node.
	 * 
	 * @return the rule applied to this node or <code>null</code> is this node
	 *         is a leaf node
	 */
	IProofRule getRule();

	/**
	 * Returns the proof sequent associated to this node.
	 * 
	 * @return the proof sequent of this node
	 */
	IProverSequent getSequent();

	/**
	 * Tells whether this node has any children.
	 * <p>
	 * Returns identical information to <code>getChildren().length != 0</code>,
	 * but is implemented in a more efficient way.
	 * </p>
	 * 
	 * @return <code>true</code> iff this node has some child
	 * @see #getChildNodes()
	 */
	boolean hasChildren();

	/**
	 * Tells whether this node is closed.
	 * 
	 * @return <code>true</code> iff this node is closed
	 */
	boolean isClosed();

	/**
	 * Tells whether this node is open.
	 * 
	 * @return <code>true</code> iff this node is open (no rule applied to it)
	 */
	boolean isOpen();
	
	/**
	 * Copies the sub proof tree corresping to the current node and returns a new
	 * proof tree corresponding to this sub tree
	 * 
	 * @return A new proof tree with the root identical to the current node
	 */
	IProofTree copySubTree();
	
	/**
	 * Extracts the proof skeleton rooted in the current node and returns it
	 * 
	 * @return A new proof skeleton rooted at this node
	 */
	IProofSkeleton copyProofSkeleton();
	
	/**
	 * Returns the comment field of the current proof tree node.
	 * 
	 * @return The comment field of the current proof tree node.
	 * @see #setComment()
	 */
	String getComment();
	
	
	/**
	 * Returns the confidence of this proof tree node.
	 * 
	 * @return the confidence of this proof tree node (see {@see IConfidence})
	 */
	int getConfidence();
	
	/**
	 * Applies the given rule to this node.
	 * 
	 * <p>
	 * Note that outside the sequent prover, tactics provide a more uniform way
	 * to modify proof tree nodes and should be used instead of directly calling
	 * this method. 
	 * </p>
	 * 
	 * @param rule
	 *            the rule to apply to this node
	 * @return <code>true</code> iff the application of the rule succeeded
	 */
	boolean applyRule(IProofRule rule);

	/**
	 * Prune the children of this node, undoing the result of applying a rule to
	 * this node. Has no effect if no rule was currently applied to this node.
	 * 
	 * <p>
	 * Note that outside the sequent prover, tactics provide a more uniform way
	 * to modify proof tree nodes and should be used instead of directly calling
	 * this method. 
	 * </p>
	 * 
	 * @see ITactic
	 * 
	 * @return the child subtrees that have been pruned, or <code>null</code> if no rule 
	 * was applied to this node. 
	 */
	IProofTree[] pruneChildren();
	
	/**
	 * Sets the comment field of the current proof tree node.
	 * 
	 * @param comment
	 * 	 The new comment for the current proof tree node
	 * 
	 * @see #getComment()
	 */
	void setComment(String comment);
	
	/**
	 * Grafts a proof tree to this proof node if it is open, and its sequent is identical
	 * to the root sequent of the given proof tree. 
	 * 
	 * A successful graft has the effect of pruning the input proof tree.
	 * 
	 * <p>
	 * Note that outside the sequent prover, tactics provide a more uniform way
	 * to modify proof tree nodes and should be used instead of directly calling
	 * this method. 
	 * </p>
	 * 
	 * @see ITactic
	 * 
	 * @return <code>true</code> iff the operation suceeded.
	 * 
	 * @deprecated
	 * Instead first extract the proof skeleton using {@link #copyProofSkeleton()} and
	 * then use one of the proof reconstruction tactics in {@link BasicTactics}.
	 *  
	 */
	@Deprecated
	boolean graft(IProofTree tree);

	/**
	 * Returns the next node with satisfies the filter encountered when
	 * traversing the proof tree of this node in preorder. The flag
	 * <code>rootIncluded</code> indicate if the current node should be
	 * included or not.
	 * <p>
	 * If <code>rootIncluded</code> is true and if this node satisfies the
	 * filter, it will be returned. Otherwise, this method will traverse the
	 * proof tree to which this node belongs, in pre-order and return the first
	 * node satisfies the filter encountered after traversing this node.
	 * </p>
	 * 
	 * @param rootIncluded
	 *            the boolean flag to indicate if the current not should be
	 *            considered or not
	 * @param filter
	 *            a proof tree node filter
	 * 
	 * @return the next node satisfies the input filter after this node in
	 *         pre-order or <code>null</code> if there is no such node.
	 */
	IProofTreeNode getNextNode(boolean rootIncluded, IProofTreeNodeFilter filter);

}