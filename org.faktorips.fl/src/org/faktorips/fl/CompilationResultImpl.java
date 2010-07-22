/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Implementation of the CompilationResult interface.
 */
public class CompilationResultImpl implements CompilationResult {

    /**
     * Creates a new result, that contains a message that the given identifier is undefined. This
     * method is intended to be used by implementations of <code>IdentifierResolver</code>
     */
    public final static CompilationResult newResultUndefinedIdentifier(Locale locale, String identifier) {
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale, identifier);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }

    /**
     * Extracts the datatypes from an array of compilation results.
     */
    public final static Datatype[] getDatatypes(CompilationResult[] results) {
        Datatype[] types = new Datatype[results.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = results[i].getDatatype();
        }
        return types;
    }

    private JavaCodeFragment codeFragment;
    private MessageList messages;
    private Datatype datatype;
    private Set<String> identifiersUsed;

    /**
     * Creates a CompilationResult with the given parameters.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype, MessageList messages,
            Set<String> identifiers) {
        codeFragment = sourcecode;
        this.datatype = datatype;
        this.messages = messages;
        identifiersUsed = identifiers;
    }

    /**
     * Creates a CompilationResult that contains the given sourcecode fragment and datatype.
     */
    public CompilationResultImpl(JavaCodeFragment sourcecode, Datatype datatype) {
        codeFragment = sourcecode;
        this.datatype = datatype;
        messages = new MessageList();
    }

    /**
     * Creates a CompilationResult that contains the given sourcecode and datatype.
     * 
     * @throws IllegalArgumentException if either sourcecode or datatype is null.
     */
    public CompilationResultImpl(String sourcecode, Datatype datatype) {
        this(new JavaCodeFragment(sourcecode), datatype);
    }

    /**
     * Creates a CompilationResult that contains the given message.
     * 
     * @throws IllegalArgumentException if msg is null.
     */
    public CompilationResultImpl(Message message) {
        messages = new MessageList(message);
        codeFragment = new JavaCodeFragment();
    }

    /**
     * Creates a new CompilationResult.
     */
    public CompilationResultImpl() {
        codeFragment = new JavaCodeFragment();
        messages = new MessageList();
    }

    /**
     * Appends the given compilation result's sourcecode fragment and messages to this result's
     * sourcecode fragment. This result's datatype remains unchanged.
     */
    public void add(CompilationResult result) {
        codeFragment.append(result.getCodeFragment());
        messages.add(result.getMessages());
        Set<String> otherIdenifiers = ((CompilationResultImpl)result).identifiersUsed;
        if (otherIdenifiers == null) {
            return;
        }
        if (identifiersUsed == null) {
            identifiersUsed = new LinkedHashSet<String>(2);
        }
        identifiersUsed.addAll(otherIdenifiers);
    }

    /**
     * Returns the generated Java sourcecode.
     */
    public JavaCodeFragment getCodeFragment() {
        return codeFragment;
    }

    /**
     * Sets the code fragment.
     */
    public void setJavaCodeFragment(JavaCodeFragment code) {
        codeFragment = code;
    }

    /**
     * Adds the code fragment to the result ones.
     */
    public void addCodeFragment(JavaCodeFragment code) {
        codeFragment.append(code);
    }

    /**
     * Adds the code fragment to the result ones.
     */
    public void addCodeFragment(String code) {
        codeFragment.append(code);
    }

    /**
     * Sets the result's datatype.
     */
    public void setDatatype(Datatype newType) {
        datatype = newType;
    }

    /**
     * Returns the compiled expression's datatype.
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
     * @throws IllegalArgumentException if msg is null.
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

    public Set<String> getIdentifiersUsedAsSet() {
        return identifiersUsed;
    }

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

    public void addAllIdentifierUsed(CompilationResult[] argResults) {
        for (CompilationResult argResult : argResults) {
            addIdentifier(argResult.getResolvedIdentifiers());
        }
    }

    private void addIdentifier(String[] identifiers) {
        for (String identifier : identifiers) {
            addIdentifierUsed(identifier);
        }
    }

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
        if (!(obj instanceof CompilationResultImpl)) {
            return false;
        }
        CompilationResultImpl other = (CompilationResultImpl)obj;
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

}
