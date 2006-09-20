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
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRuleTest extends AbstractIpsPluginTest {

    private ITestRule rule;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        rule = testCase.newTestRule();
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
}
