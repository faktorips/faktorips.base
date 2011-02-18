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

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.testcasetype.TestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
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

    @Override
    @Before
    public void setUp() throws Exception {
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
        PolicyCmptType type3 = newPolicyCmptType(project, "PolicyCmptType3");

        // PolicyCmptType4 abstract
        PolicyCmptType newPolicyCmptType4 = newPolicyCmptType(project, "PolicyCmptType4");
        newPolicyCmptType4.setAbstract(true);

        // PolicyCmptType5 sub-type of PolicyCmptType4
        PolicyCmptType newPolicyCmptType5 = newPolicyCmptType(project, "PolicyCmptType5");
        newPolicyCmptType5.setSupertype(newPolicyCmptType4.getQualifiedName());

        // PolicyCmptType6 sub-type of PolicyCmptType1
        PolicyCmptType newPolicyCmptType6 = newPolicyCmptType(project, "PolicyCmptType6");
        newPolicyCmptType6.setSupertype(type1.getQualifiedName());

        // Product1 based ProductCmptType1
        IProductCmpt productCmpt1 = newProductCmpt(project, "ProductCmpt1");
        productCmpt1.setProductCmptType(productCmptType1.getQualifiedName());
        IProductCmptGeneration generationProductCmpt1 = (IProductCmptGeneration)productCmpt1
                .newGeneration(new GregorianCalendar());

        // Product2 based ProductCmptType2
        IProductCmpt productCmpt2 = newProductCmpt(project, "ProductCmpt2");
        productCmpt2.setProductCmptType(productCmptType2.getQualifiedName());
        productCmpt2.newGeneration(new GregorianCalendar());

        // Product3 based ProductCmptType2
        IProductCmpt productCmpt3 = newProductCmpt(project, "ProductCmpt3");
        productCmpt3.setProductCmptType(productCmptType2.getQualifiedName());
        productCmpt3.newGeneration(new GregorianCalendar());

        // association1: PolicyCmptType1 -> PolicyCmptType2
        IPolicyCmptTypeAssociation association = type1.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("association1");
        association.setTargetRolePlural("associations1");
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(type2.getQualifiedName());
        // assoziaotion1: ProductCmptType1 -> ProductCmptType2
        IProductCmptTypeAssociation association1 = productCmptType1.newProductCmptTypeAssociation();
        association1.setTarget(productCmptType2.getQualifiedName());
        association1.setTargetRoleSingular("association1");
        association1.setTargetRolePlural("associations1");

        // association2: PolicyCmptType1 -> PolicyCmptType1
        IPolicyCmptTypeAssociation association2 = type1.newPolicyCmptTypeAssociation();
        association2.setTargetRoleSingular("association2");
        association2.setTargetRolePlural("associations2");
        association2.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association2.setTarget(type1.getQualifiedName());
        // assoziaotion2: ProductCmptType1 -> ProductCmptType1
        IProductCmptTypeAssociation association3 = productCmptType1.newProductCmptTypeAssociation();
        association3.setTarget(productCmptType2.getQualifiedName());
        association3.setTargetRoleSingular("association3");
        association3.setTargetRolePlural("associations3");

        // Link: between Product1 -> Product2 (Association1)
        generationProductCmpt1.newLink("association1").setTarget(productCmpt2.getQualifiedName());
        // Link: between Product1 -> Product3 (Association2)
        generationProductCmpt1.newLink("association2").setTarget(productCmpt1.getQualifiedName());

        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ITestPolicyCmptTypeParameter parameter = testCaseType.newInputTestPolicyCmptTypeParameter();
        parameter.setName("testPolicyCmptTypeParameter1");
        parameter.setPolicyCmptType(type1.getQualifiedName());

        childTestPolicyCmptTypeParameter = parameter.newTestPolicyCmptTypeParamChild();
        childTestPolicyCmptTypeParameter.setName("testPolicyCmptTypeParameterChild1");
        childTestPolicyCmptTypeParameter.setPolicyCmptType(type2.getQualifiedName());
        childTestPolicyCmptTypeParameter.setAssociation("association1");
        childTestPolicyCmptTypeParameter.setTestParameterType(TestParameterType.INPUT);
        childTestPolicyCmptTypeParameter.setMaxInstances(2);
        childTestPolicyCmptTypeParameter.setRequiresProductCmpt(true);

        ITestPolicyCmptTypeParameter childTestPolicyCmptTypeParameter2 = parameter.newTestPolicyCmptTypeParamChild();
        childTestPolicyCmptTypeParameter2.setName("testPolicyCmptTypeParameterChild2");
        childTestPolicyCmptTypeParameter2.setPolicyCmptType(type1.getQualifiedName());
        childTestPolicyCmptTypeParameter2.setAssociation("association2");
        childTestPolicyCmptTypeParameter2.setTestParameterType(TestParameterType.INPUT);
        childTestPolicyCmptTypeParameter2.setMaxInstances(2);
        childTestPolicyCmptTypeParameter2.setRequiresProductCmpt(true);

        ITestPolicyCmptTypeParameter parameter2 = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        parameter2.setName("testPolicyCmptTypeParameter2");
        parameter2.setPolicyCmptType(type2.getQualifiedName());
        ITestPolicyCmptTypeParameter parameter3 = testCaseType.newCombinedPolicyCmptTypeParameter();
        parameter3.setName("testPolicyCmptTypeParameter3");
        parameter3.setPolicyCmptType(type3.getQualifiedName());

        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        (testPolicyCmptObjectInput = testCase.newTestPolicyCmpt())
                .setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter1");
        testPolicyCmptObjectInput.setName(testPolicyCmptObjectInput.getTestPolicyCmptTypeParameter());
        (testPolicyCmptObjectExpected = testCase.newTestPolicyCmpt())
                .setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter2");
        testPolicyCmptObjectExpected.setName(testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        ITestPolicyCmpt testPolicyCmptObjectCombined;
        (testPolicyCmptObjectCombined = testCase.newTestPolicyCmpt())
                .setTestPolicyCmptTypeParameter("testPolicyCmptTypeParameter3");
        testPolicyCmptObjectCombined.setName(testPolicyCmptObjectCombined.getTestPolicyCmptTypeParameter());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeObject", 0);
        testPolicyCmptObjectExpected.initFromXml(paramEl);
        assertEquals("base.Test1", testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        assertEquals("productCmpt1", testPolicyCmptObjectExpected.getProductCmpt());
        assertEquals("policyCmptType1", testPolicyCmptObjectExpected.getName());
        assertEquals(2, testPolicyCmptObjectExpected.getTestPolicyCmptLinks().length);
        assertEquals(3, testPolicyCmptObjectExpected.getTestAttributeValues().length);
        assertAssociation(testPolicyCmptObjectExpected.getTestPolicyCmptLink("association2"), "base.Test2");

        assertTrue(testPolicyCmptObjectExpected.getTestPolicyCmptLinks()[0].isAccoziation());
        assertFalse(testPolicyCmptObjectExpected.getTestPolicyCmptLinks()[0].isComposition());
        assertFalse(testPolicyCmptObjectExpected.getTestPolicyCmptLinks()[1].isAccoziation());
        assertTrue(testPolicyCmptObjectExpected.getTestPolicyCmptLinks()[1].isComposition());
    }

    @Test
    public void testToXml() {
        testPolicyCmptObjectExpected.setTestPolicyCmptTypeParameter("base.Test2");
        testPolicyCmptObjectExpected.setProductCmpt("productCmpt1");
        testPolicyCmptObjectExpected.setName("Label1");
        testPolicyCmptObjectExpected.newTestPolicyCmptLink();
        ITestPolicyCmptLink association = testPolicyCmptObjectExpected.newTestPolicyCmptLink();
        association.setTestPolicyCmptTypeParameter("association1");
        ITestPolicyCmpt targetChild = association.newTargetTestPolicyCmptChild();
        targetChild.setTestPolicyCmptTypeParameter("base.Test4");
        testPolicyCmptObjectExpected.newTestAttributeValue();

        Element el = testPolicyCmptObjectExpected.toXml(newDocument());

        testPolicyCmptObjectExpected.setTestPolicyCmptTypeParameter("base.Test3");
        testPolicyCmptObjectExpected.setProductCmpt("productCmpt2");
        testPolicyCmptObjectExpected.setName("Label2");
        testPolicyCmptObjectExpected.newTestAttributeValue();
        testPolicyCmptObjectExpected.newTestAttributeValue();
        testPolicyCmptObjectExpected.newTestPolicyCmptLink();

        testPolicyCmptObjectExpected.initFromXml(el);
        assertEquals("base.Test2", testPolicyCmptObjectExpected.getTestPolicyCmptTypeParameter());
        assertEquals("Label1", testPolicyCmptObjectExpected.getName());
        assertEquals(2, testPolicyCmptObjectExpected.getTestPolicyCmptLinks().length);
        assertEquals(1, testPolicyCmptObjectExpected.getTestAttributeValues().length);
        assertAssociation(testPolicyCmptObjectExpected.getTestPolicyCmptLink("association1"), "base.Test4");
        assertEquals("productCmpt1", testPolicyCmptObjectExpected.getProductCmpt());
    }

    @Test
    public void testNewAndDelete() {
        assertEquals(3, testCase.getTestPolicyCmpts().length);

        // test root
        ITestPolicyCmpt policyCmpt = testCase.newTestPolicyCmpt();
        assertEquals(4, testCase.getTestPolicyCmpts().length);

        policyCmpt.delete();
        assertEquals(3, testCase.getTestPolicyCmpts().length);
        assertEquals(testPolicyCmptObjectExpected, testCase.getExpectedResultTestPolicyCmpts()[0]);
        assertEquals(testPolicyCmptObjectInput, testCase.getInputTestPolicyCmpts()[0]);

        // test child
        ITestPolicyCmptLink rel = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        ITestPolicyCmpt child = rel.newTargetTestPolicyCmptChild();
        assertEquals(1, testPolicyCmptObjectInput.getTestPolicyCmptLinks().length);

        child.delete();
        assertEquals(0, testPolicyCmptObjectInput.getTestPolicyCmptLinks().length);
    }

    @Test
    public void testInputOrExpectedResultObject() {
        assertFalse(testPolicyCmptObjectExpected.isInput());
        assertTrue(testPolicyCmptObjectExpected.isExpectedResult());
        assertFalse(testPolicyCmptObjectExpected.isCombined());

        ITestPolicyCmptLink r = testPolicyCmptObjectExpected.newTestPolicyCmptLink();
        ITestPolicyCmpt testPc = r.newTargetTestPolicyCmptChild();
        assertFalse(testPc.isInput());
        assertTrue(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());

        assertTrue(testPolicyCmptObjectInput.isInput());
        assertFalse(testPolicyCmptObjectInput.isExpectedResult());
        assertFalse(testPolicyCmptObjectInput.isCombined());
        r = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        testPc = r.newTargetTestPolicyCmptChild();
        assertTrue(testPc.isInput());
        assertFalse(testPc.isExpectedResult());
        assertFalse(testPc.isCombined());
    }

    private void assertAssociation(ITestPolicyCmptLink association, String policyCmptTypeName) {
        assertNotNull(association);
        ITestPolicyCmpt targetChild = null;
        try {
            targetChild = association.findTarget();
            assertNotNull(targetChild);
            assertEquals(policyCmptTypeName, targetChild.getTestPolicyCmptTypeParameter());
        } catch (CoreException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testValidateTestCaseTypeParamNotFound() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPolicyCmptObjectInput.setTestPolicyCmptTypeParameter("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    @Test
    public void testValidateProductCmptIsRequired() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));

        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setRequiresProductCmpt(true);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));

        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate(project);
        // even though x does not exist, there is no error-message for required ProdCmpt
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        // but there is another that sais "x" cannot be found
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        // any value allowd for TestPolicyCmpts that do not require a productcmpt
        param.setRequiresProductCmpt(false);
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        testPolicyCmptObjectInput.setProductCmpt("");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
        testPolicyCmptObjectInput.setProductCmpt(null);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_IS_REQUIRED));
    }

    @Test
    public void testValidatePolicyCmptTypeNotExistsWithGivenProduct() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        param.setPolicyCmptType("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertEquals(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS,
                ml.getFirstMessage(Message.WARNING).getCode());
    }

    @Test
    public void testValidatePolicyCmptTypeNotExistsWithoutProduct() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType(policyCmptType.getQualifiedName());

        testPolicyCmptObjectInput.setPolicyCmptType(policyCmptType.getQualifiedName());
        testPolicyCmptObjectInput.setProductCmpt("");

        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        testPolicyCmptObjectInput.setProductCmpt("");
        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType5");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        testPolicyCmptObjectInput.setProductCmpt("");
        testPolicyCmptObjectInput.setPolicyCmptType("Nonexistent_PolicyCmptType");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));
    }

    @Test
    public void testValidateMinInstancesNotReached() throws Exception {
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
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
        ITestPolicyCmptLink testAssociation = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        testAssociation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testAssociation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));
    }

    @Test
    public void testValidateMaxInstancesNotReached() throws Exception {
        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);

        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));

        // create instance child 1 on parameter side and validate
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("ProductCmpt1");
        paramChild.setMaxInstances(1);
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));

        // create two child and validate again
        ITestPolicyCmptLink testAssociation = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        testAssociation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testAssociation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
        testAssociation = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        testAssociation.setTestPolicyCmptTypeParameter("ProductCmpt1");
        testAssociation.newTargetTestPolicyCmptChild();
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));
    }

    @Test
    public void testValidateProductCmptNotExists() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        testPolicyCmptObjectInput.setProductCmpt("productCmpt");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));

        newProductCmpt(project, "productCmpt");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_CMPT_NOT_EXISTS));
    }

    @Test
    public void testValidateProductComponentNotRequired() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));

        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setRequiresProductCmpt(false);
        testPolicyCmptObjectInput.setProductCmpt("x");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));

        testPolicyCmptObjectInput.setProductCmpt(null);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED));
    }

    @Test
    public void testValidatePolicyCmptTypeIsAbstract() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT));

        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("PolicyCmptType4");
        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType4");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT));

        param.setPolicyCmptType("PolicyCmptType4");
        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType5");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT));
    }

    @Test
    public void testValidatePolicyCmptTypeAndProductCmptTypeGiven() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN));

        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType4");
        testPolicyCmptObjectInput.setProductCmpt("DummyProduct");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN));

        testPolicyCmptObjectInput.setPolicyCmptType("");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN));

        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType4");
        testPolicyCmptObjectInput.setProductCmpt("");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN));
    }

    @Test
    public void testValidatePolicyCmptTypeNotAssignable() throws Exception {
        MessageList ml = testPolicyCmptObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE));

        ITestPolicyCmptTypeParameter param = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("PolicyCmptType4");
        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType5");
        ml = testPolicyCmptObjectInput.validate(project);
        // type5 is subtype of type4 and therefore assignable
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE));

        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType6");
        ml = testPolicyCmptObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE));

        param.setPolicyCmptType("PolicyCmptType1");
        testPolicyCmptObjectInput.setPolicyCmptType("PolicyCmptType6");
        ml = testPolicyCmptObjectInput.validate(project);
        // type6 is subtype of type1 and therefore assignable
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE));
    }

    @Test
    public void testGetIndexOfChildTestPolicyCmpt() throws CoreException {
        ITestPolicyCmptLink associationPos0 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "A");
        ITestPolicyCmptLink associationPos1 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "B");
        ITestPolicyCmptLink associationPos2 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "C");

        ITestPolicyCmpt testPolicyCmptChild0 = associationPos0.newTargetTestPolicyCmptChild();
        ITestPolicyCmpt testPolicyCmptChild1 = associationPos1.newTargetTestPolicyCmptChild();
        ITestPolicyCmpt testPolicyCmptChild2 = associationPos2.newTargetTestPolicyCmptChild();

        assertEquals(0, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild0));
        assertEquals(1, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild1));
        assertEquals(2, testPolicyCmptObjectInput.getIndexOfChildTestPolicyCmpt(testPolicyCmptChild2));
    }

    @Test
    public void testMoveTestPolicyCmptAssociations() throws CoreException {
        ITestPolicyCmptLink associationPos0 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "A");
        ITestPolicyCmptLink associationPos1 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "B");
        ITestPolicyCmptLink associationPos2 = testPolicyCmptObjectInput.addTestPcTypeLink(
                childTestPolicyCmptTypeParameter, "testValueParameter1Child", null, "C");

        assertEquals(associationPos0, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[0]);
        assertEquals(associationPos1, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[1]);
        assertEquals(associationPos2, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[2]);

        testPolicyCmptObjectInput.moveTestPolicyCmptLink(new int[] { 1, 2 }, true);
        assertTrue(testPolicyCmptObjectInput.getIpsSrcFile().isDirty());

        assertEquals(associationPos1, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[0]);
        assertEquals(associationPos2, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[1]);
        assertEquals(associationPos0, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[2]);

        testPolicyCmptObjectInput.moveTestPolicyCmptLink(new int[] { 0 }, false);

        assertEquals(associationPos2, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[0]);
        assertEquals(associationPos1, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[1]);
        assertEquals(associationPos0, testPolicyCmptObjectInput.getTestPolicyCmptLinks()[2]);
    }

    /**
     * Test that adding test objects doesn't end in a fix sort order
     */
    @Test
    public void testAddTestPcTypeAssociationCorrectOrder() throws CoreException {
        ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParam = childTestPolicyCmptTypeParameter
                .getParentTestPolicyCmptTypeParam();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParamChilds = parentTestPolicyCmptTypeParam
                .getTestPolicyCmptTypeParamChilds();

        testPolicyCmptObjectInput.addTestPcTypeLink(testPolicyCmptTypeParamChilds[1], "ProductCmpt1", null, null);
        testPolicyCmptObjectInput.addTestPcTypeLink(testPolicyCmptTypeParamChilds[0], "ProductCmpt3", null, null);

        assertFalse(testCase.validate(project).containsErrorMsg());
        assertFalse(testCase.containsDifferenceToModel(project));
    }

    @Test
    public void testUpdateDefaultTestAttributeValues() throws CoreException {
        // create model objects
        IPolicyCmptType policy = newPolicyCmptType(project, "Policy");
        IPolicyCmptTypeAssociation association = policy.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("childPolicyCmptType");
        association.setTarget("Coverage");
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

        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration(new GregorianCalendar(1742,
                11, 1));
        IConfigElement ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute(attr.getName());
        ce.setValue("attrCoverage_Default_Product");

        // create test case type side
        ITestPolicyCmptTypeParameter tp = testPolicyCmptObjectInput.findTestPolicyCmptTypeParameter(project);
        // clean the default test data, because we use two new policy cmpts
        ITestPolicyCmptTypeParameter[] childs = tp.getTestPolicyCmptTypeParamChilds();
        for (ITestPolicyCmptTypeParameter child : childs) {
            tp.removeTestPolicyCmptTypeParamChild((TestPolicyCmptTypeParameter)child);
        }
        tp.setPolicyCmptType(policy.getQualifiedName());
        ITestPolicyCmptTypeParameter tpChild = tp.newTestPolicyCmptTypeParamChild();
        tpChild.setTestParameterType(TestParameterType.INPUT);
        tpChild.setName("childPolicyCmptType");
        tpChild.setAssociation("childPolicyCmptType");
        tpChild.setPolicyCmptType(coverage.getQualifiedName());
        ITestAttribute testAttr = tp.newInputTestAttribute();
        testAttr.setName("attrPolicy");
        testAttr.setAttribute("attrPolicy");
        testAttr = tpChild.newInputTestAttribute();
        testAttr.setName("attrCoverage");
        testAttr.setAttribute("attrCoverage");

        // create test case side
        ITestPolicyCmptLink tr = testPolicyCmptObjectInput.newTestPolicyCmptLink();
        tr.setTestPolicyCmptTypeParameter(tpChild.getName());
        ITestPolicyCmpt tcChild = tr.newTargetTestPolicyCmptChild();
        tcChild.setTestPolicyCmptTypeParameter(tpChild.getName());
        tcChild.setName(tpChild.getName());

        assertFalse(testCaseType.validate(testCaseType.getIpsProject()).containsErrorMsg());
        assertFalse(testCase.validate(project).containsErrorMsg());

        // use delta fix to add all missing test attributes
        testCase.fixDifferences(testCase.computeDeltaToTestCaseType());
        String path = testPolicyCmptObjectInput.getTestParameterName() + TestCaseHierarchyPath.SEPARATOR
                + tpChild.getAssociation() + TestCaseHierarchyPath.SEPARATOR + tpChild.getName();
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

        testAttrValuePolicy.updateDefaultTestAttributeValue();
        testAttrValueCoverage.updateDefaultTestAttributeValue();
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

        // test extension attributes
        ITestAttribute attribute = testAttrValueCoverage.findTestAttribute(project);
        attribute.setAttribute((String)null);
        attribute.setDatatype("undef");
        testAttrValueCoverage.updateDefaultTestAttributeValue();
        assertNull(testAttrValueCoverage.getValue());
        attribute.setDatatype("String");
        testAttrValueCoverage.updateDefaultTestAttributeValue();
        assertNull(testAttrValueCoverage.getValue());
    }

    @Test
    public void testFindProductCmpsCurrentGeneration_notProdRelevant() throws CoreException {
        // test find method if no product is specified, e.g. test object is not product relevant
        // in this case the find method should return null
        IProductCmptGeneration generation = null;
        testPolicyCmptObjectInput.setProductCmpt(null);
        generation = ((TestPolicyCmpt)testPolicyCmptObjectInput).findProductCmpsCurrentGeneration(project);
        assertNull(generation);
        testPolicyCmptObjectInput.setProductCmpt("");
        generation = ((TestPolicyCmpt)testPolicyCmptObjectInput).findProductCmpsCurrentGeneration(project);
        assertNull(generation);
    }

    private class TestContent {
        private IPolicyCmptType policy;
        private IPolicyCmptTypeAssociation coverages;
        private IPolicyCmptType coverage;

        private ProductCmptType productCmptTypePolicy;
        private ProductCmptType productCmptTypeCoverage;

        private IProductCmpt policyProduct;
        private IProductCmpt coverageProductA; // is related in policyProduct
        private IProductCmpt coverageProductB; // is not related in policyProduct

        private ITestPolicyCmptTypeParameter parameter;
        private ITestPolicyCmptTypeParameter childParameter;

        public void init(IIpsProject project) throws CoreException {
            policy = newPolicyCmptType(project, "Policy");
            policy.setConfigurableByProductCmptType(true);
            productCmptTypePolicy = newProductCmptType(project, "PolicyType");
            policy.setProductCmptType(productCmptTypePolicy.getQualifiedName());
            productCmptTypePolicy.setPolicyCmptType(policy.getQualifiedName());

            coverage = newPolicyCmptType(project, "Coverage");
            coverage.setConfigurableByProductCmptType(true);
            productCmptTypeCoverage = newProductCmptType(project, "CoverageType");
            coverage.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            productCmptTypeCoverage.setPolicyCmptType(coverage.getQualifiedName());

            coverages = policy.newPolicyCmptTypeAssociation();
            coverages.setTarget(coverage.getQualifiedName());
            coverages.setTargetRoleSingular("Coverage");
            coverages.setTargetRolePlural("Coverages");
            coverages.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

            IProductCmptTypeAssociation association = productCmptTypePolicy.newProductCmptTypeAssociation();
            association.setTarget(productCmptTypeCoverage.getQualifiedName());
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
            childParameter.setAssociation(coverages.getName());
            childParameter.setName("CoverageParam");
            childParameter.setRequiresProductCmpt(true);

            IProductCmptGeneration generation = (IProductCmptGeneration)policyProduct
                    .newGeneration(new GregorianCalendar());
            IProductCmptLink productCmptAssociation = generation.newLink("Coverage");
            productCmptAssociation.setTarget(coverageProductA.getQualifiedName());
        }
    }

    @Test
    public void testValidateAllowedProductCmpt() throws Exception {
        // test these two validation messages:
        // Error: WrongProductCmptOfAssociation
        // Warning: ParentProductCmptOfAssociationNotSpecified
        ITestPolicyCmpt testPolicyCmpt;
        ITestPolicyCmpt testPolicyCmptChild;
        MessageList ml;

        TestContent testContent = new TestContent();
        testContent.init(project);

        testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setTestPolicyCmptTypeParameter(testContent.parameter.getName());
        testPolicyCmpt.setName(testContent.parameter.getName());
        ITestPolicyCmptLink testPolicyCmptAssociation = testPolicyCmpt.newTestPolicyCmptLink();
        testPolicyCmptAssociation.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        testPolicyCmptChild = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmptChild.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        testPolicyCmptChild.setName(testContent.childParameter.getName());

        // wrong product cmpt of parent (root) (coverageProductA is not a productCmpt that can
        // configure a policy; "policyProduct" would be a valid component)
        testPolicyCmpt.setProductCmpt(testContent.coverageProductA.getQualifiedName());
        ml = testPolicyCmpt.validate(project);
        Message message = ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK);
        assertNotNull(message);
        assertEquals(Message.ERROR, message.getSeverity());
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE));

        // no product defined, no error about a wrong product cmpt and no warning about missing
        // parent product cmpt
        testPolicyCmptChild.setProductCmpt("");
        ml = testPolicyCmptChild.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));

        // product defined but parent product cmpt not specified, no error but warning
        testPolicyCmpt.setProductCmpt("");
        testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
        ml = testPolicyCmptChild.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));
        assertEquals(ml.getFirstMessage(Message.WARNING).getCode(),
                ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED);

        // product defined but parent not specified and not product relevant, no error no warning
        testPolicyCmpt.findPolicyCmptType().setConfigurableByProductCmptType(false);
        testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
        ml = testPolicyCmptChild.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));
        testPolicyCmpt.findPolicyCmptType().setConfigurableByProductCmptType(true);
        testPolicyCmpt.findPolicyCmptType().setProductCmptType(testContent.productCmptTypePolicy.getQualifiedName());

        // correct product cmpt of parent (root)
        testPolicyCmpt.setProductCmpt(testContent.policyProduct.getQualifiedName());
        ml = testPolicyCmpt.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        // with wrong child a) (wrong type)
        testPolicyCmptChild.setProductCmpt(testContent.policyProduct.getQualifiedName());
        ml = testPolicyCmptChild.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));
        // with wrong child b) (no link between components policyProduct and coverageProductB)
        testPolicyCmptChild.setProductCmpt(testContent.coverageProductB.getQualifiedName());
        ml = testPolicyCmptChild.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));

        // with correct child
        testPolicyCmpt.setProductCmpt(testContent.policyProduct.getQualifiedName());
        testPolicyCmptChild.setProductCmpt(testContent.coverageProductA.getQualifiedName());
        ml = testPolicyCmptChild.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK));
        assertNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED));

    }

    @Test
    public void testSetProductCmptAndNameAfterIfApplicable() throws CoreException {
        TestContent testContent = new TestContent();
        testContent.init(project);

        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setTestPolicyCmptTypeParameter(testContent.parameter.getName());
        testPolicyCmpt.setName(testContent.parameter.getName());
        ITestPolicyCmptLink testPolicyCmptAssociation = testPolicyCmpt.newTestPolicyCmptLink();
        testPolicyCmptAssociation.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        ITestPolicyCmpt testPolicyCmptChild = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmptChild.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        testPolicyCmptChild.setName(testContent.childParameter.getName());
        ITestPolicyCmptLink testPolicyCmptAssociation2 = testPolicyCmpt.newTestPolicyCmptLink();
        testPolicyCmptAssociation2.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        ITestPolicyCmpt testPolicyCmptChild2 = testPolicyCmptAssociation2.newTargetTestPolicyCmptChild();
        testPolicyCmptChild2.setTestPolicyCmptTypeParameter(testContent.childParameter.getName());
        testPolicyCmptChild2.setName(testContent.childParameter.getName());

        // set name of prodCmpt
        testPolicyCmpt.setName("");
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(testContent.policyProduct.getQualifiedName());
        assertEquals(testContent.policyProduct.getName(), testPolicyCmpt.getName());

        // reset name to PCTParameter
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable("");
        assertEquals(testContent.parameter.getName(), testPolicyCmpt.getName());

        // again set name of prodCmpt, even if name not empty
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(testContent.policyProduct.getQualifiedName());
        assertEquals(testContent.policyProduct.getName(), testPolicyCmpt.getName());

        // retain manually set name
        testPolicyCmpt.setName("DUMMY_NAME");
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(testContent.policyProduct.getQualifiedName());
        assertEquals("DUMMY_NAME", testPolicyCmpt.getName());
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable("");
        assertEquals("DUMMY_NAME", testPolicyCmpt.getName());

        // PCTParameter name is assumed as manual name if prodCmpt is set
        testPolicyCmpt.setName(testContent.parameter.getName());
        testPolicyCmpt.setProductCmpt(testContent.coverageProductA.getQualifiedName());
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(testContent.policyProduct.getQualifiedName());
        assertEquals(testContent.parameter.getName(), testPolicyCmpt.getName());
        testPolicyCmpt.setName(testContent.policyProduct.getName());

        // Test name uniqueness
        testPolicyCmptChild.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductA.getQualifiedName());
        testPolicyCmptChild2.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductA.getQualifiedName());
        assertEquals(testContent.coverageProductA.getName(), testPolicyCmptChild.getName());
        assertEquals(testContent.coverageProductA.getName() + " (2)", testPolicyCmptChild2.getName());

        // test whether unique name is recognized as standard name even with postfix " (2)"
        testPolicyCmptChild2.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductB.getQualifiedName());
        assertEquals(testContent.coverageProductB.getName(), testPolicyCmptChild2.getName());

        // test Uniqueness with manual name
        testPolicyCmptChild.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductA.getQualifiedName());
        testPolicyCmptChild.setName("DUMMY_NAME");
        testPolicyCmptChild2.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductA.getQualifiedName());
        assertEquals("DUMMY_NAME", testPolicyCmptChild.getName());
        assertEquals(testContent.coverageProductA.getName(), testPolicyCmptChild2.getName());

        // Test null-tolerance
        ITestPolicyCmpt testPolicyCmptNULL = testCase.newTestPolicyCmpt();
        testPolicyCmptNULL.setTestPolicyCmptTypeParameter(null);
        testPolicyCmptNULL.setProductCmpt(null);
        testPolicyCmptNULL.setName(null);
        testPolicyCmptNULL.setProductCmptAndNameAfterIfApplicable(testContent.coverageProductA.getQualifiedName());
        // no NPE!
        assertEquals(testContent.coverageProductA.getName(), testPolicyCmptNULL.getName());

        // new Product Component also null
        testPolicyCmptNULL.setTestPolicyCmptTypeParameter(null);
        testPolicyCmptNULL.setProductCmpt(null);
        testPolicyCmptNULL.setName(null);
        testPolicyCmptNULL.setProductCmptAndNameAfterIfApplicable(null);
        // no NPE!
        assertEquals("", testPolicyCmptNULL.getName());
    }
}
