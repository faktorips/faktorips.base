package org.faktorips.fl;


/**
 *
 */
public interface FlFunction extends FunctionSignature {

    /**
     * Generates the Java sourcecode for the function given the compilation results for 
     * the arguments.
     */
    public CompilationResult compile(CompilationResult[] argResults);
    
    /**
     * Sets the compiler in which the function is used.
     */
    public void setCompiler(ExprCompiler compiler);
    
    /**
     * Returns the compiler in which the function is used.
     */
    public ExprCompiler getCompiler();
    
    /**
     * Returns the function's description.
     */
    public String getDescription();
    
    /**
     * Sets the function's description.
     */
    public void setDescription(String description);
    
}
