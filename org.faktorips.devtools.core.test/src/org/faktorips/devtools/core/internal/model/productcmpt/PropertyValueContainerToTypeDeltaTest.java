/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author Jan Ortmann
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyValueContainerToTypeDeltaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IPolicyCmptType superPolicyCmptType;
    private IProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;
    private PropertyValueContainerToTypeDelta propertyValueContainerToTypeDelta;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue value;

    @Mock
    private SingleValueHolder singleValueHolder;

    @Mock
    private MultiValueHolder multiValueHolder;

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

        propertyValueContainerToTypeDelta = mock(PropertyValueContainerToTypeDelta.class, Mockito.CALLS_REAL_METHODS);
        doNothing().when(propertyValueContainerToTypeDelta).addEntry(any(IDeltaEntry.class));
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
        IProductCmptGeneration generation = productCmpt.getFirstGeneration();
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
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                ((IDeltaEntryForProperty)entries[0]).getPropertyType());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[0]).getPropertyName());
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(0, entries.length);
        IProductCmptGeneration generation = productCmpt.getFirstGeneration();
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
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                ((IDeltaEntryForProperty)entries[0]).getPropertyType());

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
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                ((IDeltaEntryForProperty)entries[0]).getPropertyType());
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
        assertTrue(productCmpt.getFirstGeneration().getPropertyValues(IAttributeValue.class).isEmpty());

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
        assertEquals(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE,
                ((IDeltaEntryForProperty)entries[0]).getPropertyType());

        delta.fixAllDifferencesToModel();
        delta = productCmpt.computeDeltaToModel(ipsProject);
        genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);

        entries = genDelta.getEntries();
        assertEquals(0, entries.length);
        entries = delta.getEntries();
        assertEquals(0, entries.length);
        assertTrue(productCmpt.getFirstGeneration().getPropertyValues(IAttributeValue.class).isEmpty());
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
        assertNotNull(productCmpt.getFirstGeneration().getAttributeValue("premium"));

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
        assertNull(productCmpt.getFirstGeneration().getAttributeValue("premium"));
        assertNotNull(productCmpt.getFirstGeneration().getFormula("premium"));
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
        assertNotNull(productCmpt.getFirstGeneration().getConfigElement("a1"));
        range = (IRangeValueSet)productCmpt.getFirstGeneration().getConfigElement("a1").getValueSet();
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
        IValueSet valueSet = productCmpt.getFirstGeneration().getConfigElement("a1").getValueSet();
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
    public void testValueMismatch_singleValue_multiAttribute() throws Exception {
        ArgumentCaptor<IDeltaEntry> captor = ArgumentCaptor.forClass(IDeltaEntry.class);

        when(attribute.isMultiValueAttribute()).thenReturn(true);
        doReturn(singleValueHolder).when(value).getValueHolder();

        propertyValueContainerToTypeDelta.checkForValueMismatch(attribute, value);
        verify(propertyValueContainerToTypeDelta).addEntry(captor.capture());
        assertEquals(DeltaType.VALUE_HOLDER_MISMATCH, captor.getValue().getDeltaType());
    }

    @Test
    public void testValueMismatch_multiValue_singeAttribute() throws Exception {
        ArgumentCaptor<IDeltaEntry> captor = ArgumentCaptor.forClass(IDeltaEntry.class);

        when(attribute.isMultiValueAttribute()).thenReturn(false);
        doReturn(multiValueHolder).when(value).getValueHolder();

        propertyValueContainerToTypeDelta.checkForValueMismatch(attribute, value);
        verify(propertyValueContainerToTypeDelta).addEntry(captor.capture());
        assertEquals(DeltaType.VALUE_HOLDER_MISMATCH, captor.getValue().getDeltaType());
    }

    @Test
    public void testValueMismatch_singleValue_singeAttribute() throws Exception {

        when(attribute.isMultiValueAttribute()).thenReturn(false);
        doReturn(singleValueHolder).when(value).getValueHolder();

        propertyValueContainerToTypeDelta.checkForValueMismatch(attribute, value);
        verify(propertyValueContainerToTypeDelta, times(0)).addEntry(any(IDeltaEntry.class));
    }

    @Test
    public void testValueMismatch_multiValue_multiAttribute() throws Exception {

        when(attribute.isMultiValueAttribute()).thenReturn(true);
        doReturn(multiValueHolder).when(value).getValueHolder();

        propertyValueContainerToTypeDelta.checkForValueMismatch(attribute, value);
        verify(propertyValueContainerToTypeDelta, times(0)).addEntry(any(IDeltaEntry.class));
    }

    @Test
    public void testValueMismatch_integrated() throws CoreException {
        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute();
        attr.setName("a1");
        attr.setMultiValueAttribute(true);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(delta.isEmpty());
        delta.fixAllDifferencesToModel();
        IAttributeValue attributeValue = productCmpt.getFirstGeneration().getAttributeValue("a1");
        assertTrue(attributeValue.getValueHolder() instanceof MultiValueHolder);

        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        attr.setMultiValueAttribute(false);
        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(delta.isEmpty());
        delta.fixAllDifferencesToModel();
        assertTrue(attributeValue.getValueHolder() instanceof SingleValueHolder);

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

        List<IValidationRuleConfig> validationRuleConfigs = productCmpt.getFirstGeneration().getValidationRuleConfigs();
        assertEquals(1, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());

        delta.fixAllDifferencesToModel();
        validationRuleConfigs = productCmpt.getFirstGeneration().getValidationRuleConfigs();
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
        productCmpt.getFirstGeneration().newValidationRuleConfig(unconfigurableRule);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = genDelta.getEntries();
        assertEquals(2, entries.length);
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());

        List<IValidationRuleConfig> validationRuleConfigs = productCmpt.getFirstGeneration().getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("unconfigurableRule", validationRuleConfigs.get(1).getName());

        delta.fixAllDifferencesToModel();
        validationRuleConfigs = productCmpt.getFirstGeneration().getValidationRuleConfigs();
        assertEquals(2, validationRuleConfigs.size());
        assertEquals("Rule1", validationRuleConfigs.get(0).getName());
        assertEquals("UnconfiguredRule", validationRuleConfigs.get(1).getName());
    }

    protected void initRules() {
        IValidationRule rule = policyCmptType.newRule();
        rule.setName("Rule1");
        rule.setConfigurableByProductComponent(true);
        productCmpt.getFirstGeneration().newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("UnconfiguredRule");
        rule.setConfigurableByProductComponent(true);
    }

    @Test
    public void testCheckForHiddenAttributeMismatch_noEntryAdded() {
        doReturn(new SingleValueHolder(value)).when(value).getValueHolder();
        when(attribute.isVisible()).thenReturn(true);
        when(attribute.getDefaultValue()).thenReturn("newDefaultValue");

        propertyValueContainerToTypeDelta.checkForHiddenAttributeMismatch(attribute, value);

        verify(propertyValueContainerToTypeDelta, never()).addEntry(any(IDeltaEntry.class));
    }

    @Test
    public void testCheckForHiddenAttributeMismatch_entryAdded() {
        doReturn(new SingleValueHolder(value)).when(value).getValueHolder();
        when(attribute.isVisible()).thenReturn(false);
        when(attribute.getDefaultValue()).thenReturn("newDefaultValue");

        propertyValueContainerToTypeDelta.checkForHiddenAttributeMismatch(attribute, value);

        verify(propertyValueContainerToTypeDelta, times(1)).addEntry(any(IDeltaEntry.class));
    }

    @Test
    public void testCheckForHiddenAttributeMismatch_noDifference() {
        doReturn(new SingleValueHolder(value, "newDefaultValue")).when(value).getValueHolder();
        when(attribute.isVisible()).thenReturn(false);
        when(attribute.getDefaultValue()).thenReturn("newDefaultValue");

        propertyValueContainerToTypeDelta.checkForHiddenAttributeMismatch(attribute, value);

        verify(propertyValueContainerToTypeDelta, never()).addEntry(any(IDeltaEntry.class));
    }

}
