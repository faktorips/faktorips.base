package org.faktorips.util.message;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * A set of <code>Message</code>s.
 * 
 * @see Message
 * 
 * @author Jan Ortmann
 */
public class MessageList {

    private List messages = new ArrayList(0);

    /**
     * Creates an empty message list.
     */
    public MessageList() {
    }

    /**
     * Creates a message list that contains the given message.
     * 
     * @throws IllegalArgumentException
     *             if msg is null.
     */
    public MessageList(Message msg) {
        add(msg);
    }

    /**
     * Adds the message to the list.
     * 
     * @throws IllegalArgumentException
     *             if msg is null.
     */
    public void add(Message msg) {
        ArgumentCheck.notNull(msg);
        messages.add(msg);
    }

    /**
     * Adds the message in the given list to this list.
     * 
     * @throws IllegalArgumentException
     *             if msgList is null.
     */
    public void add(MessageList msgList) {
        ArgumentCheck.notNull(msgList);
        int max = msgList.getNoOfMessages();
        for (int i = 0; i < max; i++) {
            add(msgList.getMessage(i));
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
     * @throws IndexOutOfBoundsException
     *             if the index is out of range.
     */
    public Message getMessage(int index) {
        return (Message) messages.get(index);
    }

    /**
     * Returns the first message with the given severity or null if none is found.
     */
    public Message getFirstMessage(int severity) {
        for (Iterator it = messages.iterator(); it.hasNext();) {
            Message msg = (Message) it.next();
            if (msg.getSeverity() == severity) {
                return msg;
            }
        }
        return null;
    }
    
    /**
     * Returns the first message in the list that has the indicacted message code. Returns null, if
     * the list does not contain such a message.
     */
    public Message getMessageByCode(String code) {
        for (int i = 0; i < getNoOfMessages(); i++) {
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
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getSeverity() > severity) {
                severity = getMessage(i).getSeverity();
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
        for (int i = 0; i < getNoOfMessages(); i++) {
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
        for (int i = 0; i < getNoOfMessages(); i++) {
            if (getMessage(i).getSeverity() == Message.ERROR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new list witht the messages in this list that belong to the given object (any
     * property). Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object) {
        return getMessagesFor(object, null);
    }

    /**
     * Returns a new list witht the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public MessageList getMessagesFor(Object object, String property) {
        MessageList result = new MessageList();
        for (int i = 0; i < getNoOfMessages(); i++) {
            Message msg = getMessage(i);
            ObjectProperty[] op = msg.getInvalidObjectProperties();
            for (int j = 0; j < op.length; j++) {
                if (op[j].getObject()==object
                        && (property == null || property.equals(op[j]
                                .getProperty()))) {
                    result.add(msg);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns all messages in the list separated by a line separator. Overridden method.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < getNoOfMessages(); i++) {
            s.append(getMessage(i).toString() + SystemUtils.LINE_SEPARATOR);
        }
        return s.toString();
    }

    /**
     * Returns true if o is a MessageList that contains the same messages in the same order.
     * 
     * Overridden method.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof MessageList)) {
            return false;
        }
        MessageList other = (MessageList) o;
        if (this.getNoOfMessages() != other.getNoOfMessages()) {
            return false;
        }
        for (int i = 0; i < other.getNoOfMessages(); i++) {
            if (!this.messages.get(i).equals(other.messages.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * clear löscht alle Elemente der Liste
     * 
     */
    public void clear() {
        messages.clear();
    }
    
    /** Liefert einen iterator, mit dessen Hilfe man über die vorhandene 
     *  Liste iterieren kann
     * @return Iterator
     */
    public Iterator iterator (){
        return messages.iterator();
    }


}
