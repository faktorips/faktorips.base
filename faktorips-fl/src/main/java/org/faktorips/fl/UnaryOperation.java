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

/**
 * An operation combining one operator with it's single operand.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface UnaryOperation<T extends CodeFragment> {

    String NOT = "!";

    /**
     * Returns the operator.
     */
    String getOperator();

    /**
     * Returns the {@link Datatype} of the operation's result.
     */
    Datatype getDatatype();

    /**
     * Generates the {@link CompilationResult} for the given operand.
     * 
     * @param arg the operand
     * @return the given operand combined with this operation's operator
     */
    CompilationResult<T> generate(CompilationResult<T> arg);

}
