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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private ITestCase testCase;
    private ITestPolicyCmpt testPolicyCmptTypeObjectExpected;
    private ITestPolicyCmpt testPolicyCmptTypeObjectInput;
    
    /**
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("testValueParameter1");
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("testValueParameter2");
        testCaseType.newCombinedPolicyCmptTypeParameter().setName("testValueParameter3");
        
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        (testPolicyCmptTypeObjectInput = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter1");
        (testPolicyCmptTypeObjectExpected = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter2");
        
        testCase.setTestCaseType(testCaseType.getName());
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeObject",0);
        testPolicyCmptTypeObjectExpected.initFromXml(paramEl);
        assertEquals("base.Test1", testPolicyCmptTypeObjectExpected.getTestPolicyCmptTypeParameter());   
        assertEquals("productCmpt1", testPolicyCmptTypeObjectExpected.getProductCmpt());
        assertEquals("policyCmptType1", testPolicyCmptTypeObjectExpected.getName());
        assertEquals(2, testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(3, testPolicyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelation("relation2"), "base.Test2");  
        
        assertTrue(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isAccoziation());
        assertFalse(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isComposition());
        assertFalse(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isAccoziation());
        assertTrue(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isComposition());
    }

    public void testToXml() {
        testPolicyCmptTypeObjectExpected.setTestPolicyCmptTypeParameter("base.Test2");
        testPolicyCmptTypeObjectExpected.setProductCmpt("productCmpt1");
        testPolicyCmptTypeObjectExpected.setName("Label1");
        testPolicyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmptRelation relation = testPolicyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptTypeParameter("relation1");
        ITestPolicyCmpt targetChild = relation.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptTypeParameter("base.Test4");
        testPolicyCmptTypeObjectExpected.newTestAttributeValue();
        
        Element el = testPolicyCmptTypeObjectExpected.toXml(newDocument());
        
        testPolicyCmptTypeObjectExpected.setTestPolicyCmptTypeParameter("base.Test3");
        testPolicyCmptTypeObjectExpected.setProductCmpt("productCmpt2");
        testPolicyCmptTypeObjectExpected.setName("Label2");
        testPolicyCmptTypeObjectExpected.newTestAttributeValue();
        testPolicyCmptTypeObjectExpected.newTestAttributeValue();
        testPolicyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        
        testPolicyCmptTypeObjectExpected.initFromXml(el);
        assertEquals("base.Test2", testPolicyCmptTypeObjectExpected.getTestPolicyCmptTypeParameter());
        assertEquals("Label1", testPolicyCmptTypeObjectExpected.getName());
        assertEquals(2, testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(1, testPolicyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(testPolicyCmptTypeObjectExpected.getTestPolicyCmptRelation("relation1"),
                "base.Test4");
        assertEquals("productCmpt1", testPolicyCmptTypeObjectExpected.getProductCmpt());
    }
    
    public void testNewAndDelete(){
        assertEquals(2, testCase.getTestPolicyCmpts().length);
        
        // test root
        ITestPolicyCmpt policyCmpt = testCase.newTestPolicyCmpt();
        assertEquals(3, testCase.getTestPolicyCmpts().length);
        
        policyCmpt.delete();
        assertEquals(2, testCase.getTestPolicyCmpts().length);
        assertEquals(testPolicyCmptTypeObjectExpected, testCase.getExpectedResultTestPolicyCmpts()[0]);
        assertEquals(testPolicyCmptTypeObjectInput, testCase.getInputTestPolicyCmpts()[0]);
        
        // test child
        ITestPolicyCmptRelation rel = testPolicyCmptTypeObjectInput.newTestPolicyCmptRelation();
        ITestPolicyCmpt child = rel.newTargetTestPolicyCmptChild();
        assertEquals(1, testPolicyCmptTypeObjectInput.getTestPolicyCmptRelations().length);
        
        child.delete();
        assertEquals(0, testPolicyCmptTypeObjectInput.getTestPolicyCmptRelations().length);
    }
                           
    
    public void testInputOrExpectedResultObject(){
        assertFalse(testPolicyCmptTypeObjectExpected.isInput());
        assertTrue(testPolicyCmptTypeObjectExpected.isExpectedResult());
        assertFalse(testPolicyCmptTypeObjectExpected.isCombined());
        
        ITestPolicyCmptRelation r = testPolicyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPc = r.newTargetTestPolicyCmptChild();
        assertFalse(testPc.isInput());
        assertTrue(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());
        
        assertTrue(testPolicyCmptTypeObjectInput.isInput());
        assertFalse(testPolicyCmptTypeObjectInput.isExpectedResult());
        assertFalse(testPolicyCmptTypeObjectInput.isCombined());
        r = testPolicyCmptTypeObjectInput.newTestPolicyCmptRelation();
        testPc = r.newTargetTestPolicyCmptChild();
        assertTrue(testPc.isInput());
        assertFalse(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());
    }
    
    private void assertRelation(ITestPolicyCmptRelation relation, String policyCmptTypeName) {
        assertNotNull(relation);
        ITestPolicyCmpt targetChild = null;
        try {
            targetChild = relation.findTarget();
        } catch (CoreException e) {
            fail(e.getLocalizedMessage());
        }
        assertNotNull(targetChild);
        assertEquals(policyCmptTypeName, targetChild.getTestPolicyCmptTypeParameter());
    }    
    
    public void testValidateTestCaseTypeParamNotFound() throws Exception{
        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPolicyCmptTypeObjectInput.setTestPolicyCmptTypeParameter("x");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateProductCmptIsRequired() throws Exception{
        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(true);
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        testPolicyCmptTypeObjectInput.setProductCmpt("x");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
    }
    
    public void testValidatePolicyCmptTypeNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = testPolicyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        param.setPolicyCmptType("x");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertEquals(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, ml.getFirstMessage(
                Message.WARNING).getCode());
    }    
    
    public void testValidateMinInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = testPolicyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setMinInstances(0);
        
        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));

        // create mandatory instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("child1");
        paramChild.setMinInstances(1);
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));
        
        // create child and validate again
        ITestPolicyCmptRelation testRelation = testPolicyCmptTypeObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("child1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));
    }

    public void testValidateProductCmptNotExists() throws Exception{
        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        testPolicyCmptTypeObjectInput.setProductCmpt("productCmpt");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
        
        newProductCmpt(project, "productCmpt");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    public void testValidateProductComponentNotRequired() throws Exception{
        MessageList ml = testPolicyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(false);
        testPolicyCmptTypeObjectInput.setProductCmpt("x");
        ml = testPolicyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
    }
}
