package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestValueParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
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
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;

public class TestCaseTypeContentPageElement extends AbstractObjectContentPageElement<ITestCaseType> {

	private class TestAttributesTablePageElement extends AbstractSpecificTablePageElement {

		protected ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

		public TestAttributesTablePageElement(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter) {
			super();
			this.testPolicyCmptTypeParameter = testPolicyCmptTypeParameter;
		}

		@Override
		protected void addDataRows() {
			ITestAttribute[] attributes = testPolicyCmptTypeParameter.getTestAttributes();
			for (ITestAttribute attribute : attributes) {
				addAttributeRow(attribute);
			}
		}

		protected void addAttributeRow(ITestAttribute attribute) {
			addSubElement(new TableRowPageElement(getAttributeData(attribute)));
		}

		protected PageElement[] getAttributeData(ITestAttribute attribute) {
			List<PageElement> attributeData = new ArrayList<PageElement>();

			attributeData.add(new TextPageElement(attribute.getName()));
			attributeData.add(new WrapperPageElement(WrapperType.NONE).addPageElements(
					new ImagePageElement(attribute.getTestAttributeType())).addPageElements(
					new TextPageElement(attribute.getTestAttributeType().getName())));
			attributeData.add(new TextPageElement(attribute.getAttribute()));
			try {
				attributeData.add(new TextPageElement(attribute.getCorrespondingPolicyCmptType() + "TODO")
						.addStyles(Style.BOLD));
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			attributeData.add(new TextPageElement(attribute.getDatatype() + "TODO").addStyles(Style.BOLD));
			attributeData.add(new TextPageElement(attribute.getDescription()));

			return attributeData.toArray(new PageElement[attributeData.size()]);
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(ITestAttribute.PROPERTY_NAME);
			headline.add(ITestAttribute.PROPERTY_TEST_ATTRIBUTE_TYPE);
			headline.add(ITestAttribute.PROPERTY_ATTRIBUTE);
			headline.add(ITestAttribute.PROPERTY_POLICYCMPTTYPE_OF_ATTRIBUTE);
			headline.add(ITestAttribute.PROPERTY_DATATYPE);
			headline.add(ITestAttribute.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(testPolicyCmptTypeParameter.getTestAttributes());
		}
	}

	protected TestCaseTypeContentPageElement(ITestCaseType object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	public void build() {
		super.build();

		HierarchyPageElement root = createTestCaseTypeHierarchie();

		addPageElements(root);
	}

	private HierarchyPageElement createTestCaseTypeHierarchie() {
		addPageElements(new TextPageElement("Aufbau", TextType.HEADING_2));
		HierarchyPageElement root = new HierarchyPageElement(new WrapperPageElement(WrapperType.NONE).addPageElements(
				new ImagePageElement(object)).addPageElements(new TextPageElement(object.getQualifiedName())));

		ITestParameter[] testParameters = object.getTestParameters();
		for (ITestParameter testParameter : testParameters) {
			root.addPageElements(getTestParameterPageElement(testParameter));
		}
		return root;
	}

	private PageElement getTestParameterPageElement(ITestParameter testParameter) {
		if (testParameter instanceof TestValueParameter)
			return getTestValueParameterPageElement((TestValueParameter) testParameter);
		if (testParameter instanceof TestRuleParameter) {
			return getTestRuleParameterPageElement((TestRuleParameter) testParameter);
		}
		if (testParameter instanceof TestPolicyCmptTypeParameter) {
			return getTestPolicyCmptTypePageElement((TestPolicyCmptTypeParameter) testParameter);
		}
		return TextPageElement.createParagraph(testParameter.getName() + " " + testParameter.getClass()).addStyles(
				Style.BIG).addStyles(Style.BOLD);
	}

	private PageElement getTestPolicyCmptTypePageElement(TestPolicyCmptTypeParameter testParameter) {
		IPolicyCmptType policyCmptType;
		try {
			policyCmptType = testParameter.findPolicyCmptType(testParameter.getIpsProject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		LinkPageElement linkPageElement = new LinkPageElement(policyCmptType, "content", policyCmptType.getName(), true);
		HierarchyPageElement testParameterPageElement = new HierarchyPageElement(new WrapperPageElement(
				WrapperType.BLOCK).addPageElements(linkPageElement).addPageElements(
				new TextPageElement((" - " + testParameter.getTestParameterType().getName()))));

		testParameterPageElement.addPageElements(createKeyValueTableForTestPolicyCmptTypeParameter(testParameter));

		testParameterPageElement.addPageElements(TextPageElement.createParagraph("Testattribute:"));

		testParameterPageElement.addPageElements(new TestAttributesTablePageElement(testParameter));

		testParameterPageElement.addPageElements(TextPageElement.createParagraph("SUBTYPEN").addStyles(Style.BOLD).addStyles(Style.BIG));

		return testParameterPageElement;
	}

	private KeyValueTablePageElement createKeyValueTableForTestPolicyCmptTypeParameter(
			TestPolicyCmptTypeParameter testParameter) {
		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		keyValueTable.addKeyValueRow(ITestPolicyCmptTypeParameter.PROPERTY_NAME, testParameter.getName());
		keyValueTable.addKeyValueRow(ITestPolicyCmptTypeParameter.PROPERTY_TEST_PARAMETER_TYPE, new WrapperPageElement(
				WrapperType.NONE).addPageElements(new ImagePageElement(testParameter.getTestParameterType()))
				.addPageElements(new TextPageElement(testParameter.getTestParameterType().getName())));
		keyValueTable.addKeyValueRow(ITestPolicyCmptTypeParameter.PROPERTY_REQUIRES_PRODUCTCMT, testParameter
				.isRequiresProductCmpt() ? "X" : "-");
		keyValueTable.addKeyValueRow(ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES, Integer
				.toString(testParameter.getMinInstances()));
		keyValueTable.addKeyValueRow(ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES, Integer
				.toString(testParameter.getMaxInstances()));
		return keyValueTable;
	}

	private PageElement getTestRuleParameterPageElement(TestRuleParameter testParameter) {
		String name = testParameter.getName() + " - " + testParameter.getTestParameterType().getName();
		HierarchyPageElement testParameterPageElement = new HierarchyPageElement(new WrapperPageElement(
				WrapperType.BLOCK).addPageElements(new ImagePageElement(testParameter)).addPageElements(
				new TextPageElement(name)));

		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		keyValueTable.addKeyValueRow(ITestRuleParameter.PROPERTY_NAME, testParameter.getName());
		keyValueTable.addKeyValueRow(ITestRuleParameter.PROPERTY_TEST_PARAMETER_TYPE, new WrapperPageElement(
				WrapperType.NONE).addPageElements(new ImagePageElement(testParameter.getTestParameterType()))
				.addPageElements(new TextPageElement(testParameter.getTestParameterType().getName())));
		keyValueTable.addKeyValueRow(ITestRuleParameter.PROPERTY_DESCRIPTION, testParameter.getDescription());

		testParameter.getTestParameterType();

		testParameterPageElement.addPageElements(keyValueTable);
		return testParameterPageElement;
	}

	private PageElement getTestValueParameterPageElement(TestValueParameter testParameter) {
		String name = testParameter.getName() + " - " + testParameter.getTestParameterType().getName();
		HierarchyPageElement testParameterPageElement = new HierarchyPageElement(new WrapperPageElement(
				WrapperType.BLOCK).addPageElements(new ImagePageElement(testParameter)).addPageElements(
				new TextPageElement(name)));

		KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
		keyValueTable.addKeyValueRow(ITestValueParameter.PROPERTY_NAME, testParameter.getName());
		keyValueTable.addKeyValueRow(ITestValueParameter.PROPERTY_VALUEDATATYPE, testParameter.getValueDatatype());
		keyValueTable.addKeyValueRow(ITestValueParameter.PROPERTY_TEST_PARAMETER_TYPE, new WrapperPageElement(
				WrapperType.NONE).addPageElements(new ImagePageElement(testParameter.getTestParameterType()))
				.addPageElements(new TextPageElement(testParameter.getTestParameterType().getName())));
		keyValueTable.addKeyValueRow(ITestValueParameter.PROPERTY_DESCRIPTION, testParameter.getDescription());

		testParameter.getTestParameterType();

		testParameterPageElement.addPageElements(keyValueTable);
		return testParameterPageElement;
	}
}
