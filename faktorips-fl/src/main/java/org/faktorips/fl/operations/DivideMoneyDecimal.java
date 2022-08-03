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

import java.math.RoundingMode;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Operation for the multiplication of two decimals.
 */
public class DivideMoneyDecimal extends AbstractBinaryJavaOperation {

    public DivideMoneyDecimal() {
        super(Operation.DivideMoneyDecimal);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.operations.AbstractBinaryJavaOperation#generate(org.faktorips.fl.CompilationResultImpl,
     *          org.faktorips.fl.CompilationResultImpl)
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment fragment = lhs.getCodeFragment();
        fragment.append(".divide("); //$NON-NLS-1$
        lhs.add(rhs);
        fragment.append(", "); //$NON-NLS-1$
        fragment.appendClassName(RoundingMode.class);
        fragment.append(".HALF_UP)"); //$NON-NLS-1$
        lhs.setDatatype(Datatype.MONEY);
        return lhs;
    }

}
