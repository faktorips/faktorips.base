/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Minus (-) operator for datatype primitive int.
 */
public class MinusPrimitiveInt extends AbstractUnaryJavaOperation {

    public MinusPrimitiveInt() {
        super(Operation.MinusPrimitiveInt);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResult)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        if (arg.failed()) {
            return arg;
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append('-');
        fragment.append(arg.getCodeFragment());
        arg.setCodeFragment(fragment);
        return arg;
    }

}
