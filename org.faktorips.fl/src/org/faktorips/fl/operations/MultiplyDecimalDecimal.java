/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Operation for the multiplication of two decimals.
 */
public class MultiplyDecimalDecimal extends AbstractBinaryJavaOperation {

    public MultiplyDecimalDecimal() {
        super(Operation.MultiplyDecimalDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.operations.AbstractBinaryJavaOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *      org.faktorips.fl.CompilationResultImpl)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".multiply("); //$NON-NLS-1$
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        return lhs;
    }

}
