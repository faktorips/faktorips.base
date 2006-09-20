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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRuleParameterTest extends AbstractIpsPluginTest {

    private ITestRuleParameter ruleInput;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType )newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ruleInput = type.newExpectedResultRuleParameter();
    }
    
    public void testIsRoot(){
        // a test rule parameter is always a root element, no childs are supported by the test rule parameter
        assertTrue(ruleInput.isRoot());
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleParameter", 0);
        ruleInput.initFromXml(paramEl);
        assertEquals("rule1", ruleInput.getName());
        assertFalse(ruleInput.isInputParameter());
        assertTrue(ruleInput.isExpextedResultParameter());
        assertFalse(ruleInput.isCombinedParameter());        
        
        paramEl = XmlUtil.getElement(docEl, "RuleParameter", 1);
        ruleInput.initFromXml(paramEl);
        assertEquals("rule2", ruleInput.getName());
        assertFalse(ruleInput.isInputParameter());
        assertTrue(ruleInput.isExpextedResultParameter());
        assertFalse(ruleInput.isCombinedParameter());                
    }
    
    public void testToXml() {
        ruleInput.setName("rule3");
        ruleInput.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        Element el = ruleInput.toXml(newDocument());
        
        ruleInput.setName("rule4");
        boolean exceptionThrown = false;
        try {
            ruleInput.setTestParameterType(TestParameterType.INPUT);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        ruleInput.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        
        ruleInput.initFromXml(el);
        assertEquals("rule3", ruleInput.getName());
        assertFalse(ruleInput.isInputParameter());
        assertTrue(ruleInput.isExpextedResultParameter());
        assertFalse(ruleInput.isCombinedParameter());
    }
}
