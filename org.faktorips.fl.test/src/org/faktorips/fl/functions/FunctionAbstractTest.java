/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
        DefaultFunctionResolver<JavaCodeFragment> resolver = new DefaultFunctionResolver<>();
        resolver.add(function);
        getCompiler().add(resolver);
    }
}
