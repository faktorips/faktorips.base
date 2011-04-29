/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

/**
 * A <code>FunctionResolver</code> resolves function calls used in an expression, e.g.
 * <code>ROUND(2.34; 2)</code>. The resolver receives as arguments the name of the called function
 * along with the compilation results the compiler has generated for the arguments. For the above
 * function call, the resolver would receive the name 'ROUND' and a CompilationResult[2] array. The
 * first result would contain the sourcecode to create a Decimal value of 2.34, the result's
 * datatype would be Decimal. The second result would contain the sourcecode to create a Integer
 * value of 2 and the result's datatype would be Integer.
 */
public interface FunctionResolver {

    /**
     * Returns the functions that are supported by this resolver.
     */
    public FlFunction[] getFunctions();

}
