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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals(6, map.size());

        assertNull(map.get("attribute1"));

        Element attr1El = map.get("@default_attribute1");
        assertEquals("ConfiguredDefault", attr1El.getNodeName());
        assertEquals("attribute1", attr1El.getAttribute("attribute"));
        assertEquals("2", attr1El.getAttribute("value"));
        Element attr1VSEl = map.get("@valueSet_attribute1");
        assertEquals("ConfiguredValueSet", attr1VSEl.getNodeName());
        assertEquals("attribute1", attr1VSEl.getAttribute("attribute"));

        Element attr2El = map.get("@default_attribute2");
        assertEquals("ConfiguredDefault", attr2El.getNodeName());
        assertEquals("attribute2", attr2El.getAttribute("attribute"));
        assertEquals("m", attr2El.getAttribute("value"));
        Element attr2VSEl = map.get("@valueSet_attribute2");
        assertEquals("ConfiguredValueSet", attr2VSEl.getNodeName());
        assertEquals("attribute2", attr2VSEl.getAttribute("attribute"));

        Element attr3El = map.get("attribute3");
        assertEquals(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE, attr3El.getNodeName());
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

    @Test
    public void testGetAvailableFormulars() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, String> availableFormulas = ProductComponentXmlUtil.getAvailableFormulars(genEl);

        assertEquals(3, availableFormulas.size());
        assertTrue(availableFormulas.containsKey("testFormula"));
        assertFalse(IpsStringUtils.isEmpty(availableFormulas.get("testFormula")));

        assertTrue(availableFormulas.containsKey("emptyFormula"));
        assertTrue(IpsStringUtils.isEmpty(availableFormulas.get("emptyFormula")));

        assertTrue(availableFormulas.containsKey("whitespaceFormula"));
        assertTrue(IpsStringUtils.isEmpty(availableFormulas.get("whitespaceFormula")));
    }
}
