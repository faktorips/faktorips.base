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

import static org.faktorips.testsupport.IpsMatchers.containsNoErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasErrorMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SingleValueHolderValidatorTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private ValueDatatype datatype;

    @Before
    public void setUp() {
        when(attributeValue.getIpsProject()).thenReturn(ipsProject);
        when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        when(attribute.findDatatype(ipsProject)).thenReturn(datatype);
    }

    @Test
    public void testValidate_NoValidationErrors() {
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(ipsProject))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "foo");
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_ValueNotInValueSet() {
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).containsValue("abc", ipsProject);
        doReturn(false).when(valueSet).containsValue("foo", ipsProject);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "foo");
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        MessageList messages = validator.validate();
        assertThat(messages.size(), is(1));
        Message message = messages.getMessageByCode(AttributeValue.MSGCODE_VALUE_NOT_IN_SET);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

        valueHolder = new SingleValueHolder(attributeValue, "abc");
        validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        assertThat(validator.validate().size(), is(0));
    }

    @Test
    public void testValidate_DatatypeNotFound() {
        when(attribute.findDatatype(ipsProject)).thenReturn(null);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

    }

    @Test
    public void testValidate_NotReadyToUse() {
        when(datatype.checkReadyToUse()).thenReturn(new MessageList(Message.newError("", "")));

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    @Test
    public void testValidate_ValueNotParsable() {
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue);
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        MessageList messages = validator.validate();

        assertThat(messages.size(), is(1));
        Message message = messages
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    @Test
    public void testValidate_InvalidMultilingualValue() {
        when(attribute.isMultilingual()).thenReturn(true);

        IIpsProjectProperties projectProperties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.getSupportedLanguages()).thenReturn(new HashSet<ISupportedLanguage>());

        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(ipsProject))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "Versicherung");
        SingleValueHolderValidator validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);

        MessageList messages = validator.validate();
        assertThat(messages.size(), is(1));
        Message message = messages.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);

        when(attribute.isMultilingual()).thenReturn(false);
        valueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        validator = new SingleValueHolderValidator(valueHolder, attributeValue, ipsProject);
        messages = validator.validate();

        assertThat(messages.size(), is(1));
        message = messages.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertThat(message, is(notNullValue()));
        verifyObjectProperties(message, attributeValue, valueHolder);
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryValidValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue("valid"), false);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryNullValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue(null), false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value <null> is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryEmptyStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue(""), false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value \"\" is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryBlankStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue(" "), false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value \" \" is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalValidValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue("valid"), true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalNullValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue(null), true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalEmptyStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(new StringValue(""), true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValdidateEmptyMultilingualValue_mandatoryEmptyStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(setupMultiLangValue(" "), false);
        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute multilangTestAttribute is mandatory and therefore must have a non-empty value. The value \" \" is considered empty."));
    }

    @Test
    public void testValdidateEmptyMultilingualValue_mandatoryNullValue() {
        SingleValueHolderValidator validator = setupValueHolder(setupMultiLangValue(null), false);
        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute multilangTestAttribute is mandatory and therefore must have a non-empty value. The value <null> is considered empty."));
    }

    @Test
    public void testValdidateEmptyMultilingualValue_mandatoryValidValue() {
        SingleValueHolderValidator validator = setupValueHolder(setupMultiLangValue("SomeValidString"), false);
        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValdidateEmptyMultilingualValue_mandatoryBlankStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(setupMultiLangValue(""), false);
        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute multilangTestAttribute is mandatory and therefore must have a non-empty value. The value \"\" is considered empty."));
    }

    private IValue<IInternationalString> setupMultiLangValue(String value) {
        IValue<IInternationalString> intValue = new InternationalStringValue();
        intValue.getContent().add(new LocalizedString(Locale.getDefault(), value == null ? "<null>" : value));
        return intValue;
    }

    private SingleValueHolderValidator setupValueHolder(IValue<?> value, boolean includeNull) {
        IValueSet unrestricted = new UnrestrictedValueSet(attribute, "partId", includeNull);
        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, value);

        when(attribute.findDatatype(ipsProject)).thenReturn(ValueDatatype.STRING);
        when(attribute.findValueDatatype(ipsProject)).thenReturn(ValueDatatype.STRING);
        when(attribute.getValueSet()).thenReturn(unrestricted);
        when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        doReturn("stringTestAttribute").when(attributeValue).getName();

        if (value instanceof InternationalStringValue) {
            IIpsProjectProperties ipsProjectProperties = mock(IIpsProjectProperties.class);
            ISupportedLanguage supportedLanguages = mock(ISupportedLanguage.class);
            when(supportedLanguages.getLocale()).thenReturn(Locale.getDefault());
            when(ipsProjectProperties.getSupportedLanguages()).thenReturn(Set.of(supportedLanguages));
            when(ipsProject.getReadOnlyProperties()).thenReturn(ipsProjectProperties);
            when(attribute.isMultilingual()).thenReturn(true);
            doReturn("multilangTestAttribute").when(attributeValue).getName();
        }

        return new SingleValueHolderValidator(singleValueHolder, attributeValue,
                ipsProject);
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
