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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
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
public class PropertyValueContainerToTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IPolicyCmptType superPolicyCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;

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
    }

    @Test
    public void testEmpty() throws CoreException {
        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertEquals(0, delta.getEntries().length);
        assertTrue(delta.isEmpty());
        assertEquals(productCmpt, delta.getPropertyValueContainer());
        assertEquals(productCmpt.getFirstGeneration(),
                ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getPropertyValueContainer());
        assertEquals(productCmptType, delta.getProductCmptType());
        delta.fixAllDifferencesToModel();
    }

    public void getEntriesByType() throws CoreException {
        productCmptType.newProductCmptTypeAttribute("a1");
        productCmptType.newProductCmptTypeAttribute("a2");

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertEquals(2, delta.getEntries(DeltaType.MISSING_PROPERTY_VALUE).length);
        assertEquals(0, delta.getEntries(DeltaType.VALUE_SET_MISMATCH).length);
    }

    @Test
    public void testLinksWithMissingAssociation() throws CoreException {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.getFirstGeneration();
        IProductCmptLink link = generation.newLink(association.getName());
        assertEquals(1, generation.getNumOfLinks());

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        association.delete();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(1, entries.length);

        delta.fixAllDifferencesToModel();
        assertEquals(0, generation.getNumOfLinks());
        assertTrue(link.isDeleted());
    }

    @Test
    public void testAttributes() throws CoreException {
        IProductCmptTypeAttribute attribute1 = productCmptType.newProductCmptTypeAttribute("a1");
        attribute1.setChangingOverTime(true);
        IProductCmptTypeAttribute attribute2 = superProductCmptType.newProductCmptTypeAttribute("a_super");
        attribute2.setChangingOverTime(true);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(0, entries.length);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.getFirstGeneration();
        assertNotNull(generation.getAttributeValue("a1"));
        assertNotNull(generation.getAttributeValue("a_super"));
        assertTrue(productCmpt.getPropertyValues(IAttributeValue.class).isEmpty());

        attribute1.delete();
        attribute2.delete();
        IProductCmptTypeAttribute attribute3 = productCmptType.newProductCmptTypeAttribute("a2");
        attribute3.setChangingOverTime(true);

        delta = productCmpt.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(3, entries.length);
        assertEquals(attribute3.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[1]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[2]).getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(0, entries.length);
        assertNull(generation.getAttributeValue("a1"));
        assertNull(generation.getAttributeValue("a_super"));
        assertNotNull(generation.getAttributeValue(attribute3.getName()));
        assertTrue(productCmpt.getPropertyValues(IAttributeValue.class).isEmpty());
    }

    @Test
    public void testAttributesNotChangingOverTime() throws CoreException {
        IProductCmptTypeAttribute attribute1 = productCmptType.newProductCmptTypeAttribute("a1");
        IProductCmptTypeAttribute attribute2 = superProductCmptType.newProductCmptTypeAttribute("a_super");
        attribute1.setChangingOverTime(false);
        attribute2.setChangingOverTime(false);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        IDeltaEntry[] entries = genDelta.getEntries();
        assertEquals(0, entries.length);
        entries = delta.getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);

        entries = genDelta.getEntries();
        assertEquals(0, entries.length);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertNotNull(productCmpt.getAttributeValue("a1"));
        assertNotNull(productCmpt.getAttributeValue("a_super"));
        assertTrue(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getPropertyValues(IAttributeValue.class)
                .isEmpty());

        attribute1.delete();
        attribute2.delete();
        IProductCmptTypeAttribute attribute3 = productCmptType.newProductCmptTypeAttribute("a2");
        attribute3.setChangingOverTime(false);

        delta = productCmpt.computeDeltaToModel(ipsProject);
        genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);

        entries = genDelta.getEntries();
        assertEquals(0, entries.length);
        entries = delta.getEntries();
        assertEquals(3, entries.length);
        assertEquals(attribute3.getName(), ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[1]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[2]).getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);

        entries = genDelta.getEntries();
        assertEquals(0, entries.length);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertTrue(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getPropertyValues(IAttributeValue.class)
                .isEmpty());
        assertNull(productCmpt.getAttributeValue("a1"));
        assertNull(productCmpt.getAttributeValue("a_super"));
        assertNotNull(productCmpt.getAttributeValue(attribute3.getName()));
    }

    @Test
    public void testMissingPropertyWithPredecessor() throws Exception {
        IProductCmptTypeAttribute attr1 = productCmptType.newProductCmptTypeAttribute("attr1");
        attr1.setChangingOverTime(false);
        IPropertyValueContainerToTypeDelta deltaToModel = productCmpt.computeDeltaToModel(ipsProject);
        deltaToModel.fixAllDifferencesToModel();

        attr1.setChangingOverTime(true);
        deltaToModel = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(deltaToModel.isEmpty());
        assertEquals(1, deltaToModel.getEntries().length);
        ValueWithoutPropertyEntry valueWithoutPropertyEntry = (ValueWithoutPropertyEntry)deltaToModel.getEntries()[0];
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, valueWithoutPropertyEntry.getDeltaType());
        IPropertyValueContainerToTypeDelta generationFix = (IPropertyValueContainerToTypeDelta)deltaToModel
                .getChildren().get(0);
        IDeltaEntry[] generationEntries = generationFix.getEntries();
        assertEquals(1, generationEntries.length);
        MissingPropertyValueEntry missingPropertyValueEntry = (MissingPropertyValueEntry)generationEntries[0];
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, missingPropertyValueEntry.getDeltaType());
        assertEquals(valueWithoutPropertyEntry, missingPropertyValueEntry.getPredecessor());

        productCmpt.fixAllDifferencesToModel(ipsProject);

        attr1.setChangingOverTime(false);
        deltaToModel = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(deltaToModel.isEmpty());
        assertEquals(1, deltaToModel.getEntries().length);
        generationFix = (IPropertyValueContainerToTypeDelta)deltaToModel.getChildren().get(0);
        generationEntries = generationFix.getEntries();
        valueWithoutPropertyEntry = (ValueWithoutPropertyEntry)generationEntries[0];
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, valueWithoutPropertyEntry.getDeltaType());
        assertEquals(1, generationEntries.length);
        missingPropertyValueEntry = (MissingPropertyValueEntry)deltaToModel.getEntries()[0];
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, missingPropertyValueEntry.getDeltaType());
        assertEquals(valueWithoutPropertyEntry, missingPropertyValueEntry.getPredecessor());

    }

    @Test
    public void testTypeMismatch() throws CoreException {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("premium");
        attribute.setChangingOverTime(true);
        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        assertNotNull(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getAttributeValue("premium"));

        attribute.delete();
        productCmptType.newFormulaSignature("premium");
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta generationDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren()
                .get(0);
        assertEquals(1, generationDelta.getEntries().length);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)generationDelta.getEntries()[0];
        assertEquals("premium", entry.getPropertyName());
        assertEquals(DeltaType.PROPERTY_TYPE_MISMATCH, entry.getDeltaType());
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, entry.getPropertyType());

        delta.fixAllDifferencesToModel();
        assertNull(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getAttributeValue("premium"));
        assertNotNull(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getFormula("premium"));
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

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        assertNotNull(((IProductCmptGeneration)productCmpt.getFirstGeneration()).getConfigElement("a1"));
        range = (IRangeValueSet)((IProductCmptGeneration)productCmpt.getFirstGeneration()).getConfigElement("a1")
                .getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("10", range.getUpperBound());

        // now the range in the config element is not a subset of the range in the attribute
        // but this is not a value set type mismatch!!!
        range.setUpperBound("20");
        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        attr.setValueSetType(ValueSetType.ENUM);
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        assertEquals(1, genDelta.getEntries().length);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)genDelta.getEntries()[0];
        assertEquals(DeltaType.VALUE_SET_MISMATCH, entry.getDeltaType());
        assertEquals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, entry.getPropertyType());
        delta.fixAllDifferencesToModel();
        IValueSet valueSet = ((IProductCmptGeneration)productCmpt.getFirstGeneration()).getConfigElement("a1")
                .getValueSet();
        assertTrue(valueSet.isEnum());

        // now the enum in the config element is not a subset of the enum in the attribute
        // but this is not a value set type mismatch!!!
        IEnumValueSet enumSet = (IEnumValueSet)valueSet;
        enumSet.addValue("4711");
        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        // If the value set in the attribute is unrestricted, config element is always ok.
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());
    }

    @Test
    public void testVRuleMismatch() throws CoreException {
        initRules();

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = genDelta.getEntries();
        assertEquals(1, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());

        List<IValidationRuleConfig> validationRuleConfigs = ((IProductCmptGeneration)productCmpt.getFirstGeneration())
                .getValidationRuleConfigs();
        assertEquals(1, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());

        delta.fixAllDifferencesToModel();
        validationRuleConfigs = ((IProductCmptGeneration)productCmpt.getFirstGeneration()).getValidationRuleConfigs();
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
        ((IProductCmptGeneration)productCmpt.getFirstGeneration()).newValidationRuleConfig(unconfigurableRule);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = genDelta.getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());

        List<IValidationRuleConfig> validationRuleConfigs = ((IProductCmptGeneration)productCmpt.getFirstGeneration())
                .getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("unconfigurableRule", validationRuleConfigs.get(1).getName());

        delta.fixAllDifferencesToModel();
        validationRuleConfigs = ((IProductCmptGeneration)productCmpt.getFirstGeneration()).getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("UnconfiguredRule", validationRuleConfigs.get(1).getName());
    }

    protected void initRules() {
        IValidationRule rule = policyCmptType.newRule();
        rule.setName("Rule1");
        rule.setConfigurableByProductComponent(true);
        ((IProductCmptGeneration)productCmpt.getFirstGeneration()).newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("UnconfiguredRule");
        rule.setConfigurableByProductComponent(true);
    }

}
