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
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilationResult;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;

/**
 * Abstract implementation of {@link BinaryOperation} for {@link JavaCodeFragment Java code}
 * generating operations.
 */
public abstract class AbstractBinaryJavaOperation extends AbstractBinaryOperation<JavaCodeFragment> {

    /**
     * Creates a new binary operation for the indicated {@link Operation}.
     */
    public AbstractBinaryJavaOperation(Operation operation) {
        super(operation);
    }

    /**
     * Creates a new binary operation for the indicated left hand side and right hand side
     * {@link Datatype data types}.
     */
    public AbstractBinaryJavaOperation(String operator, Datatype lhs, Datatype rhs) {
        super(operator, lhs, rhs);
    }

    @Override
    public AbstractCompilationResult<JavaCodeFragment> generate(CompilationResult<JavaCodeFragment> lhs,
            CompilationResult<JavaCodeFragment> rhs) {
        CompilationResultImpl result = generate((CompilationResultImpl)lhs, (CompilationResultImpl)rhs);
        return result;
    }

    /**
     * Generates the combined {@link CompilationResult} from the given operands.
     * 
     * @param lhs the left hand side operand
     * @param rhs the right hand side operand
     * @return the given operands combined with this operation's operator
     */
    public abstract CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs);

}
