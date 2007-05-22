package org.eventb.internal.pp.core.provers.equality;

import java.util.List;
import java.util.Set;

import org.eventb.internal.pp.core.elements.IClause;
import org.eventb.internal.pp.core.elements.IEquality;

public interface IQueryResult {

	public boolean getValue();
	
	public List<IClause> getSolvedValueOrigin();
	
	public Set<IClause> getSolvedClauses();

	public IEquality getEquality();
}
