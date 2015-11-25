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

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AttributeValueTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute attribute;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    private IAttributeValue attrValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("minimumPremium");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
        attrValue = generation.newAttributeValue(attribute);
    }

    @Test
    public void testValidate_UnknownAttribute() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attrValue.setAttribute("AnotherAttribute");
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(supertype.getQualifiedName());

        supertype.newProductCmptTypeAttribute().setName("AnotherAttribute");
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
    public void testValidate_ValueNotParsable() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "abc"));
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }

    @Test
    public void testValidate_ValueNotInSet() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("100");

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "0"));
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "100"));
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "42"));
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "-1"));
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "101"));
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
    }

    @Test
    public void testSetAttribute() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_ATTRIBUTE, attrValue,
                "premium");
    }

    @SuppressWarnings("deprecation")
    // testing the deprecated property
    @Test
    public void testSetValue() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_VALUE, attrValue, "newValue");
    }

    @Test
    public void testSetValueHolder() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_VALUE_HOLDER, attrValue,
                new SingleValueHolder(attrValue, "newValue"));
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        attrValue.initFromXml(el);
        assertEquals("rate", attrValue.getAttribute());
        assertEquals("42", attrValue.getPropertyValue());
        assertEquals(TemplateValueStatus.INHERITED, attrValue.getTemplateValueStatus());
    }

    @Test
    public void testToXml() {
        Document doc = newDocument();
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "42"));
        attrValue.setAttribute("rate");
        attrValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        Element el = attrValue.toXml(doc);

        IAttributeValue copy = generation.newAttributeValue();
        copy.initFromXml(el);
        assertEquals("rate", copy.getAttribute());
        assertEquals("42", copy.getPropertyValue());
        assertEquals(TemplateValueStatus.INHERITED, copy.getTemplateValueStatus());
    }

    @Test
    public void testGetCaption() throws CoreException {
        ILabel label = attribute.getLabel(Locale.US);
        label.setValue("TheCaption");
        assertEquals("TheCaption", attrValue.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(attrValue.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
        try {
            attrValue.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        assertEquals(StringUtils.capitalize(attrValue.getAttribute()), attrValue.getLastResortCaption());
    }

    @Test
    public void testValueHolder_isNull() throws Exception {
        assertNull(attrValue.getPropertyValue());
        assertNotNull(attrValue.getValueHolder().getValue());
        assertTrue(attrValue.getValueHolder().isNullValue());

        attrValue.setValueHolder(null);

        assertNull(attrValue.getPropertyValue());
        assertNull(attrValue.getValueHolder());
    }

    @Test
    public void testGetPropertyValue() throws Exception {
        assertNull(attrValue.getPropertyValue());

        ((SingleValueHolder)attrValue.getValueHolder()).setValue(new StringValue("abc"));

        assertEquals("abc", attrValue.getPropertyValue());
    }

    @Test
    public void testValidate() throws CoreException {
        attribute.setMultiValueAttribute(true);
        attribute.setDatatype("String");
        MultiValueHolder multiValueHolder = new MultiValueHolder(attrValue);
        attrValue.setValueHolder(multiValueHolder);

        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "A");
        values.add(valueHolder);
        values.add(new SingleValueHolder(attrValue, "B"));
        values.add(new SingleValueHolder(attrValue, "A"));
        values.add(new SingleValueHolder(attrValue, "C"));
        multiValueHolder.setValue(values);

        MessageList messageList = attrValue.validate(ipsProject).getMessages(Message.ERROR);
        assertEquals(2, messageList.getNoOfMessages(Message.ERROR));
        assertEquals(valueHolder, messageList.getMessage(0).getInvalidObjectProperties()[0].getObject());
        assertEquals(multiValueHolder.getParent(),
                messageList.getMessage(1).getInvalidObjectProperties()[0].getObject());
        assertEquals(multiValueHolder, messageList.getMessage(1).getInvalidObjectProperties()[1].getObject());
    }

    @Test
    public void testValidate_hiddenAttribute() throws CoreException {
        attribute.setMultiValueAttribute(true);
        attribute.setDatatype("String");
        attribute.setVisible(false);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attrValue);
        attrValue.setValueHolder(multiValueHolder);
        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attrValue, "A");
        values.add(valueHolder);
        multiValueHolder.setValue(values);

        MessageList messageList = attrValue.validate(ipsProject);

        assertEquals(1, messageList.size());
        assertEquals(IAttributeValue.MSGCODE_HIDDEN_ATTRIBUTE, messageList.getMessage(0).getCode());
    }

    @Test
    public void setTemplateStatus() {
        attrValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
    }

    @Test
    public void getTemplateStatus_defaultValue() {
        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void validateTemplateStatus_excludedNotAllowedForProductCmpt() throws CoreException {
        attrValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(attrValue.validate(ipsProject), hasMessageCode(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS));
    }

    @Test
    public void validateTemplateStatus_inheritedOnlyIfInheritablePropertyExists() throws CoreException {
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IAttributeValue templateAV = templateGen.newAttributeValue(attribute);
        templateAV.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        productCmpt.setTemplate(template.getQualifiedName());
        attrValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(attrValue.validate(ipsProject), lacksMessageCode(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS));

        productCmpt.setTemplate("invalid template");
        assertThat(attrValue.validate(ipsProject), hasMessageCode(IAttributeValue.MSGCODE_INVALID_TEMPLATE_STATUS));
    }

}
