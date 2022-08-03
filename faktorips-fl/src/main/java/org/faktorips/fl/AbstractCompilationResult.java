/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Objects;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Basic implementation of the CompilationResult interface, independent of compilation target
 * language.
 */
public abstract class AbstractCompilationResult<T extends CodeFragment> implements CompilationResult<T> {

    private T codeFragment;
    private MessageList messages;
    private Datatype datatype;

    /**
     * Creates a CompilationResult with the given parameters.
     */
    public AbstractCompilationResult(T sourcecode, Datatype datatype, MessageList messages) {
        codeFragment = sourcecode;
        this.datatype = datatype;
        this.messages = messages;
    }

    /**
     * Creates a CompilationResult that contains the given source code fragment and data type.
     */
    public AbstractCompilationResult(T sourcecode, Datatype datatype) {
        codeFragment = sourcecode;
        this.datatype = datatype;
        messages = new MessageList();
    }

    /**
     * Creates a CompilationResult that contains the given message and source code fragment.
     * 
     * @throws IllegalArgumentException if message is null.
     */
    public AbstractCompilationResult(Message message, T codeFragment) {
        messages = new MessageList(message);
        this.codeFragment = codeFragment;
    }

    /**
     * Creates a CompilationResult that contains the given source code fragment.
     */
    public AbstractCompilationResult(T codeFragment) {
        this.codeFragment = codeFragment;
        messages = new MessageList();
    }

    /**
     * Appends the given compilation result's source code fragment and messages to this result's
     * source code fragment. This result's data type remains unchanged.
     */
    public void add(CompilationResult<T> result) {
        codeFragment.append(result.getCodeFragment());
        messages.add(result.getMessages());
    }

    /**
     * Returns the generated source code.
     */
    @Override
    public T getCodeFragment() {
        return codeFragment;
    }

    /**
     * Sets the code fragment.
     */
    public void setCodeFragment(T code) {
        codeFragment = code;
    }

    /**
     * Adds the code fragment to the result ones.
     */
    public void addCodeFragment(T code) {
        codeFragment.append(code);
    }

    /**
     * Adds the code fragment to the result ones.
     */
    public void addCodeFragment(String code) {
        codeFragment.append(code);
    }

    /**
     * Sets the result's data type.
     */
    public void setDatatype(Datatype newType) {
        datatype = newType;
    }

    /**
     * Returns the compiled expression's data type.
     */
    @Override
    public Datatype getDatatype() {
        return datatype;
    }

    /**
     * Returns the messages generated during compilation.
     */
    @Override
    public MessageList getMessages() {
        return messages;
    }

    /**
     * Adds the message to the result.
     * 
     * @throws IllegalArgumentException if the given message is null.
     */
    public void addMessage(Message msg) {
        messages.add(msg);
    }

    /**
     * Adds the message list to the result.
     * 
     * @throws IllegalArgumentException if list is null.
     */
    public void addMessages(MessageList list) {
        messages.add(list);
    }

    @Override
    public boolean successfull() {
        return !messages.containsErrorMsg();
    }

    @Override
    public boolean failed() {
        return messages.containsErrorMsg();
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeFragment, datatype, messages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof AbstractCompilationResult)) {
            return false;
        }
        AbstractCompilationResult<?> other = (AbstractCompilationResult<?>)obj;
        return Objects.equals(codeFragment, other.codeFragment)
                && Objects.equals(datatype, other.datatype)
                && Objects.equals(messages, other.messages);
    }

    @Override
    public String toString() {
        return "Datatype: " + (datatype == null ? "null" : datatype.toString()) //$NON-NLS-1$ //$NON-NLS-2$
                + System.lineSeparator() + messages.toString() + codeFragment.toString();
    }

    /**
     * Extracts the datatypes from an array of compilation results.
     */
    public final Datatype[] getDatatypes(CompilationResult<T>[] results) {
        Datatype[] types = new Datatype[results.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = results[i].getDatatype();
        }
        return types;
    }

}
