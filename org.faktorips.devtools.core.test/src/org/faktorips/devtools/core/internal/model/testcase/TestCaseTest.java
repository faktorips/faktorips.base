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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private ITestCase testCase;
    private ITestCaseType testCaseType;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        
        newPolicyCmptType(ipsProject, "testCaseTypeX");
        
        testCaseType = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseType1");
        testCaseType.newInputTestValueParameter().setName("inputTestValue0");
        testCaseType.newInputTestValueParameter().setName("inputTestValue1");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("inputTestPolicyCmpt0");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("inputTestPolicyCmpt1");

        testCaseType.newExpectedResultValueParameter().setName("expResultTestValue0");
        testCaseType.newExpectedResultValueParameter().setName("expResultTestValue1");
        testCaseType.newExpectedResultValueParameter().setName("expResultTestValue2");
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expResultTestPolicyCmpt0");
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expResultTestPolicyCmpt1");
        testCaseType.newExpectedResultRuleParameter().setName("expResultTestRule1");
        
        testCase = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseType1");
        testCase.setTestCaseType(testCaseType.getName());
    }

    private void assertTestCaseObjects( 
            int testInputValues, int testExpResultValues,
            int testInputPolicyCmpts, int testExpPolicyCmpts){
        assertTestCaseObjects(testInputValues, testExpResultValues, testInputPolicyCmpts, testExpPolicyCmpts, 0);
    }
    
    private void assertTestCaseObjects( 
            int testInputValues, int testExpResultValues,
            int testInputPolicyCmpts, int testExpPolicyCmpts, int testExpResultRules){
        assertEquals(testInputValues, testCase.getInputTestValues().length);
        assertEquals(testExpResultValues, testCase.getExpectedResultTestValues().length);
        assertEquals(testInputPolicyCmpts, testCase.getInputTestPolicyCmpts().length);
        assertEquals(testExpPolicyCmpts, testCase.getExpectedResultTestPolicyCmpts().length);
        assertEquals(testExpResultRules, testCase.getExpectedResultTestRules().length);
    }
    
    public void testNewParameterAndGet() throws Exception {
        ITestValue param1 = testCase.newTestValue();
        param1.setTestValueParameter("inputTestValue0");
        assertNotNull(param1);
        assertTrue(param1.isInput());
        assertTestCaseObjects(1,0,0,0);
        
        ITestValue param2 = testCase.newTestValue();
        param2.setTestValueParameter("expResultTestValue0");
        assertNotNull(param2);
        assertTestCaseObjects(1,1,0,0);
        
        ITestPolicyCmpt param3 = testCase.newTestPolicyCmpt();
        param3.setTestPolicyCmptTypeParameter("inputTestPolicyCmpt0");
        param3.setName("inputTestPolicyCmpt0");
        assertNotNull(param3);
        assertTestCaseObjects(1,1,1,0);
        
        ITestPolicyCmpt param4 = testCase.newTestPolicyCmpt();
        param4.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt0");
        param4.setName("expResultTestPolicyCmpt0");
        assertNotNull(param4);
        assertTestCaseObjects(1,1,1,1);
        
        ITestPolicyCmpt param5 = testCase.newTestPolicyCmpt();
        param5.setTestPolicyCmptTypeParameter("xyz");
        assertNotNull(param5);
        assertTestCaseObjects(1,1,1,1);
        
        ITestPolicyCmpt param6 = testCase.newTestPolicyCmpt();
        param6.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt1");
        assertNotNull(param6);
        assertTestCaseObjects(1,1,1,2);
        
        ITestRule param7 = testCase.newTestRule();
        param7.setTestRuleParameter("expResultTestRule1");
        assertNotNull(param7);
        assertTestCaseObjects(1,1,1,2,1);
        
        assertEquals(param1, testCase.getInputTestValues()[0]);
        assertEquals(param2, testCase.getExpectedResultTestValues()[0]);
        assertEquals(param3, testCase.findTestPolicyCmpt("inputTestPolicyCmpt0"));
        assertEquals(param4, testCase.findTestPolicyCmpt("expResultTestPolicyCmpt0"));
        assertEquals(param3, testCase.getInputTestPolicyCmpts()[0]);
        assertEquals(param4, testCase.getExpectedResultTestPolicyCmpts()[0]);
        assertEquals(param6, testCase.getExpectedResultTestPolicyCmpts()[1]);
        assertEquals(param7, testCase.getExpectedResultTestRules()[0]);
    }
       
    public void testInitFromXml() throws CoreException  {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        testCase.initFromXml(typeEl);
        
        assertEquals(10, testCase.getTestObjects().length);
        assertTestCaseObjects(2, 3, 2, 2, 1);
        
        assertEquals("testCaseType1", testCase.getTestCaseType());
        
        ITestValue[] testValues = testCase.getInputTestValues();
        for (int i = 0; i < testValues.length; i++) {
            assertEquals("inputTestValue" + i, testValues[i].getTestValueParameter());
            assertTrue(testValues[i].isInput());
            assertFalse(testValues[i].isExpectedResult());
            assertFalse(testValues[i].isCombined());
        }
        
        testValues = testCase.getExpectedResultTestValues();
        for (int i = 0; i < testValues.length; i++) {
            assertEquals("expResultTestValue" + i, testValues[i].getTestValueParameter());
            assertFalse(testValues[i].isInput());
            assertTrue(testValues[i].isExpectedResult());
            assertFalse(testValues[i].isCombined());
        }
     
        ITestPolicyCmpt[] testPc = testCase.getInputTestPolicyCmpts();
        for (int i = 0; i < testPc.length; i++) {
            assertEquals("inPolicyCmptType" + i, testPc[i].getName());
            assertTrue(testPc[i].isInput());
            assertFalse(testPc[i].isExpectedResult());
            assertFalse(testPc[i].isCombined());
        }
        
        testPc = testCase.getExpectedResultTestPolicyCmpts();
        for (int i = 0; i < testPc.length; i++) {
            assertEquals("expPolicyCmptType" + i, testPc[i].getName());
            assertFalse(testPc[i].isInput());
            assertTrue(testPc[i].isExpectedResult());
            assertFalse(testPc[i].isCombined());
        }
        
        ITestRule[] testRules = testCase.getExpectedResultTestRules();
        assertEquals("expResultTestRule1", testRules[0].getTestRuleParameter());
        assertTrue(testRules[0].isExpectedResult());
        assertFalse(testRules[0].isCombined());
        assertTrue(testRules[0].isExpectedResult());
    }
    
    public void testToXml() {
      ITestValue valueParamInput0 = testCase.newTestValue();
      valueParamInput0.setTestValueParameter("inputTestValue0");
      ITestValue valueParamInput1 = testCase.newTestValue();
      valueParamInput1.setTestValueParameter("inputTestValue1");
      ITestPolicyCmpt policyCmptInput0 = testCase.newTestPolicyCmpt();
      policyCmptInput0.setTestPolicyCmptTypeParameter("inputTestPolicyCmpt0");
      ITestValue valueParamExpectedResult1 = testCase.newTestValue();
      valueParamExpectedResult1.setTestValueParameter("expResultTestValue0");
      ITestPolicyCmpt policyCmptExpected0 = testCase.newTestPolicyCmpt();
      policyCmptExpected0.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt0");
      testCase.setTestCaseType("testCaseType1");
      ITestRule rule = testCase.newTestRule();
      rule.setTestRuleParameter("expResultTestRule1");
      
      Document doc = newDocument();
      Element el = testCase.toXml(doc);
      
      // overwrite parameter
      testCase.setTestCaseType("temp");
      valueParamInput1.setTestValueParameter("Test");
      valueParamExpectedResult1.setTestValueParameter("Test2");
      testCase.newTestValue().setTestValueParameter("inputTestValueDummy");
      testCase.newTestRule().setTestRuleParameter("xyz");
      
      // read the xml which was written before
      testCase.initFromXml(el);
      assertEquals(1,  testCase.getInputTestPolicyCmpts().length);
      assertEquals(2,  testCase.getInputTestValues().length);
      assertEquals(1,  testCase.getExpectedResultTestValues().length);
      assertEquals(1,  testCase.getExpectedResultTestPolicyCmpts().length);
      assertEquals(valueParamInput0, testCase.getInputTestValues()[0]);
      assertEquals(valueParamInput1, testCase.getInputTestValues()[1]);
      assertEquals(valueParamExpectedResult1, testCase.getExpectedResultTestValues()[0]);
      assertEquals(policyCmptInput0, testCase.getInputTestPolicyCmpts()[0]);
      assertEquals(policyCmptExpected0, testCase.getExpectedResultTestPolicyCmpts()[0]);
      assertEquals(rule, testCase.getExpectedResultTestRules()[0]);
      
      assertEquals("inputTestValue0", testCase.getInputTestValues()[0].getTestValueParameter());
      assertEquals("inputTestValue1", testCase.getInputTestValues()[1].getTestValueParameter());
      assertEquals("expResultTestValue0", testCase.getExpectedResultTestValues()[0].getTestValueParameter());
      assertEquals("inputTestPolicyCmpt0", testCase.getInputTestPolicyCmpts()[0].getTestPolicyCmptTypeParameter());
      assertEquals("expResultTestPolicyCmpt0", testCase.getExpectedResultTestPolicyCmpts()[0].getTestPolicyCmptTypeParameter());
      assertEquals("expResultTestRule1", testCase.getExpectedResultTestRules()[0].getTestRuleParameter());
      
      assertEquals("testCaseType1", testCase.getTestCaseType());
    }
    
    public void testDependsOn() throws Exception {
        List dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(1, dependsOnList.size());
        assertTrue(dependsOnList.contains(testCaseType.getQualifiedNameType()));
        
        ITestCase testCase2 = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseType2");
        List dependsOnList2 = CollectionUtil.toArrayList(testCase2.dependsOn());
        assertEquals(0, dependsOnList2.size());
        
        IProductCmpt prodCmpt1 = newProductCmpt(root, "ProductCmpt1");
        IProductCmpt prodCmpt2 = newProductCmpt(root, "ProductCmpt2");

        // test dependency to product cmpt, root test cmpt
        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setProductCmpt(prodCmpt1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(2, dependsOnList.size());
        assertTrue(dependsOnList.contains(prodCmpt1.getQualifiedNameType()));

        // test dependency to product cmpt, child test cmpt
        ITestPolicyCmptRelation testRelation1 = testPolicyCmpt1.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPolicyCmpt2 = testRelation1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setProductCmpt(prodCmpt2.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(3, dependsOnList.size());
        assertTrue(dependsOnList.contains(prodCmpt1.getQualifiedNameType()));
        assertTrue(dependsOnList.contains(prodCmpt2.getQualifiedNameType()));
    }
    
    public void testGenerateUniqueNameForTestPolicyCmpt(){
        // test for root parameters
        ITestPolicyCmpt param1 = testCase.newTestPolicyCmpt();
        param1.setName(testCase.generateUniqueNameForTestPolicyCmpt(param1, "Test"));
        assertEquals("Test", param1.getName());
        
        ITestPolicyCmpt param2 = testCase.newTestPolicyCmpt();
        param2.setName(testCase.generateUniqueNameForTestPolicyCmpt(param2, "Test"));
        assertEquals("Test (2)", param2.getName());

        ITestPolicyCmpt param3 = testCase.newTestPolicyCmpt();
        param3.setName(testCase.generateUniqueNameForTestPolicyCmpt(param3, "Test"));
        assertEquals("Test (3)", param3.getName());
        
        param2.delete();
        
        ITestPolicyCmpt param4 = testCase.newTestPolicyCmpt();
        param4.setName(testCase.generateUniqueNameForTestPolicyCmpt(param4, "Test"));
        assertEquals("Test (2)", param4.getName());
        
        ITestPolicyCmpt param5 = testCase.newTestPolicyCmpt();
        param5.setName(testCase.generateUniqueNameForTestPolicyCmpt(param5, "Test"));
        assertEquals("Test (4)", param5.getName());
        
        // test for child parameters
        ITestPolicyCmptRelation relation =  param1.newTestPolicyCmptRelation();
        ITestPolicyCmpt child = relation.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test", child.getName());
        
        relation =  param1.newTestPolicyCmptRelation();
        ITestPolicyCmpt child2 = relation.newTargetTestPolicyCmptChild();
        child2.setName(testCase.generateUniqueNameForTestPolicyCmpt(child2, "Test"));
        assertEquals("Test (2)", child2.getName());
        
        relation =  param1.newTestPolicyCmptRelation();
        child = relation.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (3)", child.getName());
        
        child2.delete();
        
        relation =  param1.newTestPolicyCmptRelation();
        child = relation.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (2)", child.getName());
        
        relation =  param1.newTestPolicyCmptRelation();
        child = relation.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (4)", child.getName());        
    }
    
    public void testValidateTestCaseTypeNotFound() throws Exception{
        MessageList ml = testCase.validate();
        assertNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));

        testCase.setTestCaseType("x");
        ml = testCase.validate();
        assertNotNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));
    }
    
    public void testGetTestRuleCandidates() throws CoreException{
        // create policy cmpts with validation rules 
        IPolicyCmptType policyCmptTypeA = newPolicyAndProductCmptType(ipsProject, "PolicyCmptA", "ProductCmptA");
        policyCmptTypeA.setAbstract(true);
        IPolicyCmptType policyCmptTypeB = newPolicyAndProductCmptType(ipsProject, "PolicyCmptB", "ProductCmptB");
        policyCmptTypeB.setSupertype(policyCmptTypeA.getQualifiedName());
        IPolicyCmptType policyCmptTypeC = newPolicyAndProductCmptType(ipsProject, "PolicyCmptC", "ProductCmptC");
        policyCmptTypeC.setSupertype(policyCmptTypeB.getQualifiedName());
        IPolicyCmptType policyCmptTypeD = newPolicyAndProductCmptType(ipsProject, "PolicyCmptD", "ProductCmptD");
        policyCmptTypeD.setSupertype(policyCmptTypeC.getQualifiedName());
        
        // create product cmpts will be added in the test case
        IProductCmpt productCmptB = newProductCmpt(policyCmptTypeB.findProductCmptType(ipsProject), "ProductCmptB");
        IProductCmpt productCmptC = newProductCmpt(policyCmptTypeC.findProductCmptType(ipsProject), "ProductCmptC");
        IProductCmpt productCmptD = newProductCmpt(policyCmptTypeD.findProductCmptType(ipsProject), "ProductCmptD");
        
        IValidationRule ruleA = policyCmptTypeA.newRule();
        ruleA.setName("RuleA");
        ruleA.setMessageCode("RuleA");
        IValidationRule ruleB = policyCmptTypeB.newRule();
        ruleB.setName("RuleB");
        ruleB.setMessageCode("RuleB");
        IValidationRule ruleC = policyCmptTypeC.newRule();
        ruleC.setName("RuleC");
        ruleC.setMessageCode("RuleC");
        IValidationRule ruleD = policyCmptTypeD.newRule();
        ruleD.setName("RuleD");
        ruleD.setMessageCode("RuleD");
        
        // create parameter for the abstract policy cmpt
        ITestPolicyCmptTypeParameter paramA1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramA1.setPolicyCmptType(policyCmptTypeA.getQualifiedName());
        paramA1.setName("PolicyCmptA1");
        ITestPolicyCmptTypeParameter paramA2 = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramA2.setPolicyCmptType(policyCmptTypeA.getQualifiedName());
        paramA2.setName("PolicyCmptA2");
        ITestPolicyCmptTypeParameter paramA3 = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramA3.setPolicyCmptType(policyCmptTypeA.getQualifiedName());
        paramA3.setName("PolicyCmptA3");
        
        ITestPolicyCmpt tpcB = testCase.newTestPolicyCmpt();
        tpcB.setTestPolicyCmptTypeParameter(paramA1.getName());
        tpcB.setProductCmpt(productCmptB.getQualifiedName());
        
        ITestPolicyCmpt tpcC = testCase.newTestPolicyCmpt();
        tpcC.setTestPolicyCmptTypeParameter(paramA2.getName());
        tpcC.setProductCmpt(productCmptC.getQualifiedName());
        
        IValidationRule[] testRuleParameters = testCase.getTestRuleCandidates();
        assertEquals(3, testRuleParameters.length);
        List testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));
        assertTrue(testRuleParametersList.contains(ruleC));
        
        testCase.removeTestObject(tpcC);
        assertEquals(1, testCase.getAllTestPolicyCmpt().length);
        testRuleParameters = testCase.getTestRuleCandidates();
        assertEquals(2, testRuleParameters.length);
        testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));
        
        ITestPolicyCmpt tpcD = testCase.newTestPolicyCmpt();
        tpcD.setTestPolicyCmptTypeParameter(paramA3.getName());
        tpcD.setProductCmpt(productCmptD.getQualifiedName());
        
        testRuleParameters = testCase.getTestRuleCandidates();
        assertEquals(4, testRuleParameters.length);
        testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));
        assertTrue(testRuleParametersList.contains(ruleC));        
        assertTrue(testRuleParametersList.contains(ruleD));        
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#containsDifferenceToModel()}.
     * @throws CoreException 
     */
    public void testContainsDifferenceToModel() throws CoreException {
        ITestCaseType testCaseTypeX = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseTypeX");
        
        ITestCase testCaseX = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseTypeX");
        testCaseX.setTestCaseType(testCaseTypeX.getName());
        
        assertEquals(false, testCaseX.containsDifferenceToModel());
        ITestValueParameter parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("inputTestValueX");
        parameter.setDatatype("String");
        assertEquals(true, testCaseX.containsDifferenceToModel());       
    } 

    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#fixAllDifferencesToModel()}.
     * @throws CoreException 
     */
    public void testFixAllDifferencesToModel() throws CoreException {
        ITestCaseType testCaseTypeX = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseTypeX");
        ITestCase testCaseX = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseTypeX");
        testCaseX.setTestCaseType(testCaseTypeX.getName());
        
        assertEquals(false, testCaseX.containsDifferenceToModel());
        testCaseX.fixAllDifferencesToModel();
        assertEquals(false, testCaseX.containsDifferenceToModel());
        
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "PolicyCmptType");
        IAttribute attribute = policyCmptType.newAttribute();
        attribute.setName("Attribute1");
        attribute.setDatatype("String");
        attribute.setDefaultValue("Test1");
        attribute = policyCmptType.newAttribute();
        attribute.setName("Attribute2");
        attribute.setDatatype("String");
        attribute.setDefaultValue("Test2");
        
        ITestPolicyCmptTypeParameter policyCmptParam = testCaseTypeX.newInputTestPolicyCmptTypeParameter();
        policyCmptParam.setPolicyCmptType(policyCmptType.getQualifiedName());
        policyCmptParam.setName("PolicyCmptType");
        ITestAttribute testAttribute = policyCmptParam.newInputTestAttribute();
        testAttribute.setAttribute("Attribute1");
        testAttribute.setName("Attribute1");
        
        ITestValueParameter parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("inputTestValueX1");
        parameter.setDatatype("String");
        parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("inputTestValueX2");
        parameter.setDatatype("String");
        
        assertEquals(true, testCaseX.containsDifferenceToModel());
        testCaseX.fixAllDifferencesToModel();
        assertEquals(false, testCaseX.containsDifferenceToModel());
        
        // further check to ensure that the old default values will not be overridden
        ITestAttributeValue testAttributeValue = testCaseX.getInputTestPolicyCmpts()[0].getTestAttributeValues()[0];
        assertEquals("Test1", testAttributeValue.getValue());
        testAttributeValue.setValue("1234");
        
        testAttribute = policyCmptParam.newInputTestAttribute();
        testAttribute.setName("Attribute2");
        testAttribute.setAttribute("Attribute2");
        
        assertEquals(true, testCaseX.containsDifferenceToModel());
        testCaseX.fixAllDifferencesToModel();
        assertEquals(false, testCaseX.containsDifferenceToModel());
        
        assertEquals("1234", testAttributeValue.getValue());
        testAttributeValue = testCaseX.getInputTestPolicyCmpts()[0].getTestAttributeValues()[1];
        assertEquals("Test2", testAttributeValue.getValue());
        
        // test the correct default values
        MessageList ml = testCaseX.validate();
        assertEquals(ml.getNoOfMessages(), 0);
        parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("testBoolean");
        parameter.setDatatype("Boolean");
        
        testCaseX.fixAllDifferencesToModel();
        assertEquals(false, testCaseX.containsDifferenceToModel());
        ml = testCaseX.validate();
        assertFalse(ml.containsErrorMsg());        
    }
    
    public void testGetAllTestPolicyCmpts() throws CoreException{
        ITestPolicyCmpt testPolicyCmptRoot1 = testCase.newTestPolicyCmpt();
        testPolicyCmptRoot1.setName("root");
        ITestPolicyCmptRelation testPolicyCmptRelation = testPolicyCmptRoot1.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPolicyCmpt = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root1_child1");
        testPolicyCmptRelation = testPolicyCmptRoot1.newTestPolicyCmptRelation();
        testPolicyCmpt = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root1_child2");
        
        ITestPolicyCmpt testPolicyCmptRoot2 = testCase.newTestPolicyCmpt();
        testPolicyCmptRoot2.setName("root2");
        testPolicyCmptRelation = testPolicyCmptRoot2.newTestPolicyCmptRelation();
        testPolicyCmpt = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child1");
        testPolicyCmptRelation = testPolicyCmpt.newTestPolicyCmptRelation();
        testPolicyCmpt = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child1_child1");
        
        testPolicyCmptRelation = testPolicyCmptRoot2.newTestPolicyCmptRelation();
        testPolicyCmpt = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child2");
        
        ITestPolicyCmpt[] allTestPolicyCmpt = testCase.getAllTestPolicyCmpt();
        List allTestPolicyCmptNames = new ArrayList();
        for (int i = 0; i < allTestPolicyCmpt.length; i++) {
            allTestPolicyCmptNames.add(allTestPolicyCmpt[i].getName());
        }
        assertEquals(7, allTestPolicyCmptNames.size());
        assertTrue(allTestPolicyCmptNames.contains("root"));
        assertTrue(allTestPolicyCmptNames.contains("root1_child1"));
        assertTrue(allTestPolicyCmptNames.contains("root1_child2"));
        assertTrue(allTestPolicyCmptNames.contains("root2"));
        assertTrue(allTestPolicyCmptNames.contains("root2_child1"));
        assertTrue(allTestPolicyCmptNames.contains("root2_child1_child1"));
        assertTrue(allTestPolicyCmptNames.contains("root2_child2"));
    }
    
    public void testGetReferencedProductCmpts() throws CoreException{
        IProductCmpt prodCmpt1 = newProductCmpt(root, "ProductCmpt1");
        IProductCmpt prodCmpt2 = newProductCmpt(root, "ProductCmpt2");

        List referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(0, referencedProductCmpts.size());

        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setProductCmpt(prodCmpt1.getQualifiedName());
        referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(1, referencedProductCmpts.size());
        assertEquals(prodCmpt1.getQualifiedName(), ((String)referencedProductCmpts.get(0)));

        ITestPolicyCmptRelation testRelation1 = testPolicyCmpt1.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPolicyCmpt2 = testRelation1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setProductCmpt(prodCmpt2.getQualifiedName());
        referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(2, referencedProductCmpts.size());
        
    }
}
