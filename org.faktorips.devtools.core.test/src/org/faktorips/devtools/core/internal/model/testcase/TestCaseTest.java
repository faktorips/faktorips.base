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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
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
    
    public void testValidateTestCaseTypeNotFound() throws Exception{
        MessageList ml = testCase.validate();
        assertNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));

        testCase.setTestCaseType("x");
        ml = testCase.validate();
        assertNotNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));
    }
}
