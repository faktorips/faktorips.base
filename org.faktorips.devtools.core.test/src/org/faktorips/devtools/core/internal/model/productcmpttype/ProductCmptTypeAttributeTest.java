/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IProductCmptTypeAttribute productAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");
    }

    @Test
    public void testDelete() {
        productAttribute.delete();
        assertNull(productCmptType.getProductCmptTypeAttribute(productAttribute.getName()));
        assertEquals(0, productCmptType.getNumOfAttributes());
    }

    @Test
    public void testXml() {
        productAttribute.setCategory("foo");
        Element el = productAttribute.toXml(newDocument());

        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        a.initFromXml(el);
        assertEquals(productAttribute.getName(), a.getName());
        assertEquals(productAttribute.getModifier(), a.getModifier());
        assertEquals(productAttribute.getDatatype(), a.getDatatype());
        assertEquals(productAttribute.getCategory(), a.getCategory());
    }

    /**
     * Tests if a attributes with properties containing null can be transformed to xml without
     * exceptions as null handling can be a problem especially transforming the xml to strings.
     */
    @Test
    public void testToXml_NullHandlng() throws TransformerException {
        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        Element el = a.toXml(newDocument());
        XmlUtil.nodeToString(el, "UTF-8");
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertFalse(productAttribute.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newAttributeValue(productAttribute);

        assertTrue(productAttribute.isPropertyFor(propertyValue));
    }

    @Test
    public void testFindOverwrittenAttribute() throws CoreException {
        productAttribute.setName("a");
        IProductCmptType supertype = newProductCmptType(productCmptType, "Supertype");
        IProductCmptType supersupertype = newProductCmptType(ipsProject, "SuperSupertype");
        productCmptType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        assertNull(productAttribute.findOverwrittenAttribute(ipsProject));

        IProductCmptTypeAttribute aInSupertype = supersupertype.newProductCmptTypeAttribute();
        aInSupertype.setName("a");

        assertEquals(aInSupertype, productAttribute.findOverwrittenAttribute(ipsProject));

        // cycle in type hierarchy
        supersupertype.setSupertype(productCmptType.getQualifiedName());
        assertEquals(aInSupertype, productAttribute.findOverwrittenAttribute(ipsProject));

        aInSupertype.delete();
        assertNull(productAttribute.findOverwrittenAttribute(ipsProject)); // this should not return
                                                                           // itself!
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentMultiValue() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setMultiValueAttribute(false);
        productAttribute.setOverwrite(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setMultiValueAttribute(true);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        productAttribute.setMultiValueAttribute(true);
        superAttr.setMultiValueAttribute(false);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        productAttribute.setMultiValueAttribute(superAttr.isMultiValueAttribute());
        ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentChangingOverTime() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setChangingOverTime(false);
        productAttribute.setOverwrite(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setChangingOverTime(true);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        productAttribute.setChangingOverTime(superAttr.isChangingOverTime());
        ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));
    }

    @Test
    public void testValidate_invalidValueSet() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setChangingOverTime(false);
        productAttribute.setValueSetType(ValueSetType.RANGE);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_INVALID_VALUE_SET));
    }

    @Test
    public void testValidate_EnumValueSet_Hidden_DefaultValueNullNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue(null);
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Hidden_DefaultValueNullContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue(null);
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3", null));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Visible_DefaultValueNullNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue(null);
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Visible_DefaultValueNullContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue(null);
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3", null));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Hidden_DefaultValueNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue("4");
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Visible_DefaultValueNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue("4");
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Hidden_DefaultValueContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue("3");
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_EnumValueSet_Visible_DefaultValueContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue("3");
        IEnumValueSet valueSet = (IEnumValueSet)productAttribute.changeValueSetType(ValueSetType.ENUM);
        valueSet.addValues(Arrays.asList("1", "2", "3"));

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Hidden_DefaultValueNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue("4");
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Visible_DefaultValueNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue("4");
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Hidden_DefaultValueContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue("3");
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Visible_DefaultValueContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue("3");
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Hidden_DefaultValueNullNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue(null);
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");
        valueSet.setContainsNull(false);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Hidden_DefaultValueNullContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(false);
        productAttribute.setDefaultValue(null);
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");
        valueSet.setContainsNull(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Visible_DefaultValueNullNotContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue(null);
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");
        valueSet.setContainsNull(false);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_RangeValueSet_Visible_DefaultValueNullContained() throws CoreException {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setVisible(true);
        productAttribute.setDefaultValue(null);
        IRangeValueSet valueSet = (IRangeValueSet)productAttribute.changeValueSetType(ValueSetType.RANGE);
        valueSet.setLowerBound("1");
        valueSet.setUpperBound("3");
        valueSet.setStep("1");
        valueSet.setContainsNull(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));
        assertNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testSetVisible() {
        assertTrue(productAttribute.isVisible());
        productAttribute.setVisible(false);
        assertFalse(productAttribute.isVisible());
    }

    @Test
    public void testSetMultilingual() {
        productAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productAttribute.setMultilingual(true);
        assertTrue(productAttribute.isMultilingual());
        productAttribute.setMultilingual(false);
        assertFalse(productAttribute.isMultilingual());
    }

    @Test
    public void testSetMultilingual_notString() {
        productAttribute.setMultilingual(true);
        assertFalse(productAttribute.isMultilingual());
        productAttribute.setMultilingual(false);
        assertFalse(productAttribute.isMultilingual());
    }

    @Test
    public void testGetAllowedValueSetTypes() throws Exception {
        productAttribute.setMultilingual(true);

        List<ValueSetType> allowedValueSetTypes = productAttribute.getAllowedValueSetTypes(ipsProject);

        assertThat(allowedValueSetTypes, hasItem(ValueSetType.UNRESTRICTED));
    }

}
