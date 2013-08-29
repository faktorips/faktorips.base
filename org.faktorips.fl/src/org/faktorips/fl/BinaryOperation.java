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
