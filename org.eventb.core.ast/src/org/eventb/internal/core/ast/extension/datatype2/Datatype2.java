/*******************************************************************************
 * Copyright (c) 2013 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.internal.core.ast.extension.datatype2;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.ast.extension.datatype2.IConstructorExtension;
import org.eventb.core.ast.extension.datatype2.IDatatype2;
import org.eventb.core.ast.extension.datatype2.IDestructorExtension;

/**
 * Implementation of a complete datatype which references all its extensions
 * (type constructor, constructors and their destructors).
 * <p>
 * Datatypes which have the exact same definition (except maybe the formula
 * factory used for building it) are represented by the same object.
 * </p>
 * 
 * @author Vincent Monfort
 */
public class Datatype2 implements IDatatype2 {

	private static final Map<Datatype2, Datatype2> REGISTRY //
	= new HashMap<Datatype2, Datatype2>();

	// Ensure uniqueness of datatypes
	public static Datatype2 makeDatatype(DatatypeBuilder dtBuilder) {
		final Datatype2 candidate = new Datatype2(dtBuilder);
		return registerDatatype(candidate);
	}

	private static Datatype2 registerDatatype(Datatype2 candidate) {
		final Datatype2 representative = REGISTRY.get(candidate);
		if (representative != null) {
			return representative;
		}
		REGISTRY.put(candidate, candidate);
		return candidate;
	}

	// The minimal factory containing all extensions that are needed to define
	// this datatype
	private final FormulaFactory baseFactory;

	// The type constructor
	private final TypeConstructorExtension typeCons;

	// The value constructors (order is mostly irrelevant, but clients might
	// rely on it)
	private final LinkedHashMap<String, IConstructorExtension> constructors;

	// Cache of the extensions provided by this datatype
	private final Set<IFormulaExtension> extensions;

	private Datatype2(DatatypeBuilder dtBuilder) {
		baseFactory = dtBuilder.getBaseFactory();
		extensions = new HashSet<IFormulaExtension>();
		final List<GivenType> typeParams = dtBuilder.getTypeParameters();
		typeCons = new TypeConstructorExtension(this, typeParams,
				dtBuilder.getName());
		extensions.add(typeCons);
		constructors = new LinkedHashMap<String, IConstructorExtension>();
		final List<ConstructorBuilder> dtConstrs = dtBuilder.getConstructors();
		for (final ConstructorBuilder dtCons : dtConstrs) {
			final ConstructorExtension constructor = dtCons.finalize(this);
			constructors.put(constructor.getName(), constructor);
			extensions.add(constructor);
			extensions.addAll(constructor.getExtensions());
		}
	}

	@Override
	public TypeConstructorExtension getTypeConstructor() {
		return typeCons;
	}

	public boolean hasSingleConstructor() {
		return constructors.size() == 1;
	}

	@Override
	public IConstructorExtension[] getConstructors() {
		final Collection<IConstructorExtension> cons = constructors.values();
		return cons.toArray(new IConstructorExtension[cons.size()]);
	}

	@Override
	public IConstructorExtension getConstructor(String name) {
		return constructors.get(name);
	}

	@Override
	public IDestructorExtension getDestructor(String name) {
		// Lookup in each value constructor, first match is the right one
		for (final IConstructorExtension cons : constructors.values()) {
			final IDestructorExtension dest = cons.getDestructor(name);
			if (dest != null) {
				return dest;
			}
		}
		return null;
	}

	@Override
	public FormulaFactory getBaseFactory() {
		return baseFactory;
	}

	@Override
	public FormulaFactory getFactory() {
		return baseFactory.withExtensions(getExtensions());
	}

	@Override
	public Set<IFormulaExtension> getExtensions() {
		return unmodifiableSet(extensions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * typeCons.hashCode() + constructors.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final Datatype2 other = (Datatype2) obj;
		return this.typeCons.equals(other.typeCons)
				&& this.constructors.equals(other.constructors);
	}

}