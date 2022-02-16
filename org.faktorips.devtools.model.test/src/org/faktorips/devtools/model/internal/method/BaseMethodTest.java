/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class BaseMethodTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IBaseMethod method;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        method = new BaseMethod(type, "0");
    }

    @Test
    public void testGetSignatureString() {
        method.setName("calc");
        assertEquals("calc()", method.getSignatureString());

        method.newParameter("base.Vertrag", "vertrag");
        assertEquals("calc(base.Vertrag)", method.getSignatureString());

        method.newParameter("Integer", "zahlweise");
        assertEquals("calc(base.Vertrag, Integer)", method.getSignatureString());
    }

    @Test
    public void testInitFromXml() {
        Element docElement = getTestDocument().getDocumentElement();

        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertEquals("42", method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        IParameter[] params = method.getParameters();
        assertEquals(2, params.length);
        assertEquals("p1", params[0].getName());
        assertEquals("Money", params[0].getDatatype());
        assertEquals("p2", params[1].getName());
        assertEquals("Decimal", params[1].getDatatype());

        method.initFromXml(XmlUtil.getElement(docElement, "Method", 1));
        assertEquals(0, method.getNumOfParameters());
    }

    @Test
    public void testToXmlDocument() {
        method = new BaseMethod(type, "1"); // => id=1, because it's the second method
        method.setName("getAge");
        method.setDatatype("Decimal");
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(newDocument());

        IBaseMethod copy = new BaseMethod(type, "2");
        copy.initFromXml(element);
        IParameter[] copyParams = copy.getParameters();
        assertEquals(method.getId(), copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());

    }

    @Test
    public void testNewParameter() {
        IParameter param = method.newParameter();
        assertEquals(1, method.getParameters().length);
        assertEquals(param, method.getParameters()[0]);
        assertTrue(method.getIpsSrcFile().isDirty());
    }

    @Test
    public void testSetName() {
        testPropertyAccessReadWrite(BaseMethod.class, IIpsElement.PROPERTY_NAME, method, "calcPremium");
    }

    @Test
    public void testSetDatatype() {
        testPropertyAccessReadWrite(BaseMethod.class, IBaseMethod.PROPERTY_DATATYPE, method, "Integer");
    }

    @Test
    public void testIsSameSignature() {
        method.setName("calc");
        method.setDatatype("void");
        IParameter param0 = method.newParameter();
        param0.setName("p1");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p2");
        param1.setDatatype("Money");

        IBaseMethod other = new BaseMethod(type, "1");
        other.setDatatype("Money");
        other.setName("calc");
        IParameter otherParam0 = other.newParameter();
        otherParam0.setName("x");
        otherParam0.setDatatype("Decimal");
        IParameter otherParam1 = other.newParameter();
        otherParam1.setName("y");
        otherParam1.setDatatype("Money");

        // ok case
        assertTrue(method.isSameSignature(other));

        // different name
        other.setName("differentName");
        assertFalse(method.isSameSignature(other));

        // different parameter type
        other.setName("calc"); // make names equals again
        assertTrue(method.isSameSignature(other)); // and test it
        otherParam1.setDatatype("int");
        assertFalse(method.isSameSignature(other));

        // different number of parameters
        other.newParameter();
        assertFalse(method.isSameSignature(other));
    }

    @Test
    public void testValidateMultipleParameterNames() {
        IType pcType = newPolicyCmptType(ipsProject, "aType");
        method = pcType.newMethod();
        method.setName("calculate");
        method.setDatatype("String");
        method.newParameter(Datatype.STRING.getName(), "param1");
        IParameter p2 = method.newParameter(Datatype.INTEGER.getName(), "param2");

        MessageList msgList = method.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        p2.setName("param1");
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

        p2.setName("param2");
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

    }
}
