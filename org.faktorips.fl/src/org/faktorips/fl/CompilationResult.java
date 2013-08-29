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

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The result of a compilation. The result consists of a list of messages generated during the
 * compilation process. If no error has occurred (and thus none of the messages is an error message)
 * the result contains the {@link CodeFragment source code} that represents the compiled expression
 * along with the expression's {@link Datatype}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface CompilationResult<T extends CodeFragment> {

    /**
     * Returns the generated source code.
     */
    public T getCodeFragment();

    /**
     * Returns the compiled expression's {@link Datatype}.
     */
    public Datatype getDatatype();

    /**
     * Returns the messages generated during compilation.
     */
    public MessageList getMessages();

    /**
     * Returns the recognized (resolved) identifiers that are used in the formula. Returns an empty
     * list if the formula does not contain any identifiers.
     */
    public String[] getResolvedIdentifiers();

    /**
     * Returns <code>true</code> if the given candidate identifier is used as identifier.
     */
    public boolean isUsedAsIdentifier(String candidateIdentifier);

    /**
     * Returns {@code true} if the compilation was successful, otherwise {@code false}.
     */
    public boolean successfull();

    /**
     * Returns {@code true} if the compilation has failed, otherwise {@code false}. If the method
     * returns {@code true}, there is a least one error {@link Message} in the {@link MessageList
     * message list}.
     */
    public boolean failed();
}
