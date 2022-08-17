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
 * Operation for the addition of two decimals.
 */
public class AddDecimalDecimal extends AbstractBinaryJavaOperation {

    public AddDecimalDecimal() {
        super(Operation.AddDecimalDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResult,
     *          org.faktorips.fl.CompilationResult)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".add("); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        return lhs;
    }

}
