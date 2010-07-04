/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;

public class TestCaseTestCaseTypeDeltaTest extends AbstractIpsPluginTest {

    private ITestCase testCase;
    private ITestCaseType testCaseType;
    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "TestCaseType1");
        testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase1");
        testCase.setTestCaseType(testCaseType.getName());
    }

    public void testIsEmpty() throws CoreException {
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isEmpty());
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0);

        addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        delta = new TestCaseTestCaseTypeDelta(testCase, testCaseType);
        // root objects are not necessary
        assertTrue(delta.isEmpty());
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0);

        ITestValueParameter param = testCaseType.newInputTestValueParameter();
        param.setName("inputValue1");
        param.setDatatype(Datatype.STRING.getQualifiedName());
        delta = new TestCaseTestCaseTypeDelta(testCase, testCaseType);
        assertFalse(delta.isEmpty());
        assertDeltaContainer(delta, 0, 0, 0, 0, 1, 0, 0);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestValuesWithMissingTestValueParam() throws CoreException {
        ITestValue value = testCase.newTestValue();
        value.setTestValueParameter("TestValueParam1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 1, 0, 0, 0, 0, 0, 0);
        assertEquals(value, delta.getTestValuesWithMissingTestValueParam()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestRulesWithMissingTestValueParam() throws CoreException {
        ITestRule rule = testCase.newTestRule();
        rule.setTestRuleParameter("TestRuleParam1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0);
        assertEquals(rule, delta.getTestRulesWithMissingTestValueParam()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());

        // no delta
        TestRuleParameter param = testCaseType.newExpectedResultRuleParameter();
        param.setName("TestRuleParam1");

        rule = testCase.newTestRule();
        rule.setTestRuleParameter("TestRuleParam1");

        delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public void testGetTestPolicyCmptsWithMissingTypeParam() throws CoreException {
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 1, 0, 0, 0, 0, 0);
        assertEquals(cmpt, delta.getTestPolicyCmptsWithMissingTypeParam()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestPolicyCmptAssociationsWithMissingTypeParam() throws CoreException {
        addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestPolicyCmptLink link = cmpt.newTestPolicyCmptLink();
        link.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 1, 0, 0, 0, 0);
        assertEquals(link, delta.getTestPolicyCmptLinkWithMissingTypeParam()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestAttributeValuesWithMissingTestAttribute() throws CoreException {
        ITestPolicyCmptTypeParameter param = addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttribute attribute = param.newInputTestAttribute();
        attribute.setName("Attribute1");
        attribute.setAttribute("Attribute1");
        attribute = param.newInputTestAttribute();
        attribute.setName("Attribute2");
        attribute.setAttribute("Attribute2");

        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttributeValue attrValue = cmpt.newTestAttributeValue();
        attrValue.setTestAttribute("Attribute3");
        cmpt.newTestAttributeValue().setTestAttribute("Attribute1");
        cmpt.newTestAttributeValue().setTestAttribute("Attribute2");

        IPolicyCmptType type = param.findPolicyCmptType(ipsProject);
        type.newPolicyCmptTypeAttribute().setName("Attribute1");
        type.newPolicyCmptTypeAttribute().setName("Attribute2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
        assertEquals(attrValue, delta.getTestAttributeValuesWithMissingTestAttribute()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestValueParametersWithMissingTestValue() throws CoreException {
        ITestValueParameter testValueParam1 = testCaseType.newInputTestValueParameter();
        testValueParam1.setName("TestValueParam1");
        testValueParam1.setDatatype("String");
        ITestValueParameter testValueParam2 = testCaseType.newInputTestValueParameter();
        testValueParam2.setName("TestValueParam2");
        testValueParam2.setDatatype("String");

        ITestValue value = testCase.newTestValue();
        value.setTestValueParameter("TestValueParam2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 1, 0, 0);
        assertEquals(testValueParam1, delta.getTestValueParametersWithMissingTestValue()[0]);
        assertTrue(delta.isDifferentTestParameterOrder());

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt() throws CoreException {
        ITestPolicyCmptTypeParameter param = addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttribute testAttribute1 = param.newInputTestAttribute();
        testAttribute1.setName("TestAttribute1");
        testAttribute1.setAttribute("TestAttribute1");
        ITestAttribute testAttribute2 = param.newInputTestAttribute();
        testAttribute2.setName("TestAttribute2");
        testAttribute2.setAttribute("TestAttribute2");

        IPolicyCmptType policyCmptType = param.findPolicyCmptType(ipsProject);
        policyCmptType.newPolicyCmptTypeAttribute().setName("TestAttribute1");
        policyCmptType.newPolicyCmptTypeAttribute().setName("TestAttribute2");

        addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam2");

        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestAttributesWithMissingTestAttributeValue() throws CoreException {
        ITestPolicyCmptTypeParameter param = addNewPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttribute testAttribute1 = param.newInputTestAttribute();
        testAttribute1.setName("Attribute1");
        testAttribute1.setAttribute("Attribute1");

        ITestAttribute testAttr = param.newInputTestAttribute();
        testAttr.setName("Attribute2");
        testAttr.setAttribute("Attribute2");

        IPolicyCmptType policyCmptType = param.findPolicyCmptType(ipsProject);
        policyCmptType.newPolicyCmptTypeAttribute().setName("Attribute1");
        policyCmptType.newPolicyCmptTypeAttribute().setName("Attribute2");

        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttributeValue attrValue = cmpt.newTestAttributeValue();
        attrValue.setTestAttribute("Attribute1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1);
        assertEquals(testAttr, delta.getTestAttributesWithMissingTestAttributeValue()[0]);

        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    /**
     * Test with complex content of test case type and test case
     */
    public void testComplex() throws CoreException {
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        ITestPolicyCmptTypeParameter param1 = addNewPolicyCmptTypeParameter("TestParam1");
        param1.setTestParameterType(TestParameterType.COMBINED);
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        ITestPolicyCmptTypeParameter param2 = addNewPolicyCmptTypeParameter("TestParam2");
        ITestAttribute testAttribute = param2.newInputTestAttribute();
        testAttribute.setName("InputAttribute1");
        testAttribute.setAttribute("InputAttribute1");

        IPolicyCmptType policyCmptType = param2.findPolicyCmptType(ipsProject);
        policyCmptType.newPolicyCmptTypeAttribute().setName("InputAttribute1");

        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        // Child of param1
        IPolicyCmptType childPolicyCmptType = newPolicyCmptType(ipsProject, "childPolicyCmptType");
        childPolicyCmptType.newPolicyCmptTypeAttribute().setName("InputAttribute1");
        childPolicyCmptType.newPolicyCmptTypeAttribute().setName("InputAttribute2");
        childPolicyCmptType.newPolicyCmptTypeAttribute().setName("ExpResultAttribute1");
        ITestPolicyCmptTypeParameter childParam = param1.newTestPolicyCmptTypeParamChild();
        childParam.setAssociation("Association1");
        childParam.setName("TestParamChild1");
        childParam.setPolicyCmptType(childPolicyCmptType.getQualifiedName());

        policyCmptType = param1.findPolicyCmptType(ipsProject);
        IPolicyCmptTypeAssociation link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Association1");
        link.setTarget(childPolicyCmptType.getQualifiedName());

        ITestAttribute testAttribute2 = childParam.newInputTestAttribute();
        testAttribute2.setName("InputAttribute1");
        testAttribute2.setAttribute("InputAttribute1");
        ITestAttribute testAttribute3 = childParam.newInputTestAttribute();
        testAttribute3.setName("InputAttribute2");
        testAttribute3.setAttribute("InputAttribute2");
        ITestAttribute testAttribute4 = childParam.newExpectedResultTestAttribute();
        testAttribute4.setName("ExpResultAttribute1");
        testAttribute4.setAttribute("ExpResultAttribute1");

        IPolicyCmptType policyCmptType2 = childParam.findPolicyCmptType(ipsProject);
        policyCmptType2.newPolicyCmptTypeAttribute().setName("InputAttribute1");
        policyCmptType2.newPolicyCmptTypeAttribute().setName("InputAttribute2");
        policyCmptType2.newPolicyCmptTypeAttribute().setName("ExpResultAttribute1");

        // missing childs are not checked in the delta
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);

        // Param1
        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("TestParamX");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 1, 0, 0, 0, 0, 0);
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("TestParam1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        // Param2
        ITestPolicyCmpt testPolicyCmpt2 = testCase.newTestPolicyCmpt();
        testPolicyCmpt2.setTestPolicyCmptTypeParameter("TestParam2");
        // one missing test attribute value and one test policy wrong sort order
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 1, 0, 0, 1);
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute2");
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute3");

        // two missing test attributes and
        // one wrong order of test attr (because test attr on same test policy cmpt)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 2, 0, 0, 0, 0, 0, 1);
        ITestPolicyCmptLink rel2 = testPolicyCmpt2.newTestPolicyCmptLink();
        rel2.setTestPolicyCmptTypeParameter("AssociationX");
        ITestPolicyCmpt child2 = rel2.newTargetTestPolicyCmptChild();
        child2.setTestPolicyCmptTypeParameter("TestParamChildX");
        child2.newTestAttributeValue().setTestAttribute("TestAttributeX");
        // one missing test policy cmpt link and
        // two missing test attributes and
        // one wrong order of test attr (because test attr on same test policy cmpt)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 0, 0, 0, 1);
        // Child of param1
        ITestPolicyCmptLink rel1 = testPolicyCmpt1.newTestPolicyCmptLink();
        rel1.setTestPolicyCmptTypeParameter("Association1");
        rel1.setTestPolicyCmptTypeParameter("TestParamChild1");
        ITestPolicyCmpt child1 = rel1.newTargetTestPolicyCmptChild();
        child1.setTestPolicyCmptTypeParameter("TestParamChild1");
        // one missing test policy cmpt link and
        // two missing test attributes
        // three missing test attributes value (the new child TestParamChild1 has three test
        // attributes)
        // two wrong order of test attr (one see below and one from the newly
        // created TestParamChild1, three missing test attr values but all from
        // the same test policy cmpt)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 3, 0, 0, 2);
        child1.newTestAttributeValue().setTestAttribute("InputAttribute1");
        child1.newTestAttributeValue().setTestAttribute("InputAttribute2");
        // see below but two test attibute values are created
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 1, 0, 0, 2);
        child1.newTestAttributeValue().setTestAttribute("ExpResultAttribute1");
        // see below but no more missing test attibute values, therefore only on left sort order of
        // test attributes
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 0, 0, 0, 1);
        child1.newTestAttributeValue().setTestAttribute("ExpResultAttributeX");
        // one missing test policy cmpt link and
        // three missing test attributes (one more for ExpResultAttributeX)
        // two wrong order of test attr (one see below and one from the newly
        // created ExpResultAttributeX)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 3, 0, 0, 0, 0, 0, 2);
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testDifferentSortOrderTestPolicyCmpt() throws CoreException {
        addNewPolicyCmptTypeParameter("a1");
        addNewPolicyCmptTypeParameter("a2");
        addNewPolicyCmptTypeParameter("a3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("a1");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("a3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("a2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderTestValue() throws CoreException {
        ITestValueParameter parameter = testCaseType.newInputTestValueParameter();
        parameter.setName("a1");
        parameter.setDatatype("String");
        ITestValueParameter parameter2 = testCaseType.newInputTestValueParameter();
        parameter2.setName("a2");
        parameter2.setDatatype("String");
        ITestValueParameter parameter3 = testCaseType.newInputTestValueParameter();
        parameter3.setName("a3");
        parameter3.setDatatype("String");

        testCase.newTestValue().setTestValueParameter("a1");
        testCase.newTestValue().setTestValueParameter("a3");
        testCase.newTestValue().setTestValueParameter("a2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderTestRule() throws CoreException {
        testCaseType.newExpectedResultRuleParameter().setName("a1");
        testCaseType.newExpectedResultRuleParameter().setName("a2");
        testCaseType.newExpectedResultRuleParameter().setName("a3");
        testCase.newTestRule().setTestRuleParameter("a1");
        testCase.newTestRule().setTestRuleParameter("a3");
        testCase.newTestRule().setTestRuleParameter("a2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testSameSortOrderTestRule() throws CoreException {
        testCaseType.newExpectedResultRuleParameter().setName("a1");
        testCaseType.newExpectedResultRuleParameter().setName("a2");
        testCaseType.newExpectedResultRuleParameter().setName("a3");
        testCase.newTestRule().setTestRuleParameter("a1");
        testCase.newTestRule().setTestRuleParameter("a2");
        testCase.newTestRule().setTestRuleParameter("a3");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderTestRuleSameRuleParam() throws CoreException {
        ITestValueParameter parameter = testCaseType.newInputTestValueParameter();
        parameter.setName("Value1");
        parameter.setDatatype("String");
        testCaseType.newExpectedResultRuleParameter().setName("a1");
        addNewPolicyCmptTypeParameter("PolicyCmpt1");

        testCase.newTestValue().setTestValueParameter("Value1");
        ITestRule testRule1 = testCase.newTestRule();
        testRule1.setTestRuleParameter("a1");
        testRule1.setValidationRule("rule1");
        ITestRule testRule2 = testCase.newTestRule();
        testRule2.setTestRuleParameter("a1");
        testRule2.setValidationRule("rule2");
        ITestRule testRule3 = testCase.newTestRule();
        testRule3.setTestRuleParameter("a1");
        testRule3.setValidationRule("rule3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("PolicyCmpt1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderMixed() throws CoreException {
        testCaseType.newExpectedResultRuleParameter().setName("a0");
        ITestValueParameter parameter = testCaseType.newInputTestValueParameter();
        parameter.setName("a1");
        parameter.setDatatype("String");
        ITestValueParameter parameter2 = testCaseType.newInputTestValueParameter();
        parameter2.setName("a2");
        parameter2.setDatatype("String");
        addNewPolicyCmptTypeParameter("a3");
        testCaseType.newExpectedResultRuleParameter().setName("a4");

        testCase.newTestRule().setTestRuleParameter("a4");
        testCase.newTestValue().setTestValueParameter("a1");
        ITestRule rule0 = testCase.newTestRule();
        rule0.setTestRuleParameter("a0");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("a3");
        testCase.newTestValue().setTestValueParameter("a2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);

        rule0.delete();
        // rules are optional
        delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        // readd rule 0 to the end
        testCase.newTestRule().setTestRuleParameter("a0");
        delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        // no specific object is wrong
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testEqualSortOrderChilds() throws CoreException {
        ITestPolicyCmptTypeParameter param1 = addNewPolicyCmptTypeParameter("Param1");
        param1.setTestParameterType(TestParameterType.COMBINED);
        IPolicyCmptType policyCmptType = param1.findPolicyCmptType(ipsProject);
        IPolicyCmptType childPolicyCmpt1 = newPolicyCmptType(ipsProject, "Child1");
        IPolicyCmptType childPolicyCmpt2 = newPolicyCmptType(ipsProject, "Child2");
        IPolicyCmptType childPolicyCmpt3 = newPolicyCmptType(ipsProject, "Child3");
        IPolicyCmptTypeAssociation link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child1");
        link.setTarget(childPolicyCmpt1.getQualifiedName());
        link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child2");
        link.setTarget(childPolicyCmpt2.getQualifiedName());
        link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child3");
        link.setTarget(childPolicyCmpt3.getQualifiedName());
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setAssociation("Child1");
        child.setPolicyCmptType(childPolicyCmpt1.getQualifiedName());
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child2");
        child.setAssociation("Child2");
        child.setPolicyCmptType(childPolicyCmpt2.getQualifiedName());
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child3");
        child.setAssociation("Child3");
        child.setPolicyCmptType(childPolicyCmpt3.getQualifiedName());

        ITestPolicyCmpt cmpt1 = testCase.newTestPolicyCmpt();
        cmpt1.setTestPolicyCmptTypeParameter("Param1");
        ITestPolicyCmptLink rel1 = cmpt1.newTestPolicyCmptLink();
        rel1.setTestPolicyCmptTypeParameter("Child1");
        rel1.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmptLink rel2 = cmpt1.newTestPolicyCmptLink();
        rel2.setTestPolicyCmptTypeParameter("Child2");
        rel2.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child2");
        ITestPolicyCmptLink rel3 = cmpt1.newTestPolicyCmptLink();
        rel3.setTestPolicyCmptTypeParameter("Child3");
        rel3.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child3");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderChilds() throws CoreException {
        ITestPolicyCmptTypeParameter param1 = addNewPolicyCmptTypeParameter("Param1");
        param1.setTestParameterType(TestParameterType.COMBINED);
        IPolicyCmptType policyCmptType = param1.findPolicyCmptType(ipsProject);
        IPolicyCmptType childPolicyCmpt1 = newPolicyCmptType(ipsProject, "Child1");
        IPolicyCmptType childPolicyCmpt2 = newPolicyCmptType(ipsProject, "Child2");
        IPolicyCmptType childPolicyCmpt3 = newPolicyCmptType(ipsProject, "Child3");
        IPolicyCmptTypeAssociation link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child1");
        link.setTarget(childPolicyCmpt1.getQualifiedName());
        link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child2");
        link.setTarget(childPolicyCmpt2.getQualifiedName());
        link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTargetRoleSingular("Child3");
        link.setTarget(childPolicyCmpt3.getQualifiedName());
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setAssociation("Child1");
        child.setPolicyCmptType(childPolicyCmpt1.getQualifiedName());
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child2");
        child.setAssociation("Child2");
        child.setPolicyCmptType(childPolicyCmpt2.getQualifiedName());
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child3");
        child.setAssociation("Child3");
        child.setPolicyCmptType(childPolicyCmpt3.getQualifiedName());

        ITestPolicyCmpt cmpt1 = testCase.newTestPolicyCmpt();
        cmpt1.setTestPolicyCmptTypeParameter("Param1");
        ITestPolicyCmptLink rel1 = cmpt1.newTestPolicyCmptLink();
        rel1.setTestPolicyCmptTypeParameter("Child1");
        rel1.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmptLink rel2 = cmpt1.newTestPolicyCmptLink();
        rel2.setTestPolicyCmptTypeParameter("Child3");
        rel2.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child3");
        ITestPolicyCmptLink rel3 = cmpt1.newTestPolicyCmptLink();
        rel3.setTestPolicyCmptTypeParameter("Child2");
        rel3.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
        fixAndAssert(delta);
    }

    public void testDifferentSortOrderChildOfChilds() throws CoreException {
        ITestPolicyCmptTypeParameter param1 = addNewPolicyCmptTypeParameter("Param1");
        param1.setTestParameterType(TestParameterType.COMBINED);

        IPolicyCmptType childPcType1 = newPolicyCmptType(ipsProject, "Child1");
        IPolicyCmptType childPcType2 = newPolicyCmptType(ipsProject, "Child2");

        IPolicyCmptType policyCmptType = param1.findPolicyCmptType(ipsProject);
        IPolicyCmptTypeAssociation rela1 = policyCmptType.newPolicyCmptTypeAssociation();
        rela1.setTarget(childPcType1.getQualifiedName());
        rela1.setTargetRoleSingular("Child1");
        IPolicyCmptTypeAssociation rela2 = policyCmptType.newPolicyCmptTypeAssociation();
        rela2.setTarget(childPcType2.getQualifiedName());
        rela2.setTargetRoleSingular("Child2");

        IPolicyCmptType childPcType10 = newPolicyCmptType(ipsProject, "Child1Child0");
        IPolicyCmptType childPcType11 = newPolicyCmptType(ipsProject, "Child1Child1");
        IPolicyCmptType childPcType12 = newPolicyCmptType(ipsProject, "Child1Child2");
        IPolicyCmptTypeAssociation rela10 = childPcType1.newPolicyCmptTypeAssociation();
        rela10.setTarget(childPcType10.getQualifiedName());
        rela10.setTargetRoleSingular("Child1Child0");
        IPolicyCmptTypeAssociation rela11 = childPcType1.newPolicyCmptTypeAssociation();
        rela11.setTarget(childPcType11.getQualifiedName());
        rela11.setTargetRoleSingular("Child1Child1");
        IPolicyCmptTypeAssociation rela12 = childPcType1.newPolicyCmptTypeAssociation();
        rela12.setTarget(childPcType12.getQualifiedName());
        rela12.setTargetRoleSingular("Child1Child2");

        // child 1
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setAssociation("Child1");
        child.setPolicyCmptType("Child1");
        // child 2
        ITestPolicyCmptTypeParameter child2 = param1.newTestPolicyCmptTypeParamChild();
        child2.setName("Child2");
        child2.setAssociation("Child2");
        child2.setPolicyCmptType("Child2");

        // child0 of child1
        ITestPolicyCmptTypeParameter childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child0");
        childchild.setAssociation("Child1Child0");
        childchild.setPolicyCmptType("Child1Child0");
        // child1 of child1
        childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child1");
        childchild.setAssociation("Child1Child1");
        childchild.setPolicyCmptType("Child1Child1");
        // child2 of child1
        childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child2");
        childchild.setAssociation("Child1Child2");
        childchild.setPolicyCmptType("Child1Child2");

        // test case side
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("Param1");
        // child11
        ITestPolicyCmptLink rel = cmpt.newTestPolicyCmptLink();
        rel.setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmpt cmptChild = rel.newTargetTestPolicyCmptChild();
        cmptChild.setTestPolicyCmptTypeParameter("Child1");

        // child2
        ITestPolicyCmptLink rel2 = cmpt.newTestPolicyCmptLink();
        rel2.setTestPolicyCmptTypeParameter("Child2");
        ITestPolicyCmpt cmptChild2 = rel2.newTargetTestPolicyCmptChild();
        cmptChild2.setTestPolicyCmptTypeParameter("Child2");

        // child1 of child1 #1
        rel = cmptChild.newTestPolicyCmptLink();
        rel.setTestPolicyCmptTypeParameter("Child1Child2");
        rel.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1Child2");
        // child1 of child1 #2
        rel = cmptChild.newTestPolicyCmptLink();
        rel.setTestPolicyCmptTypeParameter("Child1Child2");
        rel.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1Child2");
        // child2 of child1 (wrong order)
        rel = cmptChild.newTestPolicyCmptLink();
        rel.setTestPolicyCmptTypeParameter("Child1Child1");
        rel.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1Child1");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
        fixAndAssert(delta);
    }

    /**
     * Test - if there are more test policy cmpt of the same instance - that new test attributes are
     * created for each instance
     */
    public void testNewAttributeForEqualInstances() throws CoreException {
        // test case type side
        IPolicyCmptType childPolicyCmptType = newPolicyCmptType(ipsProject, "Child");
        childPolicyCmptType.newPolicyCmptTypeAttribute().setName("TestAttribute1");
        childPolicyCmptType.newPolicyCmptTypeAttribute().setName("TestAttribute2");
        ITestPolicyCmptTypeParameter param = addNewPolicyCmptTypeParameter("Param");
        param.setTestParameterType(TestParameterType.COMBINED);
        IPolicyCmptType policyCmptType = param.findPolicyCmptType(ipsProject);
        IPolicyCmptTypeAssociation link = policyCmptType.newPolicyCmptTypeAssociation();
        link.setTarget(childPolicyCmptType.getQualifiedName());
        link.setTargetRoleSingular("link");
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setPolicyCmptType(childPolicyCmptType.getQualifiedName());
        paramChild.setName("ParamChild");
        paramChild.setAssociation("link");
        ITestAttribute attribute = paramChild.newInputTestAttribute();
        attribute.setName("TestAttribute1");
        attribute.setAttribute("TestAttribute1");
        ITestAttribute attribute2 = paramChild.newInputTestAttribute();
        attribute2.setName("TestAttribute2");
        attribute2.setAttribute("TestAttribute2");

        // test case side
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter(param.getName());
        ITestPolicyCmpt cmptChild1 = cmpt.newTestPolicyCmptLink().newTargetTestPolicyCmptChild();
        cmptChild1.setTestPolicyCmptTypeParameter(paramChild.getName());
        ((ITestPolicyCmptLink)cmptChild1.getParent()).setTestPolicyCmptTypeParameter(paramChild.getName());
        ITestPolicyCmpt cmptChild2 = cmpt.newTestPolicyCmptLink().newTargetTestPolicyCmptChild();
        cmptChild2.setTestPolicyCmptTypeParameter(paramChild.getName());
        ((ITestPolicyCmptLink)cmptChild2.getParent()).setTestPolicyCmptTypeParameter(paramChild.getName());

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isEmpty());
        // a test attribute will only added once if more instances of the same test parameter are in
        // the test case
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 2, 0, 0, 2);
        fixAndAssert(delta);
    }

    private void assertDeltaContainer(ITestCaseTestCaseTypeDelta delta,
            int testValuesWithMissingTestValueParam,
            int testPolicyCmptsWithMissingTypeParam,
            int testPolicyCmptAssociationsWithMissingTypeParam,
            int testAttributeValuesWithMissingTestAttribute,
            int testValueParametersWithMissingTestValue,
            int testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
            int testAttributesWithMissingTestAttributeValue) {

        assertDeltaContainer(delta, testValuesWithMissingTestValueParam, testPolicyCmptsWithMissingTypeParam,
                testPolicyCmptAssociationsWithMissingTypeParam, testAttributeValuesWithMissingTestAttribute,
                testValueParametersWithMissingTestValue, testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
                testAttributesWithMissingTestAttributeValue, 0, 0, 0);
    }

    private void assertDeltaContainer(ITestCaseTestCaseTypeDelta delta,
            int testValuesWithMissingTestValueParam,
            int testPolicyCmptsWithMissingTypeParam,
            int testPolicyCmptAssociationsWithMissingTypeParam,
            int testAttributeValuesWithMissingTestAttribute,
            int testValueParametersWithMissingTestValue,
            int testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
            int testAttributesWithMissingTestAttributeValue,
            int testPolicyCmptWithDifferentSortOrder,
            int testRulesWithMissingTestValueParam,
            int testPolicyCmptWithDifferentSortOrderTestAttr) {

        assertEquals(testValuesWithMissingTestValueParam, delta.getTestValuesWithMissingTestValueParam().length);
        assertEquals(testPolicyCmptsWithMissingTypeParam, delta.getTestPolicyCmptsWithMissingTypeParam().length);
        assertEquals(testPolicyCmptAssociationsWithMissingTypeParam,
                delta.getTestPolicyCmptLinkWithMissingTypeParam().length);
        assertEquals(testAttributeValuesWithMissingTestAttribute, delta
                .getTestAttributeValuesWithMissingTestAttribute().length);

        assertEquals(testValueParametersWithMissingTestValue, delta.getTestValueParametersWithMissingTestValue().length);
        assertEquals(testPolicyCmptTypeParametersWithMissingTestPolicyCmpt, delta
                .getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt().length);
        assertEquals(testAttributesWithMissingTestAttributeValue, delta
                .getTestAttributesWithMissingTestAttributeValue().length);
        assertEquals(testPolicyCmptWithDifferentSortOrder, delta.getTestPolicyCmptWithDifferentSortOrder().length);
        assertEquals(testRulesWithMissingTestValueParam, delta.getTestRulesWithMissingTestValueParam().length);
        assertEquals(testPolicyCmptWithDifferentSortOrderTestAttr, delta
                .getTestPolicyCmptWithDifferentSortOrderTestAttr().length);
    }

    /**
     * Fixes the delta and assert that there is no new delta between the test case and test case
     * type.
     */
    private void fixAndAssert(ITestCaseTestCaseTypeDelta delta) throws CoreException {
        testCase.fixDifferences(delta);
        ITestCaseTestCaseTypeDelta newDelta = testCase.computeDeltaToTestCaseType();
        assertFalse(newDelta.isDifferentTestParameterOrder());
        assertTrue(newDelta.isEmpty());
        assertDeltaContainer(newDelta, 0, 0, 0, 0, 0, 0, 0);
    }

    public void testDifferentSortOrderAttributes() throws CoreException {
        newPolicyCmptType(ipsProject, "policyCmpt");
        ITestPolicyCmptTypeParameter param1 = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        param1.setName("policyCmpt");
        param1.setPolicyCmptType("policyCmpt");

        ITestAttribute attribute = param1.newInputTestAttribute();
        attribute.setName("a1");
        attribute.setAttribute("a1");
        attribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ITestAttribute attribute2 = param1.newInputTestAttribute();
        attribute2.setName("a2");
        attribute2.setAttribute("a2");
        attribute2.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ITestAttribute attribute3 = param1.newInputTestAttribute();
        attribute3.setName("a3");
        attribute3.setAttribute("a3");
        attribute3.setTestAttributeType(TestParameterType.EXPECTED_RESULT);

        IPolicyCmptType policyCmptType = param1.findPolicyCmptType(ipsProject);
        policyCmptType.newPolicyCmptTypeAttribute().setName("a1");
        policyCmptType.newPolicyCmptTypeAttribute().setName("a2");
        policyCmptType.newPolicyCmptTypeAttribute().setName("a3");

        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setTestPolicyCmptTypeParameter("policyCmpt");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("a1");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("a3");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("a2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
        fixAndAssert(delta);
    }

    private ITestPolicyCmptTypeParameter addNewPolicyCmptTypeParameter(String name) throws CoreException {
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName(name);
        IPolicyCmptType testPolicyCmptType = newPolicyCmptType(ipsProject, name);
        param.setPolicyCmptType(testPolicyCmptType.getQualifiedName());
        return param;
    }
}
