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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SingleValueHolderTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue attributeValue;

    private ValueDatatype datatype = ValueDatatype.STRING;

    @Before
    public void setUp() {
        when(attributeValue.getIpsProject()).thenReturn(ipsProject);
        when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        when(attribute.findValueDatatype(ipsProject)).thenReturn(datatype);
    }

    @Test
    public void testGetValueType() {
        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, "abc");
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new StringValue("abc"));
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        assertEquals(ValueType.INTERNATIONAL_STRING, singleValueHolder.getValueType());
    }

    @Test
    public void testCompareTo_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertTrue(v1.compareTo(null) < 0);
    }

    @Test
    public void testCompareTo_null_eq_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertThat(v1.compareTo(v2), is(0));
    }

    @Test
    public void testCompareTo_nullContent_eq_nullContent() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (String)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (String)null);

        assertThat(v1.compareTo(v2), is(0));
        assertThat(v2.compareTo(v1), is(0));
    }

    @Test
    public void testCompareTo_null_lt_any() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_nullContent_lt_any() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (String)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_any_lt_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

    @Test
    public void testCompareTo_any_lt_nullContent() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (String)null);

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

    @Test
    public void testCompareTo_eq() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v1) == 0);
    }

    @Test
    public void testCompareTo_lt() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asx");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_gt() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asx");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryValidValue() {
        SingleValueHolderValidator validator = setupValueHolder("valid", false);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryNullValue() {
        SingleValueHolderValidator validator = setupValueHolder(null, false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value <null> is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryEmptyStringValue() {
        SingleValueHolderValidator validator = setupValueHolder("", false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value \"\" is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_mandatoryBlankStringValue() {
        SingleValueHolderValidator validator = setupValueHolder(" ", false);

        MessageList messageList = validator.validate();

        assertThat(messageList, hasErrorMessage(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));
        assertThat(messageList.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET),
                containsText(
                        "The attribute stringTestAttribute is mandatory and therefore must have a non-empty value. The value \" \" is considered empty."));
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalValidValue() {
        SingleValueHolderValidator validator = setupValueHolder("valid", true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalNullValue() {
        SingleValueHolderValidator validator = setupValueHolder(null, true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    @Test
    public void testValidateEmptyStringAsNull_optionalEmptyStringValue() {
        SingleValueHolderValidator validator = setupValueHolder("", true);

        MessageList messageList = validator.validate();

        assertThat(messageList, containsNoErrorMessage());
    }

    private SingleValueHolderValidator setupValueHolder(String value, boolean includeNull) {
        IValueSet unrestricted = new UnrestrictedValueSet(attribute, "partId", includeNull);
        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, value);
        when(attribute.findDatatype(ipsProject)).thenReturn(ValueDatatype.STRING);
        when(attribute.getValueSet()).thenReturn(unrestricted);
        when(attributeValue.getName()).thenReturn("stringTestAttribute");
        return new SingleValueHolderValidator(singleValueHolder, attributeValue,
                ipsProject);
    }

}
