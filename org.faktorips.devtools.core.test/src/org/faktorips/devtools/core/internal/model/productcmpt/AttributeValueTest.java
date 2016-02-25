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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
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
import org.w3c.dom.Node;

public class AttributeValueTest extends AbstractIpsPluginTest {

    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String TEMPLATE_NAME = "Template";
    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute attribute;
    private IProductCmptTypeAttribute templatedAttribute;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    private IAttributeValue attributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("minimumPremium");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        templatedAttribute = productCmptType.newProductCmptTypeAttribute();
        templatedAttribute.setName(ATTRIBUTE_NAME);
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
        attributeValue = generation.newAttributeValue(attribute);
    }

    @Test
    public void testValidate_UnknownAttribute() throws CoreException {
        MessageList ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attributeValue.setAttribute("AnotherAttribute");
        ml = attributeValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(supertype.getQualifiedName());

        supertype.newProductCmptTypeAttribute().setName("AnotherAttribute");
        ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    @Test
    public void testValidate_ValueNotParsable() throws CoreException {
        MessageList ml = attributeValue.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "abc"));
        ml = attributeValue.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }

    @Test
    public void testValidate_ValueNotInSet() throws CoreException {
        MessageList ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("100");

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "0"));
        ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "100"));
        ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "42"));
        ml = attributeValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "-1"));
        ml = attributeValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "101"));
        ml = attributeValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
    }

    @Test
    public void testSetAttribute() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_ATTRIBUTE, attributeValue,
                "premium");
    }

    @SuppressWarnings("deprecation")
    // testing the deprecated property
    @Test
    public void testSetValue() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_VALUE, attributeValue,
                "newValue");
    }

    @Test
    public void testSetValueHolder() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_VALUE_HOLDER, attributeValue,
                new SingleValueHolder(attributeValue, "newValue"));
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        attributeValue.initFromXml(el);
        assertEquals("rate", attributeValue.getAttribute());
        assertEquals("42", attributeValue.getPropertyValue());
        assertEquals(TemplateValueStatus.DEFINED, attributeValue.getTemplateValueStatus());
    }

    @Test
    public void testInitFromXml_TemplateValueStatusIsRead() {
        productCmpt.setTemplate("anyTemplate");
        attributeValue.setAttribute("rate");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        Element el = attributeValue.toXml(newDocument());

        AttributeValue fromXml = new AttributeValue(generation, "id");
        fromXml.initFromXml(el);
        assertThat(fromXml.getAttribute(), is("rate"));
        assertThat(fromXml.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testToXml() {
        Document doc = newDocument();
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "42"));
        attributeValue.setAttribute("rate");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        Element el = attributeValue.toXml(doc);

        IAttributeValue copy = generation.newAttributeValue();
        copy.initFromXml(el);
        assertEquals("rate", copy.getAttribute());
        assertEquals("42", copy.getPropertyValue());
        assertEquals(TemplateValueStatus.DEFINED, copy.getTemplateValueStatus());
    }

    @Test
    public void testToXml_PersistTemplateValueForInheritedAttribute() throws CoreException {
        // Set some value in attributeValue, but set its status to inherited so that getValueHolder
        // returns the value from the template
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "definedValue"));
        attributeValue.setAttribute("attribute");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        // Set up the template
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGeneration = template.getProductCmptGeneration(0);
        IAttributeValue templateAttrValue = templateGeneration.newAttributeValue(attribute);
        templateAttrValue.setValueHolder(new SingleValueHolder(templateAttrValue, "inheritedValue"));
        templateAttrValue.setAttribute("attribute");
        productCmpt.setTemplate(template.getQualifiedName());

        Document doc = newDocument();
        Element el = attributeValue.toXml(doc);

        // Make sure that the inherited value is persisted correctly
        String templateValueStatus = el.getAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS);
        assertThat(templateValueStatus, is("inherited"));

        Node valueElement = el.getElementsByTagName("Value").item(0);
        assertThat(valueElement.getTextContent(), is("inheritedValue"));
    }

    @Test
    public void testGetCaption() throws CoreException {
        ILabel label = attribute.getLabel(Locale.US);
        label.setValue("TheCaption");
        assertEquals("TheCaption", attributeValue.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(attributeValue.getCaption(Locale.TAIWAN));
    }

    @Test(expected = NullPointerException.class)
    public void testGetCaptionNullPointer() throws CoreException {
        attributeValue.getCaption(null);
    }

    @Test
    public void testGetLastResortCaption() {
        assertEquals(StringUtils.capitalize(attributeValue.getAttribute()), attributeValue.getLastResortCaption());
    }

    @Test
    public void testValueHolder_isNull() throws Exception {
        assertNull(attributeValue.getPropertyValue());
        assertNotNull(attributeValue.getValueHolder().getValue());
        assertTrue(attributeValue.getValueHolder().isNullValue());

        attributeValue.setValueHolder(null);

        assertNull(attributeValue.getPropertyValue());
        assertNull(attributeValue.getValueHolder());
    }

    @Test
    public void testGetPropertyValue() throws Exception {
        assertNull(attributeValue.getPropertyValue());

        ((SingleValueHolder)attributeValue.getValueHolder()).setValue(new StringValue("abc"));

        assertEquals("abc", attributeValue.getPropertyValue());
    }

    @Test
    public void testValidate() throws CoreException {
        attribute.setMultiValueAttribute(true);
        attribute.setDatatype("String");
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);
        attributeValue.setValueHolder(multiValueHolder);

        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "A");
        values.add(valueHolder);
        values.add(new SingleValueHolder(attributeValue, "B"));
        values.add(new SingleValueHolder(attributeValue, "A"));
        values.add(new SingleValueHolder(attributeValue, "C"));
        multiValueHolder.setValue(values);

        MessageList messageList = attributeValue.validate(ipsProject).getMessages(Message.ERROR);
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
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);
        attributeValue.setValueHolder(multiValueHolder);
        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "A");
        values.add(valueHolder);
        multiValueHolder.setValue(values);

        MessageList messageList = attributeValue.validate(ipsProject);

        assertEquals(1, messageList.size());
        assertEquals(IAttributeValue.MSGCODE_HIDDEN_ATTRIBUTE, messageList.getMessage(0).getCode());
    }

    @Test
    public void testSetTemplateStatus() {
        productCmpt.setTemplate("anyTemplate");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(attributeValue.getTemplateValueStatus(), is(TemplateValueStatus.UNDEFINED));
    }

    @Test
    public void testGetTemplateStatus_defaultValue() {
        assertThat(attributeValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testValidate_TemplateStatus_excludedNotAllowedForProductCmpt() throws CoreException {
        productCmpt.setTemplate("anyTemplate");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(attributeValue.validate(ipsProject),
                hasMessageCode(ITemplatedValue.MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_TemplateStatus_inheritedOnlyIfInheritablePropertyExists() throws CoreException {
        ProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IAttributeValue templateAV = templateGen.newAttributeValue(attribute);
        templateAV.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        productCmpt.setTemplate(template.getQualifiedName());
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(attributeValue.validate(ipsProject),
                lacksMessageCode(ITemplatedValue.MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));

        productCmpt.setTemplate("invalid template");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertThat(attributeValue.validate(ipsProject),
                hasMessageCode(ITemplatedValue.MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testGetValueHolder_NullTemplateStatus() {
        attributeValue.setTemplateValueStatus(null);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "0");
        attributeValue.setValueHolder(valueHolder);

        assertThat((SingleValueHolder)attributeValue.getValueHolder(), is(valueHolder));
    }

    @Test
    public void testGetValueHolder_DefinedValue() {
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "0");
        attributeValue.setValueHolder(valueHolder);

        assertThat((SingleValueHolder)attributeValue.getValueHolder(), is(valueHolder));
    }

    @Test
    public void testSetTemplateValueStatus_DefinedShouldCopyValueHolder() throws CoreException {
        String templateValue = "template value";
        String definedValue = "defined value";

        // Set up a template with an attribute value for templatedAttribute
        IProductCmpt template = newProductTemplate(productCmptType, TEMPLATE_NAME);
        IProductCmptGeneration templateGeneration = template.getProductCmptGeneration(0);
        IAttributeValue templateAttributeValue = templateGeneration.newAttributeValue(templatedAttribute);
        templateAttributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        templateAttributeValue.setValueHolder(new SingleValueHolder(templateAttributeValue, templateValue));

        // Add attribute value to productCmpt's generation
        productCmpt.setTemplate(template.getName());
        IAttributeValue attributeValue = generation.newAttributeValue(templatedAttribute);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, definedValue));

        // Make attribute value inherit the templates value
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertThat(attributeValue.getValueHolder().getStringValue(), is(templateValue));

        // Make attribute value of productCmpt's generation DEFINED, the value should be copied
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertThat(attributeValue.getValueHolder().getStringValue(), is(templateValue));

        // Copied value holder has to be writable
        SingleValueHolder copiedValueHolder = (SingleValueHolder)attributeValue.getValueHolder();
        copiedValueHolder.setValue(new StringValue("new defined value"));
        assertThat(attributeValue.getValueHolder().getStringValue(), is("new defined value"));
    }

    @Test
    public void testGetValueHolder_UndefinedSingleValue() {
        attributeValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertThat(attributeValue.getValueHolder(), is(instanceOf(SingleValueHolder.class)));
    }

    @Test
    public void testGetValueHolder_UndefinedMultiValue() {

        IProductCmptTypeAttribute multiValueAttribute = productCmptType.newProductCmptTypeAttribute();
        multiValueAttribute.setName("multiValue");
        multiValueAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        multiValueAttribute.setMultiValueAttribute(true);

        IAttributeValue multiValueAttrValue = generation.newAttributeValue(multiValueAttribute);

        multiValueAttrValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        assertThat(multiValueAttrValue.getValueHolder(), is(instanceOf(MultiValueHolder.class)));
    }

    @Test
    public void testGetValueHolder_InheritedValue() throws CoreException {
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "0");
        attributeValue.setValueHolder(valueHolder);
        assertThat((SingleValueHolder)attributeValue.getValueHolder(), is(valueHolder));

        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGeneration = template.getProductCmptGeneration(0);
        IAttributeValue templateAttrValue = templateGeneration.newAttributeValue(attribute);
        SingleValueHolder templateValueHolder = new SingleValueHolder(templateAttrValue, "1");
        templateAttrValue.setValueHolder(templateValueHolder);

        productCmpt.setTemplate(template.getQualifiedName());
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertThat(attributeValue.getValueHolder(), is(instanceOf(DelegatingValueHolder.class)));
        DelegatingValueHolder<?> delegatingValueHolder = (DelegatingValueHolder<?>)attributeValue.getValueHolder();
        assertThat(delegatingValueHolder.getDelegate(), is(instanceOf(SingleValueHolder.class)));
        assertThat((SingleValueHolder)delegatingValueHolder.getDelegate(), is(templateValueHolder));
    }

    @Test
    public void testGetValue_InheritedValueWhenTemplateIsMissing() {
        productCmpt.setTemplate("No such template");

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "0");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(attributeValue.getValueHolder(), is(instanceOf(SingleValueHolder.class)));
        assertThat((SingleValueHolder)attributeValue.getValueHolder(), is(valueHolder));
    }

    @Test
    public void testGetValue_InheritedValueWhenTemplateDoesNotDefineValue() throws CoreException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        productCmpt.setTemplate(template.getQualifiedName());

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "0");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat((SingleValueHolder)attributeValue.getValueHolder(), is(valueHolder));
    }

    @Test
    public void testIsConcreteValue() {
        // make product cmpt part of template hierarchy
        productCmpt.setTemplate("someTemplate");
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        assertTrue(attributeValue.isConcreteValue());

        attributeValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        assertFalse(attributeValue.isConcreteValue());

        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        assertFalse(attributeValue.isConcreteValue());
    }

}
