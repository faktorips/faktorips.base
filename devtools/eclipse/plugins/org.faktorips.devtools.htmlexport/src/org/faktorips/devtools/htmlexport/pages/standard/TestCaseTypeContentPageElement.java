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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RegexTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * a page representing an {@link ITestCaseType}
 * 
 * @author dicker
 * 
 */
public class TestCaseTypeContentPageElement extends AbstractIpsObjectContentPageElement<ITestCaseType> {

    /**
     * creates a page representing the given {@link ITestCaseType} with the given context
     * 
     */
    protected TestCaseTypeContentPageElement(ITestCaseType object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addTestCaseTypeParameters();
    }

    /**
     * adds a treeview with the Parameters of the {@link ITestCaseType}
     */
    private void addTestCaseTypeParameters() {
        addPageElements(new TextPageElement(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_parameters),
                TextType.HEADING_2, getContext()));
        TreeNodePageElement root = createRootNode();

        ITestParameter[] testParameters = getDocumentedIpsObject().getTestParameters();
        for (ITestParameter testParameter : testParameters) {
            try {
                root.addPageElements(createTestParameterPageElement(testParameter));
            } catch (IpsException e) {
                getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error adding TestParameter", e)); //$NON-NLS-1$
            }
        }
        addPageElements(root);
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
            IpsStatus status = new IpsStatus(IStatus.WARNING,
                    "Could not find image for " + getDocumentedIpsObject().getName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
        }
        wrapperPageElement.addPageElements(new TextPageElement(name, getContext()));

        return new TreeNodePageElement(wrapperPageElement, getContext());
    }

    /**
     * creates a IPageElement representing the given testParameter
     * 
     */
    private IPageElement createTestParameterPageElement(ITestParameter testParameter) {
        if (testParameter instanceof ITestValueParameter) {
            return createTestValueParameterPageElement((ITestValueParameter)testParameter);
        }
        if (testParameter instanceof ITestRuleParameter) {
            return createTestRuleParameterPageElement((ITestRuleParameter)testParameter);
        }
        if (testParameter instanceof ITestPolicyCmptTypeParameter) {
            return createTestPolicyCmptTypePageElement((ITestPolicyCmptTypeParameter)testParameter);
        }

        return TextPageElement.createParagraph(getContext().getLabel(testParameter) + " " + testParameter.getClass(), //$NON-NLS-1$
                getContext()).addStyles(Style.BIG).addStyles(Style.BOLD);
    }

    private IPageElement createTestPolicyCmptTypePageElement(ITestPolicyCmptTypeParameter testParameter) {
        IPolicyCmptType policyCmptType = testParameter.findPolicyCmptType(testParameter.getIpsProject());

        IPageElement linkPageElement = new PageElementUtils(getContext()).createLinkPageElement(getContext(),
                policyCmptType, TargetType.CONTENT, getContext().getLabel(policyCmptType), true);
        TreeNodePageElement testParameterPageElement = new TreeNodePageElement(
                new WrapperPageElement(WrapperType.BLOCK, getContext()).addPageElements(linkPageElement)
                        .addPageElements(new TextPageElement((" - " + testParameter.getTestParameterType().getName()), //$NON-NLS-1$
                                getContext())),
                getContext());

        testParameterPageElement.addPageElements(createKeyValueTableForTestPolicyCmptTypeParameter(testParameter));

        testParameterPageElement.addPageElements(createTestAttributeTable(testParameter));

        ITestPolicyCmptTypeParameter[] policyCmptTypeParamChilds = testParameter.getTestPolicyCmptTypeParamChilds();
        for (ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter : policyCmptTypeParamChilds) {
            testParameterPageElement.addPageElements(createTestPolicyCmptTypePageElement(testPolicyCmptTypeParameter));
        }

        return testParameterPageElement;
    }

    private IPageElement createTestAttributeTable(ITestPolicyCmptTypeParameter testParameter) {

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext())
                .addPageElements(TextPageElement
                        .createParagraph(getContext().getMessage(
                                HtmlExportMessages.TestCaseTypeContentPageElement_testAttributes), getContext())
                        .addStyles(Style.BOLD));

        TestAttributesTablePageElement testAttributesTablePageElement = new TestAttributesTablePageElement(
                testParameter);
        if (testAttributesTablePageElement.isEmpty()) {
            return wrapper.addPageElements(TextPageElement.createParagraph(
                    getContext().getMessage("TestCaseTypeContentPageElement_noTestAttributes"), getContext())); //$NON-NLS-1$
        }

        return wrapper.addPageElements(testAttributesTablePageElement);
    }

    private KeyValueTablePageElement createKeyValueTableForTestPolicyCmptTypeParameter(
            ITestPolicyCmptTypeParameter testParameter) {
        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_name),
                getContext().getLabel(testParameter));
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_testParameterType),
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_requiresProductCmpt),
                testParameter.isRequiresProductCmpt() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_minInstances),
                Integer.toString(testParameter.getMinInstances()));
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_maxInstances),
                Integer.toString(testParameter.getMaxInstances()));
        return keyValueTable;
    }

    private IPageElement createTestRuleParameterPageElement(ITestRuleParameter testParameter) {
        String name = createNodeName(testParameter);
        TreeNodePageElement testParameterPageElement = createRootNode(name);

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_name),
                getContext().getLabel(testParameter));
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_testParameterType),
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_description),
                getContext().getDescription(testParameter));

        testParameterPageElement.addPageElements(keyValueTable);
        return testParameterPageElement;
    }

    public String createNodeName(ITestParameter testParameter) {
        return getContext().getLabel(testParameter) + " - " + testParameter.getTestParameterType().getName(); //$NON-NLS-1$
    }

    private IPageElement createTestValueParameterPageElement(ITestValueParameter testParameter) {
        String name = createNodeName(testParameter);
        TreeNodePageElement testParameterPageElement = createRootNode(name);

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_name),
                getContext().getLabel(testParameter));
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_valueDatatype),
                testParameter.getValueDatatype());
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_testParameterType),
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(
                getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_description),
                getContext().getDescription(testParameter));

        testParameter.getTestParameterType();

        testParameterPageElement.addPageElements(keyValueTable);
        return testParameterPageElement;
    }

    /**
     * a table for the {@link ITestAttribute}s of the {@link ITestCaseType}
     * 
     * @author dicker
     * 
     */
    private class TestAttributesTablePageElement
            extends AbstractIpsObjectPartsContainerTablePageElement<ITestAttribute> {

        public TestAttributesTablePageElement(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter) {
            super(Arrays.asList(testPolicyCmptTypeParameter.getTestAttributes()),
                    TestCaseTypeContentPageElement.this.getContext());
            addLayouts(new RegexTablePageElementLayout(".{1}", Style.CENTER)); //$NON-NLS-1$
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(ITestAttribute attribute) {
            return Arrays.asList(getAttributeData(attribute));
        }

        protected IPageElement[] getAttributeData(ITestAttribute attribute) {
            List<IPageElement> attributeData = new ArrayList<>();

            attributeData.add(new TextPageElement(getContext().getLabel(attribute), getContext()));
            attributeData.add(new TextPageElement(attribute.getTestAttributeType().getName(), getContext()));
            attributeData.add(new TextPageElement(attribute.getAttribute(), getContext()));

            addPolicyComponentAndDataType(attribute, attributeData);

            attributeData.add(new TextPageElement(getContext().getDescription(attribute), getContext()));

            return attributeData.toArray(new IPageElement[attributeData.size()]);
        }

        private void addPolicyComponentAndDataType(ITestAttribute attribute, List<IPageElement> attributeData) {
            String correspondingPolicyCmptType = attribute.getCorrespondingPolicyCmptType();

            if (IpsStringUtils.isEmpty(correspondingPolicyCmptType)) {
                attributeData.add(new TextPageElement("-", getContext())); //$NON-NLS-1$
                attributeData.add(new TextPageElement(attribute.getDatatype(), getContext()));
                return;
            }

            IPolicyCmptType policyCmptType = getContext().getIpsProject()
                    .findPolicyCmptType(correspondingPolicyCmptType);
            attributeData.add(new PageElementUtils(getContext()).createLinkPageElement(getContext(), policyCmptType,
                    TargetType.CONTENT, correspondingPolicyCmptType, true));
            if (policyCmptType != null) {
                attributeData.add(new TextPageElement(
                        policyCmptType.getAttribute(attribute.getAttribute()).getDatatype(), getContext()));
            }
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_name));
            headline.add(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_testAttributeType));
            headline.add(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_attribute));
            headline.add(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName());
            headline.add(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_datatype));
            headline.add(getContext().getMessage(HtmlExportMessages.TestCaseTypeContentPageElement_description));

            return headline;
        }
    }
}
