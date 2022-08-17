/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    String PLUS = "+";

    String MINUS = "-";

    String MULTIPLY = "*";

    String DIVIDE = "/";

    String GREATER_THAN = ">";

    String GREATER_THAN_OR_EQUAL = ">=";

    String LESSER_THAN = "<";

    String LESSER_THAN_OR_EQUAL = "<=";

    String EQUAL = "=";

    String NOT_EQUAL = "!=";

    /**
     * Sets the compiler in which the operation is used.
     */
    void setCompiler(ExprCompiler<T> compiler);

    /**
     * Returns the compiler this operation belongs to.
     */
    ExprCompiler<T> getCompiler();

    /**
     * Returns the operator.
     */
    String getOperator();

    /**
     * Returns the {@link Datatype} of the left hand side operand.
     */
    Datatype getLhsDatatype();

    /**
     * Returns the {@link Datatype} of the right hand side operand.
     */
    Datatype getRhsDatatype();

    /**
     * Generates the combined {@link CompilationResult} from the given operands.
     * 
     * @param lhs the left hand side operand
     * @param rhs the right hand side operand
     * @return the given operands combined with this operation's operator
     */
    CompilationResult<T> generate(CompilationResult<T> lhs, CompilationResult<T> rhs);

}
