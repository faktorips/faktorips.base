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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
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
    public void testSetValue() {
        literalNameAttributeValue.setValue("foo");
        assertEquals("FOO", literalNameAttributeValue.getValue());
    }

    @Test
    public void testSetValueNull() {
        literalNameAttributeValue.setValue(null);
        assertNull(literalNameAttributeValue.getValue());
    }

    @Test
    public void testSetValueInvalidCharacters() {
        literalNameAttributeValue.setValue("foo $$%bar");
        assertEquals("FOO____BAR", literalNameAttributeValue.getValue());
    }

    @Test
    public void testSetValueUmlaut() {
        literalNameAttributeValue.setValue("fooÄbar");
        assertEquals("FOOAEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooäbar");
        assertEquals("FOOAEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooÖbar");
        assertEquals("FOOOEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooöbar");
        assertEquals("FOOOEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooÜbar");
        assertEquals("FOOUEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooübar");
        assertEquals("FOOUEBAR", literalNameAttributeValue.getValue());

        literalNameAttributeValue.setValue("fooßbar");
        assertEquals("FOOSSBAR", literalNameAttributeValue.getValue());
    }

    @Test
    public void testGetName() {
        literalNameAttributeValue.setValue("FOOBAR");
        assertEquals("FOOBAR", literalNameAttributeValue.getName());
    }

    @Test
    public void testValidateNumber() throws CoreException {
        literalNameAttributeValue.setValue("42");
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        Message message = messages.getFirstMessage(Message.ERROR);
        assertEquals(message.getCode(),
                IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NUMBER);
        assertEquals(message.getInvalidObjectProperties()[0], new ObjectProperty(literalNameAttributeValue,
                IEnumAttributeValue.PROPERTY_VALUE));
    }

    @Test
    public void testValidateNumberInName() throws CoreException {
        literalNameAttributeValue.setValue("a42b");
        assertTrue(literalNameAttributeValue.isValid(ipsProject));
    }

    @Test
    public void testValidateLeadingNumber() throws CoreException {
        literalNameAttributeValue.setValue("42ab");
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        Message message = messages.getFirstMessage(Message.ERROR);
        assertEquals(message.getCode(),
                IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NUMBER);
        assertEquals(message.getInvalidObjectProperties()[0], new ObjectProperty(literalNameAttributeValue,
                IEnumAttributeValue.PROPERTY_VALUE));
    }

    @Test
    public void testValidateNull() throws CoreException {
        literalNameAttributeValue.setValue(null);
        MessageList messages = literalNameAttributeValue.validate(ipsProject);
        assertEquals(1, messages.getNoOfMessages(Message.ERROR));
        assertNull(messages
                .getMessageByCode(IEnumLiteralNameAttributeValue.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_VALUE_IS_NUMBER));
    }

}
