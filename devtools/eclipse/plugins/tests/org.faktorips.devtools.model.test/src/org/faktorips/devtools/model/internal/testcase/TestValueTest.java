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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
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
    private ITestCase testCase;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");

        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE,
                "PremiumCalculation");
        testCaseType.newInputTestValueParameter().setName("testValueParameter1");
        testCaseType.newInputTestValueParameter().setName("testValueParameter3");
        testCaseType.newExpectedResultValueParameter().setName("testValueParameter2");

        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");

        testCase.setTestCaseType(testCaseType.getName());
        (valueObjectInput = testCase.newTestValue()).setTestValueParameter("testValueParameter1");
        (valueObjectInput2 = testCase.newTestValue()).setTestValueParameter("testValueParameter3");
        (valueObjectExpectedValue = testCase.newTestValue()).setTestValueParameter("testValueParameter2");
        (valueObjectUnknown = testCase.newTestValue()).setTestValueParameter("testValueParameter4");
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueObject", 0);
        valueObjectInput.initFromXml(paramEl);
        assertEquals("newSumInsured", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }

    @Test
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

    @Test
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

    @Test
    public void testValidateTestValueParamNotFound() throws Exception {
        MessageList ml = valueObjectInput.validate(project);
        assertThat(ml, lacksMessageCode(ITestValue.MSGCODE_TEST_VALUE_PARAM_NOT_FOUND));

        valueObjectInput.setTestValueParameter("x");
        ml = valueObjectInput.validate(project);
        assertThat(ml, hasMessageCode(ITestValue.MSGCODE_TEST_VALUE_PARAM_NOT_FOUND));
    }

    @Test
    public void testValidateValueDatatypeNotFound() throws Exception {
        ITestValueParameter param = valueObjectInput.findTestValueParameter(project);
        param.setValueDatatype("String");
        MessageList ml = valueObjectInput.validate(project);
        assertThat(ml, lacksMessageCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));

        // check if the message is a warning, because it will be validated as error in the parameter
        param.setValueDatatype("x");
        ml = valueObjectInput.validate(project);
        assertEquals(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND,
                ml.getFirstMessage(Message.WARNING).getCode());
    }

    @Test
    public void testValidateWrongType() throws Exception {
        MessageList ml = valueObjectInput.validate(project);
        assertThat(ml, lacksMessageCode(ITestValueParameter.MSGCODE_WRONG_TYPE));

        // remark the test if the message will be set couldn't be tested here because setting
        // a wrong type of the parameter is not possible without getting an argument exception
        // see TestValueParameter#setTestParameterType
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile_TestValue() throws Exception {
        IIpsSrcFile ipsFile = testCase.getIpsSrcFile();
        valueObjectInput.setValue("Blabla");
        ipsFile.save(null);
        String encoding = testCase.getIpsProject().getXmlFileCharset();
        AFile file = ipsFile.getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("Blabla", "NewBlabla");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);

        testCase = (ITestCase)ipsFile.getIpsObject(); // forces a reload

        assertThat(valueObjectInput.isDeleted(), is(false));
        assertEquals("NewBlabla", valueObjectInput.getValue());

        ITestValue reloadedTestValue = Arrays.stream(testCase.getTestValues())
                .filter(testValue -> testValue.getTestValueParameter().equals(valueObjectInput.getTestValueParameter()))
                .findFirst()
                .orElse(null);

        assertNotNull(reloadedTestValue);
        assertSame(valueObjectInput, reloadedTestValue);
    }

}
