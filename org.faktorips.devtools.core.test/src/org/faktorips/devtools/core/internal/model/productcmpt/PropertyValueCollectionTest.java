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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
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
        valueContainer = new PropertyValueCollection();

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
        assertAttributesSize(5);
    }

    @Test
    public void testRemovePart() {
        valueContainer.addPropertyValue(new AttributeValue(parent, "ID1"));
        assertAttributesSize(4);

        valueContainer.removePropertyValue(new AttributeValue(parent, "ID1"));
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

        IPropertyValue value = valueContainer.getPropertyValue(property);
        assertNotNull(value);
        IPropertyValue value2 = valueContainer.getPropertyValue("AV1");
        assertSame("Parts", value, value2);

        ProductCmptTypeAttribute illegalTypeAttr = mock(ProductCmptTypeAttribute.class);
        when(illegalTypeAttr.getPropertyName()).thenReturn("AVIllegal");
        when(illegalTypeAttr.getProductCmptPropertyType()).thenReturn(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
        value = valueContainer.getPropertyValue(illegalTypeAttr);
        assertNull(value);
    }

    @Test
    public void testNewPropertyValueAttribute() {
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        when(attribute.getPropertyName()).thenReturn("AV5");
        when(attribute.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);

        IAttributeValue value = valueContainer.newPropertyValue(parent, attribute, "ID5", IAttributeValue.class);
        assertNotNull(value);
        value.setAttribute("AV5");
        assertAttributesSize(4);

        IPropertyValue value2 = valueContainer.getPropertyValue("AV5");
        assertSame("Parts", value, value2);
    }

    @Test
    public void testNewPropertyValueFormula() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        when(property.getPropertyName()).thenReturn("Method1");
        when(property.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION);

        assertSize(3);
        IFormula formula = valueContainer.newPropertyValue(parent, property, "MethodID1", IFormula.class);
        assertNotNull(formula);
        formula.setFormulaSignature("Method1");
        assertSize(4);

        IPropertyValue value2 = valueContainer.getPropertyValue("Method1");
        assertSame("Parts", formula, value2);
    }

    @Test
    public void testNewPartThisXML() {
        IFormula formula = (IFormula)valueContainer.newPropertyValue(parent, Formula.TAG_NAME, "MethodID1");
        assertNotNull(formula);

        IProductCmptLink link = (IProductCmptLink)valueContainer.newPropertyValue(parent, ProductCmptLink.TAG_NAME,
                "LinkID1");
        // Link must be null as valueContainer cannot create parts that are not IPropertyValues
        assertNull(link);

        IIpsObjectPart part = valueContainer.newPropertyValue(parent, "TestIllegalXMLTag", "valueID1");
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

}
