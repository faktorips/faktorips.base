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

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

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
