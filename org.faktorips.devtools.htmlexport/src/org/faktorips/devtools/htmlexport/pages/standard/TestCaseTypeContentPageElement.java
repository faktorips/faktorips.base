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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.internal.model.testcasetype.TestValueParameter;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RegexTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.KeyValueTablePageElement;

/**
 * a page representing an {@link ITestCaseType}
 * 
 * @author dicker
 * 
 */
public class TestCaseTypeContentPageElement extends AbstractIpsObjectContentPageElement<ITestCaseType> {

    /**
     * a table for the {@link ITestAttribute}s of the {@link ITestCaseType}
     * 
     * @author dicker
     * 
     */
    private class TestAttributesTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<ITestAttribute> {

        public TestAttributesTablePageElement(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter) {
            super(Arrays.asList(testPolicyCmptTypeParameter.getTestAttributes()), TestCaseTypeContentPageElement.this
                    .getContext());
            addLayouts(new RegexTablePageElementLayout(".{1}", Style.CENTER)); //$NON-NLS-1$
        }

        @Override
        protected List<? extends PageElement> createRowWithIpsObjectPart(ITestAttribute attribute) {
            return Arrays.asList(getAttributeData(attribute));
        }

        protected PageElement[] getAttributeData(ITestAttribute attribute) {
            List<PageElement> attributeData = new ArrayList<PageElement>();

            attributeData.add(new TextPageElement(attribute.getName()));
            attributeData.add(new TextPageElement(attribute.getTestAttributeType().getName()));
            attributeData.add(new TextPageElement(attribute.getAttribute()));

            try {
                addPolicyComponentAndDataType(attribute, attributeData);
            } catch (CoreException e) {
                getContext().addStatus(
                        new IpsStatus(IStatus.WARNING, "Error adding data of corresponding PolicyCmptType", e)); //$NON-NLS-1$
            }

            attributeData.add(new TextPageElement(getContext().getDescription(attribute)));

            return attributeData.toArray(new PageElement[attributeData.size()]);
        }

        private void addPolicyComponentAndDataType(ITestAttribute attribute, List<PageElement> attributeData)
                throws CoreException {

            String correspondingPolicyCmptType = attribute.getCorrespondingPolicyCmptType();

            if (StringUtils.isEmpty(correspondingPolicyCmptType)) {
                attributeData.add(new TextPageElement("-")); //$NON-NLS-1$
                attributeData.add(new TextPageElement(attribute.getDatatype()));
                return;
            }

            IPolicyCmptType policyCmptType;
            try {
                policyCmptType = getContext().getIpsProject().findPolicyCmptType(correspondingPolicyCmptType);
            } catch (CoreException e) {
                attributeData.add(new TextPageElement("-")); //$NON-NLS-1$
                attributeData.add(new TextPageElement(attribute.getDatatype()));
                throw e;
            }

            attributeData.add(PageElementUtils.createLinkPageElement(getContext(), policyCmptType,
                    "content", correspondingPolicyCmptType, true)); //$NON-NLS-1$
            attributeData.add(new TextPageElement(policyCmptType.getAttribute(attribute.getAttribute()).getDatatype()));
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<String>();

            headline.add(getContext().getMessage("TestCaseTypeContentPageElement_name")); //$NON-NLS-1$
            headline.add(getContext().getMessage("TestCaseTypeContentPageElement_testAttributeType")); //$NON-NLS-1$
            headline.add(getContext().getMessage("TestCaseTypeContentPageElement_attribute")); //$NON-NLS-1$
            headline.add(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName());
            headline.add(getContext().getMessage("TestCaseTypeContentPageElement_datatype")); //$NON-NLS-1$
            headline.add(getContext().getMessage("TestCaseTypeContentPageElement_description")); //$NON-NLS-1$

            return headline;
        }
    }

    /**
     * creates a page representing the given {@link ITestCaseType} with the given context
     * 
     */
    protected TestCaseTypeContentPageElement(ITestCaseType object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    public void build() {
        super.build();

        addTestCaseTypeParameters();
    }

    /**
     * adds a treeview with the Parameters of the {@link ITestCaseType}
     */
    private void addTestCaseTypeParameters() {
        addPageElements(new TextPageElement(getContext().getMessage("TestCaseTypeContentPageElement_parameters"), //$NON-NLS-1$
                TextType.HEADING_2));
        TreeNodePageElement root = createRootNode();

        ITestParameter[] testParameters = getDocumentedIpsObject().getTestParameters();
        for (ITestParameter testParameter : testParameters) {
            try {
                root.addPageElements(createTestParameterPageElement(testParameter));
            } catch (CoreException e) {
                getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error adding TestParameter", e)); //$NON-NLS-1$
            }
        }
        addPageElements(root);
    }

    private TreeNodePageElement createRootNode() {
        return createRootNode(getDocumentedIpsObject().getQualifiedName());
    }

    private TreeNodePageElement createRootNode(String name) {
        WrapperPageElement wrapperPageElement = new WrapperPageElement(WrapperType.NONE);
        try {
            IpsElementImagePageElement ipsElementImagePageElement = new IpsElementImagePageElement(
                    getDocumentedIpsObject());
            wrapperPageElement.addPageElements(ipsElementImagePageElement);
        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(IStatus.WARNING,
                    "Could not find image for " + getDocumentedIpsObject().getName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
        }
        wrapperPageElement.addPageElements(new TextPageElement(name));

        return new TreeNodePageElement(wrapperPageElement);
    }

    /**
     * creates a PageElement representing the given testParameter
     * 
     */
    private PageElement createTestParameterPageElement(ITestParameter testParameter) throws CoreException {
        if (testParameter instanceof TestValueParameter) {
            return createTestValueParameterPageElement((TestValueParameter)testParameter);
        }
        if (testParameter instanceof TestRuleParameter) {
            return createTestRuleParameterPageElement((TestRuleParameter)testParameter);
        }
        if (testParameter instanceof TestPolicyCmptTypeParameter) {
            return createTestPolicyCmptTypePageElement((TestPolicyCmptTypeParameter)testParameter);
        }

        return TextPageElement.createParagraph(testParameter.getName() + " " + testParameter.getClass()).addStyles( //$NON-NLS-1$
                Style.BIG).addStyles(Style.BOLD);
    }

    private PageElement createTestPolicyCmptTypePageElement(ITestPolicyCmptTypeParameter testParameter)
            throws CoreException {
        IPolicyCmptType policyCmptType = testParameter.findPolicyCmptType(testParameter.getIpsProject());

        PageElement linkPageElement = PageElementUtils.createLinkPageElement(getContext(), policyCmptType,
                "content", policyCmptType.getName(), true); //$NON-NLS-1$
        TreeNodePageElement testParameterPageElement = new TreeNodePageElement(
                new WrapperPageElement(WrapperType.BLOCK).addPageElements(linkPageElement).addPageElements(
                        new TextPageElement((" - " + testParameter.getTestParameterType().getName())))); //$NON-NLS-1$

        testParameterPageElement.addPageElements(createKeyValueTableForTestPolicyCmptTypeParameter(testParameter));

        testParameterPageElement.addPageElements(createTestAttributeTable(testParameter));

        ITestPolicyCmptTypeParameter[] policyCmptTypeParamChilds = testParameter.getTestPolicyCmptTypeParamChilds();
        for (ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter : policyCmptTypeParamChilds) {
            testParameterPageElement.addPageElements(createTestPolicyCmptTypePageElement(testPolicyCmptTypeParameter));
        }

        return testParameterPageElement;
    }

    private PageElement createTestAttributeTable(ITestPolicyCmptTypeParameter testParameter) {

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK).addPageElements(TextPageElement
                .createParagraph(getContext().getMessage("TestCaseTypeContentPageElement_testAttributes")).addStyles( //$NON-NLS-1$
                        Style.BOLD));

        TestAttributesTablePageElement testAttributesTablePageElement = new TestAttributesTablePageElement(
                testParameter);
        if (testAttributesTablePageElement.isEmpty()) {
            return wrapper.addPageElements(TextPageElement.createParagraph(getContext().getMessage(
                    "TestCaseTypeContentPageElement_noTestAttributes"))); //$NON-NLS-1$
        }

        return wrapper.addPageElements(testAttributesTablePageElement);
    }

    private KeyValueTablePageElement createKeyValueTableForTestPolicyCmptTypeParameter(ITestPolicyCmptTypeParameter testParameter) {
        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_name"), //$NON-NLS-1$
                testParameter.getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_testParameterType"), //$NON-NLS-1$
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_requiresProductCmpt"), //$NON-NLS-1$
                testParameter.isRequiresProductCmpt() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_minInstances"), //$NON-NLS-1$
                Integer.toString(testParameter.getMinInstances()));
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_maxInstances"), //$NON-NLS-1$
                Integer.toString(testParameter.getMaxInstances()));
        return keyValueTable;
    }

    private PageElement createTestRuleParameterPageElement(TestRuleParameter testParameter) {
        String name = testParameter.getName() + " - " + testParameter.getTestParameterType().getName(); //$NON-NLS-1$
        TreeNodePageElement testParameterPageElement = createRootNode(name);

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_name"), //$NON-NLS-1$
                testParameter.getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_testParameterType"), //$NON-NLS-1$
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_description"), //$NON-NLS-1$
                getContext().getDescription(testParameter));

        testParameterPageElement.addPageElements(keyValueTable);
        return testParameterPageElement;
    }

    private PageElement createTestValueParameterPageElement(TestValueParameter testParameter) {
        String name = testParameter.getName() + " - " + testParameter.getTestParameterType().getName(); //$NON-NLS-1$
        TreeNodePageElement testParameterPageElement = createRootNode(name);

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_name"), //$NON-NLS-1$
                testParameter.getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_valueDatatype"), //$NON-NLS-1$
                testParameter.getValueDatatype());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_testParameterType"), //$NON-NLS-1$
                testParameter.getTestParameterType().getName());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseTypeContentPageElement_description"), //$NON-NLS-1$
                getContext().getDescription(testParameter));

        testParameter.getTestParameterType();

        testParameterPageElement.addPageElements(keyValueTable);
        return testParameterPageElement;
    }
}
