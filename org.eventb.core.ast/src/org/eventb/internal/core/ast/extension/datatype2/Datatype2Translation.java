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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.GivenType;
import org.eventb.core.ast.IDatatypeTranslation;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.ParametricType;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.Type;
import org.eventb.core.ast.extension.IExpressionExtension;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.ast.extension.datatype2.IConstructorExtension;
import org.eventb.core.ast.extension.datatype2.IDatatype2;
import org.eventb.core.ast.extension.datatype2.IDestructorExtension;
import org.eventb.internal.core.ast.FreshNameSolver;
import org.eventb.internal.core.ast.ITypeCheckingRewriter;
import org.eventb.internal.core.ast.TypeRewriter;
import org.eventb.internal.core.ast.extension.datatype2.Datatype2;

/**
 * Implementation for translating Event-B formulas containing datatype
 * extensions to plain set theory.
 * 
 * We maintain an associative table from datatype instances to datatype
 * translator and provide a facade unifying all these datatype translators.
 * 
 * @author Thomas Muller
 */
public class Datatype2Translation implements IDatatypeTranslation {

	private final FormulaFactory sourceFactory;
	private final FormulaFactory targetFactory;

	private final FreshNameSolver nameSolver;

	private final Map<ParametricType, Datatype2Translator> translators//
	= new LinkedHashMap<ParametricType, Datatype2Translator>();

	private final TypeRewriter typeRewriter;
	private final ITypeCheckingRewriter formulaRewriter;

	public Datatype2Translation(ITypeEnvironment typenv) {
		this.sourceFactory = typenv.getFormulaFactory();
		this.targetFactory = computeTargetFactory();
		final Set<String> usedNames = new HashSet<String>(typenv.getNames());
		this.nameSolver = new FreshNameSolver(usedNames, targetFactory);
		this.typeRewriter = new TypeRewriter(targetFactory) {
			@Override
			public void visit(ParametricType type) {
				result = translateParametricType(type);
			}
		};
		this.formulaRewriter = new Datatype2Rewriter(this);
	}

	private FormulaFactory computeTargetFactory() {
		final Set<IFormulaExtension> extensions = sourceFactory.getExtensions();
		final Set<IFormulaExtension> keptExtensions = new LinkedHashSet<IFormulaExtension>();
		for (IFormulaExtension extension : extensions) {
			if (extension.getOrigin() instanceof IDatatype2)
				continue;
			keptExtensions.add(extension);
		}
		return FormulaFactory.getInstance(keptExtensions);
	}

	// public for unit tests
	public ParametricType getDatatypeInstance(ExtendedExpression src) {
		final IExpressionExtension extension = src.getExtension();
		if (extension.isATypeConstructor()) {
			return (ParametricType) src.getType().getBaseType();
		}
		if (extension instanceof IConstructorExtension) {
			return (ParametricType) src.getType();
		}
		if (extension instanceof IDestructorExtension) {
			return (ParametricType) src.getChildExpressions()[0].getType();
		}
		assert false;
		return null;
	}

	public FormulaFactory getSourceFormulaFactory() {
		return sourceFactory;
	}

	@Override
	public FormulaFactory getTargetFormulaFactory() {
		return targetFactory;
	}

	public ITypeCheckingRewriter getFormulaRewriter() {
		return formulaRewriter;
	}

	public final GivenType solveGivenType(String typeSymbol) {
		final String solvedTypeName = nameSolver.solveAndAdd(typeSymbol);
		return targetFactory.makeGivenType(solvedTypeName);
	}

	public final FreeIdentifier solveIdentifier(String symbol, Type type) {
		final String solvedIdentName = nameSolver.solveAndAdd(symbol);
		return targetFactory.makeFreeIdentifier(solvedIdentName, null, type);
	}

	public Type translate(Type type) {
		return typeRewriter.rewrite(type);
	}

	public Type translateParametricType(ParametricType type) {
		assert type.getExprExtension().getOrigin() instanceof Datatype2;
		return getTranslatorFor(type).getTranslatedType();
	}

	public Expression translate(ExtendedExpression src,
			Expression[] newChildExprs) {
		assert src.getExtension().getOrigin() instanceof Datatype2;
		final ParametricType type = getDatatypeInstance(src);
		final Datatype2Translator translator = getTranslatorFor(type);
		return translator.rewrite(src, newChildExprs);
	}

	private Datatype2Translator getTranslatorFor(ParametricType type) {
		Datatype2Translator translator = translators.get(type);
		if (translator == null) {
			translator = createTranslatorFor(type);
			translators.put(type, translator);
		}
		return translator;
	}

	@Override
	public List<Predicate> getAxioms() {
		final List<Predicate> result = new ArrayList<Predicate>();
		for (Datatype2Translator translator : translators.values()) {
			result.addAll(translator.getAxioms());
		}
		return result;
	}

	// we assume that the parametric type is an instance of a Datatype
	private Datatype2Translator createTranslatorFor(ParametricType type) {
		return new Datatype2Translator(type, this);
	}

}