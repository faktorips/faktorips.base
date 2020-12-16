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

import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Plus (+) operator for datatype Decimal.
 */
public class MinusDecimal extends AbstractUnaryJavaOperation {

    public MinusDecimal() {
        super(Operation.MinusDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResult)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        arg.getCodeFragment().append(".multiply(-1)"); //$NON-NLS-1$
        return arg;
    }

}
