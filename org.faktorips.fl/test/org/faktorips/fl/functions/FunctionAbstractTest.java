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

package org.faktorips.fl.functions;

import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.DefaultFunctionResolver;
import org.faktorips.fl.FlFunction;

/**
 *
 */
public abstract class FunctionAbstractTest extends CompilerAbstractTest {

    protected void registerFunction(FlFunction function) {
        DefaultFunctionResolver resolver = new DefaultFunctionResolver();
        resolver.add(function);
        compiler.add(resolver);
    }
}
