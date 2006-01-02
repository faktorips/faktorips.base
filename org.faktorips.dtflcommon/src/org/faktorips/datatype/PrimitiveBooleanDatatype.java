package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return "boolean";
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "boolean";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return Datatype.BOOLEAN;
    }
	
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return "boolean";
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        return Boolean.valueOf(value);
    }

}
