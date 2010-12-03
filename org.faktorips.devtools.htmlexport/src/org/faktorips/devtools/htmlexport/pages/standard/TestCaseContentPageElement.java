/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;

/**
 * a page representing an {@link ITestCase}
 * 
 * @author dicker
 * 
 */
public class TestCaseContentPageElement extends AbstractIpsObjectContentPageElement<ITestCase> {

    private ITestCaseType testCaseType;

    /**
     * creates a page for the given {@link ITestCase} with the given context
     * 
     */
    protected TestCaseContentPageElement(ITestCase object, DocumentationContext context) {
        super(object, context);
        try {
            testCaseType = object.findTestCaseType(context.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build() {
        super.build();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(
                new TextPageElement(IpsObjectType.TEST_CASE_TYPE.getDisplayName() + ": ")) //$NON-NLS-1$
                .addPageElements(
                        PageElementUtils.createLinkPageElement(getContext(), testCaseType,
                                "content", testCaseType.getQualifiedName(), true))); //$NON-NLS-1$

        addTestCaseTypeParameters();
    }

    /**
     * adds a treeview of the parameters of the testcase
     */
    private void addTestCaseTypeParameters() {
        addPageElements(new TextPageElement(Messages.TestCaseContentPageElement_parameters, TextType.HEADING_2));
        TreeNodePageElement root = new TreeNodePageElement(new WrapperPageElement(WrapperType.NONE).addPageElements(
                new IpsElementImagePageElement(getDocumentedIpsObject())).addPageElements(
                new TextPageElement(getDocumentedIpsObject().getQualifiedName())));

        ITestObject[] testObjects = getDocumentedIpsObject().getTestObjects();
        for (ITestObject testObject : testObjects) {
            root.addPageElements(createTestObjectPageElement(testObject));
        }
        addPageElements(root);
    }

    /**
     * creates a {@link PageElement} for an {@link ITestObject}
     * 
     */
    private PageElement createTestObjectPageElement(ITestObject testObject) {
        try {
            if (testObject instanceof ITestValue) {
                return createTestValuePageElement((ITestValue)testObject);
            }
            if (testObject instanceof ITestRule) {
                return createTestRulePageElement((ITestRule)testObject);
            }
            if (testObject instanceof ITestPolicyCmpt) {
                return createTestPolicyCmptPageElement((ITestPolicyCmpt)testObject);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        return TextPageElement.createParagraph(testObject.getName() + " " + testObject.getClass()).addStyles(Style.BIG) //$NON-NLS-1$
                .addStyles(Style.BOLD);
    }

    private PageElement createTestPolicyCmptPageElement(ITestPolicyCmpt testObject) throws CoreException {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK)
                .addPageElements(new IpsElementImagePageElement(testObject)).addPageElements(
                        new TextPageElement(testObject.getTestParameterName())));

        PageElement testAttributesTable = createTestPolicyCmptTestAttributesTable(testObject);

        testObjectPageElement.addPageElements(testAttributesTable);

        ITestPolicyCmptLink[] policyCmptLinks = testObject.getTestPolicyCmptLinks();

        for (ITestPolicyCmptLink testPolicyCmptLink : policyCmptLinks) {
            ITestPolicyCmpt target = testPolicyCmptLink.findTarget();
            testObjectPageElement.addPageElements(createTestPolicyCmptPageElement(target));
        }

        return testObjectPageElement;
    }

    private PageElement createTestPolicyCmptTestAttributesTable(ITestPolicyCmpt testObject) throws CoreException {
        ITestAttributeValue[] testAttributeValues = testObject.getTestAttributeValues();
        if (testAttributeValues.length == 0) {
            return new TextPageElement(Messages.TestCaseContentPageElement_noTestAttributes);
        }

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
        for (ITestAttributeValue testAttributeValue : testAttributeValues) {
            String value = testAttributeValue.getValue();
            ITestAttribute attribute = testAttributeValue.findTestAttribute(getContext().getIpsProject());
            ValueDatatype datatype = attribute.findDatatype(getContext().getIpsProject());

            keyValueTable.addKeyValueRow(testAttributeValue.getName(), IpsPlugin.getDefault().getIpsPreferences()
                    .getDatatypeFormatter().formatValue(datatype, value));
        }
        return keyValueTable;
    }

    private PageElement createTestRulePageElement(ITestRule testObject) {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK)
                .addPageElements(new IpsElementImagePageElement(testObject)).addPageElements(
                        new TextPageElement(testObject.getTestParameterName())));

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
        keyValueTable.addKeyValueRow(Messages.TestCaseContentPageElement_name, testObject.getValidationRule());
        keyValueTable.addKeyValueRow(Messages.TestCaseContentPageElement_violationType, testObject.getViolationType()
                .getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
    }

    private PageElement createTestValuePageElement(ITestValue testObject) throws CoreException {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK)
                .addPageElements(new IpsElementImagePageElement(testObject)).addPageElements(
                        new TextPageElement(testObject.getTestParameterName())));

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement();
        keyValueTable.addKeyValueRow(Messages.TestCaseContentPageElement_name, testObject.getName());

        ITestValueParameter testValueParameter = testObject.findTestValueParameter(getContext().getIpsProject());
        ValueDatatype datatype = testValueParameter.findValueDatatype(getContext().getIpsProject());

        String value = testObject.getValue();
        keyValueTable.addKeyValueRow(Messages.TestCaseContentPageElement_value, IpsPlugin.getDefault()
                .getIpsPreferences().getDatatypeFormatter().formatValue(datatype, value));

        keyValueTable.addKeyValueRow(Messages.TestCaseContentPageElement_testParameterType, testValueParameter
                .getTestParameterType().getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
    }
}
