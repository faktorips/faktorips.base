/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

public class DynamicValueDatatypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    public void testCreateFromXml_NoneEnumType() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 0);
        DynamicValueDatatype type = DynamicValueDatatype.createFromXml(ipsProject, el);
        assertEquals("foo.bar.MyDate", type.getAdaptedClassName());
        assertEquals("getMyDate", type.getValueOfMethodName());
        assertEquals("isMyDate", type.getIsParsableMethodName());
        assertFalse(type.hasNullObject());
    }

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

    public void testCreateFromXml_NullObjectWithIdNotNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 2);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertTrue(type.hasNullObject());
        assertEquals("n", type.getNullObjectId());
    }

    public void testCreateFromXml_NullObjectWithIdNull() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, "Datatype", 3);
        DynamicEnumDatatype type = (DynamicEnumDatatype)DynamicValueDatatype.createFromXml(ipsProject, el);
        assertTrue(type.hasNullObject());
        assertNull(type.getNullObjectId());
    }

}
