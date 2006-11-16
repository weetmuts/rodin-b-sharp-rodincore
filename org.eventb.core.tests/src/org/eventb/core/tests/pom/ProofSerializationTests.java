package org.eventb.core.tests.pom;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eventb.core.IPRFile;
import org.eventb.core.IPRProof;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.seqprover.IConfidence;
import org.eventb.core.seqprover.IProofTree;
import org.eventb.core.seqprover.IProverSequent;
import org.eventb.core.seqprover.ProverFactory;
import org.eventb.core.seqprover.ProverLib;
import org.eventb.core.seqprover.eventbExtensions.Tactics;
import org.eventb.core.seqprover.proofBuilder.IProofSkeleton;
import org.eventb.core.seqprover.proofBuilder.ProofBuilder;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

public class ProofSerializationTests extends TestCase {
	
	protected IRodinProject rodinProject;

	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();
	

	private FormulaFactory factory = FormulaFactory.getDefault();
	
	public final void test() throws RodinDBException{
		IPRFile prFile = (IPRFile) rodinProject.createRodinFile("x.bpr", true, null);
		
		assertTrue(prFile.exists());
		assertEquals(0, prFile.getProofTrees().length);
		
		IPRProof proof1 = prFile.createProofTree("proof1", null);
		assertEquals(1, prFile.getProofTrees().length);
		assertTrue(proof1.exists());
		assertEquals(proof1, prFile.getProofTree("proof1"));
		
		assertEquals(IConfidence.UNATTEMPTED, proof1.getConfidence(null));
		assertFalse(proof1.getProofDependencies(factory , null).hasDeps());
		
		// Test 1
		
		IProverSequent sequent = TestLib.genSeq("|- ⊤ ⇒ ⊤");
		IProofTree proofTree = ProverFactory.makeProofTree(sequent, null);
		proof1.setProofTree(proofTree, null);
		assertEquals(IConfidence.UNATTEMPTED, proof1.getConfidence(null));
		assertFalse(proof1.getProofDependencies(factory , null).hasDeps());
		
		Tactics.impI().apply(proofTree.getRoot(), null);
		proof1.setProofTree(proofTree, null);
		assertEquals(IConfidence.PENDING, proof1.getConfidence(null));
		assertTrue(proof1.getProofDependencies(factory , null).hasDeps());
		
		Tactics.tautology().apply(proofTree.getRoot().getFirstOpenDescendant(),null);
		// The next check is to see if the prover is behaving itself.
		assertTrue(proofTree.isClosed());
		proof1.setProofTree(proofTree, null);
		assertEquals(IConfidence.DISCHARGED_MAX, proof1.getConfidence(null));
		IProofSkeleton skel = proof1.getSkeleton(factory, null);
		assertTrue(ProverLib.deepEquals(skel, proofTree.getRoot()));
		
		// Test 2
		
		sequent = TestLib.genSeq("⊤ |- ⊤ ∧ ⊤");
		proofTree = ProverFactory.makeProofTree(sequent, null);
		
		Tactics.norm().apply(proofTree.getRoot(), null);
		proof1.setProofTree(proofTree, null);
		// The next check is to see if the prover is behaving itself.
		assertTrue(proofTree.isClosed());
		assertEquals(proofTree.getConfidence(), proof1.getConfidence(null));
		assertTrue(proof1.getProofDependencies(factory , null).hasDeps());
		skel = proof1.getSkeleton(factory, null);
		assertTrue(ProverLib.deepEquals(skel, proofTree.getRoot()));
		
		// Test 3
		
		sequent = TestLib.genSeq("⊤ |- 0 ∈ ℕ ∧ 0 ∈ ℤ");
		proofTree = ProverFactory.makeProofTree(sequent, null);
		proof1.setProofTree(proofTree, null);
		assertEquals(IConfidence.UNATTEMPTED, proof1.getConfidence(null));
		assertFalse(proof1.getProofDependencies(factory , null).hasDeps());
		
		Tactics.norm().apply(proofTree.getRoot(), null);
		proof1.setProofTree(proofTree, null);
		assertEquals(proofTree.getConfidence(), proof1.getConfidence(null));
		assertTrue(proof1.getProofDependencies(factory , null).hasDeps());
		skel = proof1.getSkeleton(factory, null);
		assertTrue(ProverLib.deepEquals(skel, proofTree.getRoot()));
		
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// ensure autobuilding is turned off
		IWorkspaceDescription wsDescription = workspace.getDescription();
		if (wsDescription.isAutoBuilding()) {
			wsDescription.setAutoBuilding(false);
			workspace.setDescription(wsDescription);
		}
		
		// Create a new project
		IProject project = workspace.getRoot().getProject("P");
		project.create(null);
		project.open(null);
		IProjectDescription pDescription = project.getDescription();
		pDescription.setNatureIds(new String[] {RodinCore.NATURE_ID});
		project.setDescription(pDescription, null);
		rodinProject = RodinCore.valueOf(project);
	}
	
	
	protected void tearDown() throws Exception {
		rodinProject.getProject().delete(true, true, null);
		super.tearDown();
	}

}
