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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Parenthesis <i>()</i> operator for datatype primitive integer.
 */
public class ParenthesisInt extends AbstractUnaryJavaOperation {

    public ParenthesisInt() {
        super(Operation.ParenthesisInt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        JavaCodeFragment parenthesis = new JavaCodeFragment();
        parenthesis.append('(');
        parenthesis.append(arg.getCodeFragment());
        parenthesis.append(')');
        CompilationResultImpl result = new CompilationResultImpl(parenthesis, arg.getDatatype());
        result.addMessages(arg.getMessages());
        return result;
    }

}
