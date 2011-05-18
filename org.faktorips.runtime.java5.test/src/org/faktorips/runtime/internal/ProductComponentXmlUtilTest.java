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
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductComponentXmlUtilTest extends XmlAbstractTestCase {

    @Test
    public void testGetPropertyElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, Element> map = ProductComponentXmlUtil.getPropertyElements(genEl);
        assertEquals(4, map.size());

        Element attr1El = map.get("attribute1");
        assertEquals("ConfigElement", attr1El.getNodeName());
        assertEquals("attribute1", attr1El.getAttribute("attribute"));
        assertEquals("2", attr1El.getAttribute("value"));

        Element attr2El = map.get("attribute2");
        assertEquals("ConfigElement", attr2El.getNodeName());
        assertEquals("attribute2", attr2El.getAttribute("attribute"));
        assertEquals("m", attr2El.getAttribute("value"));

        Element attr3El = map.get("attribute3");
        assertEquals("AttributeValue", attr3El.getNodeName());
        assertEquals("attribute3", attr3El.getAttribute("attribute"));
        assertEquals("42", attr3El.getAttribute("value"));

        Element tsuEl = map.get("rateTable");
        assertNotNull(tsuEl);
    }

    @Test
    public void testGetLinkElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, List<Element>> map = ProductComponentXmlUtil.getLinkElements(genEl);
        assertEquals(4, map.size());

        List<Element> list1 = map.get("relation1");
        assertEquals(2, list1.size());
        Element rel1aEl = list1.get(0);
        assertNotNull(rel1aEl);
        assertEquals("Link", rel1aEl.getNodeName());
        assertEquals("target1a", rel1aEl.getAttribute("target"));

        Element rel1bEl = list1.get(1);
        assertNotNull(rel1bEl);
        assertEquals("Link", rel1bEl.getNodeName());
        assertEquals("target1b", rel1bEl.getAttribute("target"));

        List<Element> list2 = map.get("relation2");
        assertEquals(1, list2.size());
        Element rel2El = list2.get(0);
        assertNotNull(rel2El);
        assertEquals("Link", rel2El.getNodeName());
        assertEquals("target2", rel2El.getAttribute("target"));
    }

}
