package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.ArgumentCheck;


/**
 * The abs() function.
 */
public class Abs extends AbstractFlFunction {
    
    /**
     * Constructs a abs function with the given name.
     * 
     * @param name The function name.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public Abs(String name, String description) {
        super(name, description, Datatype.DECIMAL, new Datatype[] {Datatype.DECIMAL});
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.codegen.JavaCodeFragment[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 1);
        argResults[0].getCodeFragment().append(".abs()");
        return argResults[0];
    }

}
