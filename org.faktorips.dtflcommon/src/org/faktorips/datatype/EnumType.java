package org.faktorips.datatype;


/**
 * An EnumType is a Datatype that represents an enumeration of values e.g. gender, payment mode.
 * 
 * @author Jan Ortmann
 */
public interface EnumType extends ValueDatatype {
    
    /**
     * Returns the type's values or an empty array if the type has no values.
     */
    public EnumValue[] getValues();
    
    /**
     * Returns the type's value ids as string array. 
     */
    public String[] getValueIds();  

    /**
     * Returns true if the id identifies an EnumValue, otherwise false.
     */
    public boolean containsValue(String id);
    
    /**
     * Returns the EnumValue identified by the given id.
     * 
     * @throws IllegalArgumentException if the id does not identify an EnumValue. 
     */
    public EnumValue getEnumValue(String id) throws IllegalArgumentException;
    
    /**
     * Returns the EnumValue at the given index.
     * 
     * @throws IllegalArgumentException if the index is out of bounce. 
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException;
    /**
     * Returns the number of possible values 
     */
    public int getNumOfValues();
    
}
