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

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.runtime.Message.Severity;
import org.junit.Test;

/**
 *
 */
public class MessageListTest {
    @Test
    public void testAddMessage() {
        MessageList list = new MessageList();

        Message msg1 = Message.newError("1", "blabla");
        list.add(msg1);
        assertEquals(1, list.size());
        assertTrue(msg1 == list.getMessage(0));

        Message msg2 = Message.newError("1", "blabla");
        list.add(msg2);
        assertEquals(2, list.size());
        assertTrue(msg2 == list.getMessage(1));
    }

    @Test
    public void testAddMessageList() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);

        MessageList list2 = new MessageList();
        Message msg2 = Message.newError("2", "blabla");
        list2.add(msg2);
        Message msg3 = Message.newError("3", "blabla");
        list2.add(msg3);

        list1.add(list2);
        assertEquals(3, list1.size());
        assertTrue(msg1 == list1.getMessage(0));
        assertTrue(msg2 == list1.getMessage(1));
        assertTrue(msg3 == list1.getMessage(2));

        MessageList nullList = null;
        list1.add(nullList); // should not throw an exception
        assertEquals(3, list1.size());
    }

    @Test
    public void testAddMessageListObjectProperty_OverrideTrue() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);

        MessageList list2 = new MessageList();
        Message msg2 = Message.newInfo("2", "blabla");
        list2.add(msg2);
        ObjectProperty objProp1 = new ObjectProperty(this, "property1");
        Message msg3 = new Message("", "msg2", Message.ERROR, objProp1);
        list2.add(msg3);

        ObjectProperty objProp2 = new ObjectProperty(this, "property2");
        list1.add(list2, objProp2, true);
        assertEquals(3, list1.size());
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().size());
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties().get(0));
        assertEquals(objProp2, list1.getMessage(2).getInvalidObjectProperties().get(0));

        assertEquals(msg3.getCode(), list1.getMessage(2).getCode());
        assertEquals(msg3.getText(), list1.getMessage(2).getText());
        assertEquals(msg3.getSeverity(), list1.getMessage(2).getSeverity());
    }

    @Test
    public void testAddMessageListObjectProperty_OverrideFalse() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);

        MessageList list2 = new MessageList();
        Message msg2 = Message.newError("2", "blabla");
        list2.add(msg2);
        ObjectProperty objProp1 = new ObjectProperty(this, "property1");
        Message msg3 = new Message("", "msg2", Message.ERROR, objProp1);
        list2.add(msg3);

        ObjectProperty objProp2 = new ObjectProperty(this, "property2");
        list1.add(list2, objProp2, false);
        assertEquals(3, list1.size());
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().size());
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties().get(0));
        // message 3 should remain untouched, as the invalid object properties were set!
        assertEquals(objProp1, list1.getMessage(2).getInvalidObjectProperties().get(0));
    }

    @Test
    public void testsize() {
        MessageList list = new MessageList();
        assertEquals(0, list.size());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(1, list.size());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(2, list.size()); // messages are equal, but the list does not filter
        // duplicate messages
    }

    @Test
    public void testGetMessage() {
        MessageList list = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("1", "blabla");
        list.add(msg1);
        list.add(msg2);

        assertTrue(msg1 == list.getMessage(0));
        assertTrue(msg2 == list.getMessage(1));

        try {
            list.getMessage(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // ok
        }
    }

    @Test
    public void testGetMessageByCode() {
        MessageList list = new MessageList();
        assertNull(list.getMessageByCode("1"));

        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("2", "blabla");
        Message msg3 = Message.newError("2", "blabla");
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);

        assertTrue(msg1 == list.getMessageByCode("1"));
        assertTrue(msg2 == list.getMessageByCode("2"));
        assertNull(list.getMessageByCode("3"));
    }

    @Test
    public void testGetMessagesByCode() {
        MessageList list = new MessageList();
        assertTrue(list.getMessagesByCode(null).isEmpty());
        assertTrue(list.getMessagesByCode("1").isEmpty());

        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("2", "blabla");
        Message msg3 = Message.newError("2", "blabla");
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);

        MessageList sublist = list.getMessagesByCode("1");
        assertEquals(1, sublist.size());
        assertSame(msg1, sublist.getMessage(0));

        sublist = list.getMessagesByCode("2");
        assertEquals(2, sublist.size());
        assertSame(msg2, sublist.getMessage(0));
        assertSame(msg3, sublist.getMessage(1));

        sublist = list.getMessagesByCode("unknown");
        assertEquals(0, sublist.size());
    }

    @Test
    public void testGetSeverity() {
        MessageList list = new MessageList();
        assertEquals(Severity.NONE, list.getSeverity());

        list.add(Message.newInfo("1", "blabla"));
        assertEquals(Severity.INFO, list.getSeverity());

        list.add(Message.newWarning("1", "blabla"));
        assertEquals(Severity.WARNING, list.getSeverity());

        list.add(Message.newError("1", "blabla"));
        assertEquals(Severity.ERROR, list.getSeverity());
    }

    @Test
    public void testContainsErrorMsg() {
        MessageList list = new MessageList();
        assertFalse(list.containsErrorMsg());

        list.add(Message.newInfo("1", "blabla"));
        assertFalse(list.containsErrorMsg());

        list.add(Message.newWarning("1", "blabla"));
        assertFalse(list.containsErrorMsg());

        list.add(Message.newError("1", "blabla"));
        assertTrue(list.containsErrorMsg());
    }

    @Test
    public void testToString() {
        MessageList list = new MessageList();
        list.toString();
        list.add(Message.newInfo("1", "blabla"));
        list.toString();
    }

    @Test
    public void testEquals() {
        MessageList list = new MessageList();
        list.add(Message.newInfo("1", "blabla"));
        list.add(Message.newInfo("2", "Hello World"));

        assertFalse(list.equals(null));
        assertFalse(list.equals(this));

        MessageList list2 = new MessageList();
        list2.add(Message.newInfo("1", "blabla"));
        assertFalse(list.equals(list2));

        list2.add(Message.newInfo("2", "Hello World"));
        assertTrue(list.equals(list2));
    }

    @Test
    public void testGetMessagesFor() {
        MessageList list = new MessageList();
        Message msg1 = new Message("1", "text1", Message.ERROR, this, "name");
        Message msg2 = new Message("2", "text2", Message.ERROR, "invalid object", "name");
        Message msg3 = new Message("2", "text3", Message.ERROR, this, "descriptopm");
        Message msg4 = new Message("2", "text4", Message.ERROR, new ObjectProperty[] {
                new ObjectProperty(this, "description"), new ObjectProperty(this, "name") });
        Message msg5 = new Message("5", "text5", Message.ERROR, new ObjectProperty[] {
                new ObjectProperty(this, "index", 1), new ObjectProperty(this, "index", 2) });
        Message msg6 = new Message("6", "text6", Message.ERROR, new ObjectProperty[] {
                new ObjectProperty(this, "index", 3), new ObjectProperty(this, "index", 4) });

        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);
        list.add(msg5);
        list.add(msg6);

        // the object/property variant
        MessageList result = list.getMessagesFor(this, "name");
        assertEquals(2, result.size());
        assertEquals(msg1, result.getMessage(0));
        assertEquals(msg4, result.getMessage(1));

        // the object/any property variant
        result = list.getMessagesFor(this);
        assertEquals(5, result.size());
        assertEquals(msg1, result.getMessage(0));
        assertEquals(msg3, result.getMessage(1));
        assertEquals(msg4, result.getMessage(2));
        assertEquals(msg5, result.getMessage(3));
        assertEquals(msg6, result.getMessage(4));

        // the object/property/index property variant
        result = list.getMessagesFor(this, "index", 2);
        assertEquals(1, result.size());
        assertEquals(msg5, result.getMessage(0));

        // empty list
        result = list.getMessagesFor(result, "name");
        assertEquals(0, result.size());
    }

}
