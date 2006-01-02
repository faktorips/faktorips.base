package org.faktorips.codegen;

import org.faktorips.datatype.Datatype;

/**
 * 
 */
public interface SingleConversionCg {
    
    public Datatype getFrom();
    
    public Datatype getTo();
    
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue);

}
