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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A list of {@link Message Messages}.
 * 
 * @see Message
 */
public class MessageList implements Serializable, Iterable<Message> {

    private static final long serialVersionUID = 5518835977871253111L;
    private List<Message> messages = new ArrayList<>(0);

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        // Provide default constructor
    }

    /**
     * Creates a message list that contains the given message.
     * 
     * @param message the message to add. Ignored if <code>null</code>.
     */
    public MessageList(Message message) {
        add(message);
    }

    /**
     * Creates a new empty message list.
     */
    protected MessageList createEmptyMessageList() {
        return new MessageList();
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
     * Returns a new {@code MessageList} that contains error messages with the given texts. Returns
     * an empty {@code MessageList} if {@code null} or an empty array is given.
     * 
     * @param texts the texts of the error messages in the new {@code MessageList}. May be
     *            {@code null} or empty
     * @return a new {@code MessageList} that contains error messages with the given texts or an
     *             empty {@code MessageList}
     */
    public static final MessageList ofErrors(String... texts) {
        if (texts == null) {
            return new MessageList();
        }
        return Stream.of(texts).map(text -> new Message(text, Severity.ERROR)).collect(MessageLists.collectMessages());
    }

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param list the list to copy
     * @param oldObject the old object reference that should be replaced.
     * @param newObject the object reference to set
     */
    public static final MessageList createCopy(MessageList list, Object oldObject, Object newObject) {
        MessageList newList = new MessageList();
        for (Message message : list) {
            newList.add(Message.createCopy(message, oldObject, newObject));
        }
        return newList;
    }

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param objectPropertyMap The <code>Map</code> between old and new <code>ObjectProperty</code>
     * @return MessageList
     */
    public static final MessageList createCopy(MessageList list,
            Map<ObjectProperty, ObjectProperty> objectPropertyMap) {
        MessageList newList = new MessageList();
        for (Message message : list) {
            newList.add(Message.createCopy(message, objectPropertyMap));
        }
        return newList;
    }

    /**
     * Adds the message to the list.
     * 
     * @param message the message to add. Ignored if <code>null</code>.
     */
    public void add(Message message) {
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Adds the messages in the given list to this list.
     */
    public void add(MessageList messageList) {
        if (messageList == null) {
            return;
        }
        for (Message message : messageList) {
            add(message);
        }
    }

    /**
     * Copies the messages from the given list to this list and sets the message's invalid object
     * properties.
     * 
     * @param messageList the list to copy the messages from.
     * @param invalidObjectProperty the object and it's property that the messages refer to.
     * @param override <code>true</code> if the invalidObjectProperty should be set in all messages.
     *            <code>false</code> if the invalidObjectProperty is set only for messages that do
     *            not contain any invalid object property information.
     */
    public void add(MessageList messageList, ObjectProperty invalidObjectProperty, boolean override) {
        if (messageList == null) {
            return;
        }
        for (Message message : messageList) {
            if (override || message.getInvalidObjectProperties().size() == 0) {
                add(new Message(message.getCode(), message.getText(), message.getSeverity(), invalidObjectProperty));
            } else {
                add(message);
            }
        }
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

    /**
     * Returns true if the list is empty.
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Returns the total number of messages in the list.
     * 
     */
    public int size() {
        return messages.size();
    }

    /**
     * Returns the message at the indicated index (indexing starts with 0).
     * 
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public Message getMessage(int index) {
        return messages.get(index);
    }

    /**
     * Returns the number of messages in the list.
     * 
     * @deprecated for removal. Use #size() instead.
     */
    @Deprecated
    public int getNoOfMessages() {
        return size();
    }

    /**
     * Returns the message list.
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Sets the message list.
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Returns the first message with the given severity or null if none is found.
     */
    public Message getFirstMessage(Severity severity) {
        for (Message message : getMessages()) {
            if (message.getSeverity() == severity) {
                return message;
            }
        }
        return null;
    }

    /**
     * Returns the message with the highest severity. If there are multiple such messages, the first
     * one is returned. If this list {@link #isEmpty()}, <code>null</code> is returned.
     */
    public Message getMessageWithHighestSeverity() {
        Message result = null;
        for (Message message : getMessages()) {
            if ((result == null) || (result.getSeverity().compareTo(message.getSeverity()) < 0)) {
                result = message;
            }
        }
        return result;
    }

    /**
     * Returns the first message in the list that has the specified message code. Returns
     * <code>null</code> if the list does not contain such a message.
     * 
     * @param code the code to look for. May be <code>null</code>, as messages may have
     *            <code>null</code> as their message code.
     */
    public Message getMessageByCode(String code) {
        for (Message message : getMessages()) {
            if (Objects.equals(message.getCode(), code)) {
                return message;
            }
        }
        return null;
    }

    /**
     * Returns a new message list containing all the message in this list that have the specified
     * message code. Returns an empty list if this list does not contain any message with the given
     * code.
     * 
     * @param code the code to look for. May be <code>null</code>, as messages may have
     *            <code>null</code> as their message code.
     */
    public MessageList getMessagesByCode(String code) {
        MessageList sublist = createEmptyMessageList();
        for (Message message : getMessages()) {
            if (Objects.equals(message.getCode(), code)) {
                sublist.add(message);
            }
        }
        return sublist;
    }

    /**
     * Returns a new <code>MessageList</code> containing only the <code>Message</code>s with the
     * indicated severity. Returns an empty list if this list does not contain any message with the
     * given severity.
     */
    public MessageList getMessagesBySeverity(Severity severity) {
        MessageList messageList = createEmptyMessageList();
        for (Message message : getMessages()) {
            if (message.getSeverity() == severity) {
                messageList.add(message);
            }
        }
        return messageList;
    }

    /**
     * Returns the number of messages in this list that have the indicated severity.
     */
    public int getNoOfMessages(Severity severity) {
        List<Message> msgList = new ArrayList<>(getMessages().size());
        for (Message msg : getMessages()) {
            if (msg.getSeverity() == severity) {
                msgList.add(msg);
            }
        }
        return msgList.size();
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
        MessageList sortedSubList = MessageLists.sortBySeverity(this);
        sortedSubList.setMessages(sortedSubList.getMessages().subList(0, Math.min(maxCount, size())));
        return sortedSubList;
    }

    /**
     * Returns a new message list containing all the message in this list that have the specified
     * {@link IMarker}. Returns an empty list if this list does not contain any message with the
     * given {@link IMarker}.
     * 
     * @param marker the {@link IMarker} to look for. If <code>null</code>, all messages without a
     *            {@link IMarker} are returned.
     */
    public MessageList getMessagesByMarker(IMarker marker) {
        MessageList sublist = new MessageList();
        for (Message message : getMessages()) {
            Set<? extends IMarker> markers = message.getMarkers();
            if (marker == null && markers.isEmpty() || markers.contains(marker)) {
                sublist.add(message);
            }
        }
        return sublist;
    }

    /**
     * Returns a new message list containing all the message in this list with a {@link IMarker} the
     * specified {@link Predicate} matches. Returns an empty list if this list does not contain any
     * such message.
     * <p>
     * Sample usage:
     * <code>messages.getMessagesByMarker(IMarker::isRequiredInformationMissing);</code>
     * 
     * @param markerPredicate to match an {@link IMarker}. Must not be <code>null</code>
     * @throws NullPointerException if markerPredicate is <code>null</code>
     */
    public MessageList getMessagesByMarker(Predicate<IMarker> markerPredicate) {
        if (markerPredicate == null) {
            throw new NullPointerException("markerPredicate must not be null");
        }
        MessageList sublist = new MessageList();
        messages: for (Message message : getMessages()) {
            for (IMarker marker : message.getMarkers()) {
                if (markerPredicate.test(marker)) {
                    sublist.add(message);
                    continue messages;
                }
            }
        }
        return sublist;
    }

    /**
     * Returns the message list's severity. This is the maximum severity of the list's messages. If
     * the list does not contain any messages, the method returns 0.
     */
    public Severity getSeverity() {
        Severity severity = Severity.NONE;
        for (Message message : getMessages()) {
            if (message.getSeverity().compareTo(severity) > 0) {
                severity = message.getSeverity();
            }
        }
        return severity;
    }

    /**
     * Returns the text of all messages in the list, separated by the system's default line
     * separator.
     */
    public String getText() {
        return IpsStringUtils.join(getMessages(), Message::getText, System.lineSeparator());
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

    /**
     * Returns true if one the messages in the list is an error message, otherwise false.
     */
    public boolean containsErrorMsg() {
        for (Message message : getMessages()) {
            if (message.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object (any
     * property). Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object) {
        return getMessagesFor(object, null);
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object, String property) {
        return getMessagesFor(object, property, -1);
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property and the property is of the given index. Returns an empty list if no such message is
     * found.
     */
    public MessageList getMessagesFor(Object object, String property, int index) {
        MessageList result = new MessageList();
        for (Message message : getMessages()) {
            List<ObjectProperty> op = message.getInvalidObjectProperties();
            for (ObjectProperty objectProperty : op) {
                if (objectProperty.getObject().equals(object)) {
                    if (property == null) {
                        result.add(message);
                        break;
                    }
                    if (property.equals(objectProperty.getProperty())) {
                        if (index < 0 || objectProperty.getIndex() == index) {
                            result.add(message);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds all the messages with the messageCodes at the messageList and merge the
     * ObjectProperties from the same messageTexts together. After this, the messageList has only
     * unique messageTexts inside.
     * 
     * @param messageCode the messageCode to find
     */
    public void wrapUpMessages(String messageCode) {
        Map<String, Message> messageTextMap = new LinkedHashMap<>();
        for (Message message : getMessages()) {
            if (message.getCode().equals(messageCode)) {
                Message msgByText = getMessageByText(messageTextMap, message);
                List<ObjectProperty> newInvalidObjects = concatInvalidObject(msgByText.getInvalidObjectProperties(),
                        message.getInvalidObjectProperties());
                Message newMessage = new Message(msgByText.getCode(), msgByText.getText(), msgByText.getSeverity(),
                        newInvalidObjects);
                messageTextMap.put(newMessage.getText(), newMessage);
            }
        }
        setMessages(new ArrayList<>(messageTextMap.values()));
    }

    private Message getMessageByText(Map<String, Message> messageTextMap, Message message) {
        Message msgByText = messageTextMap.get(message.getText());
        if (msgByText == null) {
            msgByText = message;
        }
        return msgByText;
    }

    private List<ObjectProperty> concatInvalidObject(List<ObjectProperty> invalidObjectProperties,
            List<ObjectProperty> newObjectProperties) {
        if (invalidObjectProperties.equals(newObjectProperties)) {
            return invalidObjectProperties;
        } else {
            List<ObjectProperty> newOne = new ArrayList<>();
            newOne.addAll(invalidObjectProperties);
            newOne.addAll(newObjectProperties);
            return newOne;
        }
    }

    /**
     * Returns a new message list containing the same number of messages as this list, with the
     * given transformer function applied to each message.
     */
    public MessageList map(UnaryOperator<Message> transformer) {
        return stream().map(transformer::apply).collect(MessageLists.collectMessages());
    }

    /**
     * Returns a new message list containing the same number of messages as this list, with the
     * given transformer function applied to each message that matches the given predicate. All
     * other messages are transferred to the new list without change.
     */
    public MessageList map(Predicate<Message> shouldBeTransformed, UnaryOperator<Message> transformer) {
        return stream().map(m -> shouldBeTransformed.test(m) ? transformer.apply(m) : m)
                .collect(MessageLists.collectMessages());
    }

    /**
     * Returns an iterator over the messages in this list.
     */
    @Override
    public Iterator<Message> iterator() {
        return messages.iterator();
    }

    /**
     * Removes all of the messages from this list. This list will be empty after this call returns.
     */
    public void clear() {
        messages.clear();
    }

    /**
     * Returns all messages in the list separated by a comma and a line separator.
     */
    @Override
    public String toString() {
        return IpsStringUtils.join(messages, ',' + System.lineSeparator());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + ((getMessages() == null) ? 0 : getMessages().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        try {
            MessageList other = (MessageList)obj;
            if (getMessages() == null) {
                if (other.getMessages() != null) {
                    return false;
                }
            } else if (!getMessages().equals(other.getMessages())) {
                return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Creates a {@link Spliterator} over the included messages.
     *
     * @since 21.6
     */
    @Override
    public Spliterator<Message> spliterator() {
        return messages.spliterator();
    }

    /**
     * Returns a sequential {@code Stream} of the included messages.
     * 
     * @since 21.6
     */
    public Stream<Message> stream() {
        return messages.stream();
    }

    /**
     * Returns a parallel {@code Stream} of the included messages.
     * 
     * @since 21.6
     */
    public Stream<Message> parallelStream() {
        return messages.parallelStream();
    }
}
