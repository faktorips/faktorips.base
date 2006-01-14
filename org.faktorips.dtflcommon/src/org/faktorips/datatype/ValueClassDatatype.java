package org.faktorips.datatype;

import org.faktorips.util.StringUtil;
import org.faktorips.datatype.NullObject;

/**
 * A datatype that represents a Java class representing a value, for example java.lang.String.
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
	 * Overridden.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Overridden.
	 */
    public String getQualifiedName() {
        return name;
    }
    
    /** 
     * Overridden.
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Overridden.
     */
    public boolean isValueDatatype() {
        return true;
    }
    
    /**
     * Overridden.
     */
    public Datatype getWrapperType() {
        return null;
    }

	public Class getJavaClass() {
		return clazz;
	}

	/**
	 * Overridden.
	 */
	public String getJavaClassName() {
		return clazz.getName();
	}

	/**
	 * Overridden.
	 */
	public String valueToString(Object value) {
		return value.toString();
	}
	
    /**
     * Overridden.
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
     * Overridden.
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
