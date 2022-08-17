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

import java.math.RoundingMode;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class WholeNumber extends AbstractFlFunction {

    /**
     */
    public WholeNumber(String name, String description) {
        super(name, description, FunctionSignatures.WholeNumber);
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
        fragment.appendClassName(Integer.class);
        fragment.append(".valueOf("); //$NON-NLS-1$
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale(0, "); //$NON-NLS-1$
        fragment.appendClassName(RoundingMode.class);
        fragment.append(".DOWN).intValue())"); //$NON-NLS-1$
        return new CompilationResultImpl(fragment, Datatype.INTEGER);
    }

}
