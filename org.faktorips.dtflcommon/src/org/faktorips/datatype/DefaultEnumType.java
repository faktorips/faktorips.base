package org.faktorips.datatype;

import org.apache.commons.lang.StringUtils;
import org.faktorips.util.ArgumentCheck;


/**
 * Default implementation of enum type.
 */
public class DefaultEnumType implements EnumType {
    
    private String name;
    private Class valueClass; // Java class representing the values.
    private DefaultEnumValue[] values = new DefaultEnumValue[0];

    /**
     * Creates a new enum type.
     * 
     * @param name The type's name.
     * @param valueClass Java class the values are instances of.
     * 
     * @throws IllegalArgumentException if name is null or if the valueClass is
     * not a subclass of DefaultEnumValue. 
     */
    public DefaultEnumType(String name, Class valueClass) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.isSubclassOf(valueClass, DefaultEnumValue.class);
        this.name = name;
        this.valueClass = valueClass;
    }
    
    /**
     * Adds the value to the type.
     * 
     * @throws IllegalArgumentException if the type contains already an id
     * with the given id.
     */
    void addValue(DefaultEnumValue newValue) {
        if (containsValue(newValue.getId())) {
            throw new IllegalArgumentException("The enum type " + this + " contains already a value " + newValue);
        }
        DefaultEnumValue[] newValues = new DefaultEnumValue[values.length+1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = newValue;
        values = newValues;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.EnumType#getValues()
     */
    public EnumValue[] getValues() {
        DefaultEnumValue[] copy = new DefaultEnumValue[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.EnumType#getValueIds()
     */
    public String[] getValueIds() {
        String[] ids = new String[values.length];
        for (int i=0; i<values.length; i++) {
            ids[i] = values[i].getId();        
        }
        return ids;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.EnumType#containsValue(java.lang.String)
     */
    public boolean containsValue(String id) {
        for (int i=0; i<values.length; i++) {
            if (StringUtils.equals(id, values[i].getId())) {
                return true;
            }
        }
        return false;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.EnumType#getValue(java.lang.String)
     */
    public EnumValue getEnumValue(String id) throws IllegalArgumentException {
        for (int i=0; i<values.length; i++) {
            if (StringUtils.equals(id, values[i].getId())) {
                return values[i];
            }
        }
        return null;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        return getEnumValue(value);
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.datatype.ValueDatatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return null;
    }
    
    /**
     * Overridden Method.
     * @see org.faktorips.datatype.ValueDatatype#valueToString(java.lang.Object)
     */
    public String valueToString(Object value) {
        return ((EnumValue)value).getId();
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
     * @see org.faktorips.datatype.Datatype#isVoid()
     */
    public boolean isVoid() {
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
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return valueClass.getName();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.valueset.ValueSet#getDatatype()
     */
    public Datatype getDatatype() {
        return this;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.valueset.ValueSet#contains(java.lang.Object)
     */
    public boolean contains(Object value) {
        if (!(value instanceof EnumValue)) {
            return false;
        }
        return contains((EnumValue)value);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.valueset.ValueSet#containsNull()
     */
    public boolean containsNull() {
        return contains((String)null);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.valueset.ValueSet#getNumOfValues()
     */
    public int getNumOfValues() {
       return values.length;
    }

    /** 
     * Overridden method.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        Datatype datatype = (Datatype)o;
        return name.compareTo(datatype.getName());
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
     * @see org.faktorips.datatype.EnumType#getEnumValue(int)
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException {
        return values[index];
    }
    
    public String toString() {
        return name;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String[])
     */
    public Object[] getValues(String[] value) {
        EnumValue [] elements = new EnumValue[value.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i]=(EnumValue)getValue(value[i]);
            
        }
        return elements;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isParsable(java.lang.String)
     */
    public boolean isParsable(String value) {
        return getEnumValue(value)!=null;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isNull(java.lang.Object)
     */
    public boolean isNull(Object value) {
        return value==null;
    }

}
