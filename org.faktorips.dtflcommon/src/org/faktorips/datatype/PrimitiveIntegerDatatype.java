package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>int</code>.
 */
public class PrimitiveIntegerDatatype extends AbstractPrimitiveDatatype {

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return "int";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "int";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return Datatype.INTEGER;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return "int";
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        return Integer.valueOf(value);
    }

}
