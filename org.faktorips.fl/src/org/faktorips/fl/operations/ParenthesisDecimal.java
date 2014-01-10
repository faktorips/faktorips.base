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
 * Parenthesis <i>()</i> operator for datatype Decimal.
 */
public class ParenthesisDecimal extends AbstractUnaryJavaOperation {

    public ParenthesisDecimal() {
        super(Operation.ParenthesisDecimal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        return arg;
    }

}
