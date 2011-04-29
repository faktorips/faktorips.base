/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTest extends AbstractDependencyTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private ITestCase testCase;
    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter policyCmptTypeParameterChild0;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = super.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];

        PolicyCmptType policyCmptType0 = newPolicyCmptType(ipsProject, "policyCmptType0");
        IAssociation association = policyCmptType0.newAssociation();
        association.setTargetRoleSingular("link0");

        PolicyCmptType policyCmptType1 = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute0 = policyCmptType1.newAttribute();
        attribute0.setName("attribute0");
        attribute0.setDatatype("Integer");
        attribute0.setDefaultValue("99");
        IAttribute attribute1 = policyCmptType1.newAttribute();
        attribute1.setName("attribute1");
        attribute1.setDatatype("Integer");
        attribute1.setDefaultValue("11");

        newPolicyCmptType(ipsProject, "testCaseTypeX");

        testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseType1");
        ITestValueParameter inputTestValueParameter0 = testCaseType.newInputTestValueParameter();
        inputTestValueParameter0.setName("inputTestValue0");
        inputTestValueParameter0.setDatatype("String");
        ITestValueParameter inputTestValueParameter1 = testCaseType.newInputTestValueParameter();
        inputTestValueParameter1.setName("inputTestValue1");
        inputTestValueParameter1.setDatatype("Integer");

        ITestPolicyCmptTypeParameter inputTestPolicyCmptTypeParameter0 = testCaseType
                .newInputTestPolicyCmptTypeParameter();
        inputTestPolicyCmptTypeParameter0.setName("inputTestPolicyCmpt0");
        inputTestPolicyCmptTypeParameter0.setPolicyCmptType("policyCmptType0");
        policyCmptTypeParameterChild0 = inputTestPolicyCmptTypeParameter0.newTestPolicyCmptTypeParamChild();
        policyCmptTypeParameterChild0.setName("inputTestPolicyCmptChild0");
        policyCmptTypeParameterChild0.setAssociation("link0");
        policyCmptTypeParameterChild0.setPolicyCmptType("policyCmptType1");
        ITestAttribute inputTestAttribute = policyCmptTypeParameterChild0.newInputTestAttribute();
        inputTestAttribute.setName("testAttributeChild0.input");
        inputTestAttribute.setAttribute("attribute0");
        ITestAttribute expectedResultTestAttribute = policyCmptTypeParameterChild0.newExpectedResultTestAttribute();
        expectedResultTestAttribute.setName("testAttributeChild0.expected");
        expectedResultTestAttribute.setAttribute("attribute1");
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("inputTestPolicyCmpt1");

        ITestValueParameter expectedResultValueParameter0 = testCaseType.newExpectedResultValueParameter();
        expectedResultValueParameter0.setName("expResultTestValue0");
        expectedResultValueParameter0.setDatatype("Integer");
        testCaseType.newExpectedResultValueParameter().setName("expResultTestValue1");
        testCaseType.newExpectedResultValueParameter().setName("expResultTestValue2");
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expResultTestPolicyCmpt0");
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expResultTestPolicyCmpt1");
        testCaseType.newExpectedResultRuleParameter().setName("expResultTestRule1");

        testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseType1");
        testCase.setTestCaseType(testCaseType.getName());
    }

    private void assertTestCaseObjects(int testInputValues,
            int testExpResultValues,
            int testInputPolicyCmpts,
            int testExpPolicyCmpts) {

        assertTestCaseObjects(testInputValues, testExpResultValues, testInputPolicyCmpts, testExpPolicyCmpts, 0);
    }

    private void assertTestCaseObjects(int testInputValues,
            int testExpResultValues,
            int testInputPolicyCmpts,
            int testExpPolicyCmpts,
            int testExpResultRules) {

        assertEquals(testInputValues, testCase.getInputTestValues().length);
        assertEquals(testExpResultValues, testCase.getExpectedResultTestValues().length);
        assertEquals(testInputPolicyCmpts, testCase.getInputTestPolicyCmpts().length);
        assertEquals(testExpPolicyCmpts, testCase.getExpectedResultTestPolicyCmpts().length);
        assertEquals(testExpResultRules, testCase.getExpectedResultTestRules().length);
    }

    @Test
    public void testNewParameterAndGet() throws Exception {
        ITestValue param1 = testCase.newTestValue();
        param1.setTestValueParameter("inputTestValue0");
        assertNotNull(param1);
        assertTrue(param1.isInput());
        assertTestCaseObjects(1, 0, 0, 0);

        ITestValue param2 = testCase.newTestValue();
        param2.setTestValueParameter("expResultTestValue0");
        assertNotNull(param2);
        assertTestCaseObjects(1, 1, 0, 0);

        ITestPolicyCmpt param3 = testCase.newTestPolicyCmpt();
        param3.setTestPolicyCmptTypeParameter("inputTestPolicyCmpt0");
        param3.setName("inputTestPolicyCmpt0");
        assertNotNull(param3);
        assertTestCaseObjects(1, 1, 1, 0);

        ITestPolicyCmpt param4 = testCase.newTestPolicyCmpt();
        param4.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt0");
        param4.setName("expResultTestPolicyCmpt0");
        assertNotNull(param4);
        assertTestCaseObjects(1, 1, 1, 1);

        ITestPolicyCmpt param5 = testCase.newTestPolicyCmpt();
        param5.setTestPolicyCmptTypeParameter("xyz");
        assertNotNull(param5);
        assertTestCaseObjects(1, 1, 1, 1);

        ITestPolicyCmpt param6 = testCase.newTestPolicyCmpt();
        param6.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt1");
        assertNotNull(param6);
        assertTestCaseObjects(1, 1, 1, 2);

        ITestRule param7 = testCase.newTestRule();
        param7.setTestRuleParameter("expResultTestRule1");
        assertNotNull(param7);
        assertTestCaseObjects(1, 1, 1, 2, 1);

        assertEquals(param1, testCase.getInputTestValues()[0]);
        assertEquals(param2, testCase.getExpectedResultTestValues()[0]);
        assertEquals(param3, testCase.findTestPolicyCmpt("inputTestPolicyCmpt0"));
        assertEquals(param4, testCase.findTestPolicyCmpt("expResultTestPolicyCmpt0"));
        assertEquals(param3, testCase.getInputTestPolicyCmpts()[0]);
        assertEquals(param4, testCase.getExpectedResultTestPolicyCmpts()[0]);
        assertEquals(param6, testCase.getExpectedResultTestPolicyCmpts()[1]);
        assertEquals(param7, testCase.getExpectedResultTestRules()[0]);
    }

    @Test
    public void testInitFromXml() {
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

    @Test
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
        assertEquals(1, testCase.getInputTestPolicyCmpts().length);
        assertEquals(2, testCase.getInputTestValues().length);
        assertEquals(1, testCase.getExpectedResultTestValues().length);
        assertEquals(1, testCase.getExpectedResultTestPolicyCmpts().length);
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
        assertEquals("expResultTestPolicyCmpt0",
                testCase.getExpectedResultTestPolicyCmpts()[0].getTestPolicyCmptTypeParameter());
        assertEquals("expResultTestRule1", testCase.getExpectedResultTestRules()[0].getTestRuleParameter());

        assertEquals("testCaseType1", testCase.getTestCaseType());
    }

    @Test
    public void testDependsOn() throws Exception {
        List<IDependency> dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(1, dependsOnList.size());
        IDependency dependency = IpsObjectDependency.createInstanceOfDependency(testCase.getQualifiedNameType(),
                testCaseType.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(testCase, dependency, testCase, ITestCase.PROPERTY_TEST_CASE_TYPE);

        ITestCase testCase2 = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseType2");
        List<IDependency> dependsOnList2 = CollectionUtil.toArrayList(testCase2.dependsOn());
        assertEquals(0, dependsOnList2.size());

        IProductCmpt prodCmpt1 = newProductCmpt(root, "ProductCmpt1");
        IProductCmpt prodCmpt2 = newProductCmpt(root, "ProductCmpt2");

        // test dependency to product cmpt, root test cmpt
        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setProductCmpt(prodCmpt1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(2, dependsOnList.size());
        dependency = IpsObjectDependency.createReferenceDependency(testCase.getQualifiedNameType(),
                prodCmpt1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(testCase, dependency, testPolicyCmpt1, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);

        // test dependency to product cmpt, child test cmpt
        ITestPolicyCmptLink testAssociation1 = testPolicyCmpt1.newTestPolicyCmptLink();
        ITestPolicyCmpt testPolicyCmpt2 = testAssociation1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setProductCmpt(prodCmpt2.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(testCase.dependsOn());
        assertEquals(3, dependsOnList.size());
        dependency = IpsObjectDependency.createReferenceDependency(testCase.getQualifiedNameType(),
                prodCmpt1.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(testCase, dependency, testPolicyCmpt1, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);

        dependency = IpsObjectDependency.createReferenceDependency(testCase.getQualifiedNameType(),
                prodCmpt2.getQualifiedNameType());
        assertTrue(dependsOnList.contains(dependency));
        assertSingleDependencyDetail(testCase, dependency, testPolicyCmpt2, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
    }

    @Test
    public void testGenerateUniqueNameForTestPolicyCmpt() {
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
        ITestPolicyCmptLink link = param1.newTestPolicyCmptLink();
        ITestPolicyCmpt child = link.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test", child.getName());

        link = param1.newTestPolicyCmptLink();
        ITestPolicyCmpt child2 = link.newTargetTestPolicyCmptChild();
        child2.setName(testCase.generateUniqueNameForTestPolicyCmpt(child2, "Test"));
        assertEquals("Test (2)", child2.getName());

        link = param1.newTestPolicyCmptLink();
        child = link.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (3)", child.getName());

        child2.delete();

        link = param1.newTestPolicyCmptLink();
        child = link.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (2)", child.getName());

        link = param1.newTestPolicyCmptLink();
        child = link.newTargetTestPolicyCmptChild();
        child.setName(testCase.generateUniqueNameForTestPolicyCmpt(child, "Test"));
        assertEquals("Test (4)", child.getName());
    }

    @Test
    public void testValidateTestCaseTypeNotFound() throws Exception {
        MessageList ml = testCase.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));

        testCase.setTestCaseType("x");
        ml = testCase.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestCase.MSGCODE_TEST_CASE_TYPE_NOT_FOUND));
    }

    @Test
    public void testGetTestRuleCandidates() throws CoreException {
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

        IValidationRule[] testRuleParameters = testCase.getTestRuleCandidates(ipsProject);
        assertEquals(3, testRuleParameters.length);
        List<IValidationRule> testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));
        assertTrue(testRuleParametersList.contains(ruleC));

        testCase.removeTestObject(tpcC);
        assertEquals(1, testCase.getAllTestPolicyCmpt().length);
        testRuleParameters = testCase.getTestRuleCandidates(ipsProject);
        assertEquals(2, testRuleParameters.length);
        testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));

        ITestPolicyCmpt tpcD = testCase.newTestPolicyCmpt();
        tpcD.setTestPolicyCmptTypeParameter(paramA3.getName());
        tpcD.setProductCmpt(productCmptD.getQualifiedName());

        testRuleParameters = testCase.getTestRuleCandidates(ipsProject);
        assertEquals(4, testRuleParameters.length);
        testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(ruleA));
        assertTrue(testRuleParametersList.contains(ruleB));
        assertTrue(testRuleParametersList.contains(ruleC));
        assertTrue(testRuleParametersList.contains(ruleD));
    }

    @Test
    public void testContainsDifferenceToModel() throws CoreException {
        ITestCaseType testCaseTypeX = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "testCaseTypeX");

        ITestCase testCaseX = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseTypeX");
        testCaseX.setTestCaseType(testCaseTypeX.getName());

        assertEquals(false, testCaseX.containsDifferenceToModel(ipsProject));
        ITestValueParameter parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("inputTestValueX");
        parameter.setDatatype("String");
        assertEquals(true, testCaseX.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testFixAllDifferencesToModel() throws CoreException {
        ITestCaseType testCaseTypeX = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "testCaseTypeX");
        ITestCase testCaseX = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCaseTypeX");
        testCaseX.setTestCaseType(testCaseTypeX.getName());

        assertEquals(false, testCaseX.containsDifferenceToModel(ipsProject));
        testCaseX.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, testCaseX.containsDifferenceToModel(ipsProject));

        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "PolicyCmptType");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("Attribute1");
        attribute.setDatatype("String");
        attribute.setDefaultValue("Test1");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
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

        assertTrue(testCaseX.containsDifferenceToModel(ipsProject));
        testCaseX.fixAllDifferencesToModel(ipsProject);
        assertFalse(testCaseX.containsDifferenceToModel(ipsProject));

        // create root test policy cmpt object
        ITestPolicyCmpt testPolicyCmpt = testCaseX.newTestPolicyCmpt();
        testPolicyCmpt.setName("PolicyCmptType");
        testPolicyCmpt.setTestPolicyCmptTypeParameter("PolicyCmptType");
        ITestAttributeValue testAttributeValue = testPolicyCmpt.newTestAttributeValue();
        testAttributeValue.setTestAttribute("Attribute1");
        testAttributeValue.setValue("Test1"); // default value
        testAttributeValue = testPolicyCmpt.newTestAttributeValue();
        testAttributeValue.setTestAttribute("Attribute1");
        testAttributeValue.setValue("Test2");// default value

        // further check to ensure that the old default values will not be overridden
        testAttributeValue = testCaseX.getInputTestPolicyCmpts()[0].getTestAttributeValues()[0];
        assertEquals("Test1", testAttributeValue.getValue());
        testAttributeValue.setValue("1234");

        testAttribute = policyCmptParam.newInputTestAttribute();
        testAttribute.setName("Attribute2");
        testAttribute.setAttribute("Attribute2");

        assertEquals(true, testCaseX.containsDifferenceToModel(ipsProject));
        testCaseX.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, testCaseX.containsDifferenceToModel(ipsProject));

        assertEquals("1234", testAttributeValue.getValue());
        testAttributeValue = testCaseX.getInputTestPolicyCmpts()[0].getTestAttributeValues()[1];
        assertEquals("Test2", testAttributeValue.getValue());

        // test the correct default values
        MessageList ml = testCaseX.validate(ipsProject);
        assertEquals(ml.size(), 0);
        parameter = testCaseTypeX.newInputTestValueParameter();
        parameter.setName("testBoolean");
        parameter.setDatatype("Boolean");

        testCaseX.fixAllDifferencesToModel(ipsProject);
        assertEquals(false, testCaseX.containsDifferenceToModel(ipsProject));
        ml = testCaseX.validate(ipsProject);
        assertFalse(ml.containsErrorMsg());
    }

    @Test
    public void testGetAllTestPolicyCmpts() throws CoreException {
        ITestPolicyCmpt testPolicyCmptRoot1 = testCase.newTestPolicyCmpt();
        testPolicyCmptRoot1.setName("root");
        ITestPolicyCmptLink testPolicyCmptAssociation = testPolicyCmptRoot1.newTestPolicyCmptLink();
        ITestPolicyCmpt testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root1_child1");
        testPolicyCmptAssociation = testPolicyCmptRoot1.newTestPolicyCmptLink();
        testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root1_child2");

        ITestPolicyCmpt testPolicyCmptRoot2 = testCase.newTestPolicyCmpt();
        testPolicyCmptRoot2.setName("root2");
        testPolicyCmptAssociation = testPolicyCmptRoot2.newTestPolicyCmptLink();
        testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child1");
        testPolicyCmptAssociation = testPolicyCmpt.newTestPolicyCmptLink();
        testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child1_child1");

        testPolicyCmptAssociation = testPolicyCmptRoot2.newTestPolicyCmptLink();
        testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root2_child2");

        ITestPolicyCmpt[] allTestPolicyCmpt = testCase.getAllTestPolicyCmpt();
        List<String> allTestPolicyCmptNames = new ArrayList<String>();
        for (ITestPolicyCmpt element : allTestPolicyCmpt) {
            allTestPolicyCmptNames.add(element.getName());
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

    public void getAllTestObjects() throws CoreException {
        ITestPolicyCmpt testPolicyCmptRoot1 = testCase.newTestPolicyCmpt();
        testPolicyCmptRoot1.setName("root");

        assertEquals(1, testCase.getAllTestObjects().length);
        assertContains(testCase.getAllTestObjects(), new Object[] { testPolicyCmptRoot1 });

        ITestPolicyCmptLink testPolicyCmptAssociation = testPolicyCmptRoot1.newTestPolicyCmptLink();
        ITestPolicyCmpt testPolicyCmpt = testPolicyCmptAssociation.newTargetTestPolicyCmptChild();
        testPolicyCmpt.setName("root1_child1");

        assertEquals(2, testCase.getAllTestObjects().length);
        assertContains(testCase.getAllTestObjects(), new Object[] { testPolicyCmptRoot1, testPolicyCmpt });

        ITestValue testValue = testCase.newTestValue();
        ITestRule testRule = testCase.newTestRule();

        assertEquals(4, testCase.getAllTestObjects().length);
        assertContains(testCase.getAllTestObjects(), new Object[] { testPolicyCmptRoot1, testPolicyCmpt, testValue,
                testRule });
    }

    private void assertContains(ITestObject[] testObjects, Object[] expected) {
        for (Object element : expected) {
            boolean found = false;
            for (ITestObject testObject : testObjects) {
                if (testObject.equals(testObjects)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("expected object not found: " + element);
            }
        }
    }

    @Test
    public void testGetReferencedProductCmpts() throws CoreException {
        IProductCmpt prodCmpt1 = newProductCmpt(root, "ProductCmpt1");
        IProductCmpt prodCmpt2 = newProductCmpt(root, "ProductCmpt2");

        List<String> referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(0, referencedProductCmpts.size());

        ITestPolicyCmpt testPolicyCmpt1 = testCase.newTestPolicyCmpt();
        testPolicyCmpt1.setProductCmpt(prodCmpt1.getQualifiedName());
        referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(1, referencedProductCmpts.size());
        assertEquals(prodCmpt1.getQualifiedName(), referencedProductCmpts.get(0));

        ITestPolicyCmptLink testAssociation1 = testPolicyCmpt1.newTestPolicyCmptLink();
        ITestPolicyCmpt testPolicyCmpt2 = testAssociation1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setProductCmpt(prodCmpt2.getQualifiedName());
        referencedProductCmpts = CollectionUtil.toArrayList(testCase.getReferencedProductCmpts());
        assertEquals(2, referencedProductCmpts.size());
    }

    @Test
    public void testFindTestCaseType() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        ITestCaseType testCaseType2 = (ITestCaseType)newIpsObject(project2, IpsObjectType.TEST_CASE_TYPE,
                "testCaseType1");

        assertTrue(testCaseType2.equals(testCase.findTestCaseType(project2)));
        assertFalse(testCaseType.equals(testCase.findTestCaseType(project2)));

        assertFalse(testCaseType2.equals(testCase.findTestCaseType(ipsProject)));
        assertTrue(testCaseType.equals(testCase.findTestCaseType(ipsProject)));
    }

    @Test
    public void testClearInputTestValues() throws CoreException {
        TestValues values = new TestValues();
        testCase.clearTestValues(TestParameterType.INPUT);
        values.assertInputValues(true);
        values.assertExpectedValues(false);
    }

    @Test
    public void testClearExpectedTestValues() throws CoreException {
        TestValues values = new TestValues();
        testCase.clearTestValues(TestParameterType.EXPECTED_RESULT);
        values.assertInputValues(false);
        values.assertExpectedValues(true);
    }

    @Test
    public void testClearCombinedTestValues() throws CoreException {
        TestValues values = new TestValues();
        testCase.clearTestValues(TestParameterType.COMBINED);
        values.assertInputValues(true);
        values.assertExpectedValues(true);
    }

    private class TestValues {

        private ITestValue paramInput;
        private ITestValue paramExpected;
        private ITestAttributeValue attributeValueInput;
        private ITestAttributeValue attributeValueExpected;

        public TestValues() {
            try {
                initTestValues();
            } catch (CoreException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private void initTestValues() throws CoreException {
            // init test case
            paramInput = testCase.newTestValue();
            paramInput.setTestValueParameter("inputTestValue0");
            paramExpected = testCase.newTestValue();
            paramExpected.setTestValueParameter("expResultTestValue0");

            ITestPolicyCmpt param3 = testCase.newTestPolicyCmpt();
            param3.setTestPolicyCmptTypeParameter("inputTestPolicyCmpt0");
            param3.setName("inputTestPolicyCmpt0");
            ITestPolicyCmptLink link = param3.addTestPcTypeLink(policyCmptTypeParameterChild0, null, null, "link0");
            ITestPolicyCmpt childPolicyCmpt = link.newTargetTestPolicyCmptChild();
            childPolicyCmpt.setTestPolicyCmptTypeParameter("inputTestPolicyCmptChild0");

            attributeValueInput = childPolicyCmpt.newTestAttributeValue();
            attributeValueInput.setTestAttribute("testAttributeChild0.input");
            attributeValueExpected = childPolicyCmpt.newTestAttributeValue();
            attributeValueExpected.setTestAttribute("testAttributeChild0.expected");

            ITestPolicyCmpt param4 = testCase.newTestPolicyCmpt();
            param4.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt0");
            param4.setName("expResultTestPolicyCmpt0");

            ITestPolicyCmpt param5 = testCase.newTestPolicyCmpt();
            param5.setTestPolicyCmptTypeParameter("xyz");
            ITestPolicyCmpt param6 = testCase.newTestPolicyCmpt();
            param6.setTestPolicyCmptTypeParameter("expResultTestPolicyCmpt1");

            ITestRule param7 = testCase.newTestRule();
            param7.setTestRuleParameter("expResultTestRule1");

            assertTestCaseObjects(1, 1, 1, 2, 1);

            paramInput.setValue("inputTestValue0");
            paramExpected.setValue("expResultTestValue0");
            attributeValueInput.setValue("testAttributeChild0.input");
            attributeValueInput.setValue("testAttributeChild0.expected");
        }

        public void assertInputValues(boolean empty) {
            if (empty) {
                assertEquals(null, paramInput.getValue());
                // if the type is input then the default value of the model attribute will be set
                assertEquals("99", attributeValueInput.getValue());
            } else {
                assertNotNull(paramInput.getValue());
                assertNotNull(attributeValueInput.getValue());
            }
        }

        public void assertExpectedValues(boolean empty) {
            if (empty) {
                assertEquals(null, paramExpected.getValue());
                assertEquals(null, attributeValueExpected.getValue());
            } else {
                assertNotNull(paramExpected.getValue());
                assertNotNull(attributeValueExpected.getValue());
            }
        }
    }

    @Test
    public void testFindMetaClass() throws CoreException {
        IIpsSrcFile typeSrcFile = testCase.findMetaClassSrcFile(ipsProject);
        assertEquals(testCaseType.getIpsSrcFile(), typeSrcFile);
    }

}
