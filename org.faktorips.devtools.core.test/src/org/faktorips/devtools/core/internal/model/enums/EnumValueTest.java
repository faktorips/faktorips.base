/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    @Test
    public void testNewEnumAttributeValue() throws CoreException {
        assertNotNull(genderEnumValueMale.newEnumAttributeValue());
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
    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumValueFemale.isValid());
    }

    @Test
    public void testValidateNumberEnumAttributeValues() throws CoreException {
        genderEnumType.newEnumAttribute();
        assertTrue(genderEnumValueFemale.isValid());

        getIpsModel().clearValidationCache();
        genderEnumValueFemale.getEnumAttributeValues().get(0).delete();
        MessageList validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumValue.MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES));
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
    public void testSetEnumAttributeValueAttributeGiven() throws CoreException {
        try {
            genderEnumValueMale.setEnumAttributeValue((IEnumAttribute)null, "");
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName, "foo");
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeNameGiven() throws CoreException {
        try {
            genderEnumValueMale.setEnumAttributeValue((String)null, "");
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName.getName(), "foo");
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeValueIndexGiven() {
        try {
            genderEnumValueMale.setEnumAttributeValue(-1, "");
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            genderEnumValueMale.setEnumAttributeValue(20, "");
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        genderEnumValueMale.setEnumAttributeValue(1, "foo");
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValues().get(1).getValue());
    }

    @Test
    public void testFindUniqueEnumAttributeValues() throws CoreException {
        IEnumValue value = paymentMode.getEnumValues().get(0);
        List<IEnumAttributeValue> uniqueAttributeValues = value.findUniqueEnumAttributeValues(
                paymentMode.findUniqueEnumAttributes(true, ipsProject), ipsProject);
        assertEquals(3, uniqueAttributeValues.size());
    }

    @SuppressWarnings("deprecation")
    // Test of deprecated method.
    @Test
    public void testGetLiteralNameAttributeValue() {
        assertNull(genderEnumValueMale.getLiteralNameAttributeValue());
        IEnumValue value = paymentMode.getEnumValues().get(0);
        assertEquals("MONTHLY", value.getLiteralNameAttributeValue().getValue());
    }

    @Test
    public void testGetEnumLiteralNameAttributeValue() {
        assertNull(genderEnumValueMale.getEnumLiteralNameAttributeValue());
        IEnumValue value = paymentMode.getEnumValues().get(0);
        assertEquals("MONTHLY", value.getEnumLiteralNameAttributeValue().getValue());
    }

}
