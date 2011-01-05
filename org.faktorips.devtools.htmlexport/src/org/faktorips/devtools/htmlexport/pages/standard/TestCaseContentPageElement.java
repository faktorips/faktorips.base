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
import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
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
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
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
     * @throws CoreException if the ITestCaseType for the ITestCase could not be found
     * 
     */
    protected TestCaseContentPageElement(ITestCase object, DocumentationContext context) throws CoreException {
        super(object, context);
        testCaseType = object.findTestCaseType(context.getIpsProject());
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
        addPageElements(new TextPageElement(getContext().getMessage("TestCaseContentPageElement_parameters"), //$NON-NLS-1$
                TextType.HEADING_2));
        TreeNodePageElement root = createRootNode();

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
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error creating PageElement for " + testObject.getName(), e)); //$NON-NLS-1$
        }

        return TextPageElement.createParagraph(testObject.getName() + " " + testObject.getClass()); //$NON-NLS-1$
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
            return new TextPageElement(getContext().getMessage("TestCaseContentPageElement_noTestAttributes")); //$NON-NLS-1$
        }

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
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
        TreeNodePageElement testObjectPageElement = createRootNode(testObject.getTestParameterName());

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseContentPageElement_name"), //$NON-NLS-1$
                testObject.getValidationRule());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseContentPageElement_violationType"), testObject //$NON-NLS-1$
                .getViolationType().getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
    }

    private PageElement createTestValuePageElement(ITestValue testObject) throws CoreException {
        TreeNodePageElement testObjectPageElement = new TreeNodePageElement(new WrapperPageElement(WrapperType.BLOCK)
                .addPageElements(new IpsElementImagePageElement(testObject)).addPageElements(
                        new TextPageElement(testObject.getTestParameterName())));

        KeyValueTablePageElement keyValueTable = new KeyValueTablePageElement(getContext());
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseContentPageElement_name"), testObject.getName()); //$NON-NLS-1$

        ITestValueParameter testValueParameter = testObject.findTestValueParameter(getContext().getIpsProject());
        ValueDatatype datatype = testValueParameter.findValueDatatype(getContext().getIpsProject());

        String value = testObject.getValue();
        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseContentPageElement_value"), IpsPlugin //$NON-NLS-1$
                .getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(datatype, value));

        keyValueTable.addKeyValueRow(getContext().getMessage("TestCaseContentPageElement_testParameterType"), //$NON-NLS-1$
                testValueParameter.getTestParameterType().getName());

        testObjectPageElement.addPageElements(keyValueTable);

        return testObjectPageElement;
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
            getContext().addStatus(
                    new IpsStatus(IStatus.WARNING, "Could not find image for " + getDocumentedIpsObject().getName())); //$NON-NLS-1$
        }
        wrapperPageElement.addPageElements(new TextPageElement(name));

        return new TreeNodePageElement(wrapperPageElement);
    }
}
