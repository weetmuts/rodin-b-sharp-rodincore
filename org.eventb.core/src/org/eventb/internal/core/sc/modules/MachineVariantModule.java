/*******************************************************************************
 * Copyright (c) 2006 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eventb.internal.core.sc.modules;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.EventBAttributes;
import org.eventb.core.EventBPlugin;
import org.eventb.core.ILabeledElement;
import org.eventb.core.IMachineFile;
import org.eventb.core.ISCMachineFile;
import org.eventb.core.ISCVariant;
import org.eventb.core.IVariant;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ITypeEnvironment;
import org.eventb.core.ast.Type;
import org.eventb.core.sc.IFilterModule;
import org.eventb.core.sc.IModuleManager;
import org.eventb.core.sc.state.ILabelSymbolTable;
import org.eventb.core.sc.state.IState;
import org.eventb.core.sc.state.IStateRepository;
import org.eventb.core.sc.state.IVariantInfo;
import org.eventb.core.sc.symbolTable.ILabelSymbolInfo;
import org.eventb.core.sc.util.GraphProblem;
import org.eventb.core.tool.state.IToolStateRepository;
import org.eventb.internal.core.sc.Messages;
import org.eventb.internal.core.sc.ModuleManager;
import org.eventb.internal.core.sc.VariantInfo;
import org.eventb.internal.core.sc.symbolTable.VariantSymbolInfo;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalParent;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

/**
 * @author Stefan Hallerstede
 *
 */
public class MachineVariantModule extends ExpressionModule<IVariant> {

	public static final String MACHINE_VARIANT_FILTER = 
		EventBPlugin.PLUGIN_ID + ".machineVariantFilter";

	private final IFilterModule[] filterModules;

	public MachineVariantModule() {
		IModuleManager manager = ModuleManager.getModuleManager();
		filterModules = 
			manager.getFilterModules(MACHINE_VARIANT_FILTER);
	}

	private static String VARIANT_NAME_PREFIX = "VAR";

	IVariantInfo variantInfo;
	FormulaFactory factory;
	
	@Override
	public void initModule(
			IRodinElement element, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		super.initModule(element, repository, monitor);
		variantInfo = new VariantInfo();
		factory = repository.getFormulaFactory();
		repository.setState(variantInfo);
	}

	@Override
	public void endModule(
			IRodinElement element, 
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		variantInfo = null;
		factory = null;
		super.endModule(element, repository, monitor);
	}

	@Override
	protected ILabelSymbolInfo fetchLabel(
			IInternalElement internalElement, 
			String component, 
			IProgressMonitor monitor) throws CoreException {
		return new VariantSymbolInfo("VARIANT", internalElement, EventBAttributes.LABEL_ATTRIBUTE, component);
	}

	/* (non-Javadoc)
	 * @see org.eventb.core.sc.IProcessorModule#process(org.rodinp.core.IRodinElement, org.rodinp.core.IInternalParent, org.eventb.core.sc.IStateRepository, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void process(
			IRodinElement element, 
			IInternalParent target,
			IStateRepository repository, 
			IProgressMonitor monitor) throws CoreException {
		
		if (formulaElements.size() == 0)
			return;
		
		if (formulaElements.size() > 1) {
			for (int k=1; k<formulaElements.size(); k++)
				createProblemMarker(
						formulaElements.get(k), 
						EventBAttributes.EXPRESSION_ATTRIBUTE, 
						GraphProblem.TooManyVariantsError);
		}
		
		monitor.subTask(Messages.bind(Messages.progress_MachineVariant));
		
		checkAndType(
				target, 
				filterModules,
				element.getElementName(),
				repository,
				monitor);
		
		saveVariant((ISCMachineFile) target, formulaElements.get(0), formulas.get(0), null);

	}

	private void saveVariant(
			ISCMachineFile target, 
			IVariant variant, 
			Expression expression,
			IProgressMonitor monitor) throws RodinDBException {
		
		if (expression == null)
			return;
		
		variantInfo.setExpression(expression);
		
		ISCVariant scVariant = target.getSCVariant(VARIANT_NAME_PREFIX);
		scVariant.create(null, monitor);
		scVariant.setExpression(expression, null);
		scVariant.setSource(variant, monitor);
	}
	
	@Override
	protected void makeProgress(IProgressMonitor monitor) {
		monitor.worked(1);
	}

	@Override
	protected ILabelSymbolTable getLabelSymbolTableFromRepository(
			IToolStateRepository<IState> repository) throws CoreException {
		// this method is never called because fetchLabel() is overriden
		return null;
	}

	@Override
	protected ITypeEnvironment typeCheckFormula(
			IVariant formulaElement, 
			Expression formula, 
			ITypeEnvironment typeEnvironment) throws CoreException {
		ITypeEnvironment inferredEnvironment =
			super.typeCheckFormula(formulaElement, formula, typeEnvironment);
		if (inferredEnvironment == null)
			return null;
		else {
			Expression expression = formula;
			Type type = expression.getType();
			boolean ok = type.equals(factory.makeIntegerType()) || type.getBaseType() != null;
			if (!ok) {
				createProblemMarker(
						formulaElement, 
						getFormulaAttributeType(), 
						GraphProblem.InvalidVariantTypeError, 
						type.toString());
				return null;
			} else
				return inferredEnvironment;
		}
	}

	@Override
	protected ILabelSymbolInfo createLabelSymbolInfo(
			String symbol, ILabeledElement element, String component) throws CoreException {
		return new VariantSymbolInfo(symbol, element, EventBAttributes.LABEL_ATTRIBUTE, component);
	}

	@Override
	protected List<IVariant> getFormulaElements(IRodinElement element) throws CoreException {
		IMachineFile machineFile = (IMachineFile) element;
		IVariant[] variants = machineFile.getVariants();
		return Arrays.asList(variants);
	}

}
