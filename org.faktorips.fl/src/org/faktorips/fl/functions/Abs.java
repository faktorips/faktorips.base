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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.ArgumentCheck;

/**
 * The abs() function.
 */
public class Abs extends AbstractFlFunction {

    /**
     * Constructs a abs function with the given name.
     * 
     * @param name The function name.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public Abs(String name, String description) {
        super(name, description, Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL });
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.codegen.JavaCodeFragment[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 1);
        argResults[0].getCodeFragment().append(".abs()"); //$NON-NLS-1$
        return argResults[0];
    }

}
