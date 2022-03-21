/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;

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
     * @throws IpsException if the ITestCaseType for the ITestCase could not be found
     * 
     */
    protected TestCaseContentPageElement(ITestCase object, DocumentationContext context) {
        super(object, context);
        testCaseType = object.findTestCaseType(context.getIpsProject());
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext()).addPageElements(
                new TextPageElement(IpsObjectType.TEST_CASE_TYPE.getDisplayName() + ": ", getContext())) //$NON-NLS-1$
                .addPageElements(
                        new PageElementUtils(getContext()).createLinkPageElement(getContext(), testCaseType,
                                TargetType.CONTENT, testCaseType.getQualifiedName(), true)));

        addTestCaseTypeParameters();
    }

    /**
     * adds a treeview of the parameters of the testcase
     */
    private void addTestCaseTypeParameters() {
        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.TestCaseContentPageElement_parameters), TextType.HEADING_2, getContext()));
        TreeNodePageElement root = createRootNode();

        ITestObject[] testObjects = getDocumentedIpsObject().getTestObjects();
        for (ITestObject testObject : testObjects) {
            root.addPageElements(createTestObjectPageElement(testObject));
        }
        addPageElements(root);
    }

    /**
     * creates a {@link IPageElement} for an {@link ITestObject}
     * 
     */
    private IPageElement createTestObjectPageElement(ITestObject testObject) {
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
        } catch (IpsException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error creating IPageElement for " + testObject.getName(), e)); //$NON-NLS-1$
        }

        return TextPageElement.createParagraph(
                getContext().getLabel(testObject) + " " + testObject.getClass(), getContext()); //$NON-NLS-1$
    }

    private IPageElement createTestPolicyCmptPageElement(ITestPolicyCmpt testObject) {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK,
                getContext()).addPageElements(new IpsElementImagePageElement(testObject, getContext())).addPageElements(
                        new TextPageElement(testObject.getTestParameterName(), getContext())),
                getContext());

        IPageElement testAttributesTable = createTestPolicyCmptTestAttributesTable(testObject);

        testObjectPageElement.addPageElements(testAttributesTable);

        ITestPolicyCmptLink[] policyCmptLinks = testObject.getTestPolicyCmptLinks();

        for (ITestPolicyCmptLink testPolicyCmptLink : policyCmptLinks) {
            ITestPolicyCmpt target = testPolicyCmptLink.findTarget();
            testObjectPageElement.addPageElements(createTestPolicyCmptPageElement(target));
        }

        return testObjectPageElement;
    }

    private IPageElement createTestPolicyCmptTestAttributesTable(ITestPolicyCmpt testObject)
            {
        ITestAttributeValue[] testAttributeValues = testObject.getTestAttributeValues();
        if (testAttributeValues.length == 0) {
            return new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.TestCaseContentPageElement_noTestAttributes), getContext());
        }

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        for (ITestAttributeValue testAttributeValue : testAttributeValues) {
            String value = testAttributeValue.getValue();
            ITestAttribute attribute = testAttributeValue.findTestAttribute(getContext().getIpsProject());
            ValueDatatype datatype = attribute.findDatatype(getContext().getIpsProject());

            keyValueTable.addKeyValueRow(getContext().getLabel(testAttributeValue), getContext().getDatatypeFormatter()
                    .formatValue(datatype, value));
        }
        return keyValueTable;
    }

    private IPageElement createTestRulePageElement(ITestRule testObject) {
        TreeNodePageElement testObjectPageElement = createRootNode(testObject.getTestParameterName());

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseContentPageElement_name),
                testObject.getValidationRule());
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseContentPageElement_violationType), testObject
                        .getViolationType().getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
    }

    private IPageElement createTestValuePageElement(ITestValue testObject) {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK,
                getContext()).addPageElements(new IpsElementImagePageElement(testObject, getContext())).addPageElements(
                        new TextPageElement(testObject.getTestParameterName(), getContext())),
                getContext());

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseContentPageElement_name),
                getContext().getLabel(testObject));

        ITestValueParameter testValueParameter = testObject.findTestValueParameter(getContext().getIpsProject());
        ValueDatatype datatype = testValueParameter.findValueDatatype(getContext().getIpsProject());

        String value = testObject.getValue();
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseContentPageElement_value),
                getContext().getDatatypeFormatter().formatValue(datatype, value));

        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseContentPageElement_testParameterType),
                testValueParameter.getTestParameterType().getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
    }

    private TreeNodePageElement createRootNode() {
        return createRootNode(getDocumentedIpsObject().getQualifiedName());
    }

    private TreeNodePageElement createRootNode(String name) {
        WrapperPageElement wrapperPageElement = new WrapperPageElement(WrapperType.NONE, getContext());
        try {
            IpsElementImagePageElement ipsElementImagePageElement = new IpsElementImagePageElement(
                    getDocumentedIpsObject(), getContext());
            wrapperPageElement.addPageElements(ipsElementImagePageElement);
        } catch (IpsException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.WARNING, "Could not find image for " + getDocumentedIpsObject().getName())); //$NON-NLS-1$
        }
        wrapperPageElement.addPageElements(new TextPageElement(name, getContext()));

        return new TreeNodePageElement(wrapperPageElement, getContext());
    }
}
