/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributeTest extends AbstractIpsPluginTest {

    private static final String ATTR1 = "attr1";

    private static final String SUPER_ENUM_TYPE = "SuperEnum";

    private static final String ENUM_TYPE = "Enum";

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IProductCmptTypeAttribute productAttribute;

    private IType superProductCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");

        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");

        EnumType superEnumType = newEnumType(ipsProject, SUPER_ENUM_TYPE);
        superEnumType.setAbstract(true);
        EnumType enumType = newEnumType(ipsProject, ENUM_TYPE);
        enumType.setSuperEnumType(SUPER_ENUM_TYPE);
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
    public void testIsPropertyFor() {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newAttributeValue(productAttribute);

        assertTrue(productAttribute.isPropertyFor(propertyValue));
    }

    @Test
    public void testFindOverwrittenAttribute() {
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
    public void testValidate_invalidValueSet() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setChangingOverTime(false);
        productAttribute.setValueSetType(ValueSetType.RANGE);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_INVALID_VALUE_SET));
    }

    @Test
    public void testValidate_EnumValueSet_Hidden_DefaultValueNullNotContained() {
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
    public void testValidate_EnumValueSet_Hidden_DefaultValueNullContained() {
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
    public void testValidate_EnumValueSet_Visible_DefaultValueNullNotContained() {
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
    public void testValidate_EnumValueSet_Visible_DefaultValueNullContained() {
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
    public void testValidate_EnumValueSet_Hidden_DefaultValueNotContained() {
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
    public void testValidate_EnumValueSet_Visible_DefaultValueNotContained() {
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
    public void testValidate_EnumValueSet_Hidden_DefaultValueContained() {
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
    public void testValidate_EnumValueSet_Visible_DefaultValueContained() {
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
    public void testValidate_RangeValueSet_Hidden_DefaultValueNotContained() {
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
    public void testValidate_RangeValueSet_Visible_DefaultValueNotContained() {
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
    public void testValidate_RangeValueSet_Hidden_DefaultValueContained() {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
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
    public void testValidate_RangeValueSet_Visible_DefaultValueContained() {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
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
    public void testValidate_RangeValueSet_Hidden_DefaultValueNullNotContained() {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
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
    public void testValidate_RangeValueSet_Hidden_DefaultValueNullContained() {
        productAttribute.setName("productAttribute");
        productAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
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
    public void testValidate_RangeValueSet_Visible_DefaultValueNullNotContained() {
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
    public void testValidate_RangeValueSet_Visible_DefaultValueNullContained() {
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

    @Test
    public void testValidateMultiDefaultValue() {
        MessageList list = new MessageList();
        StringDatatype data = new StringDatatype();
        productAttribute.setVisible(false);
        ((ProductCmptTypeAttribute)productAttribute).validateDefaultValue("testTest", data, list, ipsProject);

        assertFalse(list.isEmpty());
        assertNotNull(list.getMessageByCode(IProductCmptTypeAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET_WHILE_HIDDEN));

    }

    @Test
    public void testValidateChangingOverTime_typeDoesNotAcceptChangingOverTime() {
        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(false);

        MessageList ml = productAttribute.validate(productAttribute.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(true);
        productAttribute.setChangingOverTime(true);

        ml = productAttribute.validate(productAttribute.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(false);

        ml = productAttribute.validate(productAttribute.getIpsProject());
        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productAttribute.setChangingOverTime(true);

        ml = productAttribute.validate(productAttribute.getIpsProject());
        assertNotNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testChangingOverTime_default() {
        productCmptType.setChangingOverTime(false);
        productAttribute = productCmptType.newProductCmptTypeAttribute();

        assertFalse(productAttribute.isChangingOverTime());

        productCmptType.setChangingOverTime(true);
        productAttribute = productCmptType.newProductCmptTypeAttribute();

        assertTrue(productAttribute.isChangingOverTime());
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentDatatype() throws Exception {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));

        ProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute("name");
        superAttr.setDatatype("Integer");

        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));

        attribute.setDatatype(superAttr.getDatatype());
        ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));
    }

    @Test
    public void testValidate_OverwrittenAttributeCovariantDatatype() throws Exception {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("name");
        attribute.setDatatype(ENUM_TYPE);
        attribute.setOverwrite(true);
        ProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute("name");
        superAttr.setDatatype(SUPER_ENUM_TYPE);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));

        superAttr.setDatatype(ENUM_TYPE);
        attribute.setDatatype(SUPER_ENUM_TYPE);
        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));
    }

    @Test
    public void testValidateNoAbstractDatatypeOfAttributes_overwrittenAbstractType() throws Exception {
        IAttribute superAttr1 = superProductCmptType.newAttribute();
        superAttr1.setName(ATTR1);
        superAttr1.setDatatype(SUPER_ENUM_TYPE);
        IAttribute attr1 = productCmptType.newAttribute();
        attr1.setName(ATTR1);
        attr1.setOverwrite(true);
        attr1.setDatatype(SUPER_ENUM_TYPE);

        MessageList list = productCmptType.validate(ipsProject);

        Message message = list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING);
        assertNotNull(message);
        assertEquals(new ObjectProperty(attr1, IAttribute.PROPERTY_DATATYPE),
                message.getInvalidObjectProperties().get(0));
        assertEquals(new ObjectProperty(productCmptType, IType.PROPERTY_ABSTRACT),
                message.getInvalidObjectProperties().get(1));
    }

}
