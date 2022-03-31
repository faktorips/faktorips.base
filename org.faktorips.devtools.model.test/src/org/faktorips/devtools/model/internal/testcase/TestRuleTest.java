/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRuleTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestRule testRule;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");

        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE,
                "PremiumCalculation");
        testCaseType.newExpectedResultRuleParameter().setName("testValueParameter1");

        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());

        testRule = testCase.newTestRule();
        testRule.setTestRuleParameter("testValueParameter1");
    }

    @Test
    public void testIsRoot() {
        // a test rule parameter is always a root element, no childs are supported by the test rule
        // parameter
        assertTrue(testRule.isRoot());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleObject", 0);
        testRule.initFromXml(paramEl);
        assertEquals("validationRule1", testRule.getValidationRule());
        assertEquals("testRuleParameter1", testRule.getTestParameterName());
        assertEquals("testRuleParameter1", testRule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.VIOLATED, testRule.getViolationType());

        paramEl = XmlUtil.getElement(docEl, "RuleObject", 1);
        testRule.initFromXml(paramEl);
        assertEquals("validationRule2", testRule.getValidationRule());
        assertEquals("testRuleParameter2", testRule.getTestParameterName());
        assertEquals("testRuleParameter2", testRule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.NOT_VIOLATED, testRule.getViolationType());

        paramEl = XmlUtil.getElement(docEl, "RuleObject", 2);
        testRule.initFromXml(paramEl);
        assertEquals("validationRule3", testRule.getValidationRule());
        assertEquals("testRuleParameter3", testRule.getTestParameterName());
        assertEquals("testRuleParameter3", testRule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.UNKNOWN, testRule.getViolationType());
    }

    @Test
    public void testToXml() {
        testRule.setValidationRule("validationRule0");
        testRule.setTestRuleParameter("testRuleParameter0");
        testRule.setViolationType(TestRuleViolationType.VIOLATED);
        Element el = testRule.toXml(newDocument());

        testRule.setValidationRule("validationRuleX");
        testRule.setTestRuleParameter("testRuleParameterX");
        testRule.setViolationType(TestRuleViolationType.NOT_VIOLATED);

        testRule.initFromXml(el);
        assertEquals("validationRule0", testRule.getValidationRule());
        assertEquals("testRuleParameter0", testRule.getTestParameterName());
        assertEquals("testRuleParameter0", testRule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.VIOLATED, testRule.getViolationType());
    }

    @Test
    public void testValidateRuleNotExists() throws Exception {
        // create policy cmpts with validation rules
        IPolicyCmptType policyCmptTypeA = newPolicyAndProductCmptType(project, "PolicyCmptA", "ProductCmptA");
        policyCmptTypeA.setAbstract(true);
        IPolicyCmptType policyCmptTypeB = newPolicyAndProductCmptType(project, "PolicyCmptB", "ProductCmptB");
        policyCmptTypeB.setSupertype(policyCmptTypeA.getQualifiedName());
        IPolicyCmptType policyCmptTypeC = newPolicyAndProductCmptType(project, "PolicyCmptC", "ProductCmptC");
        policyCmptTypeC.setSupertype(policyCmptTypeA.getQualifiedName());

        // create product cmpts for B and C (will be added in the test case)
        IProductCmpt productCmptB = newProductCmpt(policyCmptTypeB.findProductCmptType(project), "ProductCmptB");
        IProductCmpt productCmptC = newProductCmpt(policyCmptTypeC.findProductCmptType(project), "ProductCmptC");

        IValidationRule ruleA = policyCmptTypeA.newRule();
        ruleA.setName("RuleA");
        ruleA.setMessageCode("RuleA");
        IValidationRule ruleB = policyCmptTypeB.newRule();
        ruleB.setName("RuleB");
        ruleB.setMessageCode("RuleB");
        IValidationRule ruleC = policyCmptTypeC.newRule();
        ruleC.setName("RuleC");
        ruleC.setMessageCode("RuleC");

        // simple validation
        MessageList ml = testRule.validate(project);
        testRule.setValidationRule("x");
        assertThat(ml, hasMessageCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));

        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType1");
        IValidationRule validationRule = policyCmptType.newRule();
        validationRule.setName("rule1");
        testRule.setValidationRule("rule1");

        // check rule not found because the test parameter doesn't contain the policy cmpt which
        // contains the rule
        ml = testRule.validate(project);
        assertThat(ml, hasMessageCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));

        // assign the policy cmpt (including the rule) to the corresponding test case type
        ITestCase testCase = (ITestCase)testRule.getParent();
        ITestCaseType testCaseType = testCase.findTestCaseType(project);
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setPolicyCmptType("policyCmptType1");

        ml = testRule.validate(project);
        assertThat(ml, lacksMessageCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));

        // complex validation @see TestCaseTest#testGetTestRuleCandidates
        ITestPolicyCmptTypeParameter paramA1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramA1.setPolicyCmptType(policyCmptTypeA.getQualifiedName());
        paramA1.setName("PolicyCmptA1");
        ITestPolicyCmptTypeParameter paramA2 = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramA2.setPolicyCmptType(policyCmptTypeA.getQualifiedName());
        paramA2.setName("PolicyCmptA2");
        ITestRuleParameter paramRule = testCaseType.newExpectedResultRuleParameter();
        paramRule.setName("Rule1");

        ITestPolicyCmpt tpcB = testCase.newTestPolicyCmpt();
        tpcB.setTestPolicyCmptTypeParameter(paramA1.getName());
        tpcB.setProductCmpt(productCmptB.getQualifiedName());

        ITestPolicyCmpt tpcC = testCase.newTestPolicyCmpt();
        tpcC.setTestPolicyCmptTypeParameter(paramA2.getName());
        tpcC.setProductCmpt(productCmptC.getQualifiedName());

        ITestRule testRuleNew = testCase.newTestRule();
        testRuleNew.setTestRuleParameter(paramRule.getName());
        testRuleNew.setValidationRule(ruleA.getMessageCode());
        ml = testRule.validate(project);
        assertEquals(0, ml.size());

        testRuleNew = testCase.newTestRule();
        testRuleNew.setTestRuleParameter(paramRule.getName());
        testRuleNew.setValidationRule(ruleB.getMessageCode());
        ml = testRule.validate(project);
        assertEquals(0, ml.size());

        testRuleNew = testCase.newTestRule();
        testRuleNew.setTestRuleParameter(paramRule.getName());
        testRuleNew.setValidationRule(ruleC.getMessageCode());
        ml = testRule.validate(project);
        assertEquals(0, ml.size());
    }

    @Test
    public void testValidateDuplicateValidationRule() throws Exception {
        testRule.setValidationRule("validationRule1");
        ITestRule rule2 = testCase.newTestRule();
        rule2.setValidationRule("validationRule2");

        MessageList ml = testRule.validate(project);
        assertThat(ml, lacksMessageCode(ITestRule.MSGCODE_DUPLICATE_VALIDATION_RULE));

        rule2.setValidationRule("validationRule1");
        ml = testRule.validate(project);
        assertThat(ml, hasMessageCode(ITestRule.MSGCODE_DUPLICATE_VALIDATION_RULE));
    }

    @Test
    public void testValidateTestValueParamNotFound() throws Exception {
        MessageList ml = testRule.validate(project);
        assertThat(ml, lacksMessageCode(ITestRule.MSGCODE_TEST_RULE_PARAM_NOT_FOUND));

        testRule.setTestRuleParameter("x");
        ml = testRule.validate(project);
        assertThat(ml, hasMessageCode(ITestRule.MSGCODE_TEST_RULE_PARAM_NOT_FOUND));
    }
}
