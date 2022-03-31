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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

public class EnumLiteralNameAttributeValueTest extends AbstractIpsEnumPluginTest {

    private IEnumLiteralNameAttributeValue literalNameAttributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        literalNameAttributeValue = (IEnumLiteralNameAttributeValue)paymentMode.getEnumValues().get(0)
                .getEnumAttributeValues().get(0);
    }

    @Test
    public void testGetName() {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue("FOOBAR"));
        assertEquals("FOOBAR", literalNameAttributeValue.getName());
    }

    @Test
    public void testValidateNumber() {
        expectInvalidLiteralNameValidationMessage("42");
    }

    @Test
    public void testValidateNumberNotAtBeginning() {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue("foo12bar"));
        assertTrue(literalNameAttributeValue.isValid(ipsProject));
    }

    @Test
    public void testValidateSpecialCharacter() {
        expectInvalidLiteralNameValidationMessage("foo%% bar &//");
    }

    private void expectInvalidLiteralNameValidationMessage(String value) {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue(value));
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        Message message = messages.getFirstMessage(Message.ERROR);
        assertEquals(message.getCode(),
                IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER);
        assertEquals(message.getInvalidObjectProperties().get(0), new ObjectProperty(literalNameAttributeValue,
                IEnumAttributeValue.PROPERTY_VALUE));
    }

    @Test
    public void testValidateNumberInName() {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue("a42b"));
        assertTrue(literalNameAttributeValue.isValid(ipsProject));
    }

    @Test
    public void testValidateLeadingNumber() {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue("42ab"));
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        Message message = messages.getFirstMessage(Message.ERROR);
        assertEquals(message.getCode(),
                IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER);
        assertEquals(message.getInvalidObjectProperties().get(0), new ObjectProperty(literalNameAttributeValue,
                IEnumAttributeValue.PROPERTY_VALUE));
    }

    @Test
    public void testValidateNull() {
        literalNameAttributeValue.setValue(ValueFactory.createStringValue(null));
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        assertNull(messages
                .getMessageByCode(
                        IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NO_VALID_JAVA_IDENTIFIER));
    }

}
