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
 * Parenthesis <em>()</em> operator for datatype Money.
 */
public class ParenthesisMoney extends AbstractUnaryJavaOperation {

    public ParenthesisMoney() {
        super(Operation.ParenthesisMoney);
    }

    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        return arg;
    }

}
