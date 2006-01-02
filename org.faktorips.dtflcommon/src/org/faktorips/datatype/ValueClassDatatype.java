package org.faktorips.datatype;

import org.faktorips.util.StringUtil;
import org.faktorips.datatype.NullObject;

/**
 * A datatype that represents a Java class that represents a value.
 * 
 * @author Jan Ortmann
 */
public abstract class ValueClassDatatype extends AbstractDatatype implements ValueDatatype {
	
	private Class clazz;
	private String name;

	public ValueClassDatatype(Class clazz) {
		this(clazz, StringUtil.unqualifiedName(clazz.getName()));	
	}
	
	public ValueClassDatatype(Class clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}

	/**
	 * Overridden method.
	 * @see org.faktorips.datatype.Datatype#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Overridden method.
	 * @see org.faktorips.datatype.Datatype#getQualifiedName()
	 */
    public String getQualifiedName() {
        return name;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
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
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return null;
    }

	public Class getJavaClass() {
		return clazz;
	}

	/**
	 * Overridden Method.
	 *
	 * @see org.faktorips.datatype.Datatype#getJavaClassName()
	 */
	public String getJavaClassName() {
		return clazz.getName();
	}

	/**
	 * Overridden Method.
	 *
	 * @see org.faktorips.datatype.ValueDatatype#valueToString(java.lang.Object)
	 */
	public String valueToString(Object value) {
		return value.toString();
	}
	
    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isNull(java.lang.Object)
     */
    public boolean isNull(Object value) {
        if (value==null) {
            return true;
        }
        if (!(value instanceof NullObject)) {
            return false;
        }
        return ((NullObject)value).isNull();
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
