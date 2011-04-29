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

package org.faktorips.devtools.core.internal.model.testcasetype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
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
        assertNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_WRONG_TYPE));

        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueParameter", 3);
        // force object change to revalidate the object
        String name = valueParamInput.getName();
        valueParamInput.setName("x");
        valueParamInput.setName(name);
        valueParamInput.initFromXml(paramEl);
        ml = valueParamInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_WRONG_TYPE));
    }

    @Test
    public void testValidateValueDatatypeNotFound() throws Exception {
        valueParamInput.setDatatype("String");
        MessageList ml = valueParamInput.validate(project);
        assertNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));

        valueParamInput.setDatatype("x");
        ml = valueParamInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestValueParameter.MSGCODE_VALUEDATATYPE_NOT_FOUND));
    }
}
