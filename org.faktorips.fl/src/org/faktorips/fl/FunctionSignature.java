package org.faktorips.fl;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 * Interface that defines a function's signature.
 */
public interface FunctionSignature {
    
    /**
     * Returns the function's return type.
     */
    public Datatype getType();
    
    /**
     * Returns the function's name.
     */
    public String getName();
    
    /**
     * Returns the function's arguments' types.
     */
    public Datatype[] getArgTypes();
    
    /**
     * Returns true if the given fctSignature is the same as this one. This is the
     * case if they have the same type, name and the arguments' types are the same
     * (in the same order).
     * <p>
     * We could also use <code>equals()</code> here, but by defining a new function
     * we emphasize the fact, that classes implementing this interface must implement
     * the desired functionality. This is not the case with the standard 
     * <code>equals()</code> method.
     */
    public boolean isSame(FunctionSignature fctSignature);

    /**
     * Returns true if this function signature has the indicated name and argument
     * types.
     */
    public boolean match(String name, Datatype[] argTypes);
    
    /**
     * Returns true if this function signature has the indicated name and if each
     * given argument type is either equal to this function's argument type or can
     * be convert to it.  
     */
    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix);
    
}
