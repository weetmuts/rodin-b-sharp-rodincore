/*******************************************************************************
 * Copyright (c) 2011, 2012 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package org.eventb.core.seqprover.eventbExtensionTests.mbGoal;

import static org.eventb.core.seqprover.tests.TestLib.genExpr;
import static org.eventb.internal.core.seqprover.Util.getNullProofMonitor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.ITypeEnvironmentBuilder;
import org.eventb.core.ast.Predicate;
import org.eventb.core.seqprover.IProofMonitor;
import org.eventb.core.seqprover.tests.TestLib;
import org.eventb.internal.core.seqprover.eventbExtensions.mbGoal.MembershipGoalRules;
import org.eventb.internal.core.seqprover.eventbExtensions.mbGoal.Rule;

public class AbstractMbGoalTests {

	protected static final FormulaFactory ff = FormulaFactory.getDefault();
	protected static final MembershipGoalRules rf = new MembershipGoalRules(ff);
	protected static final IProofMonitor pm = getNullProofMonitor();

	public AbstractMbGoalTests() {
		super();
	}

	protected static class TestItem {

		protected final ITypeEnvironmentBuilder typenv;
		protected final Set<Predicate> hyps;

		TestItem(String typenvImage, String... hypImages) {
			this.typenv = TestLib.mTypeEnvironment(typenvImage);
			this.hyps = new LinkedHashSet<Predicate>();
			for (String hypImage : hypImages) {
				hyps.add(TestLib.genPred(typenv, hypImage));
			}
		}

		public Rule<?> hyp(String hypImage) {
			final Predicate hyp = TestLib.genPred(typenv, hypImage);
			return rf.hypothesis(hyp);
		}

		public Rule<?> setExtMember(String memberImage, Rule<?> child) {
			final Expression member = genExpr(typenv, memberImage);
			return rf.setExtMember(member, child);
		}

	}

}