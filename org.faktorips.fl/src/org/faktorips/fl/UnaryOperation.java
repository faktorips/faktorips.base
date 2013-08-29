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
 * An operation combining one operator with it's single operand.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface UnaryOperation<T extends CodeFragment> {

    /**
     * Returns the operator.
     */
    public String getOperator();

    /**
     * Returns the {@link Datatype} of the operation's result.
     */
    public Datatype getDatatype();

    /**
     * Generates the {@link CompilationResult} for the given operand.
     * 
     * @param arg the operand
     * @return the given operand combined with this operation's operator
     */
    public CompilationResult<T> generate(CompilationResult<T> arg);

}
