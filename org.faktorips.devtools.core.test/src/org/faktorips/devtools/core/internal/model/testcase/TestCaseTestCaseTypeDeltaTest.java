/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;

public class TestCaseTestCaseTypeDeltaTest extends AbstractIpsPluginTest {
    private ITestCase testCase;
    private ITestCaseType testCaseType;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = super.newIpsProject("TestProject");
        testCaseType = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "TestCaseType1");
        testCase = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase1");
        testCase.setTestCaseType(testCaseType.getName());
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.testcase.ITestCaseTestCaseTypeDelta.isEmpty()'
     */
    public void testIsEmpty() throws CoreException {
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isEmpty());
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 0, 0);
        
        // negative test
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName("TestPolicyCmptTypeParam1");
        delta = new TestCaseTestCaseTypeDelta(testCase, testCaseType);
        assertFalse(delta.isEmpty());
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 1, 0);
        
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    //
    // Missing test case type side objects
    //
    
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
    
    public void testGetTestPolicyCmptRelationsWithMissingTypeParam() throws CoreException {
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("TestPolicyCmptTypeParam1");
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestPolicyCmptRelation relation = cmpt.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam2");
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 1, 0, 0, 0, 0);
        assertEquals(relation, delta.getTestPolicyCmptRelationsWithMissingTypeParam()[0]);
        
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }
    
    public void testGetTestAttributeValuesWithMissingTestAttribute() throws CoreException {
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName("TestPolicyCmptTypeParam1");
        param.newInputTestAttribute().setName("Attribute1");
        param.newInputTestAttribute().setName("Attribute2");
        
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam1");
        ITestAttributeValue attrValue = cmpt.newTestAttributeValue();
        attrValue.setTestAttribute("Attribute3");
        cmpt.newTestAttributeValue().setTestAttribute("Attribute1");
        cmpt.newTestAttributeValue().setTestAttribute("Attribute2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
        assertEquals(attrValue, delta.getTestAttributeValuesWithMissingTestAttribute()[0]);
        
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    //
    // Missing test case type side objects
    //
    
    public void testGetTestValueParametersWithMissingTestValue() throws CoreException {
        ITestValueParameter testValueParam = testCaseType.newInputTestValueParameter();
        testValueParam.setName("TestValueParam1");
        testCaseType.newInputTestValueParameter().setName("TestValueParam2");

        testCase.newTestValue().setTestValueParameter("TestValueParam2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 1, 0, 0);
        assertEquals(testValueParam, delta.getTestValueParametersWithMissingTestValue()[0]);
        assertTrue(delta.isDifferentTestParameterOrder());
        
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }

    public void testGetTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt() throws CoreException {
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName("TestPolicyCmptTypeParam1");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("TestPolicyCmptTypeParam2");
        
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("TestPolicyCmptTypeParam2");
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertDeltaContainer(delta, 0, 0, 0, 0, 0, 1, 0);
        assertEquals(param, delta.getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt()[0]);
        assertTrue(delta.isDifferentTestParameterOrder());
        
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }
    
    public void testGetTestAttributesWithMissingTestAttributeValue() throws CoreException {
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName("TestPolicyCmptTypeParam1");
        param.newInputTestAttribute().setName("Attribute1");
        ITestAttribute testAttr = param.newInputTestAttribute();
        testAttr.setName("Attribute2");
        
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
    public void testComplex() throws CoreException{
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("TestParam1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 1, 0);
        ITestPolicyCmptTypeParameter param2 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param2.setName("TestParam2");
        param2.newInputTestAttribute().setName("InputAttribute1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 2, 0);
        // Child of param1
        ITestPolicyCmptTypeParameter childParam = param1.newTestPolicyCmptTypeParamChild();
        childParam.setRelation("Relation1");
        childParam.setName("TestParamChild1");
        childParam.newInputTestAttribute().setName("InputAttribute1");
        childParam.newInputTestAttribute().setName("InputAttribute2");
        childParam.newExpectedResultTestAttribute().setName("ExpResultAttribute1");
        // missing childs are not checked in the delta
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 2, 0);
        
        // Param1
        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("TestParamX");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 1, 0, 0, 0, 2, 0);
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("TestParam1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 1, 0);
        // Param2
        ITestPolicyCmpt testPolicyCmpt2 = testCase.newTestPolicyCmpt();
        testPolicyCmpt2.setTestPolicyCmptTypeParameter("TestParam2");
        //   one missing test attribute value and one test policy wrong sort order
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 1, 0, 0, 1);
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute1");
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute2");
        testPolicyCmpt2.newTestAttributeValue().setTestAttribute("InputAttribute3");
        //   two missing test attributes and
        //   one wrong order of test attr (because test attr on same test policy cmpt)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 2, 0, 0, 0, 0, 0, 1);
        ITestPolicyCmptRelation rel2 = testPolicyCmpt2.newTestPolicyCmptRelation();
        rel2.setTestPolicyCmptTypeParameter("RelationX");
        ITestPolicyCmpt child2 = rel2.newTargetTestPolicyCmptChild();
        child2.setTestPolicyCmptTypeParameter("TestParamChildX");
        child2.newTestAttributeValue().setTestAttribute("TestAttributeX");
        //   one missing test policy cmpt relation and
        //   two missing test attributes and 
        //   one wrong order of test attr (because test attr on same test policy cmpt)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 0, 0, 0, 1);
        // Child of param1
        ITestPolicyCmptRelation rel1 = testPolicyCmpt1.newTestPolicyCmptRelation();
        rel1.setTestPolicyCmptTypeParameter("Relation1");
        rel1.setTestPolicyCmptTypeParameter("TestParamChild1");
        ITestPolicyCmpt child1 = rel1.newTargetTestPolicyCmptChild();
        child1.setTestPolicyCmptTypeParameter("TestParamChild1");
        //   one missing test policy cmpt relation and
        //   two missing test attributes
        //   three missing test attributes value (the new child TestParamChild1 has three test attributes)
        //   two wrong order of test attr (one see below and one from the newly
		//      created TestParamChild1, three missing test attr values but all from
		//      the same test policy cmpt)
		assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 3, 0, 0, 2);
        child1.newTestAttributeValue().setTestAttribute("InputAttribute1");
        child1.newTestAttributeValue().setTestAttribute("InputAttribute2");
        //   see below but two test attibute values are created
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 1, 0, 0, 2);
        child1.newTestAttributeValue().setTestAttribute("ExpResultAttribute1");
        //   see below but no more missing test attibute values, therefore only on left sort order of test attributes
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 2, 0, 0, 0, 0, 0, 1);
        child1.newTestAttributeValue().setTestAttribute("ExpResultAttributeX");
        //   one missing test policy cmpt relation and
        //   three missing test attributes (one more for ExpResultAttributeX)
        //   two wrong order of test attr (one see below and one from the newly
		//      created ExpResultAttributeX)
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 1, 3, 0, 0, 0, 0, 0, 2);
        fixAndAssert(testCase.computeDeltaToTestCaseType());
    }
    
    public void testDifferentSortOrderTestPolicyCmpt() throws CoreException{
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("1");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("2");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("1");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testDifferentSortOrderTestValue() throws CoreException{
        testCaseType.newInputTestValueParameter().setName("1");
        testCaseType.newInputTestValueParameter().setName("2");
        testCaseType.newInputTestValueParameter().setName("3");
        testCase.newTestValue().setTestValueParameter("1");
        testCase.newTestValue().setTestValueParameter("3");
        testCase.newTestValue().setTestValueParameter("2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testDifferentSortOrderTestRule() throws CoreException{
        testCaseType.newExpectedResultRuleParameter().setName("1");
        testCaseType.newExpectedResultRuleParameter().setName("2");
        testCaseType.newExpectedResultRuleParameter().setName("3");
        testCase.newTestRule().setTestRuleParameter("1");
        testCase.newTestRule().setTestRuleParameter("3");
        testCase.newTestRule().setTestRuleParameter("2");
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testDifferentSortOrderTestRuleSameRuleParam() throws CoreException{
        testCaseType.newInputTestValueParameter().setName("Value1");
        testCaseType.newExpectedResultRuleParameter().setName("1");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("PolicyCmpt1");
        
        testCase.newTestValue().setTestValueParameter("Value1");
        ITestRule testRule1 = testCase.newTestRule();
        testRule1.setTestRuleParameter("1");
        testRule1.setValidationRule("rule1");
        ITestRule testRule2 = testCase.newTestRule();
        testRule2.setTestRuleParameter("1");
        testRule2.setValidationRule("rule2");
        ITestRule testRule3 = testCase.newTestRule();
        testRule3.setTestRuleParameter("1");
        testRule3.setValidationRule("rule3");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("PolicyCmpt1");
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testDifferentSortOrderMixed() throws CoreException{
        testCaseType.newExpectedResultRuleParameter().setName("0");
        testCaseType.newInputTestValueParameter().setName("1");
        testCaseType.newInputTestValueParameter().setName("2");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("3");
        testCaseType.newExpectedResultRuleParameter().setName("4");

        testCase.newTestRule().setTestRuleParameter("4");
        testCase.newTestValue().setTestValueParameter("1");
        ITestRule rule0 = testCase.newTestRule();
        rule0.setTestRuleParameter("0");
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("3");
        testCase.newTestValue().setTestValueParameter("2");
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
        testCase.newTestRule().setTestRuleParameter("0");
        delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        // no specific object is wrong
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testEqualSortOrderChilds() throws CoreException{
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("Param1");
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setRelation("Child1");
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child2");
        child.setRelation("Child2");
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child3");
        child.setRelation("Child3");
        
        ITestPolicyCmpt cmpt1 = testCase.newTestPolicyCmpt();
        cmpt1.setTestPolicyCmptTypeParameter("Param1");
        ITestPolicyCmptRelation rel1 = cmpt1.newTestPolicyCmptRelation();
        rel1.setTestPolicyCmptTypeParameter("Child1");
        rel1.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmptRelation rel2 = cmpt1.newTestPolicyCmptRelation();
        rel2.setTestPolicyCmptTypeParameter("Child2");
        rel2.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child2");
        ITestPolicyCmptRelation rel3 = cmpt1.newTestPolicyCmptRelation();
        rel3.setTestPolicyCmptTypeParameter("Child3");
        rel3.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child3");
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0);
        fixAndAssert(delta);  
    }
    
    public void testDifferentSortOrderChilds() throws CoreException{
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("Param1");
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setRelation("Child1");
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child2");
        child.setRelation("Child2");
        child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child3");
        child.setRelation("Child3");
        
        ITestPolicyCmpt cmpt1 = testCase.newTestPolicyCmpt();
        cmpt1.setTestPolicyCmptTypeParameter("Param1");
        ITestPolicyCmptRelation rel1 = cmpt1.newTestPolicyCmptRelation();
        rel1.setTestPolicyCmptTypeParameter("Child1");
        rel1.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmptRelation rel2 = cmpt1.newTestPolicyCmptRelation();
        rel2.setTestPolicyCmptTypeParameter("Child3");
        rel2.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child3");
        ITestPolicyCmptRelation rel3 = cmpt1.newTestPolicyCmptRelation();
        rel3.setTestPolicyCmptTypeParameter("Child2");
        rel3.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child2");
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
        fixAndAssert(delta);
    }
    
    public void testDifferentSortOrderChildOfChilds() throws CoreException{
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("Param1");
        // child 1
        ITestPolicyCmptTypeParameter child = param1.newTestPolicyCmptTypeParamChild();
        child.setName("Child1");
        child.setRelation("Child1");
        // child 2
        ITestPolicyCmptTypeParameter child2 = param1.newTestPolicyCmptTypeParamChild();
        child2.setName("Child2");
        child2.setRelation("Child2");
        
        // child0 of child1
        ITestPolicyCmptTypeParameter childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child0");
        childchild.setRelation("Child1Child0"); 
        // child1 of child1
        childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child1");
        childchild.setRelation("Child1Child1");
        // child2 of child1
        childchild = child.newTestPolicyCmptTypeParamChild();
        childchild.setName("Child1Child2");
        childchild.setRelation("Child1Child2");
        
        // test case side
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter("Param1");
        // child11
        ITestPolicyCmptRelation rel = cmpt.newTestPolicyCmptRelation();
        rel.setTestPolicyCmptTypeParameter("Child1");
        ITestPolicyCmpt cmptChild = rel.newTargetTestPolicyCmptChild();
        cmptChild.setTestPolicyCmptTypeParameter("Child1");
        
        // child2
        ITestPolicyCmptRelation rel2 = cmpt.newTestPolicyCmptRelation();
        rel2.setTestPolicyCmptTypeParameter("Child2");
        ITestPolicyCmpt cmptChild2 = rel2.newTargetTestPolicyCmptChild();
        cmptChild2.setTestPolicyCmptTypeParameter("Child2");
        
        // child1 of child1 #1
        rel = cmptChild.newTestPolicyCmptRelation();
        rel.setTestPolicyCmptTypeParameter("Child1Child2");
        rel.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1Child2");
        // child1 of child1 #2
        rel = cmptChild.newTestPolicyCmptRelation();
        rel.setTestPolicyCmptTypeParameter("Child1Child2");
        rel.newTargetTestPolicyCmptChild().setTestPolicyCmptTypeParameter("Child1Child2");        
        // child2 of child1 (wrong order)
        rel = cmptChild.newTestPolicyCmptRelation();
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
    public void testNewAttributeForEqualInstances() throws CoreException{
        // test case type side
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setName("Param");
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("ParamChild");
        paramChild.newInputTestAttribute().setName("TestAttribute1");
        paramChild.newInputTestAttribute().setName("TestAttribute2");
        
        // test case side
        ITestPolicyCmpt cmpt = testCase.newTestPolicyCmpt();
        cmpt.setTestPolicyCmptTypeParameter(param.getName());
        ITestPolicyCmpt cmptChild1 = cmpt.newTestPolicyCmptRelation().newTargetTestPolicyCmptChild();
        cmptChild1.setTestPolicyCmptTypeParameter(paramChild.getName());
        ((ITestPolicyCmptRelation)cmptChild1.getParent()).setTestPolicyCmptTypeParameter(paramChild.getName());
        ITestPolicyCmpt cmptChild2 = cmpt.newTestPolicyCmptRelation().newTargetTestPolicyCmptChild();
        cmptChild2.setTestPolicyCmptTypeParameter(paramChild.getName());
        ((ITestPolicyCmptRelation)cmptChild2.getParent()).setTestPolicyCmptTypeParameter(paramChild.getName());
        
        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertFalse(delta.isEmpty());
        // a test attribute will only added once if more instances of the same test parameter are in the test case
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 2, 0, 0, 2);
        fixAndAssert(delta);
    }
    
    /**
     * Asserts the container length
     */
    private void assertDeltaContainer(ITestCaseTestCaseTypeDelta delta,
            int testValuesWithMissingTestValueParam,
            int testPolicyCmptsWithMissingTypeParam,
            int testPolicyCmptRelationsWithMissingTypeParam,
            int testAttributeValuesWithMissingTestAttribute,
            int testValueParametersWithMissingTestValue,
            int testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
            int testAttributesWithMissingTestAttributeValue) {
        assertDeltaContainer(delta, testValuesWithMissingTestValueParam, testPolicyCmptsWithMissingTypeParam,
                testPolicyCmptRelationsWithMissingTypeParam, testAttributeValuesWithMissingTestAttribute,
                testValueParametersWithMissingTestValue, testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
                testAttributesWithMissingTestAttributeValue, 0);
    }
    private void assertDeltaContainer(ITestCaseTestCaseTypeDelta delta, int testValuesWithMissingTestValueParam,
            int testPolicyCmptsWithMissingTypeParam,
            int testPolicyCmptRelationsWithMissingTypeParam,
            int testAttributeValuesWithMissingTestAttribute,
            int testValueParametersWithMissingTestValue,
            int testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
            int testAttributesWithMissingTestAttributeValue,
            int testPolicyCmptWithDifferentSortOrder) {
        assertDeltaContainer(delta, testValuesWithMissingTestValueParam, testPolicyCmptsWithMissingTypeParam,
                testPolicyCmptRelationsWithMissingTypeParam, testAttributeValuesWithMissingTestAttribute,
                testValueParametersWithMissingTestValue, testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
                testAttributesWithMissingTestAttributeValue, 0, 0, 0);
    }
    private void assertDeltaContainer(ITestCaseTestCaseTypeDelta delta, 
    		int testValuesWithMissingTestValueParam,
            int testPolicyCmptsWithMissingTypeParam,
            int testPolicyCmptRelationsWithMissingTypeParam,
            int testAttributeValuesWithMissingTestAttribute,
            int testValueParametersWithMissingTestValue,
            int testPolicyCmptTypeParametersWithMissingTestPolicyCmpt,
            int testAttributesWithMissingTestAttributeValue,
            int testPolicyCmptWithDifferentSortOrder,
            int testRulesWithMissingTestValueParam,
            int testPolicyCmptWithDifferentSortOrderTestAttr) {

        assertEquals(testValuesWithMissingTestValueParam, delta.getTestValuesWithMissingTestValueParam().length);  
        assertEquals(testPolicyCmptsWithMissingTypeParam, delta.getTestPolicyCmptsWithMissingTypeParam().length);
        assertEquals(testPolicyCmptRelationsWithMissingTypeParam, delta.getTestPolicyCmptRelationsWithMissingTypeParam().length);
        assertEquals(testAttributeValuesWithMissingTestAttribute, delta.getTestAttributeValuesWithMissingTestAttribute().length);

        assertEquals(testValueParametersWithMissingTestValue, delta.getTestValueParametersWithMissingTestValue().length);
        assertEquals(testPolicyCmptTypeParametersWithMissingTestPolicyCmpt, delta
                .getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt().length);
        assertEquals(testAttributesWithMissingTestAttributeValue, delta
                .getTestAttributesWithMissingTestAttributeValue().length);
        assertEquals(testPolicyCmptWithDifferentSortOrder, delta
                .getTestPolicyCmptWithDifferentSortOrder().length);        
        assertEquals(testRulesWithMissingTestValueParam, delta
                .getTestRulesWithMissingTestValueParam().length);
        assertEquals(testPolicyCmptWithDifferentSortOrderTestAttr, delta
                .getTestPolicyCmptWithDifferentSortOrderTestAttr().length);
    }
    
    /*
     * Fixes the delta and assert that there is no new delta between the test case and test case type.
     */
    private void fixAndAssert(ITestCaseTestCaseTypeDelta delta) throws CoreException{
        testCase.fixDifferences(delta);
        ITestCaseTestCaseTypeDelta newDelta = testCase.computeDeltaToTestCaseType();
        assertFalse(newDelta.isDifferentTestParameterOrder());
        assertTrue(newDelta.isEmpty());
        assertDeltaContainer(newDelta, 0, 0, 0, 0, 0, 0, 0, 0);
    }
    
    public void testDifferentSortOrderAttributes() throws CoreException{
        ITestPolicyCmptTypeParameter param1 = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        param1.setName("policyCmpt");
        param1.newInputTestAttribute().setName("1");
        param1.newInputTestAttribute().setName("2");
        param1.newInputTestAttribute().setName("3");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setTestPolicyCmptTypeParameter("policyCmpt");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("1");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("3");
        testPolicyCmpt.newTestAttributeValue().setTestAttribute("2");

        ITestCaseTestCaseTypeDelta delta = testCase.computeDeltaToTestCaseType();
        assertTrue(delta.isDifferentTestParameterOrder());
        assertDeltaContainer(testCase.computeDeltaToTestCaseType(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
        fixAndAssert(delta);
    }
}
