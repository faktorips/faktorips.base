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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.Decimal;

/**
 * The function: Decimal sum(Decimal[])
 */
public class SumDecimal extends AbstractJavaFlFunction {

    /**
     * Constructs a sum() function with the given name.
     * 
     * @param name The function name.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public SumDecimal(String name, String description) {
        super(name, description, FunctionSignatures.SumDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.FlFunction#compile(CompilationResult[])
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".sum("); //$NON-NLS-1$
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(")"); //$NON-NLS-1$
        return new CompilationResultImpl(fragment, getType());
    }

}
