package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

public class TestCaseContentPageElement extends AbstractObjectContentPageElement<ITestCase> {

	private ITestCaseType testCaseType;

	protected TestCaseContentPageElement(ITestCase object, DocumentorConfiguration config) {
		super(object, config);
		try {
			testCaseType = object.findTestCaseType(config.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void build() {
		super.build();
		
		addPageElements(TextPageElement.createParagraph("HUHU"));
	}
}
