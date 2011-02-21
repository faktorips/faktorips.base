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

package org.faktorips.runtime.internal;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Thorsten Guenther
 */
public class ValueToXmlHelperTest extends XmlAbstractTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAddValueToElement() {
        Document doc = getTestDocument();
        Element node = doc.createElement("ParentEl");
        assertEquals(0, node.getChildNodes().getLength());
        ValueToXmlHelper.addValueToElement("Value", node, "Property");
        assertEquals("Value", ValueToXmlHelper.getValueFromElement(node, "Property"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        ValueToXmlHelper.addValueToElement(null, node, "ValueNode");
        node = (Element)node.getElementsByTagName("ValueNode").item(0);
        assertEquals("true", node.getAttribute("isNull"));
    }

    @Test
    public void testGetValueFromElement() {
        Document doc = getTestDocument();

        Element node = (Element)doc.getDocumentElement().getElementsByTagName("TestElement").item(0);
        assertEquals("cdataValue", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyValueTestElement").item(0);
        assertEquals("", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("NullTestElement").item(0);
        assertNull(ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        assertNull(ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("TextNodeElement").item(0);
        assertEquals("42", ValueToXmlHelper.getValueFromElement(node, "Property"));
    }

    @Test
    public void testGetRangeFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(0);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNotNull(range);
        assertEquals("100", range.getLower());
        assertEquals("200", range.getUpper());
        assertEquals("10", range.getStep());
        assertFalse(range.containsNull());

        node = (Element)configElements.item(1);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNull(range);

        node = (Element)configElements.item(2);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNull(range);
    }

    @Test
    public void testGetEnumValueSetFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(3);
        EnumValues enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertNotNull(enumValues);
        assertEquals(2, enumValues.getNumberOfValues());
        assertEquals("10.0", enumValues.getValue(0));
        assertEquals("20.0", enumValues.getValue(1));
        assertFalse(enumValues.containsNull());

        node = (Element)configElements.item(4);
        enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertNotNull(enumValues);
        assertEquals(3, enumValues.getNumberOfValues());
        assertEquals("j", enumValues.getValue(0));
        assertEquals("h", enumValues.getValue(1));
        assertTrue(enumValues.containsNull());
    }
}
