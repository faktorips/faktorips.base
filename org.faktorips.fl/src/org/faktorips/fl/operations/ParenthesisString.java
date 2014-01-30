/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Parenthesis <i>()</i> operator for datatype String.
 */
public class ParenthesisString extends AbstractUnaryJavaOperation {

    public ParenthesisString() {
        super(Operation.ParenthesisString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        return arg;
    }
}
