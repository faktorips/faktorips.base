package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;

/**
 * Default implementation of enum value.
 */
public class DefaultEnumValue implements EnumValue {
    
    private DefaultEnumType type;
    private String id;
    private String name;
    
    public DefaultEnumValue(DefaultEnumType type, String id) {
        this(type, id, id);
    }

    public DefaultEnumValue(DefaultEnumType type, String id, String name) {
        ArgumentCheck.notNull(type);
        this.type = type;
        this.id = id;
        this.name = name;
        type.addValue(this);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.EnumValue#getType()
     */
    public EnumType getType() {
        return type;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.EnumValue#getId()
     */
    public String getId() {
        return id;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.EnumValue#getName()
     */
    public String getName() {
        return name;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof EnumValue )) {
            return false;
        }
        EnumValue other = (EnumValue )o;
        return id.equals(other.getId()) && type.equals(other.getType());
    }
    
    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return type.toString() + "." + id;
    }

    /** 
     * Overridden method.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        EnumValue other = (EnumValue )o;
        return id.compareTo(other.getId());
    }

}
