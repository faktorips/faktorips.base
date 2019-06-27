/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.util.function.IPredicate;
import org.faktorips.values.ObjectUtil;

/**
 * A list of <code>Message</code>s.
 * 
 * @see Message
 */
public class MessageList implements Serializable, Iterable<Message> {

    private static final long serialVersionUID = 5518835977871253111L;

    private List<Message> messages = new ArrayList<Message>(0);

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        // Provides default constructor.
    }

    /**
     * Creates a message list that contains the given message.
     * 
     * @throws NullPointerException if message is null.
     */
    public MessageList(Message message) {
        add(message);
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
     * Adds the message to the list.
     * 
     * @param message the {@link Message} to add. Ignored if <code>null</code>.
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
     * Returns true if the list is empty.
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Returns the number of messages in the list.
     * 
     * @deprecated Use #size() instead
     */
    @Deprecated
    public int getNoOfMessages() {
        return size();
    }

    /**
     * Returns the number of messages in the list.
     * 
     * @return The size of the message list
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
     * Returns the first message with the given severity or null if none is found.
     */
    public Message getFirstMessage(Severity severity) {
        for (Message message : messages) {
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
        for (Message message : messages) {
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
     * Returns the first message in the list that has the specified message code. Returns
     * <code>null</code> if the list does not contain such a message.
     * 
     * @param code the code to look for. May be <code>null</code>, as messages may have
     *            <code>null</code> as their message code.
     */
    public Message getMessageByCode(String code) {
        for (Message message : messages) {
            if (ObjectUtil.equals(message.getCode(), code)) {
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
        MessageList sublist = new MessageList();
        for (Message message : messages) {
            if (ObjectUtil.equals(message.getCode(), code)) {
                sublist.add(message);
            }
        }
        return sublist;
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
        for (Message message : messages) {
            Set<? extends IMarker> markers = message.getMarkers();
            if (marker == null && markers.isEmpty() || markers.contains(marker)) {
                sublist.add(message);
            }
        }
        return sublist;
    }

    /**
     * Returns a new message list containing all the message in this list with a {@link IMarker} the
     * specified {@link IPredicate} matches. Returns an empty list if this list does not contain any
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
     */
    public MessageList getMessagesByMarker(IPredicate<IMarker> markerPredicate) {
        if (markerPredicate == null) {
            throw new NullPointerException("markerPredicate must not be null");
        }
        MessageList sublist = new MessageList();
        messages: for (Message message : messages) {
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
        for (Message message : messages) {
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
        String lineSeparator = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                s.append(lineSeparator);
            }
            s.append(getMessage(i).getText());
        }
        return s.toString();

    }

    /**
     * Returns true if one the messages in the list is an error message, otherwise false.
     */
    public boolean containsErrorMsg() {
        for (Message message : messages) {
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
     * property and the property is of the given index. Returns an empty list if no such message is
     * found.
     */
    public MessageList getMessagesFor(Object object, String property, int index) {
        MessageList result = new MessageList();
        for (Message message : messages) {
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
     * Returns a new list with the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object, String property) {
        return getMessagesFor(object, property, -1);
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
     * Returns all messages in the list separated by a line separator. Overridden method.
     */
    @Override
    public String toString() {
        String lineSeparator = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        for (Message message : messages) {
            s.append(message.toString() + lineSeparator);
        }
        return s.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messages == null) ? 0 : messages.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageList other = (MessageList)obj;
        if (messages == null) {
            if (other.messages != null) {
                return false;
            }
        } else if (!messages.equals(other.messages)) {
            return false;
        }
        return true;
    }

}
