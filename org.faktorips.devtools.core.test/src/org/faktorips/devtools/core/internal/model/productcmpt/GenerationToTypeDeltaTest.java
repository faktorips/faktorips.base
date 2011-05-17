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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class GenerationToTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IPolicyCmptType superPolicyCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        superProductCmptType = superPolicyCmptType.findProductCmptType(ipsProject);
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
    }

    @Test
    public void testEmpty() throws CoreException {
        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        assertEquals(0, delta.getEntries().length);
        assertEquals(true, delta.isEmpty());
        assertEquals(generation, delta.getProductCmptGeneration());
        assertEquals(productCmptType, delta.getProductCmptType());
        delta.fix();
    }

    public void getEntriesByType() throws CoreException {
        productCmptType.newProductCmptTypeAttribute("a1");
        productCmptType.newProductCmptTypeAttribute("a2");

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        assertEquals(2, delta.getEntries(DeltaType.MISSING_PROPERTY_VALUE).length);
        assertEquals(0, delta.getEntries(DeltaType.VALUE_SET_MISMATCH).length);
    }

    @Test
    public void testLinksWithMissingAssociation() throws CoreException {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        IProductCmptLink link = generation.newLink(association.getName());
        assertEquals(1, generation.getNumOfLinks());

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        association.delete();
        delta = generation.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(1, entries.length);

        delta.fix();
        assertEquals(0, generation.getNumOfLinks());
        assertTrue(link.isDeleted());
    }

    @Test
    public void testAttributes() throws CoreException {
        IProductCmptTypeAttribute attribute1 = productCmptType.newProductCmptTypeAttribute("a1");
        IProductCmptTypeAttribute attribute2 = superProductCmptType.newProductCmptTypeAttribute("a_super");

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(ProductCmptPropertyType.VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());

        delta.fix();
        delta = generation.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertNotNull(generation.getAttributeValue("a1"));
        assertNotNull(generation.getAttributeValue("a_super"));

        attribute1.delete();
        attribute2.delete();
        productCmptType.newProductCmptTypeAttribute("a2");

        delta = generation.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(3, entries.length);
        assertEquals("a2", ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[1]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[2]).getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(ProductCmptPropertyType.VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());

        delta.fix();
        delta = generation.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertNull(generation.getAttributeValue("a1"));
        assertNull(generation.getAttributeValue("a_super"));
        assertNotNull(generation.getAttributeValue("a2"));
    }

    @Test
    public void testTypeMismatch() throws CoreException {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("premium");
        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        delta.fix();
        assertNotNull(generation.getAttributeValue("premium"));

        attribute.delete();
        productCmptType.newFormulaSignature("premium");
        delta = generation.computeDeltaToModel(ipsProject);
        assertEquals(1, delta.getEntries().length);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertEquals("premium", entry.getPropertyName());
        assertEquals(DeltaType.PROPERTY_TYPE_MISMATCH, entry.getDeltaType());
        assertEquals(ProductCmptPropertyType.VALUE, entry.getPropertyType());

        delta.fix();
        assertNull(generation.getAttributeValue("premium"));
        assertNotNull(generation.getFormula("premium"));
    }

    @Test
    public void testValueSetTypeMismatch() throws CoreException {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setProductRelevant(true);
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attr.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("10");

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        delta.fix();
        assertNotNull(generation.getConfigElement("a1"));
        range = (IRangeValueSet)generation.getConfigElement("a1").getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("10", range.getUpperBound());

        // now the range in the config element is not a subset of the range in the attribute
        // but this is not a value set type mismatch!!!
        range.setUpperBound("20");
        delta = generation.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        attr.setValueSetType(ValueSetType.ENUM);
        delta = generation.computeDeltaToModel(ipsProject);
        assertEquals(1, delta.getEntries().length);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertEquals(DeltaType.VALUE_SET_MISMATCH, entry.getDeltaType());
        assertEquals(ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET, entry.getPropertyType());
        delta.fix();
        IValueSet valueSet = generation.getConfigElement("a1").getValueSet();
        assertTrue(valueSet.isEnum());

        // now the enum in the config element is not a subset of the enum in the attribute
        // but this is not a value set type mismatch!!!
        IEnumValueSet enumSet = (IEnumValueSet)valueSet;
        enumSet.addValue("4711");
        delta = generation.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        // If the value set in the attribute is unrestricted, config element is always ok.
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        delta = generation.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());
    }

    @Test
    public void testVRuleMismatch() throws CoreException {
        initRules();

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());

        List<IValidationRuleConfig> validationRuleConfigs = generation.getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("", validationRuleConfigs.get(1).getName());

        delta.fix();
        validationRuleConfigs = generation.getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("UnconfiguredRule", validationRuleConfigs.get(1).getName());
    }

    @Test
    public void testVRuleMismatchNotConfigurable() throws CoreException {
        initRules();
        IValidationRule unconfigurableRule = policyCmptType.newRule();
        unconfigurableRule.setName("unconfigurableRule");
        unconfigurableRule.setConfigurableByProductComponent(false);
        generation.newValidationRuleConfig(unconfigurableRule);

        IGenerationToTypeDelta delta = generation.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(3, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());

        List<IValidationRuleConfig> validationRuleConfigs = generation.getValidationRuleConfigs();
        assertEquals(3, validationRuleConfigs.size());
        assertEquals("unconfigurableRule", validationRuleConfigs.get(2).getName());

        delta.fix();
        validationRuleConfigs = generation.getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("UnconfiguredRule", validationRuleConfigs.get(1).getName());
    }

    protected void initRules() {
        IValidationRule rule = policyCmptType.newRule();
        rule.setName("Rule1");
        rule.setConfigurableByProductComponent(true);
        generation.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("UnconfiguredRule");
        rule.setConfigurableByProductComponent(true);
        generation.newValidationRuleConfig();
    }
}
