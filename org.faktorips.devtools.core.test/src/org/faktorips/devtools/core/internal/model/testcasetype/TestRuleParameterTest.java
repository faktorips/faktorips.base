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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestRuleParameterTest extends AbstractIpsPluginTest {

    private ITestRuleParameter ruleInput;
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ruleInput = type.newExpectedResultRuleParameter();
    }

    @Test
    public void testIsRoot() {
        // a test rule parameter is always a root element, no childs are supported by the test rule
        // parameter
        assertTrue(ruleInput.isRoot());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleParameter", 0);
        ruleInput.initFromXml(paramEl);
        assertEquals("rule1", ruleInput.getName());
        assertFalse(ruleInput.isInputOrCombinedParameter());
        assertTrue(ruleInput.isExpextedResultOrCombinedParameter());
        assertFalse(ruleInput.isCombinedParameter());

        paramEl = XmlUtil.getElement(docEl, "RuleParameter", 1);
        ruleInput.initFromXml(paramEl);
        assertEquals("rule2", ruleInput.getName());
        assertFalse(ruleInput.isInputOrCombinedParameter());
        assertTrue(ruleInput.isExpextedResultOrCombinedParameter());
        assertFalse(ruleInput.isCombinedParameter());
    }

    @Test
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
        assertFalse(ruleInput.isInputOrCombinedParameter());
        assertTrue(ruleInput.isExpextedResultOrCombinedParameter());
        assertFalse(ruleInput.isCombinedParameter());
    }

    @Test
    public void testValidateWrongType() throws Exception {
        MessageList ml = ruleInput.validate(project);
        assertNull(ml.getMessageByCode(ITestRuleParameter.MSGCODE_NOT_EXPECTED_RESULT));

        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleParameter", 2);
        // force object change to revalidate the object
        String name = ruleInput.getName();
        ruleInput.setName("x");
        ruleInput.setName(name);
        ruleInput.initFromXml(paramEl);
        ml = ruleInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestRuleParameter.MSGCODE_NOT_EXPECTED_RESULT));
    }
}
