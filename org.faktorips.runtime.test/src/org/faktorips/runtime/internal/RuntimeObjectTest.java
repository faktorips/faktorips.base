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

import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class RuntimeObjectTest {
    @Test
    public void writeExtensionPropertiesToXML() throws ParserConfigurationException {
        Element productCmpt = createProdCmptElement();
        Map<String, String> extPropertyMap = createExtPropertyMap();

        assertEquals(0, productCmpt.getChildNodes().getLength());
        RuntimeObject rtObject = new RuntimeObject();
        rtObject.writeExtensionPropertiesToXml(productCmpt, extPropertyMap);

        assertEquals(1, productCmpt.getChildNodes().getLength());

        Element extPropRoot = (Element)productCmpt.getChildNodes().item(0);
        assertEquals(2, extPropRoot.getChildNodes().getLength());

        Element extProp1 = (Element)extPropRoot.getChildNodes().item(0);
        Element extProp2 = (Element)extPropRoot.getChildNodes().item(1);

        assertEquals(0, extProp1.getChildNodes().getLength());
        assertEquals("true", extProp1.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL));

        assertEquals(1, extProp2.getChildNodes().getLength());
        assertEquals("importantValue", extProp2.getChildNodes().item(0).getNodeValue());
    }

    private Map<String, String> createExtPropertyMap() {
        // use a SortedMap to ensure order and thus predictable tests
        Map<String, String> map = new TreeMap<>();
        map.put("id1", null);
        map.put("idTwo", "importantValue");
        return map;
    }

    protected Element createProdCmptElement() throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element productCmpt = document.createElement("ProductComponent");
        return productCmpt;
    }

}
