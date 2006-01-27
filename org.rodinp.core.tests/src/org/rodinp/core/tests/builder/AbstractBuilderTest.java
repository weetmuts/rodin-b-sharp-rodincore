package org.rodinp.core.tests.builder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;
import org.rodinp.core.tests.ModifyingResourceTests;
import org.rodinp.core.tests.util.Util;

public abstract class AbstractBuilderTest extends ModifyingResourceTests {
	
	public AbstractBuilderTest(String name) {
		super(name);
	}
	
	protected void runBuilder(IRodinProject project, String expectedTrace) throws CoreException {
		project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		if (expectedTrace != null)
			assertStringEquals("Unexpected tool trace", expectedTrace, ToolTrace.getTrace());
	}
	
	private String expandFile(IRodinFile file) throws RodinDBException {
		StringBuilder builder = new StringBuilder(file.getElementName());
		IRodinElement[] children = file.getChildren();
		for (IRodinElement element : children) {
			IInternalElement child = (IInternalElement) element;
			if (child.getElementType() == IDependency.ELEMENT_TYPE) {
				builder.append("\n  dep: ");
				builder.append(child.getElementName());
			} else {
				builder.append("\n  data: ");
				builder.append(child.getContents());
			}
		}
		return builder.toString();
	}
	
	private void assertStringEquals(String message, String expected, String actual) {
		if (!expected.equals(actual)){
			System.out.println(Util.displayString(actual, 4));
		}
		assertEquals(message, expected, actual);
	}
	
	protected void assertContents(String message,  String expected, IRodinFile file) throws CoreException {
		assertStringEquals(message, expected, expandFile(file));
	}
	
	protected IData createData(IRodinFile parent, String contents) throws RodinDBException {
		IData data = (IData) parent.createInternalElement(
				IData.ELEMENT_TYPE,
				null,
				null,
				null);
		data.setContents(contents);
		return data;
	}

	protected IDependency createDependency(IRodinFile parent, String target) throws RodinDBException {
		IDependency dep = (IDependency) parent.createInternalElement(
				IDependency.ELEMENT_TYPE,
				target,
				null,
				null);
		return dep;
	}

}
