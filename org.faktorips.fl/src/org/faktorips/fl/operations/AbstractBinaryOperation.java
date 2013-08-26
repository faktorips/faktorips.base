/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    /**
     * {@inheritDoc}
     */
    public void setCompiler(ExprCompiler<T> compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }

    /**
     * {@inheritDoc}
     */
    public ExprCompiler<T> getCompiler() {
        return compiler;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getOperator()
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getLhsDatatype()
     */
    public Datatype getLhsDatatype() {
        return lhsDatatype;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.BinaryOperation#getRhsDatatype()
     */
    public Datatype getRhsDatatype() {
        return rhsDatatype;
    }

}
