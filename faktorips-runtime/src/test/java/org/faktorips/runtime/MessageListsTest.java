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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.junit.Test;

public class MessageListsTest {

    private final MessageList emptyMessageList = new MessageList();

    @Test
    public void testSortBySeverity_NullMessageList() {
        assertThat(MessageLists.sortBySeverity(null), is(emptyMessageList));
    }

    @Test
    public void testSortBySeverity() {
        Message e1 = Message.newError("e1", "E1");
        Message e2 = Message.newError("e2", "E2");
        Message e3 = Message.newError("e3", "E3");
        Message w1 = Message.newWarning("w1", "W1");
        Message w2 = Message.newWarning("w2", "W2");
        Message i1 = Message.newInfo("i1", "I1");
        Message i2 = Message.newInfo("i2", "I2");
        MessageList messageList = MessageList.of(i2, e1, w1, e3, i1, e2, w2);
        MessageList sortedList = MessageLists.sortBySeverity(messageList);

        assertThat(sortedList.getMessage(0), is(e1));
        assertThat(sortedList.getMessage(1), is(e3));
        assertThat(sortedList.getMessage(2), is(e2));
        assertThat(sortedList.getMessage(3), is(w1));
        assertThat(sortedList.getMessage(4), is(w2));
        assertThat(sortedList.getMessage(5), is(i2));
        assertThat(sortedList.getMessage(6), is(i1));
    }

    public void testFiltered_NullMessageList() {
        assertThat(MessageLists.filtered(null, m -> true), is(emptyMessageList));
    }

    @Test
    public void testFiltered_SomeMessagesMatch() {
        Message e1 = Message.newError("e1", "E1");
        Message e2 = Message.newError("e2", "E2");
        Message e3 = Message.newError("e3", "E3");
        Message w1 = Message.newWarning("w1", "W1");
        Message w2 = Message.newWarning("w2", "W2");

        MessageList messageList = MessageList.of(e1, e2, e3, w1, w2);
        MessageList filteredList = MessageLists.filtered(messageList, m -> m.getCode().contains("2"));
        assertThat(filteredList.getMessage(0), is(e2));
        assertThat(filteredList.getMessage(1), is(w2));
    }

    @Test
    public void testFiltered_NoMessagesMatch() {
        Message m1 = Message.newError("e1", "E1");
        Message m2 = Message.newError("e2", "E2");

        MessageList messageList = MessageList.of(m1, m2);
        assertThat(MessageLists.filtered(messageList, m -> false), is(emptyMessageList));
    }

    @Test
    public void testFiltered_AllMessagesMatch() {
        Message m1 = Message.newError("e1", "E1");
        Message m2 = Message.newError("e2", "E2");

        MessageList messageList = MessageList.of(m1, m2);
        assertThat(MessageLists.filtered(messageList, m -> true), is(messageList));
    }

    @Test
    public void testOrEmptyMessageList_NullMessageList() {
        assertThat(MessageLists.orEmptyMessageList(null), is(emptyMessageList));
    }

    @Test
    public void testOrEmptyMessageList() {
        MessageList ml = new MessageList();
        assertThat(MessageLists.orEmptyMessageList(ml), is(sameInstance(ml)));
    }

    @Test
    public void testEmptyMessageList() {
        MessageList ml1 = MessageLists.emptyMessageList();
        MessageList ml2 = MessageLists.emptyMessageList();

        assertThat(ml1, is(notNullValue()));
        assertThat(ml2, is(notNullValue()));
        assertThat(ml1, is(not(sameInstance(ml2))));
    }

    @Test
    public void testFlatten_EmptyStream() {
        Stream<MessageList> empty = Stream.empty();
        MessageList collected = empty.collect(MessageLists.flatten());
        assertThat(collected, is(notNullValue()));
        assertThat(collected.size(), is(0));
    }

    @Test
    public void testFlatten_SeveralMessageLists() {
        Message m1 = Message.newError("1", "1");
        Message m2 = Message.newError("2", "2");
        Message m3 = Message.newError("3", "3");
        Message m4 = Message.newError("4", "4");
        MessageList ml1 = new MessageList(m1);
        MessageList ml2 = new MessageList(m2);
        MessageList ml3 = new MessageList();
        MessageList ml4 = MessageList.of(m3, m4);
        MessageList collected = Stream.of(ml1, ml2, ml3, ml4).collect(MessageLists.flatten());
        assertThat(collected.size(), is(4));
        assertThat(collected.getMessage(0), is(m1));
        assertThat(collected.getMessage(1), is(m2));
        assertThat(collected.getMessage(2), is(m3));
        assertThat(collected.getMessage(3), is(m4));
    }

    @Test
    public void testCollectMessages_SeveralMessages() {
        Message m1 = Message.newError("1", "1");
        Message m2 = Message.newError("2", "2");
        Message m3 = Message.newError("3", "3");
        Message m4 = Message.newError("4", "4");
        MessageList collected = Stream.of(m1, m2, m3, m4).collect(MessageLists.collectMessages());
        assertThat(collected.size(), is(4));
        assertThat(collected.getMessage(0), is(m1));
        assertThat(collected.getMessage(1), is(m2));
        assertThat(collected.getMessage(2), is(m3));
        assertThat(collected.getMessage(3), is(m4));
    }

    @Test
    public void testCollectMessages_EmptyStream() {
        Stream<Message> empty = Stream.empty();
        MessageList collected = empty.collect(MessageLists.collectMessages());
        assertThat(collected, is(notNullValue()));
        assertThat(collected.size(), is(0));
    }

    @Test
    public void testJoin_NullArray() {
        assertThat(MessageLists.join((MessageList[])null), is(emptyMessageList));
    }

    @Test
    public void testJoin_EmptyArray() {
        assertThat(MessageLists.join(), is(emptyMessageList));
    }

    @Test
    public void testJoin() {
        Message e1 = Message.newError("error", "error1");
        Message e2 = Message.newError("error", "error2");
        Message w1 = Message.newError("warning", "warning1");
        MessageList m1 = MessageList.of(e1, e2);
        MessageList m2 = MessageList.of(w1);
        assertThat(MessageLists.join(m1, m2), hasItems(e1, e2, w1));
    }

    @Test
    public void testJoin_Null() {
        Message e1 = Message.newError("error", "error1");
        Message e2 = Message.newError("error", "error2");
        Message w1 = Message.newError("warning", "warning1");
        MessageList m1 = MessageList.of(e1, e2);
        MessageList m2 = MessageList.of(w1);
        assertThat(MessageLists.join(m1, null, m2), hasItems(e1, e2, w1));
    }
}
