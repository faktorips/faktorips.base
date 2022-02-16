/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Predicate;

import org.junit.Test;

public class MessageListTest {

    private final Predicate<IMarker> requiredInformationMarker = IMarker::isRequiredInformationMissing;
    private final Predicate<IMarker> technicalConstraintViolatedInstance = TechnicalConstraintViolated.class::isInstance;
    private final MessageList emptyMessageList = new MessageList();

    @Test
    public void testOf_NullArray() {
        assertThat(MessageList.of((Message[])null), is(emptyMessageList));
    }

    @Test
    public void testOf_EmptyArray() {
        assertThat(MessageList.of(), is(emptyMessageList));
        assertThat(MessageList.of(), is(emptyMessageList));
    }

    @Test
    public void testOf() {
        Message m1 = Message.newError("error", "error");
        Message m2 = Message.newWarning("warning", "warning");
        assertThat(MessageList.of(m1, m2), hasItems(m1, m2));
    }

    @Test
    public void testOfErrors_NullStringArray() {
        assertThat(MessageList.ofErrors((String[])null), is(emptyMessageList));
    }

    @Test
    public void testOfErrors_EmptyStringArray() {
        assertThat(MessageList.ofErrors(), is(emptyMessageList));
    }

    @Test
    public void testOfErrors() {
        MessageList errorList = MessageList.ofErrors("foo", "bar");
        assertThat(errorList.size(), is(2));
        assertThat(errorList.getMessage(0), is(new Message("foo", Severity.ERROR)));
        assertThat(errorList.getMessage(1), is(new Message("bar", Severity.ERROR)));
    }

    @Test
    public void testNewError() {
        MessageList list = new MessageList();
        Message error = list.newError("code", "text", this, "foo");

        assertSame(error, list.getFirstMessage(Message.ERROR));
        assertEquals("code", list.getFirstMessage(Message.ERROR).getCode());
        assertEquals("text", list.getFirstMessage(Message.ERROR).getText());
        assertEquals(new ObjectProperty(this, "foo"),
                list.getFirstMessage(Message.ERROR).getInvalidObjectProperties().get(0));
    }

    @Test
    public void testNewWarning() {
        MessageList list = new MessageList();
        Message warning = list.newWarning("code", "text", this, "foo");

        assertSame(warning, list.getFirstMessage(Message.WARNING));
        assertEquals("code", list.getFirstMessage(Message.WARNING).getCode());
        assertEquals("text", list.getFirstMessage(Message.WARNING).getText());
        assertEquals(new ObjectProperty(this, "foo"), list.getFirstMessage(Message.WARNING)
                .getInvalidObjectProperties().get(0));
    }

    @Test
    public void testNewInfo() {
        MessageList list = new MessageList();
        Message info = list.newInfo("code", "text", this, "foo");

        assertSame(info, list.getFirstMessage(Message.INFO));
        assertEquals("code", list.getFirstMessage(Message.INFO).getCode());
        assertEquals("text", list.getFirstMessage(Message.INFO).getText());
        assertEquals(new ObjectProperty(this, "foo"),
                list.getFirstMessage(Message.INFO).getInvalidObjectProperties().get(0));
    }

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
    public void testAddMessage_null() {
        MessageList list = new MessageList();

        Message msg1 = Message.newError("1", "blabla");
        list.add(msg1);
        assertEquals(1, list.size());
        assertTrue(msg1 == list.getMessage(0));

        list.add((Message)null);
        assertEquals(1, list.size());
        assertTrue(msg1 == list.getMessage(0));
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
    public void testRemove() {
        MessageList list = new MessageList();
        list.add(Message.newError("code", "text"));
        list.remove(new Message("code", "text", Message.ERROR));

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
    public void testSize() {
        MessageList list = new MessageList();
        assertEquals(0, list.size());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(1, list.size());
        list.add(Message.newInfo("1", "blabla"));
        // messages are equal, but the list does not filter duplicate messages
        assertEquals(2, list.size());
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
            // OK
        }
    }

    @Test
    public void testGetMessageByCode() {
        MessageList list = new MessageList();
        assertNull(list.getMessageByCode("1"));

        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("2", "blabla");
        Message msg3 = Message.newError("2", "blabla");
        Message msg4 = Message.newError(null, "blabla");
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);

        assertSame(msg1, list.getMessageByCode("1"));
        assertSame(msg2, list.getMessageByCode("2"));
        assertNull(list.getMessageByCode("3"));
        assertSame(msg4, list.getMessageByCode(null));
    }

    @Test
    public void testGetMessagesByCode() {
        MessageList list = new MessageList();
        assertTrue(list.getMessagesByCode(null).isEmpty());
        assertTrue(list.getMessagesByCode("1").isEmpty());

        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("2", "blabla");
        Message msg3 = Message.newError("2", "blabla");
        Message msg4 = Message.newError(null, "blabla");
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);

        MessageList messagesWithCode = list.getMessagesByCode("1");
        assertEquals(1, messagesWithCode.size());
        assertSame(msg1, messagesWithCode.getMessage(0));

        messagesWithCode = list.getMessagesByCode("2");
        assertEquals(2, messagesWithCode.size());
        assertSame(msg2, messagesWithCode.getMessage(0));
        assertSame(msg3, messagesWithCode.getMessage(1));

        messagesWithCode = list.getMessagesByCode("unknown");
        assertEquals(0, messagesWithCode.size());

        messagesWithCode = list.getMessagesByCode(null);
        assertEquals(1, messagesWithCode.size());
        assertSame(msg4, messagesWithCode.getMessage(0));
    }

    @Test
    public void testGetMessagesByMarker() {
        MessageList list = new MessageList();
        IMarker marker1 = new RequiredInformationMissing();
        IMarker marker2 = new TechnicalConstraintViolated();
        IMarker marker3 = new TestMarker();
        assertTrue(list.getMessagesByMarker(marker1).isEmpty());

        Message msg1 = new Message.Builder("foo", Severity.ERROR).markers(marker1).create();
        Message msg2 = new Message.Builder("bar", Severity.ERROR).markers(marker2).create();
        Message msg3 = new Message.Builder("baz", Severity.ERROR).markers(marker1, marker2).create();
        Message msg4 = new Message.Builder("foobar", Severity.ERROR).create();
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);

        MessageList messagesWithMarker1 = list.getMessagesByMarker(marker1);
        assertEquals(2, messagesWithMarker1.size());
        assertSame(msg1, messagesWithMarker1.getMessage(0));
        assertSame(msg3, messagesWithMarker1.getMessage(1));

        MessageList messagesWithMarker2 = list.getMessagesByMarker(marker2);
        assertEquals(2, messagesWithMarker2.size());
        assertSame(msg2, messagesWithMarker2.getMessage(0));
        assertSame(msg3, messagesWithMarker2.getMessage(1));

        MessageList messagesWithMarker3 = list.getMessagesByMarker(marker3);
        assertEquals(0, messagesWithMarker3.size());

        MessageList messagesWithOutMarker = list.getMessagesByMarker((IMarker)null);
        assertEquals(1, messagesWithOutMarker.size());
        assertSame(msg4, messagesWithOutMarker.getMessage(0));

    }

    @Test
    public void testGetMessagesByMarker_withPredicate() {
        MessageList list = new MessageList();
        IMarker marker1 = new RequiredInformationMissing();
        IMarker marker2 = new TechnicalConstraintViolated();
        IMarker marker3 = new TestMarker(false, false);
        IMarker marker3_technical = new TestMarker(false, true);
        IMarker marker3_required = new TestMarker(true, false);
        assertTrue(list.getMessagesByMarker(technicalConstraintViolatedInstance).isEmpty());

        Message msg0 = new Message.Builder("foobar", Severity.ERROR).create();
        Message msg1 = new Message.Builder("foo", Severity.ERROR).markers(marker1).create();
        Message msg2 = new Message.Builder("bar", Severity.ERROR).markers(marker3_technical).create();
        Message msg3 = new Message.Builder("baz", Severity.ERROR).markers(marker2, marker3).create();
        Message msg4 = new Message.Builder("bat", Severity.ERROR).markers(marker3_required).create();
        list.add(msg0);
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);

        MessageList messagesWithRequiredInformationMarker = list.getMessagesByMarker(requiredInformationMarker);
        assertEquals(2, messagesWithRequiredInformationMarker.size());
        assertSame(msg1, messagesWithRequiredInformationMarker.getMessage(0));
        assertSame(msg4, messagesWithRequiredInformationMarker.getMessage(1));

        MessageList messagesWithTechnicalConstraintMarker = list
                .getMessagesByMarker(technicalConstraintViolatedInstance);
        assertEquals(1, messagesWithTechnicalConstraintMarker.size());
        assertSame(msg3, messagesWithTechnicalConstraintMarker.getMessage(0));
    }

    @Test
    public void testGetMessagesByMarker_withPredicate_noDuplicates() {
        MessageList list = new MessageList();
        IMarker marker1 = new RequiredInformationMissing();
        IMarker marker2 = new TechnicalConstraintViolated();
        IMarker marker3 = new TestMarker(false, false);
        assertTrue(list.getMessagesByMarker(technicalConstraintViolatedInstance).isEmpty());

        Message msg0 = new Message.Builder("foobar", Severity.ERROR).create();
        Message msg1 = new Message.Builder("foo", Severity.ERROR).markers(marker1).create();
        Message msg2 = new Message.Builder("bar", Severity.ERROR).markers(marker2, marker3).create();
        list.add(msg0);
        list.add(msg1);
        list.add(msg2);

        MessageList messagesWithAnyMarker = list.getMessagesByMarker((Predicate<IMarker>)t -> true);
        assertEquals(2, messagesWithAnyMarker.size());
        assertSame(msg1, messagesWithAnyMarker.getMessage(0));
        assertSame(msg2, messagesWithAnyMarker.getMessage(1));
    }

    @Test(expected = NullPointerException.class)
    public void testGetMessagesByMarker_withPredicate_null() {
        MessageList list = new MessageList();
        list.getMessagesByMarker((Predicate<IMarker>)null);
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

    @SuppressWarnings("unlikely-arg-type")
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
        Message msg4 = new Message("2", "text4", Message.ERROR, new ObjectProperty(this, "description"),
                new ObjectProperty(this, "name"));
        Message msg5 = new Message("5", "text5", Message.ERROR, new ObjectProperty(this, "index", 1),
                new ObjectProperty(this, "index", 2));
        Message msg6 = new Message("6", "text6", Message.ERROR, new ObjectProperty(this, "index", 3),
                new ObjectProperty(this, "index", 4));

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
    public void testGetMessageWithHighestSeverity_empty() {
        MessageList list = new MessageList();

        assertNull(list.getMessageWithHighestSeverity());
    }

    @Test
    public void testGetMessageWithHighestSeverity() {
        MessageList list = new MessageList();
        Message error1 = Message.newError(null, "e1");
        Message error2 = Message.newError(null, "e2");
        Message warning1 = Message.newWarning(null, "w1");
        Message warning2 = Message.newWarning(null, "w2");
        Message info1 = Message.newInfo(null, "i1");
        Message info2 = Message.newInfo(null, "i2");
        Message none1 = new Message("n1", Severity.NONE);
        Message none2 = new Message("n2", Severity.NONE);

        list.add(none1);
        list.add(none2);

        assertSame(none1, list.getMessageWithHighestSeverity());

        list.add(info2);
        list.add(info1);

        assertSame(info2, list.getMessageWithHighestSeverity());

        list.add(warning1);
        list.add(warning2);

        assertSame(warning1, list.getMessageWithHighestSeverity());

        list.add(error1);
        list.add(error2);

        assertSame(error1, list.getMessageWithHighestSeverity());
    }

    @Test
    public void testStream() {
        MessageList list = new MessageList();
        Message error1 = Message.newError(null, "e1");
        Message error2 = Message.newError(null, "e2");
        list.add(error1);
        list.add(error2);

        assertThat(list.stream().findFirst().get(), is(error1));
        assertThat(list.stream().filter(m -> "e2".equals(m.getText())).findFirst().get(), is(error2));
    }

    @Test
    public void testMap() {
        MessageList list = new MessageList();
        Message error1 = Message.newError("error", "e1");
        Message error2 = Message.newError("error", "e2");
        Message warning1 = Message.newWarning("warning", "w1");
        list.add(error1);
        list.add(error2);
        list.add(warning1);

        MessageList transformedList = list.map(m -> new Message.Builder(m).text("transformed text").create());

        assertThat(transformedList.getMessage(0), is(new Message("error", "transformed text", Severity.ERROR)));
        assertThat(transformedList.getMessage(1), is(new Message("error", "transformed text", Severity.ERROR)));
        assertThat(transformedList.getMessage(2), is(new Message("warning", "transformed text", Severity.WARNING)));
    }

    @Test
    public void testMap_WithPredicate() {
        MessageList list = new MessageList();
        Message error1 = Message.newError("error", "e1");
        Message error2 = Message.newError("error", "e2");
        Message warning1 = Message.newWarning("warning", "w1");
        list.add(error1);
        list.add(error2);
        list.add(warning1);

        MessageList transformedList = list.map(m -> m.getSeverity().equals(Severity.WARNING),
                m -> new Message.Builder(m).text("transformed text").create());

        assertThat(transformedList.getMessage(0), is(new Message("error", "e1", Severity.ERROR)));
        assertThat(transformedList.getMessage(1), is(new Message("error", "e2", Severity.ERROR)));
        assertThat(transformedList.getMessage(2), is(new Message("warning", "transformed text", Severity.WARNING)));
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
        assertEquals(3, list.getMessageByCode("code").getInvalidObjectProperties().size());
        assertEquals(2, list.getMessage(1).getInvalidObjectProperties().size());
    }

    private static final class RequiredInformationMissing implements IMarker {

        @Override
        public boolean isRequiredInformationMissing() {
            return true;
        }

        @Override
        public boolean isTechnicalConstraintViolated() {
            return false;
        }
    }

    private static final class TechnicalConstraintViolated implements IMarker {

        @Override
        public boolean isRequiredInformationMissing() {
            return false;
        }

        @Override
        public boolean isTechnicalConstraintViolated() {
            return true;
        }
    }

    private static final class TestMarker implements IMarker {
        private final Boolean isRequiredInformationMissing;
        private final boolean isTechnicalConstraintViolated;

        public TestMarker() {
            isRequiredInformationMissing = false;
            isTechnicalConstraintViolated = false;
        }

        public TestMarker(boolean isRequiredInformationMissing, boolean isTechnicalConstraintViolated) {
            this.isRequiredInformationMissing = isRequiredInformationMissing;
            this.isTechnicalConstraintViolated = isTechnicalConstraintViolated;
        }

        @Override
        public boolean isRequiredInformationMissing() {
            return isRequiredInformationMissing;
        }

        @Override
        public boolean isTechnicalConstraintViolated() {
            return isTechnicalConstraintViolated;
        }
    }

}
