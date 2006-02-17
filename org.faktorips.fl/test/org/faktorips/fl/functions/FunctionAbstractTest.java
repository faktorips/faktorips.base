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
