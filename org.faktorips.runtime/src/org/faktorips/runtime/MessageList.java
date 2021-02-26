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
import java.util.Iterator;
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

    /* kept for compile compatibility */
    @Override
    public void add(Message message) {
        super.add(message);
    }

    /* kept for compile compatibility */
    @Override
    public void add(MessageList messageList) {
        super.add(messageList);
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

    /* kept for compile compatibility */
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    /* kept for compile compatibility */
    @Override
    public int size() {
        return super.size();
    }

    /* kept for compile compatibility */
    @Override
    public Message getMessage(int index) {
        return super.getMessage(index);
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

    /* kept for compile compatibility */
    @Override
    public Message getMessageByCode(String code) {
        return super.getMessageByCode(code);
    }

    /* kept for compile compatibility */
    @Override
    public MessageList getMessagesByCode(String code) {
        return super.getMessagesByCode(code);
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
     * specified {@code IPredicate} matches. Returns an empty list if this list does not contain any
     * such message.
     * <p>
     * Sample usage:
     * <code>messages.getMessagesByMarker(new IPredicate&lt;IMarker&gt;(){public boolean test(IMarker t){return t.isRequiredInformationMissing();}});</code>
     * <p>
     * Sample usage (Java8):
     * <code>messages.getMessagesByMarker(IMarker::isRequiredInformationMissing);</code>
     * 
     * @param markerPredicate to match a {@link IMarker} with. Must not be <code>null</code>
     * @throws NullPointerException if markerPredicate is <code>null</code>
     * @deprecated for removal since 21.6; Use {@link #getMessagesByMarker(Predicate)} with the Java
     *             8 {@link Predicate} instead.
     */
    @Deprecated
    public MessageList getMessagesByMarker(org.faktorips.runtime.util.function.IPredicate<IMarker> markerPredicate) {
        return getMessagesByMarker((Predicate<IMarker>)markerPredicate);
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

    /* kept for compile compatibility */
    @Override
    public String getText() {
        return super.getText();
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
    public MessageList getMessagesFor(Object object, String property) {
        return super.getMessagesFor(object, property);
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

    /* kept for compile compatibility */
    @Override
    public Iterator<Message> iterator() {
        return super.iterator();
    }

    /* kept for compile compatibility */
    @Override
    public void clear() {
        super.clear();
    }

    /* kept for compile compatibility */
    @Override
    public String toString() {
        return super.toString();
    }

    /* kept for compile compatibility */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* kept for compile compatibility */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
