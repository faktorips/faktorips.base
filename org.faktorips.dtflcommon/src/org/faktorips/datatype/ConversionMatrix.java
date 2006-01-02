package org.faktorips.datatype;

/**
 * A <code>ConversionMatric</code> holds the information if the value of a datatype 
 * can be converted into the value of another datatype.
 */
public interface ConversionMatrix {

    /**
     * Returns true if a value of datatype from can be converted into one of datatype to.
     * If datatype from and to are equal, the method returns true.
     * 
     * @throws IllegalArgumentException if either from or to is null.
     */
    public boolean canConvert(Datatype from, Datatype to);
    
}
