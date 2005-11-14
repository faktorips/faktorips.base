package org.faktorips.fl;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.util.ArgumentCheck;


/**
 * A default FunctionResolver.
 */
public class DefaultFunctionResolver implements FunctionResolver {
    
    // list of supported FlFunction 
    private List functions = new ArrayList();
    
    /**
     * Creates a new resolver.
     */
    public DefaultFunctionResolver() {
    }

    /**
     * Adds the FlFunction.
     * 
     * @throws IllegalArgumentException if function is null.  
     */
    public void add(FlFunction function) {
        ArgumentCheck.notNull(function); 
        functions.add(function);
    }

    /**
     * Removes the FlFunction from the resolver. Does nothing if the function
     * hasn't been added before.
     * 
     * @throws IllegalArgumentException if function is null.  
     */
    public void remove(FlFunction function) {
        ArgumentCheck.notNull(function); 
        functions.remove(function);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.fl.FunctionResolver#getFunctions()
     */
    public FlFunction[] getFunctions() {
        return (FlFunction[])functions.toArray(new FlFunction[functions.size()]);
    }
    
}
