/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * A set of <code>Message</code>s.
 * 
 * @see Message
 * 
 * @author Jan Ortmann
 */
public class MessageList implements Iterable<Message> {

    private List<Message> messages = new ArrayList<Message>(0);

    /**
     * Creates an empty message list.
     */
    public MessageList() {
        // Provide default constructor.
    }

    /**
     * Creates a message list that contains the given message. <code>null</code> will be ignored as
     * a parameter value.
     */
    public MessageList(Message msg) {
        add(msg);
    }

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param objectPropertyMap The <tt>Map</tt> between old and new <tt>ObjectProperty</tt>
     * @return MessageList
     */
    public MessageList createCopy(Map<ObjectProperty, ObjectProperty> objectPropertyMap) {
        if (isEmpty()) {
            return this;
        }
        MessageList newList = new MessageList();
        for (Message message : messages) {
            newList.add(message.createCopy(objectPropertyMap));
        }
        return newList;
    }

    /**
     * Creates and returns a new message with severity {@link Message#ERROR} with the given code,
     * text and object properties and adds the message to the list.
     */
    public Message newError(String code, String text, Object invalidObject, String invalidProperty) {
        Message newError = Message.newError(code, text, invalidObject, invalidProperty);
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
    public Message newWarning(String code, String text, Object invalidObject, String invalidProperty) {
        Message newWarning = Message.newWarning(code, text, invalidObject, invalidProperty);
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
     * Adds the message to the list. <code>null</code> will be ignored as a parameter value.
     */
    public void add(Message msg) {
        if (msg == null) {
            return;
        }
        messages.add(msg);
    }

    /**
     * Adds the messages in the given list to this list.
     * <p>
     * <code>null</code> will be ignored as a parameter value.
     */
    public void add(MessageList msgList) {
        if (msgList == null) {
            return;
        }
        int max = msgList.size();
        for (int i = 0; i < max; i++) {
            add(msgList.getMessage(i));
        }
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
        int max = msgList.size();
        for (int i = 0; i < max; i++) {
            Message msg = msgList.getMessage(i);
            if (override || msg.getInvalidObjectProperties().length == 0) {
                add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), invalidObjectProperty));
            } else {
                add(msg);
            }
        }
    }

    /**
     * Removes the given message from this message list.
     * <p>
     * Does nothing if the given message is not actually contained in this message list.
     * 
     * @param message message to remove from this message list
     */
    public void remove(Message message) {
        messages.remove(message);
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
     * Returns the total number of messages in the list.
     * 
     * @deprecated use {@link #size()}
     */
    @Deprecated
    public int getNoOfMessages() {
        return size();
    }

    /**
     * Returns the number of messages in this list that have the indicated severity.
     */
    public int getNoOfMessages(int severity) {
        List<Message> msgList = new ArrayList<Message>(messages.size());
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (msg.getSeverity() == severity) {
                msgList.add(msg);
            }
        }
        return msgList.size();
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
    public Message getFirstMessage(int severity) {
        for (Message msg : messages) {
            if (msg.getSeverity() == severity) {
                return msg;
            }
        }
        return null;
    }

    /**
     * Returns the message with the highest severity or <code>null</code> if the list does not
     * contain any message. If more than one message with the highest severity exists, the first one
     * is returned.
     */
    public Message getMessageWithHighestSeverity() {
        int highestSeverity = getSeverity();
        return getFirstMessage(highestSeverity);
    }

    /**
     * Returns the first message in the list that has the indicated message code. Returns null, if
     * the list does not contain such a message.
     */
    public Message getMessageByCode(String code) {
        for (int i = 0; i < size(); i++) {
            if (getMessage(i).getCode().equals(code)) {
                return getMessage(i);
            }
        }
        return null;
    }

    /**
     * Returns the message list's severity. This is the maximum severity of the list's messages. If
     * the list does not contain any messages, the method returns 0.
     */
    public int getSeverity() {
        int severity = 0;
        for (int i = 0; i < size(); i++) {
            if (getMessage(i).getSeverity() > severity) {
                severity = getMessage(i).getSeverity();
                if (severity == Message.ERROR) {
                    return severity;
                }
            }
        }
        return severity;
    }

    /**
     * Returns the text of all messages in the list, separated by the system's default line
     * separator.
     */
    public String getText() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                s.append(SystemUtils.LINE_SEPARATOR);
            }
            s.append(getMessage(i).getText());
        }
        return s.toString();

    }

    /**
     * Returns true if one the messages in the list is an error message, otherwise false.
     */
    public boolean containsErrorMsg() {
        for (int i = 0; i < size(); i++) {
            if (getMessage(i).getSeverity() == Message.ERROR) {
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
        for (int i = 0; i < size(); i++) {
            Message msg = getMessage(i);
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

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object, String property) {
        return getMessagesFor(object, property, -1);
    }

    /**
     * Returns a new <tt>MessageList</tt> containing only the <tt>Message</tt>s with the indicated
     * severity.
     */
    public MessageList getMessages(int severity) {
        MessageList messageList = new MessageList();
        for (Message message : messages) {
            if (message.getSeverity() == severity) {
                messageList.add(message);
            }
        }
        return messageList;
    }

    /**
     * Returns all messages in the list separated by a line separator.
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            s.append(getMessage(i).toString() + SystemUtils.LINE_SEPARATOR);
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MessageList)) {
            return false;
        }

        MessageList other = (MessageList)o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < other.size(); i++) {
            Message message = messages.get(i);
            Message otherMessage = other.messages.get(i);
            if (!((message == null) ? otherMessage == null : message.equals(otherMessage))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + size();
        for (Message message : messages) {
            int c = (message == null) ? 0 : message.hashCode();
            result = 31 * result + c;
        }
        return result;
    }

    /**
     * Deletes all messages this list contains.
     */
    public void clear() {
        messages.clear();
    }

    public Iterator<Message> iterator() {
        return messages.iterator();
    }

    /**
     * Finds all the messages with the messageCodes at the messageList and merge the
     * ObjectProperties from the same messageTexts together. After this, the messageList has only
     * unique messageTexts inside.
     * 
     * @param messageCode the messageCode to find
     */
    public void wrapUpMessages(String messageCode) {
        Map<String, Message> messageTextMap = new HashMap<String, Message>();
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
            Message message = iterator.next();
            if (message.getCode().equals(messageCode)) {
                Message msgByText = getMessageByText(messageTextMap, message);
                ObjectProperty[] newInvalidObjects = concatInvalidObject(msgByText.getInvalidObjectProperties(),
                        message.getInvalidObjectProperties());
                Message newMessage = new Message(msgByText.getCode(), msgByText.getText(), msgByText.getSeverity(),
                        newInvalidObjects);
                messageTextMap.put(newMessage.getText(), newMessage);
            }
        }
        messages = new ArrayList<Message>(messageTextMap.values());
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
        ArrayList<Message> messagesCopy = new ArrayList<Message>(messages);
        Collections.sort(messagesCopy, new SeverityComparator());
        messageList.messages = messagesCopy.subList(0, Math.min(maxCount, size()));
        return messageList;
    }

    public static class SeverityComparator implements Comparator<Message>, Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3947147318451484963L;

        public int compare(Message o1, Message o2) {
            if (o1.getSeverity() > o2.getSeverity()) {
                return -1;
            }
            return o1.getSeverity() == o2.getSeverity() ? 0 : 1;
        }

    }
}
