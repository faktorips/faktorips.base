/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public CompilationResult<T> compile(CompilationResult<T>[] argResults);

    /**
     * Sets the compiler in which the function is used.
     */
    public void setCompiler(ExprCompiler<T> compiler);

    /**
     * Returns the compiler in which the function is used.
     */
    public ExprCompiler<T> getCompiler();

    /**
     * Returns the function's description.
     */
    public String getDescription();

    /**
     * Sets the function's description.
     */
    public void setDescription(String description);

}
