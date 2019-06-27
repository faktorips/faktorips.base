/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Thorsten Guenther
 */
public class ValueToXmlHelperTest extends XmlAbstractTestCase {

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
        assertEquals("true", node.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL));
    }

    @Test
    public void testAddValueToElement_NullValue() {
        Document doc = getTestDocument();
        Element node = doc.createElement("ParentEl");
        assertEquals(0, node.getChildNodes().getLength());
        ValueToXmlHelper.addValueToElement(null, node, "Property");
        assertNull(ValueToXmlHelper.getValueFromElement(node, "Property"));
        assertFalse(node.hasAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL));
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

    @Test
    public void testGetUnrestrictedValueSet_containsNull() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");

        Element node = (Element)configElements.item(5);
        UnrestrictedValueSet<String> valueSet = ValueToXmlHelper.getUnrestrictedValueSet(node, "ValueSet");
        assertTrue(valueSet.containsNull());

        node = (Element)configElements.item(6);
        valueSet = ValueToXmlHelper.getUnrestrictedValueSet(node, "ValueSet");
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testAddTableUsageToElement() {
        Element element = getTestDocument().getDocumentElement();
        NodeList childNodes = element.getChildNodes();
        assertEquals(27, childNodes.getLength());

        ValueToXmlHelper.addTableUsageToElement(element, "structureUsageValue", "tableContentNameValue");

        assertEquals(28, childNodes.getLength());
        Node namedItem = childNodes.item(27).getAttributes().getNamedItem("structureUsage");
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        String nodeValue = childNodes.item(27).getFirstChild().getTextContent();
        assertEquals("tableContentNameValue", nodeValue);
    }

    @Test
    public void testGetInternationalStringFromElement() {
        Element attributeValueElement = (Element)getTestDocument().getDocumentElement()
                .getElementsByTagName("AttributeValue").item(0);

        DefaultInternationalString internationalString = ValueToXmlHelper.getInternationalStringFromElement(
                attributeValueElement, "Value");

        assertEquals("Wrong default locale", new Locale("hy"), internationalString.getDefaultLocale());
        assertEquals("Wrong value for locale 'as'", "asfdsa", internationalString.get(new Locale("as")));
        assertEquals("Wrong value for locale 'hy'", "hyfds", internationalString.get(new Locale("hy")));
        assertEquals("Wrong value for undefined locale 'ko'", "hyfds", internationalString.get(new Locale("ko")));

    }
}
