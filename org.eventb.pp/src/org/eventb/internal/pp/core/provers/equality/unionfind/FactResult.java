package org.eventb.internal.pp.core.provers.equality.unionfind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eventb.internal.pp.core.Level;
import org.eventb.internal.pp.core.elements.IClause;
import org.eventb.internal.pp.core.provers.equality.IFactResult;
import org.eventb.internal.pp.core.provers.equality.unionfind.Source.FactSource;

public class FactResult implements IFactResult {

	private Set<FactSource> source;
	private List<QueryResult> queries;
	private List<InstantiationResult> instantiations;
	
	public FactResult(Set<FactSource> source) {
		this.source = source;
	}
	
	public FactResult(List<QueryResult> queries, boolean dummy) {
		this.queries = queries;
	}
	
	public FactResult(List<InstantiationResult> instantiations) {
		this.instantiations = instantiations;
	}
	
	public FactResult(List<QueryResult> queries, List<InstantiationResult> instantiations) {
		this.instantiations = instantiations;
		this.queries = queries;
	}
	
	public List<IClause> getContradictionOrigin() {
		List<IClause> result = new ArrayList<IClause>();
		for (FactSource s : source) {
			result.add(s.getClause());
		}
		return result;
	}

	public List<QueryResult> getSolvedQueries() {
		return queries;
	}

	public boolean hasContradiction() {
		return source != null;
	}

	public Set<FactSource> getContradictionSource() {
		return source;
	}

	public Level getContradictionLevel() {
		return Source.getLevel(source);
	}

	public List<InstantiationResult> getSolvedInstantiations() {
		return instantiations;
	}
	
}
