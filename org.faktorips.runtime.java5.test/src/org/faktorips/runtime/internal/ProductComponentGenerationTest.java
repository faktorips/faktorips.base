/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.valueset.IntegerRange;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

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

    @Test
    public void testGetValidationRuleConfigElements() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.doInitValidationRuleConfigsFromXml(genElement);
        Map<String, ValidationRuleConfiguration> configsMap = gen.getNameToValidationRuleConfigMap();

        assertEquals(3, configsMap.size());
        ValidationRuleConfiguration config = configsMap.get("Regel1");
        assertNotNull(config);
        assertEquals("Regel1", config.getRuleName());
        assertEquals(true, config.isActive());

        config = configsMap.get("RegelZwei");
        assertNotNull(config);
        assertEquals("RegelZwei", config.getRuleName());
        assertEquals(false, config.isActive());

        config = configsMap.get("RegelDrei");
        assertNotNull(config);
        assertEquals("RegelDrei", config.getRuleName());
        assertEquals(false, config.isActive());

        config = configsMap.get("nonExistentRule");
        assertNull(config);
    }

    @Test
    public void testInitVRuleConfigs() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        assertEquals(true, gen.isValidationRuleActivated("Regel1"));
        assertEquals(false, gen.isValidationRuleActivated("RegelZwei"));
        assertEquals(false, gen.isValidationRuleActivated("RegelDrei"));

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
}
