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
        assertEquals("true", extProp1.getAttribute("isNull"));

        assertEquals(1, extProp2.getChildNodes().getLength());
        assertEquals("importantValue", extProp2.getChildNodes().item(0).getNodeValue());
    }

    private Map<String, String> createExtPropertyMap() {
        // use a SortedMap to ensure order and thus predictable tests
        Map<String, String> map = new TreeMap<String, String>();
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
