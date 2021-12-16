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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.valueset.IntegerRange;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProductComponentGenerationTest extends XmlAbstractTestCase {

    private IRuntimeRepository repository;
    private ProductComponent pc;
    private TestProductCmptGeneration gen;

    @Before
    public void setUp() {
        repository = new InMemoryRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        gen = new TestProductCmptGeneration(pc);
    }

    @Test
    public void testAddToCardinalityMap() {
        Element genEl = getTestDocument().getDocumentElement();
        Map<String, List<Element>> map = ProductComponentXmlUtil.getLinkElements(genEl);
        List<Element> list = map.get("relation3");
        assertEquals(1, list.size());
        Element relEl = list.get(0);
        HashMap<String, IntegerRange> cardinalityMap = new HashMap<>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation3", relEl);
        IntegerRange cardinality = cardinalityMap.get("relation3");
        assertEquals(IntegerRange.valueOf(0, Integer.MAX_VALUE), cardinality);

        list = map.get("relation4");
        assertEquals(1, list.size());
        relEl = list.get(0);
        cardinalityMap = new HashMap<>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation4", relEl);
        cardinality = cardinalityMap.get("relation4");
        assertEquals(IntegerRange.valueOf(0, Integer.MAX_VALUE), cardinality);
    }

    @Test
    public void testSetValidationRuleActivated() {

        assertEquals(false, gen.isValidationRuleActivated("MyRule"));

        gen.setValidationRuleActivated("MyRule", true);

        assertEquals(true, gen.isValidationRuleActivated("MyRule"));

        gen.setValidationRuleActivated("MyRule", false);

        assertEquals(false, gen.isValidationRuleActivated("MyRule"));

    }

    @Test
    public void testInitVRuleConfigs() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        assertEquals(true, gen.isValidationRuleActivated("activeRule"));
        assertEquals(false, gen.isValidationRuleActivated("inactiveRule"));
        assertEquals(false, gen.isValidationRuleActivated("invalidActivationRule"));

        assertEquals(false, gen.isValidationRuleActivated("nonExistentRule"));
    }

    @Test
    public void testIsFormulaAvailable() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        assertTrue(gen.isFormulaAvailable("testFormula"));
        assertFalse(gen.isFormulaAvailable("emptyFormula"));
        assertFalse(gen.isFormulaAvailable("notExistingFormula"));
    }

    @Test
    public void testWriteTableUsageToXml() {
        Element genElement = getTestDocument().getDocumentElement();
        NodeList childNodes = genElement.getChildNodes();
        assertEquals(31, childNodes.getLength());

        gen.writeTableUsageToXml(genElement, "structureUsageValue", "tableContentNameValue");

        assertEquals(32, childNodes.getLength());
        Node namedItem = childNodes.item(31).getAttributes().getNamedItem("structureUsage");
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        String nodeValue = childNodes.item(31).getFirstChild().getTextContent();
        assertEquals("tableContentNameValue", nodeValue);
    }

    @Test
    public void testSetValidFrom() {
        gen.setValidFrom(new DateTime(2010, 1, 1));
        assertEquals(new DateTime(2010, 1, 1), gen.getValidFrom());
    }

    @Test
    public void testSetValidFrom_noRuntimeRepository() {
        gen = spy(gen);
        when(gen.getRepository()).thenReturn(null);

        gen.setValidFrom(new DateTime(2010, 1, 1));

        assertEquals(new DateTime(2010, 1, 1), gen.getValidFrom());
    }

    @Test(expected = IllegalRepositoryModificationException.class)
    public void testSetValidFrom_throwExceptionIfRepositoryNotModifiable() {
        IRuntimeRepository repository = mock(IRuntimeRepository.class);
        when(repository.isModifiable()).thenReturn(false);

        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        gen = new TestProductCmptGeneration(pc);

        gen.setValidFrom(new DateTime(2010, 1, 1));
    }

    @Test(expected = NullPointerException.class)
    public void testSetValidFrom_throwExceptionIfValidFromIsNull() {
        gen.setValidFrom(null);
    }

}
