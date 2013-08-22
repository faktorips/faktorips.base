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

/**
 * A function used in the formula language.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface FlFunction<T extends CodeFragment> extends FunctionSignature {

    /**
     * Generates the {@link CompilationResult source code} for the function given the
     * {@link CompilationResult compilation results} for the arguments.
     */
    public CompilationResult<T> compile(CompilationResult<T>[] argResults);

    /**
     * Sets the compiler in which the function is used.
     */
    public void setCompiler(ExprCompiler<T> compiler);

    /**
     * Returns the compiler in which the function is used.
     */
    public ExprCompiler<T> getCompiler();

    /**
     * Returns the function's description.
     */
    public String getDescription();

    /**
     * Sets the function's description.
     */
    public void setDescription(String description);

}
