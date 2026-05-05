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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
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
        assertThat(list.size(), is(1));
        Element relEl = list.get(0);
        HashMap<String, IntegerRange> cardinalityMap = new HashMap<>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation3", relEl);
        IntegerRange cardinality = cardinalityMap.get("relation3");
        assertThat(cardinality, is(IntegerRange.valueOf(0, Integer.MAX_VALUE)));

        list = map.get("relation4");
        assertThat(list.size(), is(1));
        relEl = list.get(0);
        cardinalityMap = new HashMap<>();
        ProductComponentGeneration.addToCardinalityMap(cardinalityMap, "relation4", relEl);
        cardinality = cardinalityMap.get("relation4");
        assertThat(cardinality, is(IntegerRange.valueOf(0, Integer.MAX_VALUE)));
    }

    @Test
    public void testSetValidationRuleActivated() {

        assertThat(gen.isValidationRuleActivated("MyRule"), is(false));

        gen.setValidationRuleActivated("MyRule", true);

        assertThat(gen.isValidationRuleActivated("MyRule"), is(true));

        gen.setValidationRuleActivated("MyRule", false);

        assertThat(gen.isValidationRuleActivated("MyRule"), is(false));

    }

    @Test
    public void testInitVRuleConfigs() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        assertThat(gen.isValidationRuleActivated("activeRule"), is(true));
        assertThat(gen.isValidationRuleActivated("inactiveRule"), is(false));
        assertThat(gen.isValidationRuleActivated("invalidActivationRule"), is(false));

        assertThat(gen.isValidationRuleActivated("nonExistentRule"), is(false));
    }

    @Test
    public void testIsFormulaAvailable() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        assertThat(gen.isFormulaAvailable("testFormula"), is(true));
        assertThat(gen.isFormulaAvailable("emptyFormula"), is(false));
        assertThat(gen.isFormulaAvailable("notExistingFormula"), is(false));
    }

    @Test
    public void testWriteTableUsageToXml() {
        Element genElement = getTestDocument().getDocumentElement();
        NodeList childNodes = genElement.getChildNodes();
        int initialLength = childNodes.getLength();

        gen.writeTableUsageToXml(genElement, "structureUsageValue", "tableContentNameValue");

        assertThat(childNodes.getLength(), is(initialLength + 1));
        Node addedNode = childNodes.item(initialLength);
        Node namedItem = addedNode.getAttributes().getNamedItem("structureUsage");
        assertThat(namedItem.getNodeValue(), is("structureUsageValue"));
        String nodeValue = addedNode.getFirstChild().getTextContent();
        assertThat(nodeValue, is("tableContentNameValue"));
    }

    @Test
    public void testSetValidFrom() {
        gen.setValidFrom(new DateTime(2010, 1, 1));
        assertThat(gen.getValidFrom(), is(new DateTime(2010, 1, 1)));
    }

    @Test
    public void testSetValidFrom_noRuntimeRepository() {
        gen = spy(gen);
        when(gen.getRepository()).thenReturn(null);

        gen.setValidFrom(new DateTime(2010, 1, 1));

        assertThat(gen.getValidFrom(), is(new DateTime(2010, 1, 1)));
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

    @Test
    public void testDoInitPolicyLinkCardinalitiesFromXml() {
        Element genElement = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement);

        Map<String, IntegerRange> cardinalities = gen.getPolicyLinkCardinalities();
        assertThat(cardinalities, aMapWithSize(2));
        assertThat(cardinalities.keySet(), hasItems("coverage", "rider"));
        assertThat(cardinalities.get("coverage"), is(IntegerRange.valueOf(1, 5)));
        assertThat(cardinalities.get("rider"), is(IntegerRange.valueOf(0, Integer.MAX_VALUE)));
    }

    @Test
    public void testWritePolicyLinkCardinalitiesToXml_roundTrip() {
        gen.setPolicyLinkCardinality("coverage", IntegerRange.valueOf(1, 5));
        gen.setPolicyLinkCardinality("rider", IntegerRange.valueOf(0, Integer.MAX_VALUE));

        Element xmlElement = gen.toXml(newDocument());
        TestProductCmptGeneration restoredGen = new TestProductCmptGeneration(pc);
        restoredGen.initFromXml(xmlElement);

        Map<String, IntegerRange> cardinalities = restoredGen.getPolicyLinkCardinalities();
        assertThat(cardinalities, aMapWithSize(2));
        assertThat(cardinalities.get("coverage"), is(IntegerRange.valueOf(1, 5)));
        assertThat(cardinalities.get("rider"), is(IntegerRange.valueOf(0, Integer.MAX_VALUE)));
    }

}
