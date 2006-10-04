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

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRuleTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestRule rule;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testCaseType.newExpectedResultRuleParameter().setName("testValueParameter1");
        
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        
        rule = testCase.newTestRule();
        rule.setTestRuleParameter("testValueParameter1");
    }
    
    public void testIsRoot(){
        // a test rule parameter is always a root element, no childs are supported by the test rule parameter
        assertTrue(rule.isRoot());
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleObject", 0);
        rule.initFromXml(paramEl);
        assertEquals("validationRule1", rule.getValidationRule());
        assertEquals("testRuleParameter1", rule.getTestParameterName());
        assertEquals("testRuleParameter1", rule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.VIOLATED, rule.getViolationType());
        
        paramEl = XmlUtil.getElement(docEl, "RuleObject", 1);
        rule.initFromXml(paramEl);
        assertEquals("validationRule2", rule.getValidationRule());
        assertEquals("testRuleParameter2", rule.getTestParameterName());
        assertEquals("testRuleParameter2", rule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.NOT_VIOLATED, rule.getViolationType());
        
        paramEl = XmlUtil.getElement(docEl, "RuleObject", 2);
        rule.initFromXml(paramEl);
        assertEquals("validationRule3", rule.getValidationRule());
        assertEquals("testRuleParameter3", rule.getTestParameterName());
        assertEquals("testRuleParameter3", rule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.UNKNOWN, rule.getViolationType());        
    }
    
    public void testToXml() {
        rule.setValidationRule("validationRule0");
        rule.setTestRuleParameter("testRuleParameter0");
        rule.setViolationType(TestRuleViolationType.VIOLATED);
        Element el = rule.toXml(newDocument());
        
        rule.setValidationRule("validationRuleX");
        rule.setTestRuleParameter("testRuleParameterX");
        rule.setViolationType(TestRuleViolationType.NOT_VIOLATED);
        
        rule.initFromXml(el);
        assertEquals("validationRule0", rule.getValidationRule());
        assertEquals("testRuleParameter0", rule.getTestParameterName());
        assertEquals("testRuleParameter0", rule.getTestRuleParameter());
        assertEquals(TestRuleViolationType.VIOLATED, rule.getViolationType());      
    }
    
    public void testValidateRuleNotExists() throws Exception{
        MessageList ml = rule.validate();
        rule.setValidationRule("x");
        assertNotNull(ml.getMessageByCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));

        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType1");
        IValidationRule validationRule = policyCmptType.newRule();
        validationRule.setName("rule1");
        rule.setValidationRule("rule1");
        
        // check rule not found because the test parameter doesn't contain the policy cmpt which
        // contains the rule
        ml = rule.validate();
        assertNotNull(ml.getMessageByCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));

        // assign the policy cmpt (including the rule) to the corresponding test case type
        ITestCase testCase = (ITestCase) rule.getParent();
        ITestCaseType testCaseType = testCase.findTestCaseType();
        ITestPolicyCmptTypeParameter param = testCaseType.newInputTestPolicyCmptTypeParameter();
        param.setPolicyCmptType("policyCmptType1");

        ml = rule.validate();
        assertNull(ml.getMessageByCode(ITestRule.MSGCODE_VALIDATION_RULE_NOT_EXISTS));
    }

    public void testValidateDuplicateValidationRule() throws Exception{
        rule.setValidationRule("validationRule1");
        ITestRule rule2 = testCase.newTestRule();
        rule2.setValidationRule("validationRule2");

        MessageList ml = rule.validate();
        assertNull(ml.getMessageByCode(ITestRule.MSGCODE_DUPLICATE_VALIDATION_RULE));
        
        rule2.setValidationRule("validationRule1");
        ml = rule.validate();
        assertNotNull(ml.getMessageByCode(ITestRule.MSGCODE_DUPLICATE_VALIDATION_RULE));
    }

    public void testValidateTestValueParamNotFound() throws Exception{
        MessageList ml = rule.validate();
        assertNull(ml.getMessageByCode(ITestRule.MSGCODE_TEST_RULE_PARAM_NOT_FOUND));

        rule.setTestRuleParameter("x");
        ml = rule.validate();
        assertNotNull(ml.getMessageByCode(ITestRule.MSGCODE_TEST_RULE_PARAM_NOT_FOUND));
    }
}
