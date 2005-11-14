package org.faktorips.fl.functions;

import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.DefaultFunctionResolver;
import org.faktorips.fl.FlFunction;


/**
 *
 */
public class AbstractFunctionTest extends AbstractCompilerTest {

    protected void registerFunction(FlFunction function) {
        DefaultFunctionResolver resolver = new DefaultFunctionResolver();
        resolver.add(function);
        compiler.add(resolver);
    }
}
