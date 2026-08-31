/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link TestCaseDetailArea#storeActualValueInExpResult(String, String, String)} also
 * updates the model if the affected test attribute value or test value is currently hidden by the
 * input/expected-result content filter of the {@link TestCaseSection} and therefore has no
 * {@link EditField}.
 */
public class TestCaseDetailAreaStoreActualValueTest extends AbstractIpsPluginTest {

    private static final String ROOT_PARAMETER_NAME = "policyCmpt1";
    private static final String ATTRIBUTE_NAME = "attr1";
    private static final String VALUE_PARAMETER_NAME = "value1";

    private ITestPolicyCmpt testPolicyCmpt;
    private ITestAttributeValue attributeValue;
    private ITestValue expectedResultTestValue;

    private TestCaseSection testCaseSection;
    private TestCaseDetailArea testCaseDetailArea;

    private String attributeUniqueKey;
    private String testValueUniqueKey;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "TestCaseType");
        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase");
        testCase.setTestCaseType(testCaseType.getQualifiedName());

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "PolicyCmpt");

        ITestPolicyCmptTypeParameter rootParameter = testCaseType.newInputTestPolicyCmptTypeParameter();
        rootParameter.setName(ROOT_PARAMETER_NAME);
        rootParameter.setPolicyCmptType(policyCmptType.getQualifiedName());

        ITestAttribute expectedResultAttribute = rootParameter.newExpectedResultTestAttribute();
        expectedResultAttribute.setName(ATTRIBUTE_NAME);
        expectedResultAttribute.setDatatype("String");

        testCaseType.newExpectedResultValueParameter().setName(VALUE_PARAMETER_NAME);

        testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setTestPolicyCmptTypeParameter(ROOT_PARAMETER_NAME);
        testPolicyCmpt.setName(ROOT_PARAMETER_NAME);

        attributeValue = testPolicyCmpt.newTestAttributeValue();
        attributeValue.setTestAttribute(ATTRIBUTE_NAME);
        attributeValue.setValue("initialAttributeValue");

        expectedResultTestValue = testCase.newTestValue();
        expectedResultTestValue.setTestValueParameter(VALUE_PARAMETER_NAME);
        expectedResultTestValue.setValue("initialTestValue");

        IEditorPart editor = IpsUIPlugin.getDefault().openEditor(testCase);
        TestCaseEditor testCaseEditor = (TestCaseEditor)editor;
        testCaseSection = testCaseEditor.getEditorPage().getSection();
        testCaseDetailArea = testCaseSection.getTestCaseDetailArea();

        // re-fetch the objects
        testCase = testCaseSection.getContentProvider().getTestCase();
        testPolicyCmpt = testCase.getTestPolicyCmpts()[0];
        attributeValue = testPolicyCmpt.getTestAttributeValues()[0];
        expectedResultTestValue = testCase.getTestValues()[0];

        attributeUniqueKey = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testPolicyCmpt)
                + attributeValue.getTestAttribute();
        testValueUniqueKey = testCaseSection.getUniqueKey(expectedResultTestValue);
    }

    /**
     * The test attribute value is an expected-result attribute nested in an input test policy
     * component. If the content filter is set to {@link TestCaseContentProvider#INPUT} no
     * {@link EditField} is created for it, but the actual value must still be stored on the model.
     */
    @Test
    public void testStoreActualValueInExpResult_AttributeValueHiddenByContentFilter() {
        showOnlyWithContentType(TestCaseContentProvider.INPUT);
        assertThat(testCaseDetailArea.getEditField(attributeUniqueKey), nullValue());

        boolean stored = testCaseDetailArea.storeActualValueInExpResult(attributeUniqueKey, "actualAttributeValue",
                "message");

        assertThat(stored, is(true));
        assertThat(attributeValue.getValue(), is("actualAttributeValue"));
    }

    /**
     * The test value is an expected-result value. If the content filter is set to
     * {@link TestCaseContentProvider#INPUT} no {@link EditField} is created for it, but the actual
     * value must still be stored on the model.
     */
    @Test
    public void testStoreActualValueInExpResult_TestValueHiddenByContentFilter() {
        showOnlyWithContentType(TestCaseContentProvider.INPUT);
        assertThat(testCaseDetailArea.getEditField(testValueUniqueKey), nullValue());

        boolean stored = testCaseDetailArea.storeActualValueInExpResult(testValueUniqueKey, "actualTestValue",
                "message");

        assertThat(stored, is(true));
        assertThat(expectedResultTestValue.getValue(), is("actualTestValue"));
    }

    /**
     * Regression guard for the pre-existing behavior: if an {@link EditField} is visible (content
     * filter {@link TestCaseContentProvider#COMBINED}) the value is still stored via the edit
     * field/model object mapping.
     */
    @Test
    public void testStoreActualValueInExpResult_AttributeValueVisible() {
        showOnlyWithContentType(TestCaseContentProvider.COMBINED);
        assertThat(testCaseDetailArea.getEditField(attributeUniqueKey), notNullValue());

        boolean stored = testCaseDetailArea.storeActualValueInExpResult(attributeUniqueKey, "actualAttributeValue",
                "message");

        assertThat(stored, is(true));
        assertThat(attributeValue.getValue(), is("actualAttributeValue"));
    }

    @Test
    public void testStoreActualValueInExpResult_TestValueVisible() {
        showOnlyWithContentType(TestCaseContentProvider.COMBINED);
        assertThat(testCaseDetailArea.getEditField(testValueUniqueKey), notNullValue());

        boolean stored = testCaseDetailArea.storeActualValueInExpResult(testValueUniqueKey, "actualTestValue",
                "message");

        assertThat(stored, is(true));
        assertThat(expectedResultTestValue.getValue(), is("actualTestValue"));
    }

    /**
     * Neither an edit field nor a model object exists for a key that matches nothing - the method
     * must not throw and must indicate that nothing was stored.
     */
    @Test
    public void testStoreActualValueInExpResult_KeyNotFound() {
        showOnlyWithContentType(TestCaseContentProvider.COMBINED);

        boolean stored = testCaseDetailArea.storeActualValueInExpResult("doesNotExist", "actualValue", "message");

        assertThat(stored, is(false));
    }

    private void showOnlyWithContentType(int contentType) {
        testCaseSection.getContentProvider().setContentType(contentType);
        testCaseDetailArea.clearDetailArea();
        testCaseDetailArea.createTestObjectSections(List.<ITestObject> of(testPolicyCmpt, expectedResultTestValue));
    }

}
