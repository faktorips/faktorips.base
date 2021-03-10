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

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.Operation;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract implementation of {@link BinaryOperation}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public abstract class AbstractBinaryOperation<T extends CodeFragment> implements BinaryOperation<T> {

    private ExprCompiler<T> compiler;
    private String operator;
    private Datatype lhsDatatype;
    private Datatype rhsDatatype;

    /**
     * Creates a new unary operation for the indicated {@link Operation}.
     */
    public AbstractBinaryOperation(Operation operation) {
        this(operation.getOperator(), operation.getLhs(), operation.getRhs());
    }

    /**
     * Creates a new binary operation for the indicated left hand side and right hand side
     * {@link Datatype data types}.
     */
    public AbstractBinaryOperation(String operator, Datatype lhs, Datatype rhs) {
        ArgumentCheck.notNull(operator);
        ArgumentCheck.notNull(lhs);
        ArgumentCheck.notNull(rhs);
        this.operator = operator;
        lhsDatatype = lhs;
        rhsDatatype = rhs;
    }

    @Override
    public void setCompiler(ExprCompiler<T> compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }

    @Override
    public ExprCompiler<T> getCompiler() {
        return compiler;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getOperator()
     */
    @Override
    public String getOperator() {
        return operator;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getLhsDatatype()
     */
    @Override
    public Datatype getLhsDatatype() {
        return lhsDatatype;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getRhsDatatype()
     */
    @Override
    public Datatype getRhsDatatype() {
        return rhsDatatype;
    }

}
