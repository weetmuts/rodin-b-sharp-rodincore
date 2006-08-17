package org.eventb.core.seqprover.reasoners.rewriter;

import static org.eventb.core.seqprover.Lib.disjuncts;
import static org.eventb.core.seqprover.Lib.isDisj;
import static org.eventb.core.seqprover.Lib.makeDisj;
import static org.eventb.core.seqprover.Lib.makeImp;
import static org.eventb.core.seqprover.Lib.makeNeg;

import org.eventb.core.ast.Predicate;

public class DisjToImpl implements Rewriter{
	
	public String getRewriterID() {
		return "disjToImpl";
	}
	
	public String getName() {
		return "∨ to ⇒";
	}
	
	public boolean isApplicable(Predicate p) {
		if (isDisj(p)) return true;
		
		return false;
	}

	public Predicate apply(Predicate p) {
		if (isDisj(p))
		{
			Predicate[] disjuncts = disjuncts(p);
			assert disjuncts.length >= 2;
			Predicate firstDisjunct = disjuncts[0];
			Predicate[] restDisjuncts = new Predicate[disjuncts.length - 1];
			System.arraycopy(disjuncts,1,restDisjuncts,0,disjuncts.length - 1);
			return makeImp(
					makeNeg(firstDisjunct),
					makeDisj(restDisjuncts)
					);
		}

		return null;
	}

}
