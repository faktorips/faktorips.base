/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.runtime.Message.Severity;

/**
 * A list of <code>Message</code>s.
 * 
 * @see Message
 * 
 * @author Jan Ortmann
 */
public class MessageList implements Serializable, Iterable<Message> {

    private static final long serialVersionUID = 5518835977871253111L;

    /**
     * Creates a copy from the message list and replaces all references to the old object with the
     * new object.
     * 
     * @param list the list to copy
     * @param oldObject the old object reference that should be replaced.
     * @param newObject the object reference to set
     */
    public final static MessageList createCopy(MessageList list, Object oldObject, Object newObject) {
        MessageList newList = new MessageList();
        int numOfMsg = list.getNoOfMessages();
        for (int i = 0; i < numOfMsg; i++) {
            newList.add(Message.createCopy(list.getMessage(i), oldObject, newObject));
        }
        return newList;
    }

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
     * @throws IllegalArgumentException if msg is null.
     */
    public MessageList(Message msg) {
        add(msg);
    }

    /**
     * Adds the message to the list.
     * 
     * @throws NullPointerException if msg is null.
     */
    public void add(Message msg) {
        if (msg == null) {
            throw new NullPointerException();
        }
        messages.add(msg);
    }

    /**
     * Adds the messages in the given list to this list.
     * 
     * @throws IllegalArgumentException if msgList is null.
     */
    public void add(MessageList msgList) {
        if (msgList == null) {
            return;
        }
        int max = msgList.getNoOfMessages();
        for (int i = 0; i < max; i++) {
            add(msgList.getMessage(i));
        }
    }

    /**
     * Copies the messages from the given list to this list and sets the message's invalid object
     * properties.
     * 
     * @param msgList the list to copy the messages from.
     * @param invalidObjectProperty the object and it's property that the messages refer to.
     * @param override <code>true</code> if the invalidObjectProperty should be set in all messages.
     *            <code>false</code> if the invalidObjectProperty is set only for messages that do
     *            not contain any invalid object property information.
     */
    public void add(MessageList msgList, ObjectProperty invalidObjectProperty, boolean override) {
        if (msgList == null) {
            return;
        }
        int max = msgList.getNoOfMessages();
        for (int i = 0; i < max; i++) {
            Message msg = msgList.getMessage(i);
            if (override || msg.getInvalidObjectProperties().size() == 0) {
                add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), invalidObjectProperty));
            } else {
                add(msg);
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
     */
    public int getNoOfMessages() {
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
        for (Message msg : messages) {
            if (msg.getSeverity() == severity) {
                return msg;
            }
        }
        return null;
    }

    /**
     * Returns the first message in the list that has the specified message code. Returns null, if
     * the list does not contain such a message.
     */
    public Message getMessageByCode(String code) {
        for (Message msg : messages) {
            if (msg.getCode().equals(code)) {
                return msg;
            }
        }
        return null;
    }

    /**
     * Returns a new message list containing all the message in this list that have the specified
     * message code. Returns an empty list if either code is <code>null</code> or this list does
     * contain any message with the given code.
     */
    public MessageList getMessagesByCode(String code) {
        MessageList sublist = new MessageList();
        if (code == null) {
            return sublist;
        }
        for (Message msg : messages) {
            if (msg.getCode().equals(code)) {
                sublist.add(msg);
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
        for (Message msg : messages) {
            if (msg.getSeverity().compareTo(severity) > 0) {
                severity = msg.getSeverity();
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
        for (int i = 0; i < getNoOfMessages(); i++) {
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
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getSeverity() == Message.Severity.ERROR) {
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
        for (int i = 0; i < getNoOfMessages(); i++) {
            Message msg = getMessage(i);
            List<ObjectProperty> op = msg.getInvalidObjectProperties();
            for (ObjectProperty objectProperty : op) {
                if (objectProperty.getObject().equals(object)) {
                    if (property == null) {
                        result.add(msg);
                        break;
                    }
                    if (property.equals(objectProperty.getProperty())) {
                        if (index < 0 || objectProperty.getIndex() == index) {
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
     * Returns an iterator over the messages in this list.
     */
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
        for (int i = 0; i < getNoOfMessages(); i++) {
            s.append(getMessage(i).toString() + lineSeparator);
        }
        return s.toString();
    }

    /**
     * Returns true if o is a MessageList that contains the same messages in the same order.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageList)) {
            return false;
        }
        MessageList other = (MessageList)o;
        if (this.getNoOfMessages() != other.getNoOfMessages()) {
            return false;
        }
        if (!this.messages.equals(other.messages)) {
            return false;
        }
        return true;
    }

}
