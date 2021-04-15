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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.util.ArgumentCheck;

/**
 * A default {@link FunctionResolver}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public class DefaultFunctionResolver<T extends CodeFragment> implements FunctionResolver<T> {

    // list of supported FlFunction
    private List<FlFunction<T>> functions = new ArrayList<>();

    /**
     * Creates a new resolver.
     */
    public DefaultFunctionResolver() {
        // nothing to do
    }

    /**
     * Adds the {@link FlFunction}.
     * 
     * @throws IllegalArgumentException if function is {@code null}.
     */
    public void add(FlFunction<T> function) {
        ArgumentCheck.notNull(function);
        functions.add(function);
    }

    /**
     * Removes the {@link FlFunction} from the resolver. Does nothing if the function hasn't been
     * added before.
     * 
     * @throws IllegalArgumentException if function is {@code null}.
     */
    public void remove(FlFunction<T> function) {
        ArgumentCheck.notNull(function);
        functions.remove(function);
    }

    @Override
    public FlFunction<T>[] getFunctions() {
        @SuppressWarnings("unchecked")
        FlFunction<T>[] flFunctions = new FlFunction[functions.size()];
        return functions.toArray(flFunctions);
    }

}
