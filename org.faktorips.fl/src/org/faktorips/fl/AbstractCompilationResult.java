/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Basic implementation of the CompilationResult interface, independent of compilation target
 * language.
 */
public abstract class AbstractCompilationResult<T extends CodeFragment> implements CompilationResult<T> {

    private T codeFragment;
    private MessageList messages;
    private Datatype datatype;
    private Set<String> identifiersUsed;

    /**
     * Creates a CompilationResult with the given parameters.
     */
    public AbstractCompilationResult(T sourcecode, Datatype datatype, MessageList messages, Set<String> identifiers) {
        codeFragment = sourcecode;
        this.datatype = datatype;
        this.messages = messages;
        identifiersUsed = new HashSet<String>(identifiers);
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
        Set<String> otherIdenifiers = ((AbstractCompilationResult<T>)result).identifiersUsed;
        if (otherIdenifiers == null) {
            return;
        }
        if (identifiersUsed == null) {
            identifiersUsed = new LinkedHashSet<String>(2);
        }
        identifiersUsed.addAll(otherIdenifiers);
    }

    /**
     * Returns the generated source code.
     */
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
    public Datatype getDatatype() {
        return datatype;
    }

    /**
     * Returns the messages generated during compilation.
     */
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

    /**
     * {@inheritDoc}
     */
    public String[] getResolvedIdentifiers() {
        if (identifiersUsed == null) {
            return new String[0];
        }
        return identifiersUsed.toArray(new String[identifiersUsed.size()]);
    }

    /**
     * Adds the identifier to the ones being used in the formula. If the result already contains
     * such an identifier, the identifier is not added a second time.
     */
    public void addIdentifierUsed(String identifier) {
        if (identifiersUsed == null) {
            identifiersUsed = new LinkedHashSet<String>(2);
        }
        identifiersUsed.add(identifier);
    }

    /**
     * Returns the recognized (resolved) identifiers that are used in the formula. Returns
     * {@code null} set if the formula does not contain any identifiers.
     */
    public Set<String> getIdentifiersUsedAsSet() {
        return identifiersUsed == null ? null : Collections.unmodifiableSet(identifiersUsed);
    }

    /**
     * Adds the identifiers to the ones being used in the formula. If the result already contains
     * identifiers from the given set, those identifiers are not added a second time.
     */
    public void addIdentifiersUsed(Set<String> identifiers) {
        if (identifiersUsed == null) {
            if (identifiers != null) {
                identifiersUsed = new LinkedHashSet<String>(identifiers);
            }
            return;
        }
        if (identifiers != null) {
            identifiersUsed.addAll(identifiers);
        }
    }

    /**
     * Adds the identifiers from the given {@link CompilationResult compilation results} to the ones
     * being used in the formula. If the result already contains identifiers from the given set,
     * those identifiers are not added a second time.
     */
    public void addAllIdentifierUsed(CompilationResult<T>[] argResults) {
        for (CompilationResult<T> argResult : argResults) {
            addIdentifier(argResult.getResolvedIdentifiers());
        }
    }

    private void addIdentifier(String[] identifiers) {
        for (String identifier : identifiers) {
            addIdentifierUsed(identifier);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUsedAsIdentifier(String candidate) {
        if (identifiersUsed == null) {
            return false;
        }
        return identifiersUsed.contains(candidate);
    }

    /**
     * {@inheritDoc}
     */
    public boolean successfull() {
        return !messages.containsErrorMsg();
    }

    /**
     * {@inheritDoc}
     */
    public boolean failed() {
        return messages.containsErrorMsg();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codeFragment == null) ? 0 : codeFragment.hashCode());
        result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
        result = prime * result + ((messages == null) ? 0 : messages.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractCompilationResult)) {
            return false;
        }
        AbstractCompilationResult<?> other = (AbstractCompilationResult<?>)obj;
        if (codeFragment == null) {
            if (other.codeFragment != null) {
                return false;
            }
        } else if (!codeFragment.equals(other.codeFragment)) {
            return false;
        }
        if (datatype == null) {
            if (other.datatype != null) {
                return false;
            }
        } else if (!datatype.equals(other.datatype)) {
            return false;
        }
        if (messages == null) {
            if (other.messages != null) {
                return false;
            }
        } else if (!messages.equals(other.messages)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Datatype: " + (datatype == null ? "null" : datatype.toString()) //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR + messages.toString() + codeFragment.toString();
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
