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

package org.faktorips.runtime.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.internal.formula.FormulaEvaluatorFactory;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

public class ProductComponentGenerationTest extends XmlAbstractTestCase {

    private IRuntimeRepository repository;
    private ProductComponent pc;
    private TestProductCmptGeneration gen;

    @Override
    public void setUp() {
        repository = new InternalRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        gen = new TestProductCmptGeneration(pc);
    }

    public void testGetPropertyElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, Element> map = gen.getPropertyElements(genEl);
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

    public void testGetLinkElements() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, List<Element>> map = gen.getLinkElements(genEl);
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

    public void testAddToCardinalityMap() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, List<Element>> map = gen.getLinkElements(genEl);
        List<Element> list = map.get("relation3");
        assertEquals(1, list.size());
        Element relEl = list.get(0);
        HashMap<String, IntegerRange> cardinalityMap = new HashMap<String, IntegerRange>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation3", relEl);
        IntegerRange cardinality = cardinalityMap.get("relation3");
        assertEquals(new IntegerRange(0, Integer.MAX_VALUE), cardinality);

        list = map.get("relation4");
        assertEquals(1, list.size());
        relEl = list.get(0);
        cardinalityMap = new HashMap<String, IntegerRange>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation4", relEl);
        cardinality = cardinalityMap.get("relation4");
        assertEquals(new IntegerRange(0, Integer.MAX_VALUE), cardinality);

    }

    public void testFormulaEvaluation() {
        Element genEl = getTestDocument().getDocumentElement();
        gen.doInitFormulaFromXml(genEl);
        int result = gen.computeTestFormula(123, "abc");
        assertEquals(1, result);
    }

    private class InternalRuntimeRepository extends InMemoryRuntimeRepository {

        @Override
        public FormulaEvaluatorFactory getFormulaEvaluatorFactory() {
            try {
                return new FormulaEvaluatorFactory(getClassLoader(),
                        "org.faktorips.runtime.internal.formula.groovy.GroovyEvaluator");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
