/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.datatype.Datatype;

/**
 *
 */
public interface BinaryOperation {

    /**
     * Sets the compiler in which the operation is used.
     */
    public void setCompiler(ExprCompiler compiler);

    /**
     * Returns the compiler this operation belongs to.
     */
    public ExprCompiler getCompiler();

    public String getOperator();

    public Datatype getLhsDatatype();

    public Datatype getRhsDatatype();

    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs);

}
