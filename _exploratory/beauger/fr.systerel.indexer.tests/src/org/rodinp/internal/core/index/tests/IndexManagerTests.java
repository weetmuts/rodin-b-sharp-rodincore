package org.rodinp.internal.core.index.tests;

import static org.rodinp.internal.core.index.tests.IndexTestsUtil.assertDescriptor;
import static org.rodinp.internal.core.index.tests.IndexTestsUtil.assertNoSuchDescriptor;
import static org.rodinp.internal.core.index.tests.IndexTestsUtil.createDefaultOccurrence;
import static org.rodinp.internal.core.index.tests.IndexTestsUtil.createNamedElement;
import static org.rodinp.internal.core.index.tests.IndexTestsUtil.createRodinFile;
import static org.rodinp.internal.core.index.tests.IndexTestsUtil.makeIRFArray;

import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.index.IIndexer;
import org.rodinp.core.index.RodinIndexer;
import org.rodinp.core.tests.AbstractRodinDBTests;
import org.rodinp.core.tests.basis.NamedElement;
import org.rodinp.internal.core.index.Descriptor;
import org.rodinp.internal.core.index.IndexManager;
import org.rodinp.internal.core.index.RodinIndex;

public class IndexManagerTests extends AbstractRodinDBTests {

	private static IIndexer indexer;
	private static RodinIndex rodinIndex;
	private IRodinProject project;
	private IRodinFile file;
	private static NamedElement elt1;
	private static NamedElement elt2;
	private static final String name1 = "elt1Name";
	private static final String name2 = "elt2Name";

	private static final IndexManager manager = IndexManager.getDefault();

	public IndexManagerTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		project = createRodinProject("P");
		file = createRodinFile(project, "indMan.test");
		elt1 = createNamedElement(file, "elt1");
		elt2 = createNamedElement(file, "elt2");
		rodinIndex = new RodinIndex();
		final Descriptor desc1 = rodinIndex.makeDescriptor(elt1, name1);
		desc1.addOccurrence(IndexTestsUtil.createDefaultOccurrence(file));
		final Descriptor desc2 = rodinIndex.makeDescriptor(elt2, name2);
		desc2.addOccurrence(IndexTestsUtil.createDefaultOccurrence(file));
		
		indexer = new FakeIndexer(rodinIndex);
		RodinIndexer.register(indexer, file.getElementType());
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("P");
		manager.clear();
		super.tearDown();
	}

	public void testScheduleIndexing() throws Exception {

		manager.scheduleIndexing(file);

		final RodinIndex index = manager.getIndex(project);
		final Descriptor desc1 = index.getDescriptor(elt1);
		final Descriptor desc2 = index.getDescriptor(elt2);

		assertDescriptor(desc1, elt1, name1, 1);
		assertDescriptor(desc2, elt2, name2, 1);
	}

	public void testSeveralIndexing() throws Exception {

		rodinIndex.removeDescriptor(elt2);
		
		// first indexing with elt1, without elt2
		manager.scheduleIndexing(file);

		final RodinIndex index1 = manager.getIndex(project);
		final Descriptor descElement = index1.getDescriptor(elt1);

		assertDescriptor(descElement, elt1, name1, 1);
		assertNoSuchDescriptor(index1, elt2);

		// removing elt1, adding elt2
		rodinIndex.removeDescriptor(elt1);
		final Descriptor desc2 = rodinIndex.makeDescriptor(elt2, name2);
		desc2.addOccurrence(createDefaultOccurrence(file));
		
		// second indexing with element2, without element
		manager.scheduleIndexing(file);

		final RodinIndex index2 = manager.getIndex(project);
		final Descriptor descElement2 = index2.getDescriptor(elt2);

		assertNoSuchDescriptor(index2, elt1);
		assertDescriptor(descElement2, elt2, name2, 1);
	}

	public void testIndexFileDoesNotExist() throws Exception {
		final IRodinFile inexistentFile = project
				.getRodinFile("inexistentFile.test");
		try {
			manager.scheduleIndexing(inexistentFile);
		} catch (Exception e) {
			fail("trying to index a inexistent file should not raise an Exception");
		}
	}

	public void testIndexNoIndexer() throws Exception {
		manager.clearIndexers();
		try {
			manager.scheduleIndexing(file);
		} catch (IllegalStateException e) {
			return;
		}
		fail("trying to index with no indexer registered should raise IllegalStateException");
	}

	public void testIndexSeveralProjects() throws Exception {
		final String eltF2Name = "eltF2Name";

		final IRodinProject project2 = createRodinProject("P2");
		final IRodinFile file2 = createRodinFile(project2, "file2P2.test");
		final NamedElement eltF2 = createNamedElement(file2, eltF2Name);
		
		rodinIndex.makeDescriptor(eltF2, eltF2Name);
		
		final IRodinFile[] toIndex = makeIRFArray(file, file2);
		manager.scheduleIndexing(toIndex);

		final RodinIndex index1 = manager.getIndex(project);
		final Descriptor desc1 = index1.getDescriptor(elt1);
		final RodinIndex index2 = manager.getIndex(project2);
		final Descriptor desc2 = index2.getDescriptor(eltF2);

		assertDescriptor(desc1, elt1, name1, 1);
		assertDescriptor(desc2, eltF2, eltF2Name, 0);

		deleteProject("P2");
	}
}
