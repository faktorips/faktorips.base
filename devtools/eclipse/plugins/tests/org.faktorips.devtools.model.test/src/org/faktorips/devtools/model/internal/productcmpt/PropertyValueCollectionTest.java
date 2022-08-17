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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValueCollectionTest extends AbstractIpsPluginTest {

    private PropertyValueCollection valueContainer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private ProductCmpt parent;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(parent.getIpsObject()).thenReturn(ipsObject);
        when(parent.getIpsProject()).thenReturn(ipsProject);
        valueContainer = new PropertyValueCollection(parent);

        AttributeValue part1 = new AttributeValue(parent, "ID1");
        part1.setAttribute("AV1");
        AttributeValue part2 = new AttributeValue(parent, "ID2");
        part2.setAttribute("AV2");
        AttributeValue part3 = new AttributeValue(parent, "ID3");
        part3.setAttribute("AV3");
        valueContainer.addPropertyValue(part1);
        valueContainer.addPropertyValue(part2);
        valueContainer.addPropertyValue(part3);
    }

    protected void assertAttributesSize(int size) {
        assertEquals(size, valueContainer.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass())
                .size());
    }

    protected void assertSize(int size) {
        assertEquals(size, valueContainer.getAllPropertyValues().size());
    }

    @Test
    public void testAddPart() {
        assertAttributesSize(3);

        valueContainer.addPropertyValue(new AttributeValue(parent, "ID4"));
        assertAttributesSize(4);

        valueContainer.addPropertyValue(new AttributeValue(parent, "ID4"));
        assertAttributesSize(4);
    }

    @Test
    public void testRemovePart() {
        assertAttributesSize(3);

        valueContainer.removePropertyValue(new AttributeValue(parent, "ID1"));
        assertAttributesSize(2);

        valueContainer.removePropertyValue(new AttributeValue(parent, "ID1"));
        assertAttributesSize(2);
    }

    @Test
    public void testGetPropertyValues() {
        assertAttributesSize(3);
        AttributeValue part4 = new AttributeValue(parent, "ID4");
        part4.setAttribute("AV4");
        valueContainer.addPropertyValue(part4);
        assertEquals(4, valueContainer.getAllPropertyValues().size());
        assertEquals(4, valueContainer.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        assertEquals(0, valueContainer.getPropertyValues(PropertyValueType.VALIDATION_RULE_CONFIG.getInterfaceClass())
                .size());
        assertEquals(0, valueContainer.getPropertyValues(PropertyValueType.TABLE_CONTENT_USAGE.getInterfaceClass())
                .size());

        ValidationRuleConfig config = new ValidationRuleConfig(parent, "ID4", "Rule");
        valueContainer.addPropertyValue(config);
        assertEquals(5, valueContainer.getAllPropertyValues().size());
        assertEquals(4, valueContainer.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE.getInterfaceClass()).size());
        assertEquals(1, valueContainer.getPropertyValues(PropertyValueType.VALIDATION_RULE_CONFIG.getInterfaceClass())
                .size());
        assertEquals(0, valueContainer.getPropertyValues(PropertyValueType.TABLE_CONTENT_USAGE.getInterfaceClass())
                .size());
    }

    @Test
    public void testGetPropertyValue() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        when(property.getPropertyName()).thenReturn("AV1");
        when(property.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);

        IPropertyValue value = valueContainer.getPropertyValue(property, IAttributeValue.class);
        assertNotNull(value);
        IPropertyValue value2 = valueContainer.getPropertyValue("AV1", IAttributeValue.class);
        assertSame("Parts", value, value2);

        ProductCmptTypeAttribute illegalTypeAttr = mock(ProductCmptTypeAttribute.class);
        when(illegalTypeAttr.getPropertyName()).thenReturn("AVIllegal");
        when(illegalTypeAttr.getProductCmptPropertyType()).thenReturn(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
        value = valueContainer.getPropertyValue(illegalTypeAttr, IAttributeValue.class);
        assertNull(value);
    }

    @Test
    public void testNewPropertyValueAttribute() {
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        when(attribute.getPropertyName()).thenReturn("AV5");
        when(attribute.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);

        IAttributeValue value = valueContainer.newPropertyValue(attribute, "ID5", IAttributeValue.class);
        assertNotNull(value);
        value.setAttribute("AV5");
        assertAttributesSize(4);

        IPropertyValue value2 = valueContainer.getPropertyValue("AV5", IAttributeValue.class);
        assertSame("Parts", value, value2);
    }

    @Test
    public void testNewPropertyValueFormula() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        when(property.getPropertyName()).thenReturn("Method1");
        when(property.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION);

        assertSize(3);
        IFormula formula = valueContainer.newPropertyValue(property, "MethodID1", IFormula.class);
        assertNotNull(formula);
        formula.setFormulaSignature("Method1");
        assertSize(4);

        IPropertyValue value2 = valueContainer.getPropertyValue("Method1", IFormula.class);
        assertSame("Parts", formula, value2);
    }

    @Test
    public void testNewPropertyValueThisXML() {
        IFormula formula = (IFormula)valueContainer.newPropertyValue(Formula.TAG_NAME, "MethodID1");
        assertNotNull(formula);

        IProductCmptLink link = (IProductCmptLink)valueContainer.newPropertyValue(ProductCmptLink.TAG_NAME, "LinkID1");
        // Link must be null as valueContainer cannot create parts that are not IPropertyValues
        assertNull(link);

        IIpsObjectPart part = valueContainer.newPropertyValue("TestIllegalXMLTag", "valueID1");
        assertNull(part);
    }

    @Test
    public void testAddPartThis() {
        AttributeValue attr = new AttributeValue(parent, "ID1");
        attr.setAttribute("AV1");
        assertTrue(valueContainer.addPropertyValue(attr));
    }

    @Test
    public void testRemovePartThis() {
        AttributeValue attr = new AttributeValue(parent, "ID1");
        attr.setAttribute("AV1");
        assertTrue(valueContainer.removePropertyValue(attr));
        assertFalse(valueContainer.removePropertyValue(attr));
    }

    @Test
    public void testGetAllPropertyValues_Sorted() throws Exception {
        // create in pseudo random order. The suffix 1 must be before suffix 2!
        valueContainer.clear();
        IAttributeValue attributeValue1 = valueContainer.newPropertyValue(nextId(), IAttributeValue.class);
        attributeValue1.setAttribute("a1");
        IConfiguredDefault configuredDefault1 = valueContainer.newPropertyValue(nextId(), IConfiguredDefault.class);
        configuredDefault1.setPolicyCmptTypeAttribute("pc1");
        IAttributeValue attributeValue2 = valueContainer.newPropertyValue(nextId(), IAttributeValue.class);
        attributeValue2.setAttribute("a2");
        IFormula formula1 = valueContainer.newPropertyValue(nextId(), IFormula.class);
        formula1.setFormulaSignature("f1");
        ITableContentUsage tableContentUsage1 = valueContainer.newPropertyValue(nextId(), ITableContentUsage.class);
        tableContentUsage1.setStructureUsage("t1");
        IConfiguredValueSet configuredValueSet1 = valueContainer.newPropertyValue(nextId(), IConfiguredValueSet.class);
        configuredValueSet1.setPolicyCmptTypeAttribute("pc1");
        IValidationRuleConfig validationRuleConfig1 = valueContainer.newPropertyValue(nextId(),
                IValidationRuleConfig.class);
        validationRuleConfig1.setValidationRuleName("v1");
        IFormula formula2 = valueContainer.newPropertyValue(nextId(), IFormula.class);
        formula2.setFormulaSignature("f2");
        IConfiguredValueSet configuredValueSet2 = valueContainer.newPropertyValue(nextId(), IConfiguredValueSet.class);
        configuredValueSet2.setPolicyCmptTypeAttribute("pc2");
        IConfiguredDefault configuredDefault2 = valueContainer.newPropertyValue(nextId(), IConfiguredDefault.class);
        configuredDefault2.setPolicyCmptTypeAttribute("pc2");
        ITableContentUsage tableContentUsage2 = valueContainer.newPropertyValue(nextId(), ITableContentUsage.class);
        tableContentUsage2.setStructureUsage("t2");
        IValidationRuleConfig validationRuleConfig2 = valueContainer.newPropertyValue(nextId(),
                IValidationRuleConfig.class);
        validationRuleConfig2.setValidationRuleName("v2");

        List<IPropertyValue> allPropertyValues = valueContainer.getAllPropertyValues();

        assertEquals(Arrays.asList(attributeValue1, attributeValue2, configuredValueSet1, configuredDefault1,
                configuredValueSet2, configuredDefault2, formula1, formula2, tableContentUsage1, tableContentUsage2,
                validationRuleConfig1, validationRuleConfig2), allPropertyValues);
    }

    protected String nextId() {
        return UUID.randomUUID().toString();
    }

}
