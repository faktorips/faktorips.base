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
 * Resolves function calls used in an expression, e.g. <code>ROUND(2.34; 2)</code>. The resolver
 * receives as arguments the name of the called function along with the compilation results the
 * compiler has generated for the arguments. For the above function call, the resolver would receive
 * the name 'ROUND' and a {@link CompilationResult CompilationResult[2]} array. The first result
 * would contain the {@link CodeFragment source code} to create a decimal value of 2.34, the
 * result's data type would be {@code Decimal}. The second result would contain the
 * {@link CodeFragment source code} to create a integer value of 2 and the result's data type would
 * be {@code Integer}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public interface FunctionResolver<T extends CodeFragment> {

    /**
     * Returns the functions that are supported by this resolver.
     */
    public FlFunction<T>[] getFunctions();

}
