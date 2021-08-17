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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Test;

public class SingleValueHolderValidatorTest {

    @Test
    public void testValidate_NoValidationErrors() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(project))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_ValueNotInValueSet() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue("abc", project)).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        MessageList messages = validator.validate();
        assertThat(messages.size(), is(1));
        Message message = messages.getMessageByCode(AttributeValue.MSGCODE_VALUE_NOT_IN_SET);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

        valueHolder = new SingleValueHolder(attributeValue, "abc");
        validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_DatatypeNotFound() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

    }

    @Test
    public void testValidate_NotReadyToUse() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList(Message.newError("", "")));

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    @Test
    public void testValidate_ValueNotParsable() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    @Test
    public void testValidate_InvalidMultilingualValue() throws CoreException {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attribute.isMultilingual()).thenReturn(true);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);
        IIpsProjectProperties projectProperties = mock(IIpsProjectProperties.class);
        when(project.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.getSupportedLanguages()).thenReturn(new HashSet<ISupportedLanguage>());

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(project))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "Versicherung");
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);

        MessageList messages = validator.validate();
        assertThat(messages.size(), is(1));
        Message message = messages.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

        when(attribute.isMultilingual()).thenReturn(false);
        valueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        validator = new SingleValueHolderValidator(valueHolder, attributeValue, project);
        messages = validator.validate();

        assertThat(messages.size(), is(1));
        message = messages.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    private void verifyObjectProperties(Message message, IAttributeValue parent, SingleValueHolder valueHolder) {
        ObjectProperty firstObjectProperty = message.getInvalidObjectProperties().get(0);
        ObjectProperty secondObjectProperty = message.getInvalidObjectProperties().get(1);

        assertThat(firstObjectProperty.getObject(), is((Object)parent));
        assertThat(firstObjectProperty.getProperty(), is(IAttributeValue.PROPERTY_VALUE_HOLDER));
        assertThat(secondObjectProperty.getObject(), is((Object)valueHolder));
        assertThat(secondObjectProperty.getProperty(), is(IValueHolder.PROPERTY_VALUE));
    }
}
