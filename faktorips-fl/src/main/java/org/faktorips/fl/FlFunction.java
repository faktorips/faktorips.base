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

/**
 * A function used in the formula language.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface FlFunction<T extends CodeFragment> extends FunctionSignature {

    /**
     * Generates the {@link CompilationResult source code} for the function given the
     * {@link CompilationResult compilation results} for the arguments.
     */
    CompilationResult<T> compile(CompilationResult<T>[] argResults);

    /**
     * Sets the compiler in which the function is used.
     */
    void setCompiler(ExprCompiler<T> compiler);

    /**
     * Returns the compiler in which the function is used.
     */
    ExprCompiler<T> getCompiler();

    /**
     * Returns the function's description.
     */
    String getDescription();

    /**
     * Sets the function's description.
     */
    void setDescription(String description);

}
