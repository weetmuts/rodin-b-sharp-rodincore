package org.rodinp.internal.core.index.tests;

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

	private IIndexer indexer = new FakeIndexer();
	private IRodinProject project;
	private IRodinFile file;
	private final IndexManager manager = IndexManager.getDefault();

	public IndexManagerTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		project = createRodinProject("P");
		file = IndexTestsUtil.createRodinFile(project, "indMan.test");
		RodinIndexer.register(indexer, file.getElementType());
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("P");
		manager.clear();
		super.tearDown();
	}

	public void testScheduleIndexing() throws Exception {
		final NamedElement element = IndexTestsUtil.createNamedElement(file,
				IndexTestsUtil.defaultName);

		manager.scheduleIndexing(file);

		final RodinIndex index = manager.getIndex(project);
		final Descriptor desc = index.getDescriptor(element);

		IndexTestsUtil.assertDescriptor(desc, element,
				IndexTestsUtil.defaultName, 6);
	}

	public void testScheduleSeveralIndexing() throws Exception {
		// TODO test several calls to scheduleIndexing with same file

		final String elementName = IndexTestsUtil.defaultName;
		final NamedElement element = IndexTestsUtil.createNamedElement(file,
				elementName);

		final String element2Name = IndexTestsUtil.defaultName + "2";
		final NamedElement element2 = new NamedElement(element2Name, file);

		RodinIndex index;

		// first indexing with element, without element2
		manager.scheduleIndexing(file);

		index = manager.getIndex(project);
		final Descriptor descElement = index.getDescriptor(element);

		IndexTestsUtil.assertDescriptor(descElement, element, elementName, 6);
		IndexTestsUtil.assertNoSuchDescriptor(index, element2);

		element.delete(true, null);
		element2.create(null, null);

		// second indexing with element2, without element
		manager.scheduleIndexing(file);

		index = manager.getIndex(project);
		final Descriptor descElement2 = index.getDescriptor(element2);

		IndexTestsUtil.assertNoSuchDescriptor(index, element);
		IndexTestsUtil
				.assertDescriptor(descElement2, element2, element2Name, 6);
	}

	public void testIndexFileDoesNotExist() throws Exception {
		IRodinFile inexistentFile = project.getRodinFile("inexistentFile.test");
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
		final String el1Name = "elementF1Name";
		final String el2Name = "elementF2Name";

		final NamedElement elementF1 = IndexTestsUtil.createNamedElement(file,
				el1Name);
		final IRodinProject project2 = createRodinProject("P2");
		IRodinFile file2 = IndexTestsUtil.createRodinFile(project2,
				"file2P2.test");
		final NamedElement elementF2 = IndexTestsUtil.createNamedElement(file2,
				el2Name);

		IRodinFile[] toIndex = new IRodinFile[] { file, file2 };
		manager.scheduleIndexing(toIndex);

		final RodinIndex index1 = manager.getIndex(project);
		final Descriptor desc1 = index1.getDescriptor(elementF1);
		final RodinIndex index2 = manager.getIndex(project2);
		final Descriptor desc2 = index2.getDescriptor(elementF2);

		IndexTestsUtil.assertDescriptor(desc1, elementF1, el1Name, 6);

		IndexTestsUtil.assertDescriptor(desc2, elementF2, el2Name, 6);

	}
}
