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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.faktorips.runtime.util.AbstractMessageList;

/**
 * A list of {@link Message Messages}.
 * 
 * @see Message
 */
public class MessageList extends AbstractMessageList<Message, MessageList> {

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        super();
    }

    /**
     * Creates a message list that contains the given message. <code>null</code> will be ignored as
     * a parameter value.
     */
    public MessageList(Message msg) {
        super(msg);
    }

    @Override
    protected MessageList createEmptyMessageList() {
        return new MessageList();
    }

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param objectPropertyMap The <code>Map</code> between old and new <code>ObjectProperty</code>
     * @return MessageList
     */
    public MessageList createCopy(Map<ObjectProperty, ObjectProperty> objectPropertyMap) {
        if (isEmpty()) {
            return this;
        }
        MessageList newList = new MessageList();
        for (Message message : getMessages()) {
            newList.add(message.createCopy(objectPropertyMap));
        }
        return newList;
    }

    /**
     * Creates and returns a new message with severity {@link Message#ERROR} with the given code,
     * text and object properties and adds the message to the list.
     */
    public Message newError(String code, String text, Object invalidObject, String... invalidProperties) {
        Message newError = Message.newError(code, text, invalidObject, invalidProperties);
        add(newError);
        return newError;
    }

    /**
     * Creates and returns a new message with severity {@link Message#ERROR} with the given code,
     * text and invalid object properties and adds the message to the list.
     */
    public Message newError(String code, String text, ObjectProperty... invalidObjectProperty) {
        Message newError = Message.newError(code, text, invalidObjectProperty);
        add(newError);
        return newError;
    }

    /**
     * Creates and returns a new message with severity {@link Message#WARNING} with the given code,
     * text and object properties and adds the message to the list.
     */
    public Message newWarning(String code, String text, Object invalidObject, String... invalidProperties) {
        Message newWarning = Message.newWarning(code, text, invalidObject, invalidProperties);
        add(newWarning);
        return newWarning;
    }

    /**
     * Creates and returns a new message with severity {@link Message#INFO} with the given code,
     * text and object properties and adds the message to the list.
     */
    public Message newInfo(String code, String text, Object invalidObject, String invalidProperty) {
        Message newInfo = Message.newInfo(code, text, invalidObject, invalidProperty);
        add(newInfo);
        return newInfo;
    }

    @Override
    public void add(Message msg) {
        super.add(msg);
    }

    @Override
    public void add(MessageList msgList) {
        super.add(msgList);
    }

    /**
     * Copies the messages from the given list to this list and sets the message's invalid object
     * properties.
     * 
     * @param msgList the list to copy the messages from. If msgList is <code>null</code> this
     *            method does nothing.
     * @param invalidObjectProperty the object and it's property that the messages refer to.
     * @param override <code>true</code> if the invalidObjectProperty should be set in all messages.
     *            <code>false</code> if the invalidObjectProperty is set only for messages that do
     *            not contain any invalid object property information.
     */
    public void add(MessageList msgList, ObjectProperty invalidObjectProperty, boolean override) {
        if (msgList == null) {
            return;
        }
        for (Message msg : msgList) {
            if (override || msg.getInvalidObjectProperties().length == 0) {
                add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), invalidObjectProperty));
            } else {
                add(msg);
            }
        }
    }

    @Override
    public String getText() {
        return super.getText();
    }

    /**
     * Removes the given message from this message list.
     * <p>
     * Does nothing if the given message is not actually contained in this message list.
     * 
     * @param message message to remove from this message list
     */
    public void remove(Message message) {
        getMessages().remove(message);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public int size() {
        return super.size();
    }

    /**
     * @deprecated Use #size() instead
     */
    @Override
    @Deprecated
    public int getNoOfMessages() {
        return super.getNoOfMessages();
    }

    @Override
    public Message getMessage(int index) {
        return super.getMessage(index);
    }

    /**
     * Returns the number of messages in this list that have the indicated severity.
     */
    public int getNoOfMessages(int severity) {
        List<Message> msgList = new ArrayList<Message>(getMessages().size());
        for (Message msg : getMessages()) {
            if (msg.getSeverity() == severity) {
                msgList.add(msg);
            }
        }
        return msgList.size();
    }

    /**
     * Returns the first message with the given severity or null if none is found.
     */
    public Message getFirstMessage(int severity) {
        for (Message msg : getMessages()) {
            if (msg.getSeverity() == severity) {
                return msg;
            }
        }
        return null;
    }

    @Override
    public Message getMessageWithHighestSeverity() {
        int highestSeverity = getSeverity();
        return getFirstMessage(highestSeverity);
    }

    @Override
    public Message getMessageByCode(String code) {
        return super.getMessageByCode(code);
    }

    @Override
    public MessageList getMessagesByCode(String code) {
        return super.getMessagesByCode(code);
    }

    /**
     * Returns the message list's severity. This is the maximum severity of the list's messages. If
     * the list does not contain any messages, the method returns 0.
     */
    public int getSeverity() {
        int severity = 0;
        for (Message msg : getMessages()) {
            if (msg.getSeverity() > severity) {
                severity = msg.getSeverity();
                if (severity == Message.ERROR) {
                    return severity;
                }
            }
        }
        return severity;
    }

    @Override
    public boolean containsErrorMsg() {
        for (Message msg : getMessages()) {
            if (msg.getSeverity() == Message.ERROR) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageList getMessagesFor(Object object, String property) {
        return super.getMessagesFor(object, property);
    }

    @Override
    public MessageList getMessagesFor(Object object) {
        return super.getMessagesFor(object);
    }

    @Override
    public MessageList getMessagesFor(Object object, String property, int index) {
        MessageList result = new MessageList();
        for (Message msg : getMessages()) {
            ObjectProperty[] op = msg.getInvalidObjectProperties();
            for (ObjectProperty element : op) {
                if (element.getObject().equals(object)) {
                    if (property == null) {
                        result.add(msg);
                        break;
                    }
                    if (property.equals(element.getProperty())) {
                        if (index < 0 || element.getIndex() == index) {
                            result.add(msg);
                            break;
                        }
                    }
                }
            }
        }
        return result;

    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public Iterator<Message> iterator() {
        return super.iterator();
    }

    /**
     * Returns a new <code>MessageList</code> containing only the <code>Message</code>s with the
     * indicated severity.
     */
    public MessageList getMessages(int severity) {
        MessageList messageList = new MessageList();
        for (Message message : getMessages()) {
            if (message.getSeverity() == severity) {
                messageList.add(message);
            }
        }
        return messageList;
    }

    /**
     * Finds all the messages with the messageCodes at the messageList and merge the
     * ObjectProperties from the same messageTexts together. After this, the messageList has only
     * unique messageTexts inside.
     * 
     * @param messageCode the messageCode to find
     */
    public void wrapUpMessages(String messageCode) {
        Map<String, Message> messageTextMap = new LinkedHashMap<String, Message>();
        for (Message message : getMessages()) {
            if (message.getCode().equals(messageCode)) {
                Message msgByText = getMessageByText(messageTextMap, message);
                ObjectProperty[] newInvalidObjects = concatInvalidObject(msgByText.getInvalidObjectProperties(),
                        message.getInvalidObjectProperties());
                Message newMessage = new Message(msgByText.getCode(), msgByText.getText(), msgByText.getSeverity(),
                        newInvalidObjects);
                messageTextMap.put(newMessage.getText(), newMessage);
            }
        }
        setMessages(new ArrayList<Message>(messageTextMap.values()));
    }

    private Message getMessageByText(Map<String, Message> messageTextMap, Message message) {
        Message msgByText = messageTextMap.get(message.getText());
        if (msgByText == null) {
            msgByText = message;
        }
        return msgByText;
    }

    private ObjectProperty[] concatInvalidObject(ObjectProperty[] invalidObjectProperties,
            ObjectProperty[] newObjectProperties) {
        if (invalidObjectProperties == newObjectProperties) {
            return invalidObjectProperties;
        } else {
            ObjectProperty[] newOne = (ObjectProperty[])ArrayUtils.addAll(invalidObjectProperties, newObjectProperties);
            return newOne;
        }
    }

    /**
     * The method returns a MethodList containing a sublist of messages of this messageList. By
     * copying the content of this messageList, the original content won't be changed.
     * <code>maxCount</code> defines how big the sublist should be. If <code>maxCount</code> ist
     * higher than the size of the messageList the whole content of the original messageList will be
     * returned.
     * 
     * @param maxCount the number of messages to be returned
     * @return MessageList a sublist of the original MessageList
     */
    public MessageList getSubList(int maxCount) {
        MessageList messageList = new MessageList();
        ArrayList<Message> messagesCopy = new ArrayList<Message>(getMessages());
        Collections.sort(messagesCopy, new SeverityComparator());
        messageList.setMessages(messagesCopy.subList(0, Math.min(maxCount, size())));
        return messageList;
    }

    @Override
    public Spliterator<Message> spliterator() {
        return super.spliterator();
    }

    @Override
    public Stream<Message> stream() {
        return super.stream();
    }

    @Override
    public Stream<Message> parallelStream() {
        return super.parallelStream();
    }

    /**
     * Returns a new {@code MessageList} that consists of the given {@code Messages}. Returns an
     * empty {@code MessageList} if {@code null} is given.
     * 
     * @param messages the {@code Messages} that the new {@code MessageList} will contain. May be
     *            {@code null}
     * @return a new {@code MessageList} that consist of the given {@code Messages}
     */
    public static final MessageList of(Message... messages) {
        if (messages == null) {
            return new MessageList();
        }
        MessageList messageList = new MessageList();
        for (Message message : messages) {
            messageList.add(message);
        }
        return messageList;
    }

    /**
     * Returns a new {@code MessageList} that contains error messages with the given texts. The code
     * of the messages are {@code null}.
     * <p>
     * Returns an empty {@code MessageList} if {@code null} or an empty array is given.
     * </p>
     *
     * @param texts the texts of the error messages in the new {@code MessageList}. May be
     *            {@code null} or empty
     * @return a new {@code MessageList} that contains error messages with the given texts or an
     *         empty {@code MessageList}
     */
    public static final MessageList ofErrors(String... texts) {
        if (texts == null) {
            return new MessageList();
        }
        return Stream.of(texts).map(text -> Message.newError(null, text))
                .collect(MessageList.collectMessages());
    }

    /**
     * Returns a {@link Collector} that can be used to {@linkplain Stream#collect(Collector)
     * collect} a {@code Stream} of {@link MessageList MessageLists} into a new {@code MessageList}.
     * 
     * @return a {@code Collector} that collects {@link MessageList MessageLists} into a new
     *         {@code MessageList}
     */
    public static final Collector<MessageList, ?, MessageList> flatten() {
        return new MessageListCollector();
    }

    /**
     * Returns a {@link Collector} that can be used to {@linkplain Stream#collect(Collector)
     * collect} messages from a {@code Stream} of {@link Message Messages} into a
     * {@code MessageList}.
     * 
     * @return a {@code Collector} that collects messages into a {@code MessageList}
     */
    public static final Collector<Message, ?, MessageList> collectMessages() {
        return new MessageCollector();
    }

    private static class MessageListCollector implements Collector<MessageList, MessageList, MessageList> {

        @Override
        public Supplier<MessageList> supplier() {
            return () -> new MessageList();
        }

        @Override
        public BiConsumer<MessageList, MessageList> accumulator() {
            return (ml1, ml2) -> ml1.add(ml2);
        }

        @Override
        public BinaryOperator<MessageList> combiner() {
            return (ml1, ml2) -> {
                MessageList ml = new MessageList();
                ml.add(ml1);
                ml.add(ml2);
                return ml;
            };
        }

        @Override
        public Function<MessageList, MessageList> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }

    }

    private static class MessageCollector implements Collector<Message, MessageList, MessageList> {

        @Override
        public Supplier<MessageList> supplier() {
            return () -> new MessageList();
        }

        @Override
        public BiConsumer<MessageList, Message> accumulator() {
            return (messageList, message) -> messageList.add(message);
        }

        @Override
        public BinaryOperator<MessageList> combiner() {
            return (ml1, ml2) -> {
                MessageList ml = new MessageList();
                ml.add(ml1);
                ml.add(ml2);
                return ml;
            };
        }

        @Override
        public Function<MessageList, MessageList> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }

    }

    public static class SeverityComparator implements Comparator<Message>, Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3947147318451484963L;

        @Override
        public int compare(Message o1, Message o2) {
            if (o1.getSeverity() > o2.getSeverity()) {
                return -1;
            }
            return o1.getSeverity() == o2.getSeverity() ? 0 : 1;
        }

    }
}
