/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IPolicyCmptType policyCmptType;

    private IAttribute productCmptTypeAttribute;

    private IAttribute policyCmptTypeAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        policyCmptType = newPolicyCmptType(ipsProject, "policyCmptType");
        policyCmptType.setProductCmptType("Product");
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
    }

    @Test
    public void testValidate_defaultNotInValueset() throws Exception {
        IProductCmptTypeAttribute attributeWithValueSet = productCmptType.newProductCmptTypeAttribute();
        attributeWithValueSet.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeWithValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attributeWithValueSet.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("10");
        range.setStep("1");
        attributeWithValueSet.setDefaultValue("1");
        MessageList ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));

        attributeWithValueSet.setDefaultValue("100");
        ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        Message msg = ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET);
        assertNotNull(msg);
        assertEquals(Message.WARNING, msg.getSeverity());

        attributeWithValueSet.setDefaultValue(null);
        ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_defaultNotParsableUnknownDatatype() throws Exception {
        productCmptTypeAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptTypeAttribute.setDefaultValue("1");

        MessageList ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));

        productCmptTypeAttribute.setDatatype("a");
        ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testValidate_defaultNotParsableInvalidDatatype() throws Exception {
        productCmptTypeAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        MessageList ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));

        productCmptTypeAttribute.setDatatype("abc");
        ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
    }

    @Test
    public void testValidate_valueNotParsable() throws Exception {
        productCmptTypeAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptTypeAttribute.setDefaultValue("1");
        MessageList ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));

        productCmptTypeAttribute.setDefaultValue("a");
        ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    }

    @Test
    public void testValidate_invalidAttributeName() throws Exception {
        productCmptTypeAttribute.setName("test");
        MessageList ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));

        productCmptTypeAttribute.setName("a.b");
        ml = productCmptTypeAttribute.validate(productCmptTypeAttribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentModifier() throws Exception {
        productCmptTypeAttribute.setName("name");
        productCmptTypeAttribute.setDatatype("String");
        productCmptTypeAttribute.setModifier(Modifier.PUBLIC);
        productCmptTypeAttribute.setOverwrite(true);

        MessageList ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setModifier(Modifier.PUBLISHED);

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));

        productCmptTypeAttribute.setModifier(superAttr.getModifier());
        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));
    }

    @Test
    public void testSetName() {
        testPropertyAccessReadWrite(Attribute.class, IIpsElement.PROPERTY_NAME, productCmptTypeAttribute, "newName");
    }

    @Test
    public void testSetValueDatatype() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DATATYPE, productCmptTypeAttribute,
                "newDatatype");
    }

    @Test
    public void testFindValueDatatype() throws CoreException {
        productCmptTypeAttribute.setDatatype(Datatype.BOOLEAN.getName());
        assertEquals(Datatype.BOOLEAN, productCmptTypeAttribute.findDatatype(ipsProject));
        productCmptTypeAttribute.setDatatype("unkown");
        assertNull(productCmptTypeAttribute.findDatatype(ipsProject));
    }

    @Test
    public void testSetDefaultValue() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DEFAULT_VALUE, productCmptTypeAttribute,
                "newDefault");
    }

    @Test
    public void testInitFromXml() {
        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute();
        Element rootEl = getTestDocument().getDocumentElement();

        // product attribute
        attr.setModifier(Modifier.PUBLISHED);
        attr.initFromXml(XmlUtil.getElement(rootEl, Attribute.TAG_NAME, 0));
        assertEquals("rate", attr.getName());
        assertEquals(Modifier.PUBLIC, attr.getModifier());
        assertEquals("Integer", attr.getDatatype());
    }

    @Test
    public void testToXml() {
        productCmptTypeAttribute.setName("a1");
        productCmptTypeAttribute.setDefaultValue("newDefault");
        productCmptTypeAttribute.setModifier(Modifier.PUBLIC);
        productCmptTypeAttribute.setDatatype("Date");

        Element el = productCmptTypeAttribute.toXml(newDocument());

        IAttribute copy = productCmptType.newProductCmptTypeAttribute();
        copy.initFromXml(el);
        assertEquals(productCmptTypeAttribute.getName(), copy.getName());
        assertEquals(productCmptTypeAttribute.getModifier(), copy.getModifier());
        assertEquals(productCmptTypeAttribute.getDatatype(), copy.getDatatype());
        assertEquals(productCmptTypeAttribute.getDefaultValue(), copy.getDefaultValue());

        // test null as default value
        productCmptTypeAttribute.setDefaultValue(null);
        el = productCmptTypeAttribute.toXml(newDocument());
        copy.initFromXml(el);
        assertNull(copy.getDefaultValue());
    }

    @Test
    public void testValidateDefaultValueStringEmpty() throws CoreException {
        MessageList list = new MessageList();
        IEnumType enumType = newEnumType(ipsProject, "EnumType");
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        ((Attribute)productCmptTypeAttribute).validateDefaultValue(StringUtils.EMPTY, adapter, list, ipsProject);

        assertFalse(list.isEmpty());
        assertNotNull(list.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    }

    @Test
    public void testOverwrittenAttribute_nullIcompatible() throws CoreException {
        String superTypeQName = "SuperPCType";
        PolicyCmptType superPCType = newPolicyCmptType(ipsProject, superTypeQName);
        PolicyCmptType pCType = newPolicyCmptType(ipsProject, "PCType");
        pCType.setSupertype(superTypeQName);

        IPolicyCmptTypeAttribute attr = superPCType.newPolicyCmptTypeAttribute("attr");
        IPolicyCmptTypeAttribute overwritingAttr = pCType.newPolicyCmptTypeAttribute("attr");
        attr.setDatatype("Integer");
        overwritingAttr.setDatatype("Integer");
        overwritingAttr.setOverwrite(true);

        List<String> listWithNull = list(null, "1", "2", "3", "4");
        List<String> normalValues = list("1", "9", "99", "999");
        attr.setValueSetCopy(new EnumValueSet(attr, listWithNull, "partId"));
        overwritingAttr.setValueSetCopy(new EnumValueSet(overwritingAttr, normalValues, "partId"));

        MessageList messageList = overwritingAttr.validate(ipsProject);
        assertEquals(1, messageList.size());
        ObjectProperty[] invalidObjectProperties = messageList.getMessage(0).getInvalidObjectProperties();
        assertEquals(1, invalidObjectProperties.length);

        attr.setValueSetCopy(new EnumValueSet(attr, normalValues, "partId"));
        overwritingAttr.setValueSetCopy(new EnumValueSet(overwritingAttr, listWithNull, "partId"));

        messageList = overwritingAttr.validate(ipsProject);
        assertEquals(2, messageList.size());
        assertEquals(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_INCOMPAIBLE_VALUESET, messageList.getMessage(0).getCode());
        assertEquals(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET, messageList.getMessage(1)
                .getCode());
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentChangingOverTime() throws Exception {
        productCmptTypeAttribute.setName("name");
        productCmptTypeAttribute.setDatatype("String");
        productCmptTypeAttribute.setChangingOverTime(false);
        productCmptTypeAttribute.setOverwrite(true);

        MessageList ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setChangingOverTime(true);

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        productCmptTypeAttribute.setChangingOverTime(superAttr.isChangingOverTime());
        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_typeDoesNotAcceptChangingOverTime() throws CoreException {
        productCmptType.setChangingOverTime(true);
        productCmptTypeAttribute.setChangingOverTime(false);

        MessageList ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptTypeAttribute.setName("productAttribute");

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(true);
        productCmptTypeAttribute.setChangingOverTime(true);

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productCmptTypeAttribute.setChangingOverTime(false);

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

        productCmptType.setChangingOverTime(false);
        productCmptTypeAttribute.setChangingOverTime(true);

        ml = productCmptTypeAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testChangingOverTime_productCmptTypeAttribute() {
        productCmptType.setChangingOverTime(false);
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();

        assertFalse(productCmptTypeAttribute.isChangingOverTime());

        productCmptType.setChangingOverTime(true);
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();

        assertTrue(productCmptTypeAttribute.isChangingOverTime());
    }

    @Test
    public void testChangingOverTime_policyCmptTypeAttribute() {
        assertTrue(productCmptTypeAttribute.isChangingOverTime());

        policyCmptTypeAttribute.setChangingOverTime(false);
        assertFalse(policyCmptTypeAttribute.isChangingOverTime());

    }

    private List<String> list(String... values) {
        return Arrays.asList(values);
    }
}
