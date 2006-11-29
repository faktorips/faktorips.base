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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseHierarchyPath;
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
    private ITestPolicyCmpt testPolicyCmptObjectExpected;
    private ITestPolicyCmpt testPolicyCmptObjectInput;
    
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
        (testPolicyCmptObjectInput = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter1");
        testPolicyCmptObjectInput.setName(testPolicyCmptObjectInput.getTestPolicyCmptTypeParameter());
        (testPolicyCmptObjectExpected = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testValueParameter2");
        testPolicyCmptObjectExpected.setName(testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        testCase.setTestCaseType(testCaseType.getName());
    }
 
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl,"PolicyCmptTypeObject",0);
        testPolicyCmptObjectExpected.initFromXml(paramEl);
        assertEquals("base.Test1", testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());   
        assertEquals("productCmpt1", testPolicyCmptObjectExpected.getProductCmpt());
        assertEquals("policyCmptType1", testPolicyCmptObjectExpected.getName());
        assertEquals(2, testPolicyCmptObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(3, testPolicyCmptObjectExpected.getTestAttributeValues().length);
        assertRelation(testPolicyCmptObjectExpected.getTestPolicyCmptRelation("relation2"), "base.Test2");  
        
        assertTrue(testPolicyCmptObjectExpected.getTestPolicyCmptRelations()[0].isAccoziation());
        assertFalse(testPolicyCmptObjectExpected.getTestPolicyCmptRelations()[0].isComposition());
        assertFalse(testPolicyCmptObjectExpected.getTestPolicyCmptRelations()[1].isAccoziation());
        assertTrue(testPolicyCmptObjectExpected.getTestPolicyCmptRelations()[1].isComposition());
    }

    public void testToXml() {
        testPolicyCmptObjectExpected.setTestPolicyCmptTypeParameter("base.Test2");
        testPolicyCmptObjectExpected.setProductCmpt("productCmpt1");
        testPolicyCmptObjectExpected.setName("Label1");
        testPolicyCmptObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmptRelation relation = testPolicyCmptObjectExpected.newTestPolicyCmptRelation();
        relation.setTestPolicyCmptTypeParameter("relation1");
        ITestPolicyCmpt targetChild = relation.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptTypeParameter("base.Test4");
        testPolicyCmptObjectExpected.newTestAttributeValue();
        
        Element el = testPolicyCmptObjectExpected.toXml(newDocument());
        
        testPolicyCmptObjectExpected.setTestPolicyCmptTypeParameter("base.Test3");
        testPolicyCmptObjectExpected.setProductCmpt("productCmpt2");
        testPolicyCmptObjectExpected.setName("Label2");
        testPolicyCmptObjectExpected.newTestAttributeValue();
        testPolicyCmptObjectExpected.newTestAttributeValue();
        testPolicyCmptObjectExpected.newTestPolicyCmptRelation();
        
        testPolicyCmptObjectExpected.initFromXml(el);
        assertEquals("base.Test2", testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        assertEquals("Label1", testPolicyCmptObjectExpected.getName());
        assertEquals(2, testPolicyCmptObjectExpected.getTestPolicyCmptRelations().length);
        assertEquals(1, testPolicyCmptObjectExpected.getTestAttributeValues().length);
        assertRelation(testPolicyCmptObjectExpected.getTestPolicyCmptRelation("relation1"),
                "base.Test4");
        assertEquals("productCmpt1", testPolicyCmptObjectExpected.getProductCmpt());
    }
    
    public void testNewAndDelete(){
        assertEquals(2, testCase.getTestPolicyCmpts().length);
        
        // test root
        ITestPolicyCmpt policyCmpt = testCase.newTestPolicyCmpt();
        assertEquals(3, testCase.getTestPolicyCmpts().length);
        
        policyCmpt.delete();
        assertEquals(2, testCase.getTestPolicyCmpts().length);
        assertEquals(testPolicyCmptObjectExpected, testCase.getExpectedResultTestPolicyCmpts()[0]);
        assertEquals(testPolicyCmptObjectInput, testCase.getInputTestPolicyCmpts()[0]);
        
        // test child
        ITestPolicyCmptRelation rel = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        ITestPolicyCmpt child = rel.newTargetTestPolicyCmptChild();
        assertEquals(1, testPolicyCmptObjectInput.getTestPolicyCmptRelations().length);
        
        child.delete();
        assertEquals(0, testPolicyCmptObjectInput.getTestPolicyCmptRelations().length);
    }
                           
    
    public void testInputOrExpectedResultObject(){
        assertFalse(testPolicyCmptObjectExpected.isInput());
        assertTrue(testPolicyCmptObjectExpected.isExpectedResult());
        assertFalse(testPolicyCmptObjectExpected.isCombined());
        
        ITestPolicyCmptRelation r = testPolicyCmptObjectExpected.newTestPolicyCmptRelation();
        ITestPolicyCmpt testPc = r.newTargetTestPolicyCmptChild();
        assertFalse(testPc.isInput());
        assertTrue(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());
        
        assertTrue(testPolicyCmptObjectInput.isInput());
        assertFalse(testPolicyCmptObjectInput.isExpectedResult());
        assertFalse(testPolicyCmptObjectInput.isCombined());
        r = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
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
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPolicyCmptObjectInput.setTestPolicyCmptTypeParameter("x");
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateProductCmptIsRequired() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(true);
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
    }
    
    public void testValidatePolicyCmptTypeNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        param.setPolicyCmptType("x");
        ml = testPolicyCmptObjectInput.validate();
        assertEquals(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, ml.getFirstMessage(
                Message.WARNING).getCode());
    }    
    
    public void testValidateMinInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setMinInstances(0);
        
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));

        // create mandatory instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("child1");
        paramChild.setMinInstances(1);
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));
        
        // create child and validate again
        ITestPolicyCmptRelation testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("child1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));
    }
    
    public void testValidateMaxInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));

        // create instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("child1");
        paramChild.setMaxInstances(1);
        ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
        
        // create two child and validate again
        ITestPolicyCmptRelation testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("child1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
        testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("child1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));        
    }
    
    public void testValidateProductCmptNotExists() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        testPolicyCmptObjectInput.setProductCmpt("productCmpt");
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
        
        newProductCmpt(project, "productCmpt");
        ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    public void testValidateProductComponentNotRequired() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate();
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(false);
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
    }
    
    public void testUpdateDefaultTestAttributeValues() throws CoreException{
        // create model objects
        IPolicyCmptType policy = newPolicyCmptType(project, "Policy");
        IPolicyCmptType coverage = newPolicyCmptType(project, "Coverage");
        IProductCmpt product = newProductCmpt(project, "Product");
        product.setPolicyCmptType(coverage.getQualifiedName());
        // add attributes with their defaults
        IAttribute attr = policy.newAttribute();
        attr.setName("attrPolicy");
        attr.setDatatype("String");
        attr.setDefaultValue("attrPolicy_Default");
        attr = coverage.newAttribute();
        attr.setName("attrCoverage");
        attr.setDatatype("String");
        attr.setDefaultValue("attrCoverage_Default");
        IProductCmptGeneration generation = (IProductCmptGeneration) product.newGeneration(new GregorianCalendar(1742, 12, 1));
        IConfigElement ce = generation.newConfigElement();
        ce.setPcTypeAttribute(attr.getName());
        ce.setValue("attrCoverage_Default_Product");
        
        // create test case type side
        ITestPolicyCmptTypeParameter tp = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        tp.setPolicyCmptType(policy.getQualifiedName());
        ITestPolicyCmptTypeParameter tpChild = tp.newTestPolicyCmptTypeParamChild();
        tpChild.setName("childPolicyCmptType");
        tpChild.setRelation("childPolicyCmptType");
        tpChild.setPolicyCmptType("childPolicyCmptType");
        tpChild.setPolicyCmptType(coverage.getQualifiedName());
        ITestAttribute testAttr = tp.newInputTestAttribute();
        testAttr.setName("attrPolicy");
        testAttr.setAttribute("attrPolicy");
        testAttr = tpChild.newInputTestAttribute();
        testAttr.setName("attrCoverage");
        testAttr.setAttribute("attrCoverage");
        
        // create test case side
        ITestPolicyCmptRelation tr = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        tr.setTestPolicyCmptTypeParameter(tpChild.getName());
        ITestPolicyCmpt tcChild = tr.newTargetTestPolicyCmptChild();
        tcChild.setTestPolicyCmptTypeParameter(tpChild.getName());
        tcChild.setName(tpChild.getName());
        // use delta fix to add all missing test attributes
        testCase.fixDifferences(testCase.computeDeltaToTestCaseType());
        String path = testPolicyCmptObjectInput.getTestParameterName() + TestCaseHierarchyPath.SEPARATOR + tpChild.getRelation() +  TestCaseHierarchyPath.SEPARATOR + tpChild.getName();
        tcChild = testCase.findTestPolicyCmpt(path);
        
        // get the to be tested test attributes
        ITestAttributeValue testAttrValuePolicy = testPolicyCmptObjectInput.getTestAttributeValue("attrPolicy");
        ITestAttributeValue testAttrValueCoverage = tcChild.getTestAttributeValue("attrCoverage");
        
        // assert that the delta fix has set the defaults
        assertEquals("attrPolicy_Default", testAttrValuePolicy.getValue());
        assertEquals("attrCoverage_Default", testAttrValueCoverage.getValue());

        testAttrValuePolicy.setValue("x");
        testAttrValueCoverage.setValue("y");
        assertFalse("attrPolicy_Default".equals(testAttrValuePolicy.getValue()));
        assertFalse("attrCoverage_Default".equals(testAttrValueCoverage.getValue()));

        testPolicyCmptObjectInput.updateDefaultTestAttributeValues();
        tcChild.updateDefaultTestAttributeValues();
        assertEquals("attrPolicy_Default", testAttrValuePolicy.getValue());
        assertEquals("attrCoverage_Default", testAttrValueCoverage.getValue());
        
        tcChild.setProductCmpt(product.getQualifiedName());
        tcChild.updateDefaultTestAttributeValues();
        assertEquals(testAttrValueCoverage.getValue(), "attrCoverage_Default_Product");
        
        //
        // tests for the attribute value update default function, will be performed here because the 
        // test context is already set and we could reuse it
        //
        
        testAttrValueCoverage.setValue("x");
        assertFalse("attrCoverage_Default_Product".equals(testAttrValuePolicy.getValue()));
        testAttrValueCoverage.updateDefaultTestAttributeValue();
        assertEquals(testAttrValueCoverage.getValue(), "attrCoverage_Default_Product");
        
        tcChild.setProductCmpt("");
        testAttrValueCoverage.updateDefaultTestAttributeValue();
        assertEquals(testAttrValueCoverage.getValue(), "attrCoverage_Default");
    }
}
