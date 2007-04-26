package org.eventb.internal.core.seqprover;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.ITypeCheckResult;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProverSequent;

/**
 * This class is the default implementation of the {@link IProverSequent} and 
 * {@link IInternalProverSequent} interfaces.
 * 
 *  <p>
 *  Prover sequents are implemented to be immutable data structures and take advantage of 
 *  this to share their instance variables in case they do not change.
 *  </p>
 *  <p>
 *  In this implementation, the set of all hypotheses is the union of the global, and the local
 *  hypotheses. The global hypotheses are shared by all sequents constructed incrementally using this
 *  sequent.
 *  </p>
 * 
 * 
 * @author Farhad Mehta
 *
 */
public final class ProverSequent implements IInternalProverSequent{
	
	
	// TODO : Profiling : It may be that caching visible hyps may improve performance.
	
	private final ITypeEnvironment typeEnvironment;
	
	
	/**
	 * Instance fields
	 * 
	 * Chosen to be LinkedHashSets to preserve their order.
	 */
	private final LinkedHashSet<Predicate> globalHypotheses;
	private final LinkedHashSet<Predicate> localHypotheses;
	
	private final LinkedHashSet<Predicate> hiddenHypotheses;
	private final LinkedHashSet<Predicate> selectedHypotheses;
	
	private final Predicate goal;

	
	/**
	 * Static immutable variables.
	 */
	private static final LinkedHashSet<Predicate> NO_HYPS =
		new LinkedHashSet<Predicate>();
	
	private static final FormulaFactory FORMULA_FACTORY = FormulaFactory.getDefault();

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#typeEnvironment()
	 */
	public ITypeEnvironment typeEnvironment() {
		// TODO : Maybe avoid cloning by returning an immutable version of the type environemnt
		return this.typeEnvironment.clone();
	}
		
	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#goal()
	 */
	public Predicate goal() {
		return this.goal;
	}
	
	/**
	 * Constructs a new Prover Sequent from the given parameters.
	 * 
	 * <p>
	 * Note : <br>
	 * The parameters provided to construct the sequent must be consistent
	 * in order to construct a proper sequent. In particular:
	 * <ul>
	 * <li> All predicates (i.e. all hypotheses and the goal) must be type checked.
	 * <li> The type environment provided should contain all free identifiers and carrier
	 * sets appearing in the predicates of the sequent and can be used to successfully
	 * type check them.
	 * <li> All hypotheses to be selected must also be present in the set of 
	 * hypotheses provided.
	 * </ul>
	 * These checks need to be done before calling this method. The behaviour of the
	 * sequent prover is undefined if these checks are not done.
	 * </p>
	 * 
	 * @param typeEnv
	 * 		The type environment for the sequent, or <code>null</code> iff the empty
	 * 		type environment is to be used.
	 * 		It should be ensured that all predicates can be type checked using this
	 * 		type environment. 
	 * @param globalHypotheses
	 * 		The set of global hypotheses, or <code>null</code> iff this set is intended to
	 * 		be empty.
	 * @param selectedHypotheses
	 * 		The set of hypotheses to select. The set of hypotheses to select should be
	 * 		contained in the set of initial hypotheses
	 * @param goal
	 * 		The goal. This parameter must not be <code>null</code>.
	 */
	public ProverSequent(ITypeEnvironment typeEnvironment,Collection<Predicate> globalHypotheses, Collection<Predicate> selectedHypotheses,Predicate goal){
		this.typeEnvironment = typeEnvironment == null ? FORMULA_FACTORY.makeTypeEnvironment() : typeEnvironment.clone();
		this.globalHypotheses = globalHypotheses == null ? NO_HYPS : new LinkedHashSet<Predicate>(globalHypotheses);
		this.localHypotheses = NO_HYPS;
		this.hiddenHypotheses = NO_HYPS;
		this.selectedHypotheses = selectedHypotheses == null ? NO_HYPS : new LinkedHashSet<Predicate>(selectedHypotheses);
		this.goal = goal;
	}
	
	
	/**
	 * Constructs a new sequent with the given parameters.
	 * 
	 * <p>
	 * Note : <br>
	 * The parameters provided to construct the sequent must be consistent
	 * in order to construct a proper sequent. In particular:
	 * <ul>
	 * <li> All predicates (i.e. all hypotheses and the goal) must be type checked.
	 * <li> The type environment provided should contain all free identifiers and carrier
	 * sets appearing in the predicates of the sequent and can be used to successfully
	 * type check them.
	 * </ul>
	 * These checks need to be done before calling this method. The behaviour of the
	 * sequent prover is undefined if these checks are not done.
	 * </p>
	 * 
	 * @param typeEnv
	 * 		The type environment for the sequent, or <code>null</code> iff the empty
	 * 		type environment is to be used.
	 * 		It should be ensured that all predicates can be type checked using this
	 * 		type environment. 
	 * @param globalHypotheses
	 * 		The set of hypotheses, or <code>null</code> iff this set is intended to
	 * 		be empty.
	 * @param goal
	 * 		The goal. This parameter must not be <code>null</code>.
	 */
	public ProverSequent(ITypeEnvironment typeEnvironment,Collection<Predicate> globalHypotheses,Predicate goal){
		this.typeEnvironment = typeEnvironment == null ? FORMULA_FACTORY.makeTypeEnvironment() : typeEnvironment.clone();
		this.globalHypotheses = globalHypotheses == null ? NO_HYPS : new LinkedHashSet<Predicate>(globalHypotheses);
		this.localHypotheses = NO_HYPS;
		this.hiddenHypotheses = NO_HYPS;
		this.selectedHypotheses = NO_HYPS;
		this.goal = goal;
	}
	
	
	/**
	 * Constructs a new Prover Sequent incrementally, from an old one, selectively replacing the
	 * provided fields, or <code>null</code> in case this field should not be overridden.
	 * 
	 * <p>
	 * This should always remain a private constructor. All methods using this constructor must ensure that
	 * the parameters passed into it have been cloned prior to calling this constructor. 
	 * </p>
	 * 
	 * @param seq
	 * 			The sequent to base the new sequent on. Should never be <code>null</code>.
	 * @param typeEnvironment
	 * @param globalHypotheses
	 * @param localHypotheses
	 * @param hiddenHypotheses
	 * @param selectedHypotheses
	 * @param goal
	 */
	private ProverSequent(ProverSequent seq, ITypeEnvironment typeEnvironment, LinkedHashSet<Predicate> globalHypotheses,
			LinkedHashSet<Predicate> localHypotheses, LinkedHashSet<Predicate> hiddenHypotheses, LinkedHashSet<Predicate> selectedHypotheses,
			Predicate goal){
		
		assert (seq != null) | (typeEnvironment != null & globalHypotheses != null & localHypotheses != null & 
				hiddenHypotheses != null & selectedHypotheses != null & goal != null);
		
		if (typeEnvironment == null) this.typeEnvironment = seq.typeEnvironment;
		else this.typeEnvironment = typeEnvironment;
		
		if (globalHypotheses == null) this.globalHypotheses = seq.globalHypotheses;
		else this.globalHypotheses = globalHypotheses;
		
		if (localHypotheses == null) this.localHypotheses = seq.localHypotheses;
		else this.localHypotheses = localHypotheses;
		
		if (hiddenHypotheses == null) this.hiddenHypotheses = seq.hiddenHypotheses;
		else this.hiddenHypotheses = hiddenHypotheses;
		
		if (selectedHypotheses == null) this.selectedHypotheses = seq.selectedHypotheses;
		else this.selectedHypotheses = selectedHypotheses;
		
		if (goal == null) this.goal = seq.goal;
		else this.goal = goal;
	}
	

	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#modify(org.eventb.core.ast.FreeIdentifier[], java.util.Collection, org.eventb.core.ast.Predicate)
	 */
	public IInternalProverSequent modify(FreeIdentifier[] freshFreeIdents, Collection<Predicate> addhyps, Predicate newGoal) {
		boolean modified = false;
		ITypeEnvironment newTypeEnv = typeEnvironment;
		LinkedHashSet<Predicate> newLocalHypotheses = null;
		LinkedHashSet<Predicate> newSelectedHypotheses = null;
		LinkedHashSet<Predicate> newHiddenHypotheses = null;
		if (freshFreeIdents != null && freshFreeIdents.length != 0)
		{
			newTypeEnv = typeEnvironment.clone();
			for (FreeIdentifier freshFreeIdent : freshFreeIdents) {
				if (newTypeEnv.contains(freshFreeIdent.getName())) return null;
				newTypeEnv.add(freshFreeIdent);
				modified = true;
			}
		}
		if (addhyps != null && addhyps.size() != 0) {
			newLocalHypotheses = new LinkedHashSet<Predicate>(localHypotheses);
			newSelectedHypotheses = new LinkedHashSet<Predicate>(selectedHypotheses);
			newHiddenHypotheses = new LinkedHashSet<Predicate>(hiddenHypotheses);
			for (Predicate hyp : addhyps) {
				if (! typeCheckClosed(hyp,newTypeEnv)) return null;
				if (! this.containsHypothesis(hyp)){
					newLocalHypotheses.add(hyp);
					modified = true;
				}
				modified |= newSelectedHypotheses.add(hyp);
				modified |= newHiddenHypotheses.remove(hyp);
			}
		}
		if (newGoal != null && ! newGoal.equals(goal)) {
			if (! typeCheckClosed(newGoal,newTypeEnv)) return null;
			modified = true;
		}
		
		if (modified) {
			return new ProverSequent(this, newTypeEnv, null,
					newLocalHypotheses, newHiddenHypotheses,
					newSelectedHypotheses, newGoal);
		}
		return this;
	}
		
	
	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#selectHypotheses(java.util.Collection)
	 */
	public ProverSequent selectHypotheses(Collection<Predicate> toSelect){
		if (toSelect == null) return this;
		boolean modified = false;
		
		LinkedHashSet<Predicate> newSelectedHypotheses = new LinkedHashSet<Predicate>(this.selectedHypotheses);
		LinkedHashSet<Predicate> newHiddenHypotheses = new LinkedHashSet<Predicate>(this.hiddenHypotheses);
		
		for (Predicate hyp:toSelect){
			if (containsHypothesis(hyp)){
				modified |= newSelectedHypotheses.add(hyp);
				modified |= newHiddenHypotheses.remove(hyp);
			}
		}
		if (modified) return new ProverSequent(this,null,null,null,newHiddenHypotheses,newSelectedHypotheses,null);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#deselectHypotheses(java.util.Collection)
	 */
	public ProverSequent deselectHypotheses(Collection<Predicate> toDeselect){
		if (toDeselect == null) return this;
		LinkedHashSet<Predicate> newSelectedHypotheses = new LinkedHashSet<Predicate>(this.selectedHypotheses);
		boolean modified = newSelectedHypotheses.removeAll(toDeselect);
		if (modified) return new ProverSequent(this,null,null,null,null,newSelectedHypotheses,null);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#hideHypotheses(java.util.Collection)
	 */
	public ProverSequent hideHypotheses(Collection<Predicate> toHide){
		if (toHide == null) return this;
		boolean modified = false;
		
		LinkedHashSet<Predicate> newSelectedHypotheses = new LinkedHashSet<Predicate>(this.selectedHypotheses);
		LinkedHashSet<Predicate> newHiddenHypotheses = new LinkedHashSet<Predicate>(this.hiddenHypotheses);
		
		for (Predicate hyp:toHide){
			if (containsHypothesis(hyp)){
				modified |= newHiddenHypotheses.add(hyp);
				modified |= newSelectedHypotheses.remove(hyp);
			}
		}
		if (modified) return new ProverSequent(this,null,null,null,newHiddenHypotheses,newSelectedHypotheses,null);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#showHypotheses(java.util.Collection)
	 */
	public ProverSequent showHypotheses(Collection<Predicate> toShow){
		if (toShow == null) return null;
		LinkedHashSet<Predicate> newHiddenHypotheses = new LinkedHashSet<Predicate>(this.hiddenHypotheses);
		boolean modified = newHiddenHypotheses.removeAll(toShow);
		if (modified) return new ProverSequent(this,null,null,null,newHiddenHypotheses,null,null);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eventb.internal.core.seqprover.IInternalProverSequent#performfwdInf(java.util.Collection, org.eventb.core.ast.FreeIdentifier[], java.util.Collection)
	 */
	public IInternalProverSequent performfwdInf(Collection<Predicate> hyps, FreeIdentifier[] addedIdents, Collection<Predicate> infHyps) {
		boolean modified = false;
		
		ITypeEnvironment newTypeEnv = typeEnvironment;
		if (addedIdents != null)
		{
			newTypeEnv = typeEnvironment.clone();
			for (FreeIdentifier addedIdent : addedIdents) {
				if (newTypeEnv.contains(addedIdent.getName())) return this;
				newTypeEnv.add(addedIdent);
				modified = true;
			}
		}
		
		if (hyps != null && ! this.containsHypotheses(hyps)) return this;

		boolean selectInfHyps = true;
		boolean hideInfHyps = false;
		
		if (hyps != null) {
			selectInfHyps = ! Collections.disjoint(hyps, selectedHypotheses);
			hideInfHyps = selectInfHyps ? false : hiddenHypotheses.containsAll(hyps);
		}
		
		LinkedHashSet<Predicate> newLocalHypotheses = null;
		LinkedHashSet<Predicate> newSelectedHypotheses = null;
		LinkedHashSet<Predicate> newHiddenHypotheses = null;
		
		if (infHyps != null){
			newLocalHypotheses = new LinkedHashSet<Predicate>(localHypotheses);
			newSelectedHypotheses = new LinkedHashSet<Predicate>(selectedHypotheses);
			newHiddenHypotheses = new LinkedHashSet<Predicate>(hiddenHypotheses);
			for (Predicate infHyp : infHyps) {
				if (! typeCheckClosed(infHyp,newTypeEnv)) return this;
				if (! this.containsHypothesis(infHyp)){
					newLocalHypotheses.add(infHyp);
					if (selectInfHyps) newSelectedHypotheses.add(infHyp);
					if (hideInfHyps) newHiddenHypotheses.add(infHyp);
					modified = true;
				}
			}
		}		
		if (modified) return new ProverSequent(this,newTypeEnv,null,newLocalHypotheses,newHiddenHypotheses,newSelectedHypotheses,null);
		return this;
	}
	
	@Override
	public String toString(){
		return (
				typeEnvironment.toString() +
				iterablePredToString(hiddenHypIterable()) +
				iterablePredToString(visibleMinusSelectedIterable()) +
				iterablePredToString(selectedHypIterable()) + " |- " +
				goal.toString());
	}
	
	private static String iterablePredToString(Iterable<Predicate> iterable){
		StringBuilder str = new StringBuilder("[");
		Iterator<Predicate> iterator = iterable.iterator();
		while (iterator.hasNext()){
			str.append((iterator.next()).toString());
			if (iterator.hasNext()){
				str.append(", ");
			}
		}
		str.append("]");
		return str.toString();
	}
	
	
	private static boolean typeCheckClosed(Formula f, ITypeEnvironment t) {
		ITypeCheckResult tcr = f.typeCheck(t);
		// new free variables introduced
		if (tcr.isSuccess()) {
			return tcr.getInferredEnvironment().isEmpty();
		}
		return false;
	}



	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#containsHypothesis(org.eventb.core.ast.Predicate)
	 */
	public boolean containsHypothesis(Predicate pred) {
		if (localHypotheses.contains(pred)) return true;
		if (globalHypotheses.contains(pred)) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#containsHypotheses(java.util.Collection)
	 */
	public boolean containsHypotheses(Collection<Predicate> preds) {
		for (Predicate pred : preds) {
			if (! containsHypothesis(pred)) return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#hypIterable()
	 */
	public Iterable<Predicate> hypIterable() {
		return new Iterable<Predicate>(){

			public Iterator<Predicate> iterator() {
				return new CompositeIterator<Predicate>(
						globalHypotheses.iterator(),
						localHypotheses.iterator());
			}
		};
		
//		return iterable(new CompositeIterator<Predicate>(
//				globalHypotheses.iterator(),
//				localHypotheses.iterator()));
	}


//	/**
//	 * Given a Predicate iterator, this class returns an instance of its iterable class
//	 * so that it can be used in a <code>for</code> loop.
//	 * <p>
//	 * This is a convenience method.
//	 * </p>
//	 * 
//	 * @param iterator
//	 * 			The given Predicate iterator
//	 * @return
//	 * 			The iterable Predicate class that can be used in a <code>for</code>
//	 * 			loop
//	 */
//	private Iterable<Predicate> iterable(final Iterator<Predicate> iterator){
//		return new Iterable<Predicate>(){
//
//			public Iterator<Predicate> iterator() {
//				return iterator;
//			}
//		};
//	}
	
	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#hiddenHypIterable()
	 */
	public Iterable<Predicate> hiddenHypIterable() {
		return new Iterable<Predicate>(){

			public Iterator<Predicate> iterator() {
				return new ImmutableIterator<Predicate>(hiddenHypotheses);
			}
		};
		
		// return iterable(new ImmutableIterator<Predicate>(hiddenHypotheses));
		// Safer compared to:
		// return hiddenHypotheses;
	}


	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#selectedHypIterable()
	 */
	public Iterable<Predicate> selectedHypIterable() {
		return new Iterable<Predicate>(){

			public Iterator<Predicate> iterator() {
				return new ImmutableIterator<Predicate>(selectedHypotheses);
			}
		};
		// return iterable(new ImmutableIterator<Predicate>(selectedHypotheses));
		// Safer compared to:
		// return selectedHypotheses;
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#isHidden(org.eventb.core.ast.Predicate)
	 */
	public boolean isHidden(Predicate hyp) {
		return hiddenHypotheses.contains(hyp);
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#isSelected(org.eventb.core.ast.Predicate)
	 */
	public boolean isSelected(Predicate hyp) {
		return selectedHypotheses.contains(hyp);
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.seqprover.IProverSequent#visibleHypIterable()
	 */
	public Iterable<Predicate> visibleHypIterable() {
		return new Iterable<Predicate>(){

			public Iterator<Predicate> iterator() {
				return new DifferenceIterator<Predicate>(
						new CompositeIterator<Predicate>(
								globalHypotheses.iterator(),localHypotheses.iterator()),
						hiddenHypotheses);
			}
		};
//		return iterable(new DifferenceIterator<Predicate>(
//				new CompositeIterator<Predicate>(
//						globalHypotheses.iterator(),localHypotheses.iterator()),
//				hiddenHypotheses));

	}
		
	public Iterable<Predicate> visibleMinusSelectedIterable() {
		return new Iterable<Predicate>(){

			public Iterator<Predicate> iterator() {
				return new DifferenceIterator<Predicate>(
						visibleHypIterable().iterator(),
						selectedHypotheses);
			}
		};
	}
//		return iterable(new DifferenceIterator<Predicate>(
//				visibleHypIterable().iterator(),
//				selectedHypotheses)
//				);
//	}
	
}


/**
 * An implementation for an iterator that is the combination of
 * two (a first and a second) iterators.
 * 
 * <p>
 * This iterator first returns the elements contained in the first iterator, followed
 * by the elements contained in the second iterator.
 * </p>
 * <p>
 * Removal of elements is unsupported for this iterator.
 * </p>
 * 
 * @author Farhad Mehta
 *
 * @param <T> 
 * 		The base type for the elements returnded by the iterator.
 */
class CompositeIterator<T> implements Iterator<T>{

	private Iterator<T> fst;
	private Iterator<T> snd;
	
	public CompositeIterator(Iterator<T> fst, Iterator<T> snd) {
		this.fst = fst;
		this.snd = snd;
	}
	
	public boolean hasNext() {
		return fst.hasNext() || snd.hasNext();
	}

	public T next() {
		if (fst.hasNext()) return fst.next();
		if (snd.hasNext()) return snd.next();
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}	
}

/**
 * An implementation for an iterator that returns the elements of an 'original'
 * iterator, without the elements in a 'removed' collection.
 * 
 * <p>
 * This iterator is implemented by doing a pre-emptive lookup to check if the resulting
 * iterator still has more elements.
 * </p>
 * <p>
 * Removal of elements is unsupported for this iterator.
 * </p>
 * 
 * @author Farhad Mehta
 *
 * @param <T> 
 * 		The base type for the elements returnded by the iterator.
 */
class DifferenceIterator<T> implements Iterator<T>{

	private Iterator<T> iterator;
	private Collection<T> removed;
	
	/**
	 * This local variable contains the result of the pre-emptive lookup.
	 * If it is null, the iterator has no further elements, otherwise its
	 * value is the next next element for this iterator. 
	 */
	private T nextNext;
	
	
	public DifferenceIterator(Iterator<T> iterator, Collection<T> removed) {
		this.iterator = iterator;
		this.removed = removed;
		this.nextNext = nextNextLookup();
	}
	
	private T nextNextLookup(){
		while (iterator.hasNext()) {
			T next = (T) iterator.next();
			if (! removed.contains(next)) return next;
		}
		return null; 
	}
	
	public boolean hasNext() {
		return (nextNext != null);
	}

	public T next() {
		if (nextNext != null) {
			T next = nextNext;
			nextNext = nextNextLookup();
			return next;
		}
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}	
}

/**
 * An implementation for an iterator that provides an immutable version of a 
 * given iterator. 
 * <p>
 * Removal of elements is unsupported for this iterator.
 * </p>
 * 
 * @author Farhad Mehta
 *
 * @param <T> 
 * 		The base type for the elements returnded by the iterator.
 */
class ImmutableIterator<T> implements Iterator<T>{

	private Iterator<T> iterator;	
	
	public ImmutableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}
	
	public ImmutableIterator(Iterable<T> iterable) {
		this.iterator = iterable.iterator();
	}
		
	public boolean hasNext() {
		return iterator.hasNext();
	}

	public T next() {
		return iterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}		
}
