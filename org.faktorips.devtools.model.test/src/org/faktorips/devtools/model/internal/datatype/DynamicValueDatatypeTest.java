/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class DynamicValueDatatypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    @Test
    public void testCreateFromXml_NoneEnumType() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 0);
        DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);
        assertEquals("foo.bar.MyDate", type.getAdaptedClassName());
        assertEquals("getMyDate", type.getValueOfMethodName());
        assertEquals("isMyDate", type.getIsParsableMethodName());
        assertFalse(type.hasNullObject());
    }

    @Test
    public void testCreateFromXml_EnumType() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 1);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertEquals("foo.bar.PaymentMode", type.getAdaptedClassName());
        assertEquals("getPaymentMode", type.getValueOfMethodName());
        assertEquals("isPaymentMode", type.getIsParsableMethodName());
        assertEquals("getId", type.getToStringMethodName());
        assertEquals("getName", type.getGetNameMethodName());
        assertEquals("getPaymentModes", type.getAllValuesMethodName());
        assertTrue(type.isSupportingNames());
        assertFalse(type.hasNullObject());
    }

    @Test
    public void testCreateFromXml_NullObjectWithIdNotNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 2);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertTrue(type.hasNullObject());
        assertEquals("n", type.getNullObjectId());
    }

    @Test
    public void testCreateFromXml_NullObjectWithIdNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 3);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertTrue(type.hasNullObject());
        assertNull(type.getNullObjectId());
    }

    @Test
    public void testGetNamedDatatype() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 4);
        DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);

        assertThat(type.getAdaptedClassName(), is("foo.bar.MyDataType"));
        assertThat(type.isEnum(), is(false));
        assertThat(type.isSupportingNames(), is(true));
        assertThat(type.getGetNameMethodName(), is("getSymbol"));
        assertThat(type.getGetValueByNameMethodName(), is("getValueByName"));
    }
}
