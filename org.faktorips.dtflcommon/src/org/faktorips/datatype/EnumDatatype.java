package org.faktorips.datatype;

/**
 * A value datatype representing an enumeration of values.
 * 
 * @author Jan Ortmann
 */
public interface EnumDatatype extends ValueDatatype {

    /**
     * Returns the ids of all values defined in the enum type.
     */
    public String[] getAllValueIds();
}
