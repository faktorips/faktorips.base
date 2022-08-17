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
 * Resolves function calls used in an expression, e.g. <code>ROUND(2.34; 2)</code>. The resolver
 * receives as arguments the name of the called function along with the compilation results the
 * compiler has generated for the arguments. For the above function call, the resolver would receive
 * the name 'ROUND' and a {@link CompilationResult CompilationResult[2]} array. The first result
 * would contain the {@link CodeFragment source code} to create a decimal value of 2.34, the
 * result's data type would be {@code Decimal}. The second result would contain the
 * {@link CodeFragment source code} to create a integer value of 2 and the result's data type would
 * be {@code Integer}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface FunctionResolver<T extends CodeFragment> {

    /**
     * Returns the functions that are supported by this resolver.
     */
    FlFunction<T>[] getFunctions();

}
