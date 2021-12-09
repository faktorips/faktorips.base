/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    private static final String PAYMENT_CONTENT = "paymentContent";

    @Test
    public void testNewEnumAttributeValue() throws CoreRuntimeException {
        IEnumAttributeValue newEnumAttributeValue = genderEnumValueMale.newEnumAttributeValue();

        assertNotNull(newEnumAttributeValue);
    }

    @Test
    public void testNewEnumAttributeValue_fixedValue() throws CoreRuntimeException {
        IEnumAttributeValue enumAttributeValue = genderEnumValueMale.getEnumAttributeValues()
                .get(genderEnumValueMale.getEnumAttributeValuesCount() - 1);
        IEnumAttribute enumAttribute = enumAttributeValue.findEnumAttribute(ipsProject);
        enumAttributeValue.delete();
        enumAttribute.setMultilingual(true);
        final IEnumAttributeValue newEnumAttributeValue = genderEnumValueMale.newEnumAttributeValue();

        assertNotNull(newEnumAttributeValue);
        assertTrue(newEnumAttributeValue.getValue().getContent() instanceof IInternationalString);
    }

    @Test
    public void testNewEnumLiteralNameAttributeValue() {
        assertNotNull(genderEnumValueMale.newEnumLiteralNameAttributeValue());
    }

    @Test
    public void testGetEnumAttributeValues() {
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
    }

    @Test
    public void testValidateThis() throws CoreRuntimeException {
        assertTrue(genderEnumValueFemale.isValid(ipsProject));
    }

    @Test
    public void testValidateNumberEnumAttributeValues() throws CoreRuntimeException {
        genderEnumType.newEnumAttribute();
        assertTrue(genderEnumValueFemale.isValid(ipsProject));

        getIpsModel().clearValidationCache();
        genderEnumValueFemale.getEnumAttributeValues().get(0).delete();
        MessageList validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(
                IEnumValue.MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES));
    }

    @Test
    public void testGetEnumValueContainer() {
        assertEquals(genderEnumContent, genderEnumValueMale.getEnumValueContainer());
    }

    @Test
    public void testGetEnumAttributeValue() {
        assertNull(genderEnumValueMale.getEnumAttributeValue(null));

        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(0),
                genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(0),
                genderEnumValueFemale.getEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(1),
                genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(1),
                genderEnumValueFemale.getEnumAttributeValue(genderEnumAttributeName));
    }

    @Test
    public void testGetEnumAttributeValue_extensibleContent() throws CoreRuntimeException {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName(PAYMENT_CONTENT);
        EnumContent enumContent = newEnumContent(paymentMode, PAYMENT_CONTENT);
        IEnumValue enumValue = enumContent.newEnumValue();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("idValue"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("nameValue"));

        IEnumAttributeValue enumAttributeValue = enumValue
                .getEnumAttributeValue(paymentMode.findIdentiferAttribute(ipsProject));

        assertEquals(2, enumValue.getEnumAttributeValues().size());
        assertEquals("idValue", enumAttributeValue.getStringValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeGiven() throws CoreRuntimeException {
        try {
            genderEnumValueMale.setEnumAttributeValue((IEnumAttribute)null, ValueFactory.createStringValue(""));
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName, ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getStringValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeNameGiven() throws CoreRuntimeException {
        try {
            genderEnumValueMale.setEnumAttributeValue((String)null, ValueFactory.createStringValue(""));
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName.getName(),
                ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getStringValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeValueIndexGiven() {
        try {
            genderEnumValueMale.setEnumAttributeValue(-1, ValueFactory.createStringValue(""));
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            genderEnumValueMale.setEnumAttributeValue(20, ValueFactory.createStringValue(""));
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(1, ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValues().get(1).getStringValue());
    }

    @Test
    public void testFindUniqueEnumAttributeValues() throws CoreRuntimeException {
        IEnumValue value = paymentMode.getEnumValues().get(0);
        List<IEnumAttributeValue> uniqueAttributeValues = value
                .findUniqueEnumAttributeValues(paymentMode.findUniqueEnumAttributes(true, ipsProject), ipsProject);
        assertEquals(3, uniqueAttributeValues.size());
    }

    @Test
    public void testGetEnumLiteralNameAttributeValue() {
        assertNull(genderEnumValueMale.getEnumLiteralNameAttributeValue());
        IEnumValue value = paymentMode.getEnumValues().get(0);
        assertEquals("MONTHLY", value.getEnumLiteralNameAttributeValue().getStringValue());
    }

}
