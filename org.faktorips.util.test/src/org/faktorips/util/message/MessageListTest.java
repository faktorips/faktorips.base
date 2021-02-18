/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.message;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.faktorips.util.message.MessageList.SeverityComparator;
import org.junit.Test;

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
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().length);
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties()[0]);
        assertEquals(objProp2, list1.getMessage(2).getInvalidObjectProperties()[0]);

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
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().length);
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties()[0]);
        // message 3 should remain untouched, as the invalid object properties were set!
        assertEquals(objProp1, list1.getMessage(2).getInvalidObjectProperties()[0]);
    }

    @Test
    public void testRemove() {
        MessageList list = new MessageList();
        list.remove(list.newInfo("code", "text", null, null));

        assertNull(list.getMessageByCode("code"));
    }

    @Test
    public void testRemove_DoNothingIfMessageNotInList() {
        MessageList list = new MessageList();
        list.remove(new Message("code", "text", Message.ERROR));

        assertNull(list.getMessageByCode("code"));
    }

    @Test
    public void testRemove_DoNotThrowExceptionIfNullIsGiven() {
        new MessageList().remove(null);
    }

    @Test
    public void testGetNoOfMessages() {
        MessageList list = new MessageList();
        assertEquals(0, list.size());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(1, list.size());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(2, list.size()); // messages are equal, but the list does not filter
        // duplicate messages

        assertEquals(0, list.getNoOfMessages(Message.ERROR));
        assertEquals(0, list.getNoOfMessages(Message.WARNING));
        assertEquals(2, list.getNoOfMessages(Message.INFO));
        assertEquals(0, list.getNoOfMessages(Message.NONE));
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
            // An exception is expected to be thrown.
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
    public void testGetSeverity() {
        MessageList list = new MessageList();
        assertEquals(0, list.getSeverity());

        list.add(Message.newInfo("1", "blabla"));
        assertEquals(Message.INFO, list.getSeverity());

        list.add(Message.newWarning("1", "blabla"));
        assertEquals(Message.WARNING, list.getSeverity());

        list.add(Message.newError("1", "blabla"));
        assertEquals(Message.ERROR, list.getSeverity());
    }

    @Test
    public void testGetMessageWithHighestSeverity() {
        MessageList list = new MessageList();
        assertNull(list.getMessageWithHighestSeverity());

        Message info = Message.newInfo("1", "some text");
        list.add(info);
        assertSame(info, list.getMessageWithHighestSeverity());

        Message warn = Message.newWarning("1", "some text");
        list.add(warn);
        assertSame(warn, list.getMessageWithHighestSeverity());

        Message error = Message.newError("1", "some text");
        list.add(error);
        assertSame(error, list.getMessageWithHighestSeverity());

        Message warn2 = Message.newWarning("1", "some text");
        list.add(warn2);
        assertSame(error, list.getMessageWithHighestSeverity());
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

    @Test
    public void testNewError() {
        MessageList list = new MessageList();
        Message error = list.newError("code", "text", this, "foo");

        assertSame(error, list.getFirstMessage(Message.ERROR));
        assertEquals("code", list.getFirstMessage(Message.ERROR).getCode());
        assertEquals("text", list.getFirstMessage(Message.ERROR).getText());
        assertEquals(new ObjectProperty(this, "foo"),
                list.getFirstMessage(Message.ERROR).getInvalidObjectProperties()[0]);
    }

    @Test
    public void testNewWarning() {
        MessageList list = new MessageList();
        Message warning = list.newWarning("code", "text", this, "foo");

        assertSame(warning, list.getFirstMessage(Message.WARNING));
        assertEquals("code", list.getFirstMessage(Message.WARNING).getCode());
        assertEquals("text", list.getFirstMessage(Message.WARNING).getText());
        assertEquals(new ObjectProperty(this, "foo"), list.getFirstMessage(Message.WARNING)
                .getInvalidObjectProperties()[0]);
    }

    @Test
    public void testNewInfo() {
        MessageList list = new MessageList();
        Message info = list.newInfo("code", "text", this, "foo");

        assertSame(info, list.getFirstMessage(Message.INFO));
        assertEquals("code", list.getFirstMessage(Message.INFO).getCode());
        assertEquals("text", list.getFirstMessage(Message.INFO).getText());
        assertEquals(new ObjectProperty(this, "foo"),
                list.getFirstMessage(Message.INFO).getInvalidObjectProperties()[0]);
    }

    @Test
    public void testWrapUpMessageList() {
        MessageList list = new MessageList();
        list.newError("code", "text", new ObjectProperty("1", "foo"), "foo");
        list.newError("code", "text", new ObjectProperty("2", "foo"), "foo");
        list.newError("code", "text", new ObjectProperty("3", "foo"), "foo");
        list.newError("code1", "text", new ObjectProperty("4", "foo"), "foo");
        list.newError("code", "text1", new ObjectProperty("5", "foo"), "foo");
        list.newError("code", "text1", new ObjectProperty("6", "foo"), "foo");

        list.wrapUpMessages("code");
        assertEquals(2, list.size());
        assertEquals(3, list.getMessageByCode("code").getInvalidObjectProperties().length);
        assertEquals(2, list.getMessage(1).getInvalidObjectProperties().length);
    }

    @Test
    public void testGetSublist_CopyFunctionality() {
        MessageList messageList = new MessageList();

        messageList.add(new Message("warning1", "warning1", Message.WARNING));
        messageList.add(new Message("info1", "info1", Message.INFO));
        messageList.add(new Message("none", "none", Message.NONE));

        MessageList subList = messageList.getSubList(5);
        assertSame(subList.size(), messageList.size());

        Message message = new Message("error1", "error1", Message.WARNING);
        subList.add(message);

        assertNotSame(subList.size(), messageList.size());
    }

    @Test
    public void testComparator() {
        List<Message> messageList = new ArrayList<Message>();
        messageList.add(new Message("warning1", "warning1", Message.WARNING));
        messageList.add(new Message("info1", "info1", Message.INFO));
        messageList.add(new Message("none", "none", Message.NONE));
        messageList.add(new Message("info2", "info2", Message.INFO));
        messageList.add(new Message("err1", "err1", Message.ERROR));
        messageList.add(new Message("err2", "err2", Message.ERROR));
        messageList.add(new Message("err3", "err3", Message.ERROR));

        Collections.sort(messageList, new SeverityComparator());

        assertEquals("err1", messageList.get(0).getText());
        assertEquals("err3", messageList.get(2).getText());
        assertEquals("warning1", messageList.get(3).getText());
        assertEquals("info2", messageList.get(5).getText());
        assertEquals("none", messageList.get(messageList.size() - 1).getText());
    }

    @Test
    public void testGetSubList() throws Exception {
        MessageList messageList = new MessageList();
        Message msg1 = new Message("err1", "err1", Message.ERROR);
        messageList.add(msg1);
        Message msg2 = new Message("err2", "err2", Message.ERROR);
        messageList.add(msg2);
        messageList.add(new Message("err3", "err3", Message.ERROR));

        MessageList subList = messageList.getSubList(2);

        assertEquals(2, subList.size());
        assertEquals(msg1, subList.getMessage(0));
        assertEquals(msg2, subList.getMessage(1));
    }

    @Test
    public void testGetSubList_WithSort() throws Exception {
        MessageList messageList = new MessageList();
        Message msg1 = new Message("err1", "err1", Message.ERROR);
        messageList.add(msg1);
        Message msg2 = new Message("warning1", "warning1", Message.WARNING);
        messageList.add(msg2);
        Message msg3 = new Message("err3", "err3", Message.ERROR);
        messageList.add(msg3);

        MessageList subList = messageList.getSubList(2);

        assertEquals(2, subList.size());
        assertEquals(msg1, subList.getMessage(0));
        assertEquals(msg3, subList.getMessage(1));
    }

    @Test
    public void testStream() {
        MessageList list = new MessageList();
        Message error1 = Message.newError(null, "e1");
        Message error2 = Message.newError(null, "e2");
        list.add(error1);
        list.add(error2);

        assertThat(error1.equals(error2), is(false));
        assertThat(list.stream().findFirst().get(), is(error1));
        assertThat(list.stream().filter(m -> "e2".equals(m.getText())).findFirst().get(), is(error2));
    }

    @Test
    public void testMessageList_of() {
        Message e1 = Message.newError(null, "e1");
        Message i1 = Message.newInfo(null, "i1");

        MessageList list = MessageList.of(e1, i1);

        assertThat(list.isEmpty(), is(false));
        assertThat(list, hasItems(e1, i1));
    }

    @Test
    public void testMessageList_of_Null() {
        Message[] msgs = null;
        MessageList list = MessageList.of(msgs);

        assertThat(list.isEmpty(), is(true));
        assertThat(list.containsErrorMsg(), is(false));
    }

    @Test
    public void testMessageList_of_empty() {
        MessageList list = MessageList.of(new Message[0]);

        assertThat(list.isEmpty(), is(true));
        assertThat(list.containsErrorMsg(), is(false));
    }

    @Test
    public void testMessageList_ofErrors() {
        MessageList list = MessageList.ofErrors("e1", "e2", "e3");

        assertThat(list.isEmpty(), is(false));
        assertThat(list.size(), is(3));
        assertThat(list,
                hasItems(Message.newError(null, "e1"), Message.newError(null, "e3"), Message.newError(null, "e2")));
    }

    @Test
    public void testMessageList_ofErrors_null() {
        String[] input = null;
        MessageList list = MessageList.ofErrors(input);

        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void testMessageList_ofErrors_empty() {
        MessageList list = MessageList.ofErrors(new String[0]);

        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void testMessageList_MessageCollector() {
        MessageList list = MessageList.ofErrors("e1", "e2", "e3");

        MessageList newList = list.stream().collect(MessageList.collectMessages());

        assertThat(newList.isEmpty(), is(false));
        assertThat(newList.size(), is(3));
        assertThat(newList,
                hasItems(Message.newError(null, "e1"), Message.newError(null, "e3"), Message.newError(null, "e2")));
    }

    @Test
    public void testMessageList_MessageListCollector() {
        Message e1 = Message.newError(null, "e1");
        Message e2 = Message.newError(null, "e2");
        Message w1 = Message.newWarning(null, "w1");
        Message i1 = Message.newInfo(null, "i1");
        MessageList errorList = MessageList.of(e1, e2);
        MessageList otherList = MessageList.of(w1, i1);

        MessageList combined = Stream.of(errorList, otherList).collect(MessageList.flatten());

        assertThat(combined.size(), is(4));
        assertThat(combined, hasItems(e1, e2, w1, i1));
    }
}
