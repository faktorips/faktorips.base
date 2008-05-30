/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

public class ProductComponentGenerationTest extends XmlAbstractTestCase {

    private IRuntimeRepository repository;
    private ProductComponent pc;
    private ProductComponentGeneration gen;

    public void setUp() {
        repository = new InMemoryRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        gen = new TestProductCmptGeneration(pc);
    }
    
    public void testGetPropertyElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map map = gen.getPropertyElements(genEl);
        assertEquals(4, map.size());

        Element attr1El = (Element)map.get("attribute1");
        assertEquals("ConfigElement", attr1El.getNodeName());
        assertEquals("attribute1", attr1El.getAttribute("attribute"));
        assertEquals("2", attr1El.getAttribute("value"));
        
        Element attr2El = (Element)map.get("attribute2");
        assertEquals("ConfigElement", attr2El.getNodeName());
        assertEquals("attribute2", attr2El.getAttribute("attribute"));
        assertEquals("m", attr2El.getAttribute("value"));
        
        Element attr3El = (Element)map.get("attribute3");
        assertEquals("AttributeValue", attr3El.getNodeName());
        assertEquals("attribute3", attr3El.getAttribute("attribute"));
        assertEquals("42", attr3El.getAttribute("value"));
        
        Element tsuEl = (Element)map.get("rateTable");
        assertNotNull(tsuEl);
    }

    public void testGetLinkElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map map = gen.getLinkElements(genEl);
        assertEquals(4, map.size());
        
        ArrayList list1 = (ArrayList)map.get("relation1");
        assertEquals(2, list1.size());
        Element rel1aEl = (Element)list1.get(0);
        assertNotNull(rel1aEl);
        assertEquals("Link", rel1aEl.getNodeName());
        assertEquals("target1a", rel1aEl.getAttribute("target"));
        
        Element rel1bEl = (Element)list1.get(1);
        assertNotNull(rel1bEl);
        assertEquals("Link", rel1bEl.getNodeName());
        assertEquals("target1b", rel1bEl.getAttribute("target"));
        
        ArrayList list2 = (ArrayList)map.get("relation2");
        assertEquals(1, list2.size());
        Element rel2El = (Element)list2.get(0);
        assertNotNull(rel2El);
        assertEquals("Link", rel2El.getNodeName());
        assertEquals("target2", rel2El.getAttribute("target"));
    }

    public void testAddToCardinalityMap(){
        Element genEl = getTestDocument().getDocumentElement();
        Map map = gen.getLinkElements(genEl);
        ArrayList list = (ArrayList)map.get("relation3");
        assertEquals(1, list.size());
        Element relEl = (Element)list.get(0);
        HashMap cardinalityMap = new HashMap();
        gen.addToCardinalityMap(cardinalityMap, "relation3", relEl);
        IntegerRange cardinality = (IntegerRange)cardinalityMap.get("relation3");
        assertEquals(new IntegerRange(0, Integer.MAX_VALUE), cardinality);

        list = (ArrayList)map.get("relation4");
        assertEquals(1, list.size());
        relEl = (Element)list.get(0);
        cardinalityMap = new HashMap();
        gen.addToCardinalityMap(cardinalityMap, "relation4", relEl);
        cardinality = (IntegerRange)cardinalityMap.get("relation4");
        assertEquals(new IntegerRange(0, Integer.MAX_VALUE), cardinality);
        
    }
}
