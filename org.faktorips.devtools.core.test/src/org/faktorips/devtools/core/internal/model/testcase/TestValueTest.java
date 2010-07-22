/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestValueTest extends AbstractIpsPluginTest {

    private ITestValue valueObjectInput;
    private ITestValue valueObjectInput2;
    private ITestValue valueObjectExpectedValue;
    private ITestValue valueObjectUnknown;
    private IIpsProject project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");

        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE,
                "PremiumCalculation");
        testCaseType.newInputTestValueParameter().setName("testValueParameter1");
        testCaseType.newInputTestValueParameter().setName("testValueParameter3");
        testCaseType.newExpectedResultValueParameter().setName("testValueParameter2");

        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");

        testCase.setTestCaseType(testCaseType.getName());
        (valueObjectInput = testCase.newTestValue()).setTestValueParameter("testValueParameter1");
        (valueObjectInput2 = testCase.newTestValue()).setTestValueParameter("testValueParameter3");
        (valueObjectExpectedValue = testCase.newTestValue()).setTestValueParameter("testValueParameter2");
        (valueObjectUnknown = testCase.newTestValue()).setTestValueParameter("testValueParameter4");
    }

    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueObject", 0);
        valueObjectInput.initFromXml(paramEl);
        assertEquals("newSumInsured", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }

    public void testToXml() {
        valueObjectInput.setTestValueParameter("Money");
        valueObjectInput.setValue("500");
        Element el = valueObjectInput.toXml(newDocument());

        valueObjectInput.setTestValueParameter("Test");
        valueObjectInput.setValue("Test");

        valueObjectInput.initFromXml(el);
        assertEquals("Money", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }

    public void testTestObjectRole() {
        assertTrue(valueObjectInput.isInput());
        assertFalse(valueObjectInput.isExpectedResult());
        assertFalse(valueObjectInput.isCombined());

        assertFalse(valueObjectExpectedValue.isInput());
        assertTrue(valueObjectExpectedValue.isExpectedResult());
        assertFalse(valueObjectExpectedValue.isCombined());

        assertTrue(valueObjectInput2.isInput());
        assertFalse(valueObjectInput2.isExpectedResult());
        assertFalse(valueObjectInput2.isCombined());

        assertFalse(valueObjectUnknown.isInput());
        assertFalse(valueObjectUnknown.isExpectedResult());
        // combined is the default role
        assertTrue(valueObjectUnknown.isCombined());
    }

    public void testValidateTestValueParamNotFound() throws Exception {
        MessageList ml = valueObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestValue.MSGCODE_TEST_VALUE_PARAM_NOT_FOUND));

        valueObjectInput.setTestValueParameter("x");
        ml = valueObjectInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestValue.MSGCODE_TEST_VALUE_PARAM_NOT_FOUND));
    }

    public void testValidateValueDatatypeNotFound() throws Exception {
        ITestValueParameter param = valueObjectInput.findTestValueParameter(project);
        param.setValueDatatype("String");
        MessageList ml = valueObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));

        // check if the message is a warning, because it will be validated as error in the parameter
        param.setValueDatatype("x");
        ml = valueObjectInput.validate(project);
        assertEquals(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND, ml.getFirstMessage(Message.WARNING).getCode());
    }

    public void testValidateWrongType() throws Exception {
        MessageList ml = valueObjectInput.validate(project);
        assertNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_WRONG_TYPE));

        // remark the test if the message will be set couldn't be tested here because setting
        // a wrong type of the parameter is not possible without getting an argument exception
        // see TestValueParameter#setTestParameterType
    }
}
