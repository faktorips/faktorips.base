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

package org.faktorips.fl;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * An operation combining two operands with one operator.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface BinaryOperation<T extends CodeFragment> {

    /**
     * Sets the compiler in which the operation is used.
     */
    public void setCompiler(ExprCompiler<T> compiler);

    /**
     * Returns the compiler this operation belongs to.
     */
    public ExprCompiler<T> getCompiler();

    /**
     * Returns the operator.
     */
    public String getOperator();

    /**
     * Returns the {@link Datatype} of the left hand side operand.
     */
    public Datatype getLhsDatatype();

    /**
     * Returns the {@link Datatype} of the right hand side operand.
     */
    public Datatype getRhsDatatype();

    /**
     * Generates the combined {@link CompilationResult} from the given operands.
     * 
     * @param lhs the left hand side operand
     * @param rhs the right hand side operand
     * @return the given operands combined with this operation's operator
     */
    public CompilationResult<T> generate(CompilationResult<T> lhs, CompilationResult<T> rhs);

}
