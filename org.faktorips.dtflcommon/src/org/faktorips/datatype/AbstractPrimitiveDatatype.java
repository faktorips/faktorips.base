package org.faktorips.datatype;

import org.apache.commons.lang.StringUtils;

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
        return false;
    }
    
    /**
     * If the value is <code>null</code> or an empty string, <code>false</code> is
     * returned.
     * 
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            getValue(value);
            return true;
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
