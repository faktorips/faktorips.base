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

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.DefaultFunctionResolver;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.JavaExprCompilerAbstractTest;

/**
 *
 */
public abstract class FunctionAbstractTest extends JavaExprCompilerAbstractTest {

    protected void registerFunction(FlFunction<JavaCodeFragment> function) {
        DefaultFunctionResolver<JavaCodeFragment> resolver = new DefaultFunctionResolver<JavaCodeFragment>();
        resolver.add(function);
        compiler.add(resolver);
    }
}
