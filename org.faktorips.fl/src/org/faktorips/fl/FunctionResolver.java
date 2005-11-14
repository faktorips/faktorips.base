package org.faktorips.fl;



/**
 * A <code>FunctionResolver</code> resolves function calls used in an 
 * expression, e.g. <code>ROUND(2.34; 2)</code>. The resolver receives as arguments the name
 * of the called function along with the compilation results the compiler has
 * generated for the arguments. For the above function call, the resolver would
 * receive the name 'ROUND' and a CompilationResult[2] array. The first result
 * would contain the sourcecode to create a Decimal value of 2.34, the result's
 * datatype would be Decimal. The second result would contain the sourcecode to 
 * create a Integer value of 2 and the result's datatype would be Integer.
 */
public interface FunctionResolver {
    
    /**
     * Returns the functions that are supported by this resolver.
     */
    public FlFunction[] getFunctions();
    
    
}
