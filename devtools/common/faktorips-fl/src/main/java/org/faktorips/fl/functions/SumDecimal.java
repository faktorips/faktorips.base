/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
public class SumDecimal extends AbstractFlFunction {

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
    @Override
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
