package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 * A datatype representing a Java bean with it's properties.
 * 
 * @author Jan Ortmann
 */
public interface BeanDatatype extends Datatype {

    /**
     * Returns the property datatype representing the property with the given name.
     */
    public PropertyDatatype getProperty(String name);

}
