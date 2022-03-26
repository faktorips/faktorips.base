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

import java.io.InputStream;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductVariantLoadXmlIntegrationTest extends XmlAbstractTestCase {

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
    public void testInitVRuleConfigs() {
        Element genElement = getTestDocumentProductCmptGeneration().getDocumentElement();
        gen.initFromXml(genElement);

        assertEquals(true, gen.isValidationRuleActivated("Regel1"));
        assertEquals(false, gen.isValidationRuleActivated("RegelZwei"));
        assertEquals(false, gen.isValidationRuleActivated("RegelDrei"));

        assertEquals(false, gen.isValidationRuleActivated("nonExistentRule"));

        Element genElement2 = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement2);

        assertEquals(true, gen.isValidationRuleActivated("Regel1"));
        assertEquals(false, gen.isValidationRuleActivated("RegelZwei"));
        assertEquals(false, gen.isValidationRuleActivated("RegelDrei"));

        assertEquals(false, gen.isValidationRuleActivated("nonExistentRule"));
    }

    @Test
    public void testIsFormulaAvailable() {
        Element genElement = getTestDocumentProductCmptGeneration().getDocumentElement();
        gen.initFromXml(genElement);

        assertTrue(gen.isFormulaAvailable("testFormula"));
        assertFalse(gen.isFormulaAvailable("emptyFormula"));
        assertFalse(gen.isFormulaAvailable("notExistingFormula"));

        Element genElement2 = getTestDocument().getDocumentElement();
        gen.initFromXml(genElement2);

        assertTrue(gen.isFormulaAvailable("testFormula"));
        assertFalse(gen.isFormulaAvailable("emptyFormula"));
        assertFalse(gen.isFormulaAvailable("notExistingFormula"));
    }

    private Document getTestDocumentProductCmptGeneration() {
        try {
            String resourceName = "ProductVariantLoadXmlIntegration2Test.xml";
            InputStream is = getClass().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Can't find resource " + resourceName);
            }
            return getDocumentBuilder().parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
