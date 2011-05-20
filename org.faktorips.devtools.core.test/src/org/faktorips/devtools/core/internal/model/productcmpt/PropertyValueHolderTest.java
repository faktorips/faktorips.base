/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;

public class PropertyValueHolderTest extends AbstractIpsPluginTest {

    private IPropertyValueContainer parent;
    private PropertyValueHolder valueContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsObject ipsObject = mock(IIpsObject.class);
        parent = mock(ProductCmpt.class);
        when(parent.getIpsObject()).thenReturn(ipsObject);
        valueContainer = new PropertyValueHolder(parent);

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
        assertEquals(size, valueContainer.getPropertyValues(ProductCmptPropertyType.VALUE).size());
    }

    protected void assertSize(int size) {
        assertEquals(size, valueContainer.getAllPropertyValues().size());
    }

    @Test
    public void testAddPart() {
        assertAttributesSize(3);

        valueContainer.addPartThis(new AttributeValue(parent, "ID4"));
        assertAttributesSize(4);

        valueContainer.addPartThis(new AttributeValue(parent, "ID4"));
        assertAttributesSize(5);
    }

    @Test
    public void testRemovePart() {
        valueContainer.addPartThis(new AttributeValue(parent, "ID1"));
        assertAttributesSize(4);

        valueContainer.removePartThis(new AttributeValue(parent, "ID1"));
        assertAttributesSize(3);

        valueContainer.removePartThis(new AttributeValue(parent, "ID1"));
        assertAttributesSize(2);

        valueContainer.removePartThis(new AttributeValue(parent, "ID1"));
        assertAttributesSize(2);
    }

    @Test
    public void testGetPropertyValues() {
        assertAttributesSize(3);
        AttributeValue part4 = new AttributeValue(parent, "ID4");
        part4.setAttribute("AV4");
        valueContainer.addPropertyValue(part4);
        assertEquals(4, valueContainer.getAllPropertyValues().size());
        assertEquals(4, valueContainer.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        assertEquals(0, valueContainer.getPropertyValues(ProductCmptPropertyType.VALIDATION_RULE_CONFIG).size());
        assertEquals(0, valueContainer.getPropertyValues(ProductCmptPropertyType.TABLE_CONTENT_USAGE).size());

        ValidationRuleConfig config = new ValidationRuleConfig(parent, "ID4", "Rule");
        valueContainer.addPropertyValue(config);
        assertEquals(5, valueContainer.getAllPropertyValues().size());
        assertEquals(4, valueContainer.getPropertyValues(ProductCmptPropertyType.VALUE).size());
        assertEquals(1, valueContainer.getPropertyValues(ProductCmptPropertyType.VALIDATION_RULE_CONFIG).size());
        assertEquals(0, valueContainer.getPropertyValues(ProductCmptPropertyType.TABLE_CONTENT_USAGE).size());
    }

    @Test
    public void testGetPropertyValue() {
        IProductCmptProperty property = mock(IProductCmptProperty.class);
        when(property.getPropertyName()).thenReturn("AV1");
        when(property.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALUE);

        IPropertyValue value = valueContainer.getPropertyValue(property);
        assertNotNull(value);
        IPropertyValue value2 = valueContainer.getPropertyValue("AV1");
        assertSame("Parts", value, value2);

        ProductCmptTypeAttribute illegalTypeAttr = mock(ProductCmptTypeAttribute.class);
        when(illegalTypeAttr.getPropertyName()).thenReturn("AVIllegal");
        when(illegalTypeAttr.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALUE);
        value = valueContainer.getPropertyValue(illegalTypeAttr);
        assertNull(value);
    }

    @Test
    public void testNewPropertyValueAttribute() {
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        when(attribute.getPropertyName()).thenReturn("AV5");
        when(attribute.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALUE);

        IAttributeValue value = (IAttributeValue)valueContainer.newPropertyValue(attribute, "ID5");
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
        when(property.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.FORMULA);

        assertSize(3);
        IFormula formula = (IFormula)valueContainer.newPropertyValue(property, "MethodID1");
        assertNotNull(formula);
        formula.setFormulaSignature("Method1");
        assertSize(4);

        IPropertyValue value2 = valueContainer.getPropertyValue("Method1");
        assertSame("Parts", formula, value2);
    }

    @Test
    public void testNewPartThis() {
        IFormula formula = (IFormula)valueContainer.newPartThis(IFormula.class, "MethodID1");
        assertNotNull(formula);
        assertSize(4);

        IProductCmptLink link = (IProductCmptLink)valueContainer.newPartThis(IProductCmptLink.class, "LinkID1");
        // Link must be null as valueContainer cannot create parts that are not IPropertyValues
        assertNull(link);
    }

    @Test
    public void testNewPartThisXML() {
        IFormula formula = (IFormula)valueContainer.newPartThis(Formula.TAG_NAME, "MethodID1");
        assertNotNull(formula);

        IProductCmptLink link = (IProductCmptLink)valueContainer.newPartThis(ProductCmptLink.TAG_NAME, "LinkID1");
        // Link must be null as valueContainer cannot create parts that are not IPropertyValues
        assertNull(link);

        IIpsObjectPart part = valueContainer.newPartThis("TestIllegalXMLTag", "valueID1");
        assertNull(part);
    }

    @Test
    public void testAddPartThis() {
        AttributeValue attr = new AttributeValue(parent, "ID1");
        attr.setAttribute("AV1");
        assertTrue(valueContainer.addPartThis(attr));

        IProductCmptLink link = new ProductCmptLink();
        assertFalse(valueContainer.addPartThis(link));
    }

    @Test
    public void testRemovePartThis() {
        AttributeValue attr = new AttributeValue(parent, "ID1");
        attr.setAttribute("AV1");
        assertTrue(valueContainer.removePartThis(attr));
        assertFalse(valueContainer.removePartThis(attr));

        IProductCmptLink link = new ProductCmptLink();
        assertFalse(valueContainer.addPartThis(link));
    }

}
