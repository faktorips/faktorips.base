package org.faktorips.datatype;

/**
 * Abstract base class for datatypes representing a Java primtive like boolean.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatype extends AbstractDatatype implements ValueDatatype {

    public AbstractPrimitiveDatatype() {
        super();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return true;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return true;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#valueToXmlString(java.lang.Object)
     */
    public String valueToString(Object value) {
        return "" + value;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isNull(java.lang.Object)
     */
    public boolean isNull(Object value) {
        return value==null;
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isParsable(java.lang.String)
     */
    public boolean isParsable(String value) {
        try {
            getValue(value);
            return true;
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
