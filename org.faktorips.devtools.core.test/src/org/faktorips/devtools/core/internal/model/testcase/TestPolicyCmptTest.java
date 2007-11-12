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
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
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
    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter childTestPolicyCmptTypeParameter;
    
    /**
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        project = newIpsProject("TestProject");
        
        // PolicyCmptType1 and ProductCmptType1
        PolicyCmptType type1 = newPolicyCmptType(project, "PolicyCmptType1");
        IProductCmptType productCmptType1 = newProductCmptType(project, "ProductCmptType1");
        productCmptType1.setPolicyCmptType(type1.getQualifiedName());
        type1.setProductCmptType(productCmptType1.getQualifiedName());
        
        // PolicyCmptType2 and ProductCmptType2
        PolicyCmptType type2 = newPolicyCmptType(project, "PolicyCmptType2");
        IProductCmptType productCmptType2 = newProductCmptType(project, "ProductCmptType2");
        productCmptType2.setPolicyCmptType(type2.getQualifiedName());
        type2.setProductCmptType(productCmptType2.getQualifiedName());
        
        // PolicyCmptType3
        newPolicyCmptType(project, "PolicyCmptType3");
        
        // Product1 based ProductCmptType1
        IProductCmpt productCmpt1 = newProductCmpt(project, "ProductCmpt1");
        productCmpt1.setProductCmptType("ProductCmptType1");
        IProductCmptGeneration generationProductCmpt1 = (IProductCmptGeneration) productCmpt1.newGeneration(new GregorianCalendar());
        
        // Product2 based ProductCmptType2
        IProductCmpt productCmpt2 = newProductCmpt(project, "ProductCmpt2");
        productCmpt2.setProductCmptType("ProductCmptType2");
        productCmpt2.newGeneration(new GregorianCalendar());
        
        // Product3 based ProductCmptType2
        IProductCmpt productCmpt3 = newProductCmpt(project, "ProductCmpt3");
        productCmpt3.setProductCmptType("ProductCmptType2");
        productCmpt3.newGeneration(new GregorianCalendar());
        

        // relation1: PolicyCmptType1 -> PolicyCmptType2
        IPolicyCmptTypeAssociation relation = type1.newPolicyCmptTypeAssociation();
        relation.setTargetRoleSingular("relation1");
        relation.setTargetRolePlural("relations1");
        relation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relation.setTarget(type2.getQualifiedName());
        // assoziaotion1: ProductCmptType1 -> ProductCmptType2
        IProductCmptTypeAssociation association1 = productCmptType1.newProductCmptTypeAssociation();
        association1.setTarget("ProductCmptType2");
        association1.setTargetRoleSingular("relation1");
        association1.setTargetRolePlural("relations1");
        
        // relation2: PolicyCmptType1 -> PolicyCmptType1
        IPolicyCmptTypeAssociation relation2 = type1.newPolicyCmptTypeAssociation();
        relation2.setTargetRoleSingular("relation2");
        relation2.setTargetRolePlural("relations2");
        relation2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        relation2.setTarget(type1.getQualifiedName());
        //  assoziaotion2: ProductCmptType1 -> ProductCmptType1
        IProductCmptTypeAssociation association2 = productCmptType1.newProductCmptTypeAssociation();
        association2.setTarget("ProductCmptType1");
        association2.setTargetRoleSingular("relation2");
        association2.setTargetRolePlural("relations2");

        
        // Link: between Product1 -> Product2 (Relation1)
        generationProductCmpt1.newLink("relation1").setTarget(productCmpt2.getQualifiedName());
        // Link: between Product1 -> Product3 (Relation2)
        generationProductCmpt1.newLink("relation2").setTarget(productCmpt1.getQualifiedName());

        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ITestPolicyCmptTypeParameter parameter = testCaseType.newInputTestPolicyCmptTypeParameter();
        parameter.setName("testPolicyCmptTypeParameter1");
        parameter.setPolicyCmptType("PolicyCmptType1");

        childTestPolicyCmptTypeParameter = parameter.newTestPolicyCmptTypeParamChild();
        childTestPolicyCmptTypeParameter.setName("testPolicyCmptTypeParameterChild1");
        childTestPolicyCmptTypeParameter.setPolicyCmptType("PolicyCmptType2");
        childTestPolicyCmptTypeParameter.setRelation("relation1");
        childTestPolicyCmptTypeParameter.setTestParameterType(TestParameterType.INPUT);
        childTestPolicyCmptTypeParameter.setMaxInstances(2);
        childTestPolicyCmptTypeParameter.setRequiresProductCmpt(true);
        
        ITestPolicyCmptTypeParameter childTestPolicyCmptTypeParameter2 = parameter.newTestPolicyCmptTypeParamChild();
        childTestPolicyCmptTypeParameter2.setName("testPolicyCmptTypeParameterChild2");
        childTestPolicyCmptTypeParameter2.setPolicyCmptType("PolicyCmptType1");
        childTestPolicyCmptTypeParameter2.setRelation("relation2");
        childTestPolicyCmptTypeParameter2.setTestParameterType(TestParameterType.INPUT);
        childTestPolicyCmptTypeParameter2.setMaxInstances(2);
        childTestPolicyCmptTypeParameter2.setRequiresProductCmpt(true);

        ITestPolicyCmptTypeParameter parameter2 = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        parameter2.setName("testPolicyCmptTypeParameter2");
        parameter2.setPolicyCmptType("PolicyCmptType2");
        ITestPolicyCmptTypeParameter parameter3 = testCaseType.newCombinedPolicyCmptTypeParameter();
        parameter3.setName("testPolicyCmptTypeParameter3");
        parameter3.setPolicyCmptType("PolicyCmptType3");
        
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        (testPolicyCmptObjectInput = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter1");
        testPolicyCmptObjectInput.setName(testPolicyCmptObjectInput.getTestPolicyCmptTypeParameter());
        (testPolicyCmptObjectExpected = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter2");
        testPolicyCmptObjectExpected.setName(testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        ITestPolicyCmpt testPolicyCmptObjectCombined;
        (testPolicyCmptObjectCombined = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter3");
        testPolicyCmptObjectCombined.setName(testPolicyCmptObjectCombined.getTestPolicyCmptTypeParameter());
        
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
        assertEquals(3, testCase.getTestPolicyCmpts().length);
        
        // test root
        ITestPolicyCmpt policyCmpt = testCase.newTestPolicyCmpt();
        assertEquals(4, testCase.getTestPolicyCmpts().length);
        
        policyCmpt.delete();
        assertEquals(3, testCase.getTestPolicyCmpts().length);
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
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPolicyCmptObjectInput.setTestPolicyCmptTypeParameter("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateProductCmptIsRequired() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(true);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
    }
    
    public void testValidatePolicyCmptTypeNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        param.setPolicyCmptType("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertEquals(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, ml.getFirstMessage(
                Message.WARNING).getCode());
    }    
    
    public void testValidateMinInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setMinInstances(0);
        
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));

        // create mandatory instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("ProductCmpt1");
        paramChild.setMinInstances(1);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));
        
        // create child and validate again
        ITestPolicyCmptRelation testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));
    }
    
    public void testValidateMaxInstancesNotReached() throws Exception{
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));

        // create instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("ProductCmpt1");
        paramChild.setMaxInstances(1);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
        
        // create two child and validate again
        ITestPolicyCmptRelation testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
        testRelation = testPolicyCmptObjectInput.newTestPolicyCmptRelation();
        testRelation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testRelation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));        
    }
    
    public void testValidateProductCmptNotExists() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        testPolicyCmptObjectInput.setProductCmpt("productCmpt");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
        
        newProductCmpt(project, "productCmpt");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    public void testValidateProductComponentNotRequired() throws Exception{
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
        
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        param.setRequiresProductCmpt(false);
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
    }
    
    public void testGetIndexOfChildTestPolicyCmpt() throws CoreException{
        ITestPolicyCmptRelation relationPos0 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "A");
        ITestPolicyCmptRelation relationPos1 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "B");
        ITestPolicyCmptRelation relationPos2 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "C");
        
        ITestPolicyCmpt testPolicyCmptChild0 = relationPos0.newTargetTestPolicyCmptChild();
        ITestPolicyCmpt testPolicyCmptChild1 = relationPos1.newTargetTestPolicyCmptChild();
        ITestPolicyCmpt testPolicyCmptChild2 = relationPos2.newTargetTestPolicyCmptChild();
        
        assertEquals(0, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild0));
        assertEquals(1, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild1));
        assertEquals(2, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild2));
    }
    
    public void testMoveTestPolicyCmptRelations() throws CoreException{
        ITestPolicyCmptRelation relationPos0 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "A");
        ITestPolicyCmptRelation relationPos1 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "B");
        ITestPolicyCmptRelation relationPos2 = testPolicyCmptObjectInput.addTestPcTypeRelation(childTestPolicyCmptTypeParameter, "testValueParameter1Child", "C");
        
        assertEquals(relationPos0, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[0]);
        assertEquals(relationPos1, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[1]);
        assertEquals(relationPos2, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[2]);
        
        testPolicyCmptObjectInput.moveTestPolicyCmptRelations(new int[]{1,2}, true);
        assertTrue(testPolicyCmptObjectInput.getIpsSrcFile().isDirty());
        
        assertEquals(relationPos1, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[0]);
        assertEquals(relationPos2, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[1]);
        assertEquals(relationPos0, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[2]);

        testPolicyCmptObjectInput.moveTestPolicyCmptRelations(new int[]{0}, false);

        assertEquals(relationPos2, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[0]);
        assertEquals(relationPos1, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[1]);
        assertEquals(relationPos0, testPolicyCmptObjectInput.getTestPolicyCmptRelations()[2]);
    }
    
    /*
     * Test that adding test objects doesn't end in a fix sort order
     */
    public void testAddTestPcTypeRelationCorrectOrder() throws CoreException{
        ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParam = childTestPolicyCmptTypeParameter.getParentTestPolicyCmptTypeParam();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParamChilds = parentTestPolicyCmptTypeParam.getTestPolicyCmptTypeParamChilds();

        testPolicyCmptObjectInput.addTestPcTypeRelation(testPolicyCmptTypeParamChilds[1], "ProductCmpt1", null);
        testPolicyCmptObjectInput.addTestPcTypeRelation(testPolicyCmptTypeParamChilds[0], "ProductCmpt3", null);
        
        assertFalse(testCase.validate(project).containsErrorMsg());
        assertFalse(testCase.containsDifferenceToModel());
    }
    
    public void testUpdateDefaultTestAttributeValues() throws CoreException{
        // create model objects
        IPolicyCmptType policy = newPolicyCmptType(project, "Policy");
        IPolicyCmptTypeAssociation relation = policy.newPolicyCmptTypeAssociation();
        relation.setTargetRoleSingular("childPolicyCmptType");
        relation.setTarget("Coverage");
        IPolicyCmptType coverage = newPolicyCmptType(project, "Coverage");
        IProductCmpt product = newProductCmpt(project, "Product");
        product.setProductCmptType(coverage.getQualifiedName());
        
        // add attributes with their defaults
        IPolicyCmptTypeAttribute attr = policy.newPolicyCmptTypeAttribute();
        attr.setName("attrPolicy");
        attr.setDatatype("String");
        attr.setDefaultValue("attrPolicy_Default");
        attr = coverage.newPolicyCmptTypeAttribute();
        attr.setName("attrCoverage");
        attr.setDatatype("String");
        attr.setDefaultValue("attrCoverage_Default");
        attr.setProductRelevant(true);

        IProductCmptGeneration generation = (IProductCmptGeneration) product.newGeneration(new GregorianCalendar(1742, 12, 1));
        IConfigElement ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute(attr.getName());
        ce.setValue("attrCoverage_Default_Product");
        
        // create test case type side
        ITestPolicyCmptTypeParameter tp = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter();
        // clean the default test data, because we use two new policy cmpts
        ITestPolicyCmptTypeParameter[] childs = tp.getTestPolicyCmptTypeParamChilds();
        for (int i = 0; i < childs.length; i++) {
            tp.removeTestPolicyCmptTypeParamChild((TestPolicyCmptTypeParameter)childs[i]);
        }
        tp.setPolicyCmptType(policy.getQualifiedName());
        ITestPolicyCmptTypeParameter tpChild = tp.newTestPolicyCmptTypeParamChild();
        tpChild.setTestParameterType(TestParameterType.INPUT);
        tpChild.setName("childPolicyCmptType");
        tpChild.setRelation("childPolicyCmptType");
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
        
        assertFalse(testCaseType.validate(testCaseType.getIpsProject()).containsErrorMsg());
        assertFalse(testCase.validate(project).containsErrorMsg());
        
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
    
    public void testFindProductCmpsCurrentGeneration_notProdRelevant() throws CoreException{
        // test find method if no product is specified, e.g. test object is not product relvant
        // in this case the find method should return null
        IProductCmptGeneration generation = null;
        testPolicyCmptObjectInput.setProductCmpt(null);
        generation = ((TestPolicyCmpt)testPolicyCmptObjectInput).findProductCmpsCurrentGeneration();
        assertNull(generation);
        testPolicyCmptObjectInput.setProductCmpt("");
        generation = ((TestPolicyCmpt)testPolicyCmptObjectInput).findProductCmpsCurrentGeneration();
        assertNull(generation);
    }
    
    private class TestContent{
        private IPolicyCmptType policy;
        private IPolicyCmptTypeAssociation coverages;
        private IPolicyCmptType coverage;
        private IProductCmpt policyProduct;
        private IProductCmpt coverageProductA; // is related in policyProduct
        private IProductCmpt coverageProductB; // is not related in policyProduct

        private ITestPolicyCmptTypeParameter parameter;
        private ITestPolicyCmptTypeParameter childParameter;
        
        public void init(IIpsProject project) throws CoreException {
            policy = newPolicyCmptType(project, "Policy");
            policy.setConfigurableByProductCmptType(true);
            ProductCmptType productCmptTypePolicy = newProductCmptType(project, "PolicyType");
            policy.setProductCmptType(productCmptTypePolicy.getQualifiedName());
            productCmptTypePolicy.setPolicyCmptType(policy.getQualifiedName());
            
            coverage = newPolicyCmptType(project, "Coverage");
            coverage.setConfigurableByProductCmptType(true);
            ProductCmptType productCmptTypeCoverage = newProductCmptType(project, "CoverageType");
            coverage.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            productCmptTypeCoverage.setPolicyCmptType(coverage.getQualifiedName());
            
            coverages = policy.newPolicyCmptTypeAssociation();
            coverages.setTarget(coverage.getQualifiedName());
            coverages.setTargetRoleSingular("Coverage");
            coverages.setTargetRolePlural("Coverages");
            coverages.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
            
            IProductCmptTypeAssociation association = productCmptTypePolicy.newProductCmptTypeAssociation();
            association.setTarget("CoverageType");
            association.setTargetRoleSingular("Coverage");
            association.setTargetRolePlural("Coverages");
            
            policyProduct = newProductCmpt(project, "PolicyA 2007-09");
            policyProduct.setProductCmptType(productCmptTypePolicy.getQualifiedName());
            coverageProductA = newProductCmpt(project, "CoverageA 2007-09");
            coverageProductA.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            coverageProductA.newGeneration(new GregorianCalendar());
            coverageProductB = newProductCmpt(project, "CoverageB 2007-09");
            coverageProductB.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            coverageProductB.newGeneration(new GregorianCalendar());
            
            parameter = testCaseType.newCombinedPolicyCmptTypeParameter();
            parameter.setPolicyCmptType(policy.getQualifiedName());
            parameter.setName("PolicyParam");
            parameter.setRequiresProductCmpt(true);
            childParameter = parameter.newTestPolicyCmptTypeParamChild();
            childParameter.setPolicyCmptType(coverage.getQualifiedName());
            childParameter.setRelation(coverages.getName());
            childParameter.setName("CoverageParam");            
            childParameter.setRequiresProductCmpt(true);
            
            IProductCmptGeneration generation = (IProductCmptGeneration)policyProduct.newGeneration(new GregorianCalendar());
            IProductCmptLink productCmptRelation = generation.newLink("CoverageType");
            productCmptRelation.setTarget(coverageProductA.getQualifiedName());
            
        }
    }
    
  public void testValidateAllowedProductCmpt() throws Exception {
      // test these two validation messages:
      //  Error: WrongProductCmptOfRelation
      //  Warning: ParentProductCmptOfRelationNotSpecified
      ITestPolicyCmpt testPolicyCmpt;
      ITestPolicyCmpt testPolicyCmptChild;
      MessageList ml;
      
      TestContent testContent = new TestContent();
      testContent.init(project);
      
      testPolicyCmpt = testCase.newTestPolicyCmpt();
      testPolicyCmpt.setTestPolicyCmptTypeParameter(testContent.parameter.getName());
      testPolicyCmpt.setName(testContent.parameter.getName());
      ITestPolicyCmptRelation testPolicyCmptRelation = testPolicyCmpt.newTestPolicyCmptRelation();
      testPolicyCmptRelation.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
      testPolicyCmptChild = testPolicyCmptRelation.newTargetTestPolicyCmptChild();
      testPolicyCmptChild.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
      testPolicyCmptChild.setName(testContent.childParameter.getName());
      
      // wrong product cmpt of parent (root)
      testPolicyCmpt.setProductCmpt(testContent.coverageProductA.getQualifiedName());
      ml = testPolicyCmpt.validate(project);
      assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertEquals(ml.getFirstMessage(Message.ERROR).getCode(), ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION);
      
      // no product definied, no error about a wrong product cmpt and no warning about missing parent product cmpt
      testPolicyCmptChild.setProductCmpt("");
      ml = testPolicyCmptChild.validate(project);
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
      
      // product definied but parent product cmpt not specified, no error but warning
      testPolicyCmpt.setProductCmpt("");
      testPolicyCmptChild.setProductCmpt(testContent.policyProduct.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
      assertEquals(ml.getFirstMessage(Message.WARNING).getCode(), ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED);
      // product definied but parent not specified, no error but warning
      testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
      // product definied but parent not specified and not product relevant, no error no warning
      testContent.parameter.setRequiresProductCmpt(false);
      testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
      testContent.parameter.setRequiresProductCmpt(true);
      
      // correct product cmpt of parent (root)
      testPolicyCmpt.setProductCmpt(testContent.policyProduct.getQualifiedName());
      ml = testPolicyCmpt.validate(project);
      
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      // with wrong child a)
      testPolicyCmptChild.setProductCmpt(testContent.policyProduct.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
      // with wrong child a)
      testPolicyCmptChild.setProductCmpt(testContent.coverageProductB.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
       
      // with correct child
      testPolicyCmpt.setProductCmpt(testContent.policyProduct.getQualifiedName());
      testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
      ml = testPolicyCmptChild.validate(project);
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_RELATION));
      assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_RELATION_NOT_SPECIFIED));
  }
}
