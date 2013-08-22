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
    private List<FlFunction<T>> functions = new ArrayList<FlFunction<T>>();

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

    public FlFunction<T>[] getFunctions() {
        @SuppressWarnings("unchecked")
        FlFunction<T>[] flFunctions = new FlFunction[functions.size()];
        return functions.toArray(flFunctions);
    }

}
