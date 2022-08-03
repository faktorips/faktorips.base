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
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.Operation;
import org.faktorips.fl.UnaryOperation;

/**
 * Abstract implementation of {@link UnaryOperation} for {@link JavaCodeFragment Java code}
 * generating operations.
 */
public abstract class AbstractUnaryJavaOperation extends AbstractUnaryOperation<JavaCodeFragment> {

    /**
     * Creates a new unary operation for the indicated {@link Operation}.
     */
    public AbstractUnaryJavaOperation(Operation operation) {
        super(operation);
    }

    /**
     * Creates a new unary operation for the indicated operator and {@link Datatype data type}.
     */
    public AbstractUnaryJavaOperation(Datatype datatype, String operator) {
        super(datatype, operator);
    }

    @Override
    public CompilationResult<JavaCodeFragment> generate(CompilationResult<JavaCodeFragment> arg) {
        return generate((CompilationResultImpl)arg);
    }

    /**
     * Generates the {@link CompilationResult} for the given operand.
     * 
     * @param arg the operand
     * @return the given operand combined with this operation's operator
     */
    public abstract CompilationResultImpl generate(CompilationResultImpl arg);

}
