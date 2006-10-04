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
    private ITestPolicyCmpt policyCmptTypeObjectExpected;
    private ITestPolicyCmpt policyCmptTypeObjectInput;
    
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
        
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        (policyCmptTypeObjectInput = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter1");
        (policyCmptTypeObjectExpected = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter2");
        
        testCase.setTestCaseType(testCaseType.getName());
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeObject",0);
        policyCmptTypeObjectExpected.initFromXml(paramEl);
        assertEquals("base.Test1", policyCmptTypeObjectExpected.getTestPolicyCmptTypeParameter());   
        assertEquals("productCmpt1", policyCmptTypeObjectExpected.getProductCmpt());
        assertEquals("policyCmptType1", policyCmptTypeObjectExpected.getName());
        assertEquals(2, policyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(3, policyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectExpected.getTestPolicyCmptRelation("relation2"), "base.Test2");  
        
        assertTrue(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isAccoziation());
        assertFalse(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[0].isComposition());
        assertFalse(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isAccoziation());
        assertTrue(policyCmptTypeObjectExpected.getTestPolicyCmptRelations()[1].isComposition());
    }

    public void testToXml() {
        policyCmptTypeObjectExpected.setTestPolicyCmptTypeParameter("base.Test2");
        policyCmptTypeObjectExpected.setProductCmpt("productCmpt1");
        policyCmptTypeObjectExpected.setName("Label1");
        policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmptRelation relation = policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptTypeParameter("relation1");
        ITestPolicyCmpt targetChild = relation.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptTypeParameter("base.Test4");
        policyCmptTypeObjectExpected.newTestAttributeValue();
        
        Element el = policyCmptTypeObjectExpected.toXml(newDocument());
        
        policyCmptTypeObjectExpected.setTestPolicyCmptTypeParameter("base.Test3");
        policyCmptTypeObjectExpected.setProductCmpt("productCmpt2");
        policyCmptTypeObjectExpected.setName("Label2");
        policyCmptTypeObjectExpected.newTestAttributeValue();
        policyCmptTypeObjectExpected.newTestAttributeValue();
        policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        
        policyCmptTypeObjectExpected.initFromXml(el);
        assertEquals("base.Test2", policyCmptTypeObjectExpected.getTestPolicyCmptTypeParameter());
        assertEquals("Label1", policyCmptTypeObjectExpected.getName());
        assertEquals(2, policyCmptTypeObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(1, policyCmptTypeObjectExpected.getTestAttributeValues().length);
        assertRelation(policyCmptTypeObjectExpected.getTestPolicyCmptRelation("relation1"),
                "base.Test4");
        assertEquals("productCmpt1", policyCmptTypeObjectExpected.getProductCmpt());
    }
    
    public void testInputOrExpectedResultObject(){
        assertFalse(policyCmptTypeObjectExpected.isInput());
        assertTrue(policyCmptTypeObjectExpected.isExpectedResult());
        assertFalse(policyCmptTypeObjectExpected.isCombined());
        
        ITestPolicyCmptRelation r = policyCmptTypeObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPc = r.newTargetTestPolicyCmptChild();
        assertFalse(testPc.isInput());
        assertTrue(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());
        
        assertTrue(policyCmptTypeObjectInput.isInput());
        assertFalse(policyCmptTypeObjectInput.isExpectedResult());
        assertFalse(policyCmptTypeObjectInput.isCombined());
        r = policyCmptTypeObjectInput.newTestPolicyCmptRelation();
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
        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        policyCmptTypeObjectInput.setTestPolicyCmptTypeParameter("x");
        ml = policyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateProductCmptIsRequired() throws Exception{
        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = policyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(true);
        ml = policyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        policyCmptTypeObjectInput.setProductCmpt("x");
        ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
    }
    
    public void testValidatePolicyCmptTypeNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = policyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        param.setPolicyCmptType("x");
        ml = policyCmptTypeObjectInput.validate();
        assertEquals(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, ml.getFirstMessage(
                Message.WARNING).getCode());
    }    
    
    public void testValidateMinInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = policyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setMinInstances(0);
        
        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));

        // create mandatory instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("child1");
        paramChild.setMinInstances(1);
        ml = policyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));
        
        // create child and validate again
        ITestPolicyCmptRelation testRelation = policyCmptTypeObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("child1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));
    }

    public void testValidateProductCmptNotExists() throws Exception{
        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        policyCmptTypeObjectInput.setProductCmpt("productCmpt");
        ml = policyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
        
        newProductCmpt(project, "productCmpt");
        ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    public void testValidateProductComponentNotRequired() throws Exception{
        MessageList ml = policyCmptTypeObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = policyCmptTypeObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(false);
        policyCmptTypeObjectInput.setProductCmpt("x");
        ml = policyCmptTypeObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
    }
}
