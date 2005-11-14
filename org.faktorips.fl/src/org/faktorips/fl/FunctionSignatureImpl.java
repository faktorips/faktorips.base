package org.faktorips.fl;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 * Default implementation of FunctionSignature.
 */
public class FunctionSignatureImpl implements FunctionSignature {
    
    private String name;
    private Datatype type;
    private Datatype[] argTypes;

    /**
     * Creates a new function.
     */
    public FunctionSignatureImpl(String name, Datatype type, Datatype[] argTypes) {
        this.name = name;
        this.type = type;
        this.argTypes = argTypes;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#getType()
     */
    public Datatype getType() {
        return type;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#getName()
     */
    public String getName() {
        return name;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#getArgTypes()
     */
    public Datatype[] getArgTypes() {
        return argTypes;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#isSame(org.faktorips.fl.FunctionSignature)
     */
    public boolean match(String name, Datatype[] otherArgTypes) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (this.argTypes.length!=otherArgTypes.length) {
            return false;
        }
        for (int i=0; i<otherArgTypes.length; i++) {
            if (!argTypes[i].equals(otherArgTypes[i]) && !(argTypes[i] instanceof AnyDatatype)) {
                return false;
            }
        }
        return true;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#matchUsingConversion(java.lang.String, org.faktorips.datatype.Datatype[], org.faktorips.datatype.ConversionMatrix)
     */
    public boolean matchUsingConversion(String name, Datatype[] otherArgTypes, ConversionMatrix matrix) {
        if (!this.name.equals(name)) {
            return false;
        }
        if (this.argTypes.length!=otherArgTypes.length) {
            return false;
        }
        for (int i=0; i<otherArgTypes.length; i++) {
            if (!matrix.canConvert(otherArgTypes[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }
    
    
    /** 
     * Overridden method.
     * @see org.faktorips.fl.FunctionSignature#isSame(org.faktorips.fl.FunctionSignature)
     */
    public boolean isSame(FunctionSignature fctSignature) {
        if (!type.equals(fctSignature.getType())) {
            return false;
        }
        return match(fctSignature.getName(), fctSignature.getArgTypes());
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(type.getName());
        buffer.append(' ');
        buffer.append(name);
        buffer.append('(');
        for (int i=0; i<argTypes.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(argTypes[i].getName());
        }
        buffer.append(')');
        return buffer.toString(); 
    }

}
