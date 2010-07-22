/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

/**
 *
 */
public interface FlFunction extends FunctionSignature {

    /**
     * Generates the Java sourcecode for the function given the compilation results for the
     * arguments.
     */
    public CompilationResult compile(CompilationResult[] argResults);

    /**
     * Sets the compiler in which the function is used.
     */
    public void setCompiler(ExprCompiler compiler);

    /**
     * Returns the compiler in which the function is used.
     */
    public ExprCompiler getCompiler();

    /**
     * Returns the function's description.
     */
    public String getDescription();

    /**
     * Sets the function's description.
     */
    public void setDescription(String description);

}
