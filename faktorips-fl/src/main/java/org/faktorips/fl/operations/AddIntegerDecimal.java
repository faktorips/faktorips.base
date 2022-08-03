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
public class AddIntegerDecimal extends AbstractBinaryJavaOperation {

    public AddIntegerDecimal() {
        super(Operation.AddIntegerDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.operations.AbstractBinaryJavaOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *          org.faktorips.fl.CompilationResultImpl)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {

        rhs.addCodeFragment(".add(");
        rhs.add(lhs);
        rhs.addCodeFragment(")");
        return rhs;
    }

}
