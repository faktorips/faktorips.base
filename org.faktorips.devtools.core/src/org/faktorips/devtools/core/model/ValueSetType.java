package org.faktorips.devtools.core.model;

import org.faktorips.datatype.DefaultEnumType;
import org.faktorips.datatype.DefaultEnumValue;
import org.faktorips.datatype.EnumType;
import org.faktorips.datatype.EnumValue;

/**
 * The kind of value set.
 * 
 * @author Jan Ortmann
 */
public class ValueSetType extends DefaultEnumValue {

    /**
     * Defines the value set type specifiyinf all value. 
     */
    public final static ValueSetType ALL_VALUES;
    
    /**
     * Defines the value set type range. 
     */
    public final static ValueSetType RANGE;
    
    /**
     * Defines the value set type range. 
     */
    public final static ValueSetType ENUM;

    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ValueSetType", ValueSetType.class);
        ALL_VALUES = new ValueSetType(enumType, "allValues", "All values");
        RANGE = new ValueSetType(enumType, "range", "Range");
        ENUM = new ValueSetType(enumType, "enum", "Enumeration");
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    /**
     * Returns the value set type identified by the id. 
     * Returns <code>null</code> if the id does not identify a value set type.
     */
    public final static ValueSetType getValueSetType(String id) {
        return (ValueSetType)enumType.getEnumValue(id);
    }
    
    /**
     * Returns the value set type identified by the name. 
     * Returns <code>null</code> if the name does not identify a value set type.
     */
    public final static ValueSetType getValueSetTypeByName(String name) {
        ValueSetType[] types = getValueSetTypes();
        for (int i=0; i<types.length; i++) {
            if (types[i].getName().equals(name)) {
                return types[i];
            }
        }
        return null;
    }
    
    /**
     * Returns all value set types.
     */
    public final static ValueSetType[] getValueSetTypes() {
        EnumValue[] values = getEnumType().getValues();
        ValueSetType[] types = new ValueSetType[values.length];
        System.arraycopy(values, 0, types, 0, values.length);
        return types;
    }
    
    /**
     * Creates a new value set of this type.
     */
    public ValueSet newValueSet() {
        if (this==RANGE) {
            return new Range();
        } 
        if (this==ENUM) {
            return new EnumValueSet();
        }
        if (this==ALL_VALUES) {
            return ValueSet.ALL_VALUES;
        }
        throw new RuntimeException("Can't create a new value set for type " + this);
    }
    
    private ValueSetType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    
}
