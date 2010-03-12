package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.HierarchyPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;

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

		addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(new TextPageElement("Testfalltyp: "))
				.addPageElements(new LinkPageElement(testCaseType, "content", testCaseType.getQualifiedName(), true)));

		addPageElements(createTestCaseTypeHierarchie());
	}

	private HierarchyPageElement createTestCaseTypeHierarchie() {
		addPageElements(new TextPageElement("Aufbau", TextType.HEADING_2));
		HierarchyPageElement root = new HierarchyPageElement(new WrapperPageElement(WrapperType.NONE).addPageElements(
				new ImagePageElement(object)).addPageElements(new TextPageElement(object.getQualifiedName())));

		ITestObject[] testObjects = object.getTestObjects();
		for (ITestObject testObject : testObjects) {
			root.addPageElements(createTestObjectPageElement(testObject));
		}
		return root;

	}

	private PageElement createTestObjectPageElement(ITestObject testObject) {
		if (testObject instanceof ITestValue)
			return createTestValuePageElement((ITestValue) testObject);
		if (testObject instanceof ITestRule)
			return createTestRulePageElement((ITestRule) testObject);
		if (testObject instanceof ITestPolicyCmpt)
			return createTestPolicyCmptPageElement((ITestPolicyCmpt) testObject);

		return TextPageElement.createParagraph(testObject.getName() + " " + testObject.getClass()).addStyles(Style.BIG)
				.addStyles(Style.BOLD);
	}

	private PageElement createTestPolicyCmptPageElement(ITestPolicyCmpt testObject) {
		HierarchyPageElement testObjectPageElement = new HierarchyPageElement(new WrapperPageElement(WrapperType.BLOCK)
				.addPageElements(new ImagePageElement(testObject)).addPageElements(
						new TextPageElement(testObject.getTestParameterName())));

		ITestAttributeValue[] testAttributeValues = testObject.getTestAttributeValues();

		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		for (ITestAttributeValue testAttributeValue : testAttributeValues) {
			keyValueTable.addKeyValueRow(testAttributeValue.getName(), testAttributeValue.getValue());
		}

		testObjectPageElement.addPageElements(keyValueTable);
		
		return testObjectPageElement;
	}

	private PageElement createTestRulePageElement(ITestRule testObject) {
		HierarchyPageElement testObjectPageElement = new HierarchyPageElement(new WrapperPageElement(WrapperType.BLOCK)
				.addPageElements(new ImagePageElement(testObject)).addPageElements(
						new TextPageElement(testObject.getTestParameterName())));

		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		keyValueTable.addKeyValueRow(ITestRule.PROPERTY_NAME, testObject.getValidationRule());
		keyValueTable.addKeyValueRow(ITestRule.PROPERTY_VIOLATED, testObject.getViolationType().getName());

		testObjectPageElement.addPageElements(keyValueTable);

		return testObjectPageElement;
	}

	private PageElement createTestValuePageElement(ITestValue testObject) {
		HierarchyPageElement testObjectPageElement = new HierarchyPageElement(new WrapperPageElement(WrapperType.BLOCK)
				.addPageElements(new ImagePageElement(testObject)).addPageElements(
						new TextPageElement(testObject.getTestParameterName())));

		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		keyValueTable.addKeyValueRow(ITestValue.PROPERTY_NAME, testObject.getName());
		keyValueTable.addKeyValueRow(ITestValue.PROPERTY_VALUE, testObject.getValue());
		try {
			keyValueTable.addKeyValueRow(ITestValueParameter.PROPERTY_TEST_PARAMETER_TYPE, testObject
					.findTestValueParameter(config.getIpsProject()).getTestParameterType().getName());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		testObjectPageElement.addPageElements(keyValueTable);

		return testObjectPageElement;
	}
}
