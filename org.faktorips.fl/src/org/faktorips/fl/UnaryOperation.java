package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 * 
 */
public interface UnaryOperation {
    
    public String getOperator();
    
    public Datatype getDatatype();
    
    public CompilationResultImpl generate(CompilationResultImpl arg);
    

}
