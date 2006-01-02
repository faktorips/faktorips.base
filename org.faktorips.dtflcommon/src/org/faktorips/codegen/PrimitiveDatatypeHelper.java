package org.faktorips.codegen;

/**
 * Extended DatatypeHelper variant for datatypes representing Java's primitives.
 */
public interface PrimitiveDatatypeHelper extends DatatypeHelper {

    /**
     * Given a JavaCodeFragment containing an expression of the primitive type
     * this is a helper for, returns a JavaCodeFragment that converts the given
     * expression to the appropriate wrapper class.
     * 
     * @throws IllegalArgumentException if expression is null.
     */
    public JavaCodeFragment toWrapper(JavaCodeFragment expression);

}
