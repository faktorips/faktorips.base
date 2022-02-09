/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcasetype;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
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
        assertThat(ml, lacksMessageCode(ITestRuleParameter.MSGCODE_NOT_EXPECTED_RESULT));

        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "RuleParameter", 2);
        // force object change to revalidate the object
        String name = ruleInput.getName();
        ruleInput.setName("x");
        ruleInput.setName(name);
        ruleInput.initFromXml(paramEl);
        ml = ruleInput.validate(project);
        assertThat(ml, hasMessageCode(ITestRuleParameter.MSGCODE_NOT_EXPECTED_RESULT));
    }
}
