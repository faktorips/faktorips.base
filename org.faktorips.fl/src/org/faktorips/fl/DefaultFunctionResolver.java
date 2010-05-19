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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.util.ArgumentCheck;

/**
 * A default FunctionResolver.
 */
public class DefaultFunctionResolver implements FunctionResolver {

    // list of supported FlFunction
    private List<FlFunction> functions = new ArrayList<FlFunction>();

    /**
     * Creates a new resolver.
     */
    public DefaultFunctionResolver() {
        // nothing to do
    }

    /**
     * Adds the FlFunction.
     * 
     * @throws IllegalArgumentException if function is null.
     */
    public void add(FlFunction function) {
        ArgumentCheck.notNull(function);
        functions.add(function);
    }

    /**
     * Removes the FlFunction from the resolver. Does nothing if the function hasn't been added
     * before.
     * 
     * @throws IllegalArgumentException if function is null.
     */
    public void remove(FlFunction function) {
        ArgumentCheck.notNull(function);
        functions.remove(function);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FunctionResolver#getFunctions()
     */
    public FlFunction[] getFunctions() {
        return functions.toArray(new FlFunction[functions.size()]);
    }

}
