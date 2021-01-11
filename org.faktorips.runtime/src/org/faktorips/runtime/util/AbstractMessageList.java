/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.faktorips.runtime.internal.IpsStringUtils;

public abstract class AbstractMessageList<M extends IMessage, L extends AbstractMessageList<M, L>>
        implements Iterable<M> {

    private List<M> messages = new ArrayList<M>(0);

    public AbstractMessageList() {
        // Provide default constructor
    }

    /**
     * Creates a message list that contains the given message.
     * 
     * @param message the message to add. Ignored if <code>null</code>.
     */
    public AbstractMessageList(M message) {
        add(message);
    }

    /**
     * Adds the message to the list.
     * 
     * @param message the message to add. Ignored if <code>null</code>.
     */
    public void add(M message) {
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Adds the messages in the given list to this list.
     */
    public void add(L messageList) {
        if (messageList == null) {
            return;
        }
        for (M message : messageList) {
            add(message);
        }
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
     * Returns the number of messages in the list.
     * 
     * @deprecated Use #size() instead
     */
    @Deprecated
    public int getNoOfMessages() {
        return size();
    }

    /**
     * Returns the message at the indicated index (indexing starts with 0).
     * 
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public M getMessage(int index) {
        return messages.get(index);
    }

    /**
     * Returns the message list.
     */
    public List<M> getMessages() {
        return messages;
    }

    /**
     * Sets the message list.
     */
    public void setMessages(List<M> messages) {
        this.messages = messages;
    }

    /**
     * Returns all messages in the list separated by a line separator.
     */
    @Override
    public String toString() {
        return IpsStringUtils.join(messages, System.lineSeparator());
    }

    /**
     * Removes all of the messages from this list. This list will be empty after this call returns.
     */
    public void clear() {
        messages.clear();
    }

    /**
     * Returns an iterator over the messages in this list.
     */
    @Override
    public Iterator<M> iterator() {
        return messages.iterator();
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property. Returns an empty list if no such message is found.
     */
    public L getMessagesFor(Object object, String property) {
        return getMessagesFor(object, property, -1);
    }

    /**
     * Returns a new list with the messages in this list that belong to the given object (any
     * property). Returns an empty list if no such message is found.
     */
    public L getMessagesFor(Object object) {
        return getMessagesFor(object, null);
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
            @SuppressWarnings("unchecked")
            L other = (L)obj;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMessages() == null) ? 0 : getMessages().hashCode());
        return result;
    }

    /**
     * Returns the first message in the list that has the specified message code. Returns
     * <code>null</code> if the list does not contain such a message.
     * 
     * @param code the code to look for. May be <code>null</code>, as messages may have
     *            <code>null</code> as their message code.
     */
    public M getMessageByCode(String code) {
        for (M message : getMessages()) {
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
    public L getMessagesByCode(String code) {
        L sublist = createEmptyMessageList();
        for (M message : getMessages()) {
            if (Objects.equals(message.getCode(), code)) {
                sublist.add(message);
            }
        }
        return sublist;
    }

    /**
     * Returns the text of all messages in the list, separated by the system's default line
     * separator.
     */
    public String getText() {
        return IpsStringUtils.join(getMessages(), M::getText, System.lineSeparator());
    }

    /**
     * Returns true if one the messages in the list is an error message, otherwise false.
     */
    public abstract boolean containsErrorMsg();

    /**
     * Returns the message with the highest severity. If there are multiple such messages, the first
     * one is returned. If this list {@link #isEmpty()}, <code>null</code> is returned.
     */
    public abstract M getMessageWithHighestSeverity();

    /**
     * Returns a new list with the messages in this list that belong to the given object and
     * property and the property is of the given index. Returns an empty list if no such message is
     * found.
     */
    public abstract L getMessagesFor(Object object, String property, int index);

    /**
     * Creates a new empty message list.
     */
    protected abstract L createEmptyMessageList();

    /**
     * Creates a {@link Spliterator} over the included messages.
     *
     * @since 21.6
     */
    @Override
    public Spliterator<M> spliterator() {
        return messages.spliterator();
    }

    /**
     * Returns a sequential {@code Stream} of the included messages.
     * 
     * @since 21.6
     */
    public Stream<M> stream() {
        return messages.stream();
    }

    /**
     * Returns a parallel {@code Stream} of the included messages.
     * 
     * @since 21.6
     */
    public Stream<M> parallelStream() {
        return messages.parallelStream();
    }

}
