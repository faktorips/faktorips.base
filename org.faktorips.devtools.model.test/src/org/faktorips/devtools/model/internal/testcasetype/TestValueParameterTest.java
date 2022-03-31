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
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
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
public class TestValueParameterTest extends AbstractIpsPluginTest {

    private ITestValueParameter valueParamInput;
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        valueParamInput = type.newInputTestValueParameter();
    }

    @Test
    public void testIsRoot() {
        // a test value parameter is always a root element, no childs are supported by the test
        // value parameter
        assertTrue(valueParamInput.isRoot());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueParameter", 0);
        valueParamInput.initFromXml(paramEl);
        assertEquals("Money", valueParamInput.getValueDatatype());
        assertEquals("newSumInsured1", valueParamInput.getName());
        assertTrue(valueParamInput.isInputOrCombinedParameter());
        assertFalse(valueParamInput.isExpextedResultOrCombinedParameter());
        assertFalse(valueParamInput.isCombinedParameter());
    }

    @Test
    public void testToXml() {
        valueParamInput.setValueDatatype("Money");
        valueParamInput.setName("newSumInsured");
        valueParamInput.setTestParameterType(TestParameterType.INPUT);
        Element el = valueParamInput.toXml(newDocument());

        valueParamInput.setValueDatatype("Integer");
        valueParamInput.setName("test");
        valueParamInput.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        valueParamInput.initFromXml(el);

        assertEquals("Money", valueParamInput.getValueDatatype());
        assertEquals("newSumInsured", valueParamInput.getName());
        assertTrue(valueParamInput.isInputOrCombinedParameter());
        assertFalse(valueParamInput.isExpextedResultOrCombinedParameter());
        assertFalse(valueParamInput.isCombinedParameter());
    }

    @Test
    public void testValidateWrongType() throws Exception {
        MessageList ml = valueParamInput.validate(project);
        assertThat(ml, lacksMessageCode(ITestValueParameter.MSGCODE_WRONG_TYPE));

        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueParameter", 3);
        // force object change to revalidate the object
        String name = valueParamInput.getName();
        valueParamInput.setName("x");
        valueParamInput.setName(name);
        valueParamInput.initFromXml(paramEl);
        ml = valueParamInput.validate(project);
        assertThat(ml, hasMessageCode(ITestValueParameter.MSGCODE_WRONG_TYPE));
    }

    @Test
    public void testValidateValueDatatypeNotFound() throws Exception {
        valueParamInput.setDatatype("String");
        MessageList ml = valueParamInput.validate(project);
        assertThat(ml, lacksMessageCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));

        valueParamInput.setDatatype("x");
        ml = valueParamInput.validate(project);
        assertThat(ml, hasMessageCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));
    }
}
