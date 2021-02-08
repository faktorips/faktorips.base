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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.faktorips.runtime.util.AbstractMessageList;

/**
 * A list of {@link Message Messages}.
 * 
 * @see Message
 */
public class MessageList extends AbstractMessageList<Message, MessageList> implements Serializable {

    private static final long serialVersionUID = 5518835977871253111L;

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        super();
    }

    /**
     * Creates a message list that contains the given message.
     * 
     * @throws NullPointerException if message is null.
     */
    public MessageList(Message message) {
        super(message);
    }

    @Override
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
     *         empty {@code MessageList}
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

    @Override
    public Message getMessageWithHighestSeverity() {
        Message result = null;
        for (Message message : getMessages()) {
            if (result == null) {
                result = message;
            } else {
                if (result.getSeverity().compareTo(message.getSeverity()) < 0) {
                    result = message;
                }
            }
        }
        return result;
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

    @Override
    public boolean containsErrorMsg() {
        for (Message message : getMessages()) {
            if (message.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        return false;
    }

    @Override
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
}
