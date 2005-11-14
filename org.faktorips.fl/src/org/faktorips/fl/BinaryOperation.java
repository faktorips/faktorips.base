package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 *
 */
public interface BinaryOperation {
    
    public String getOperator();
    
    public Datatype getLhsDatatype();
    
    public Datatype getRhsDatatype();
    
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs);

}
