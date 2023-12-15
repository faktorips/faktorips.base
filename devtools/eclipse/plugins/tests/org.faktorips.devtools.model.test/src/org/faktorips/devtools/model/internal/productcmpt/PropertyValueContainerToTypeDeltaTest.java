/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.MissingTemplateLinkEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Jan Ortmann
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
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

    @Mock(answer = Answers.CALLS_REAL_METHODS)
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
    public void testEmpty() {
        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertEquals(0, delta.getEntries().length);
        assertTrue(delta.isEmpty());
        assertEquals(productCmpt, delta.getPropertyValueContainer());
        assertEquals(productCmpt.getFirstGeneration(),
                ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getPropertyValueContainer());
        assertEquals(productCmptType, delta.getProductCmptType());
        delta.fixAllDifferencesToModel();
    }

    public void getEntriesByType() {
        productCmptType.newProductCmptTypeAttribute("a1");
        productCmptType.newProductCmptTypeAttribute("a2");

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertEquals(2, delta.getEntries(DeltaType.MISSING_PROPERTY_VALUE).length);
        assertEquals(0, delta.getEntries(DeltaType.VALUE_SET_MISMATCH).length);
    }

    @Test
    public void testLinksWithMissingAssociation() {
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
    public void testMissingTemplateLink_EntryForDefinedLinks() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);

        IProductCmpt template = newProductTemplate(productCmptType, "template");
        IProductCmptLink templateLink = template.newLink(association);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        productCmpt.setTemplate(template.getQualifiedName());

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(delta.isEmpty());

        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(1, entries.length);
        assertTrue(entries[0] instanceof MissingTemplateLinkEntry);
    }

    @Test
    public void testMissingTemplateLink_NoEntryForUndefinedLinks() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);

        IProductCmpt template = newProductTemplate(productCmptType, "template");
        IProductCmptLink templateLink = template.newLink(association);
        templateLink.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        productCmpt.setTemplate(template.getQualifiedName());

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());
    }

    @Test
    public void testRemovedTemplateLink_OnlyLinkWithoutAssociationEntryIfRemovedInModel() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);

        IProductCmpt template = newProductTemplate(productCmptType, "template");
        IProductCmptLink templateLink = template.newLink(association);
        templateLink.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        productCmpt.setTemplate(template.getQualifiedName());
        IProductCmptLink productLink = productCmpt.newLink(association);
        productLink.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        association.delete();
        productCmptType.getIpsSrcFile().save(null);
        templateLink.delete();
        template.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);
        assertFalse(delta.isEmpty());

        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(1, entries.length);
        assertTrue(entries[0] instanceof LinkWithoutAssociationEntry);
    }

    @Test
    public void testWrongRuntimeIdForLink() {
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        IProductCmptGeneration generation = productCmpt.getFirstGeneration();
        IProductCmptLink link = generation.newLink(association.getName());
        link.setTarget(productCmpt.getQualifiedName());

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        assertTrue(delta.isEmpty());

        productCmpt.setRuntimeId("changedID");
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntry[] entries = delta.getEntries();
        assertEquals(0, entries.length);
        entries = ((IPropertyValueContainerToTypeDelta)delta.getChildren().get(0)).getEntries();
        assertEquals(1, entries.length);

        delta.fixAllDifferencesToModel();
        assertEquals("changedID", link.getTargetRuntimeId());
    }

    @Test
    public void testAttributes() {
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
        assertEquals(PropertyValueType.ATTRIBUTE_VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());
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
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[2]).getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(PropertyValueType.ATTRIBUTE_VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());

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
    public void testAttributesNotChangingOverTime() {
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
        assertEquals(PropertyValueType.ATTRIBUTE_VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());
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
        assertEquals("a1", ((IDeltaEntryForProperty)entries[1]).getPropertyName());
        assertEquals("a_super", ((IDeltaEntryForProperty)entries[2]).getPropertyName());
        assertEquals(DeltaType.MISSING_PROPERTY_VALUE, entries[0].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[1].getDeltaType());
        assertEquals(DeltaType.VALUE_WITHOUT_PROPERTY, entries[2].getDeltaType());
        assertEquals(PropertyValueType.ATTRIBUTE_VALUE, ((IDeltaEntryForProperty)entries[0]).getPropertyType());

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
    public void testTypeMismatch() {
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
        assertEquals(PropertyValueType.ATTRIBUTE_VALUE, entry.getPropertyType());

        delta.fixAllDifferencesToModel();
        assertNull(productCmpt.getFirstGeneration().getAttributeValue("premium"));
        assertNotNull(productCmpt.getFirstGeneration().getFormula("premium"));
    }

    @Test
    public void testValueSetTypeMismatch() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setValueSetConfiguredByProduct(true);
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attr.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("10");

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IConfiguredDefault configDefault = productCmpt.getFirstGeneration().getPropertyValue("a1",
                IConfiguredDefault.class);
        assertNotNull(configDefault);
        IConfiguredValueSet configValueSet = productCmpt.getFirstGeneration().getPropertyValue("a1",
                IConfiguredValueSet.class);
        range = (IRangeValueSet)configValueSet.getValueSet();
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
        assertEquals(PropertyValueType.CONFIGURED_VALUESET, entry.getPropertyType());
        delta.fixAllDifferencesToModel();
        IValueSet valueSet = configValueSet.getValueSet();
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
    public void testValueSetTypeMismatch_UndefinedConfigElement() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setValueSetConfiguredByProduct(true);
        attr.setName("a1");
        ProductCmpt template = newProductTemplate(productCmptType, "testTemplate");
        template.fixAllDifferencesToModel(ipsProject);

        productCmpt.fixAllDifferencesToModel(ipsProject);
        productCmpt.setTemplate("testTemplate");
        IConfiguredValueSet configValueSet = productCmpt.getFirstGeneration().getPropertyValue("a1",
                IConfiguredValueSet.class);
        attr.setValueSetType(ValueSetType.RANGE);
        configValueSet.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        IPropertyValueContainerToTypeDelta delta = productCmpt.computeDeltaToModel(ipsProject);
        IPropertyValueContainerToTypeDelta genDelta = (IPropertyValueContainerToTypeDelta)delta.getChildren().get(0);
        assertEquals(0, genDelta.getEntries().length);
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
    public void testValueMismatch_integrated() {
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
    public void testVRuleMismatch() {
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
    public void testVRuleMismatchNotConfigurable() {
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
        when(attribute.isVisible()).thenReturn(true);

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

    @Test
    public void testValueSetTemplateMismatch_ConfiguredValueSet() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setValueSetConfiguredByProduct(true);
        attr.setChangingOverTime(false);
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IConfiguredValueSet templateValueSet = productTemplate.getPropertyValue("a1", IConfiguredValueSet.class);
        templateValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet templateRange = (IRangeValueSet)templateValueSet.getValueSet();
        templateRange.setLowerBound("1");
        templateRange.setUpperBound("10");
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        ConfiguredValueSet valueSet = (ConfiguredValueSet)productCmpt.getPropertyValue("a1", IConfiguredValueSet.class);
        valueSet.setValueSetType(ValueSetType.RANGE);
        valueSet.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        IRangeValueSet range = (IRangeValueSet)valueSet.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("10");
        valueSet.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateRange.setUpperBound("5");
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(((IRangeValueSet)valueSet.getValueSetInternal()).getUpperBound(), is("10"));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.CONFIGURED_VALUESET));

        delta.fixAllDifferencesToModel();

        assertThat(((IRangeValueSet)valueSet.getValueSetInternal()).getUpperBound(), is("5"));
    }

    @Test
    public void testValueSetTemplateMismatch_ConfiguredDefault() {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setValueSetConfiguredByProduct(true);
        attr.setChangingOverTime(false);
        attr.setName("a1");
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IConfiguredDefault templateConfiguredDefault = productTemplate.getPropertyValue("a1", IConfiguredDefault.class);
        templateConfiguredDefault.setValue("1");
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        ConfiguredDefault configuredDefault = (ConfiguredDefault)productCmpt.getPropertyValue("a1",
                IConfiguredDefault.class);
        configuredDefault.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        configuredDefault.setValue("1");
        configuredDefault.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateConfiguredDefault.setValue("2");
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(configuredDefault.getValueInternal(), is("1"));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.CONFIGURED_DEFAULT));

        delta.fixAllDifferencesToModel();

        assertThat(configuredDefault.getValueInternal(), is("2"));
    }

    @Test
    public void testValueSetTemplateMismatch_AttributeValue() {
        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute("a1");
        attr.setChangingOverTime(false);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IAttributeValue templateAttributeValue = productTemplate.getPropertyValue("a1", IAttributeValue.class);
        SingleValueHolder templateValueHolder = new SingleValueHolder(templateAttributeValue, "10");
        templateAttributeValue.setValueHolder(templateValueHolder);
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        AttributeValue attributeValue = (AttributeValue)productCmpt.getPropertyValue("a1", IAttributeValue.class);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "10");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateValueHolder.setValue(ValueFactory.createStringValue("5"));
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(attributeValue.getValueHolderInternal().getStringValue(), is("10"));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.ATTRIBUTE_VALUE));

        delta.fixAllDifferencesToModel();

        assertThat(attributeValue.getValueHolderInternal().getStringValue(), is("5"));
    }

    @Test
    public void testValueSetTemplateMismatch_TableContentUsage() {
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setChangingOverTime(false);
        structureUsage.setRoleName("s1");
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        ITableContentUsage templateUsage = productTemplate.getPropertyValue("s1", ITableContentUsage.class);
        templateUsage.setTableContentName("c1");
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        TableContentUsage usage = (TableContentUsage)productCmpt.getPropertyValue("s1", ITableContentUsage.class);
        usage.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        usage.setTableContentName("c1");
        usage.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateUsage.setTableContentName("c2");
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(usage.getInternalTableContentName(), is("c1"));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.TABLE_CONTENT_USAGE));

        delta.fixAllDifferencesToModel();

        assertThat(usage.getInternalTableContentName(), is("c2"));
    }

    @Test
    public void testValueSetTemplateMismatch_ValidationRuleConfig() {
        IValidationRule rule = policyCmptType.newRule();
        rule.setConfigurableByProductComponent(true);
        rule.setChangingOverTime(false);
        rule.setName("r1");
        policyCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IValidationRuleConfig templateRuleConfig = productTemplate.getPropertyValue("r1", IValidationRuleConfig.class);
        templateRuleConfig.setActive(false);
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        ValidationRuleConfig ruleConfig = (ValidationRuleConfig)productCmpt.getPropertyValue("r1",
                IValidationRuleConfig.class);
        ruleConfig.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        ruleConfig.setActive(false);
        ruleConfig.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateRuleConfig.setActive(true);
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(ruleConfig.isActiveInternal(), is(false));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.VALIDATION_RULE_CONFIG));

        delta.fixAllDifferencesToModel();

        assertThat(ruleConfig.isActiveInternal(), is(true));
    }

    @Test
    public void testValueSetTemplateMismatch_Formula() {
        IProductCmptTypeMethod formulaSignature = productCmptType.newFormulaSignature("s1");
        formulaSignature.setChangingOverTime(false);
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IFormula templateFormula = productTemplate.getPropertyValue("s1", IFormula.class);
        templateFormula.setExpression("foo");
        productTemplate.getIpsSrcFile().save(null);

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        Formula formula = (Formula)productCmpt.getPropertyValue("s1", IFormula.class);
        formula.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        formula.setExpression("foo");
        formula.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);
        templateFormula.setExpression("bar");
        productTemplate.getIpsSrcFile().save(null);

        delta = productCmpt.computeDeltaToModel(ipsProject);

        assertThat(formula.getExpressionInternal(), is("foo"));
        assertFalse(delta.isEmpty());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        IDeltaEntryForProperty entry = (IDeltaEntryForProperty)delta.getEntries()[0];
        assertThat(entry.getDeltaType(), is(DeltaType.INHERITED_TEMPLATE_MISMATCH));
        assertThat(entry.getPropertyType(), is(PropertyValueType.FORMULA));

        delta.fixAllDifferencesToModel();

        assertThat(formula.getExpressionInternal(), is("bar"));
    }
}
