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
 * Parenthesis <em>()</em> operator for datatype primitive integer.
 */
public class ParenthesisInt extends AbstractUnaryJavaOperation {

    public ParenthesisInt() {
        super(Operation.ParenthesisInt);
    }

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
