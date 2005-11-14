package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 * A datatype representing a bean's property.
 * 
 * @author Jan Ortmann
 */
public interface PropertyDatatype extends Datatype {

    public Datatype getDatatype();
    
    /**
     * Returns a Java sourcecode fragment to access the property.
     */
    public String getGetterMethod();

}
