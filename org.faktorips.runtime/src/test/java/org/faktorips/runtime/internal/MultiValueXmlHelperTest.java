/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MultiValueXmlHelperTest extends XmlAbstractTestCase {

    @Test
    public void readValuesFromXML() {
        Element configElement = getConfigElement(0);

        List<String> valuesFromXML = MultiValueXmlHelper.getValuesFromXML(configElement);
        assertEquals(3, valuesFromXML.size());
        assertEquals("foo", valuesFromXML.get(0));
        assertNull(valuesFromXML.get(1));
        assertEquals("bar", valuesFromXML.get(2));
    }

    private Element getConfigElement(int index) {
        return (Element)getTestDocument().getDocumentElement().getElementsByTagName("ConfigElement").item(index);
    }

    @Test(expected = NullPointerException.class)
    public void readValuesFromXML_missingOuterValueTag() {
        Element configElement = getConfigElement(1);
        MultiValueXmlHelper.getValuesFromXML(configElement);
    }

    @Test(expected = NullPointerException.class)
    public void readValuesFromXML_missingMultiValueTag() {
        Element configElement = getConfigElement(2);
        MultiValueXmlHelper.getValuesFromXML(configElement);
    }

    @Test
    public void addValuesToElement() {
        List<String> stringList = new ArrayList<>();
        stringList.add("foo");
        stringList.add(null);
        stringList.add("bar");
        stringList.add("4711");

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addValuesToElement(attrValueElement, stringList);

        NodeList outerValueElementNodeList = attrValueElement.getChildNodes();
        assertEquals(1, outerValueElementNodeList.getLength());
        Element outerValueElement = (Element)outerValueElementNodeList.item(0);

        NodeList multiValueElementNodeList = outerValueElement.getChildNodes();
        assertEquals(1, multiValueElementNodeList.getLength());
        Element multiValueElement = (Element)multiValueElementNodeList.item(0);

        NodeList valueElementNodeList = multiValueElement.getChildNodes();
        assertEquals(4, valueElementNodeList.getLength());
        assertEquals("foo", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(0)));
        assertNull(ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(1)));
        assertEquals("bar", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(2)));
        assertEquals("4711", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(3)));
    }

    @Test
    public void roundTripTest() {
        List<String> stringList = new ArrayList<>();
        stringList.add("foo");
        stringList.add(null);
        stringList.add("bar");
        stringList.add("4711");

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addValuesToElement(attrValueElement, stringList);
        List<String> resultList = MultiValueXmlHelper.getValuesFromXML(attrValueElement);
        assertNotSame(stringList, resultList);
        assertEquals(stringList, resultList);
    }
}
