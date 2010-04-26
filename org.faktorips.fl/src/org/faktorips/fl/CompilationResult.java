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

package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * The result of a compilation. The result consists of a list of messages generated during the
 * compilation process. If no error has occurred (and thus none of the messages is an error message)
 * the result contains the Java sourcecode that represents the compiled expression along with the
 * expression's datatype.
 */
public interface CompilationResult {

    /**
     * Returns the generated Java sourcecode.
     */
    public JavaCodeFragment getCodeFragment();

    /**
     * Returns the compiled expression's datatype.
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
     * Returns true if the compilation was successfull, otherwise false.
     */
    public boolean successfull();

    /**
     * Returns true if the compilation has failed, otherwise false. If the method returns true,
     * there is a least one error message in the message list.
     */
    public boolean failed();
}
