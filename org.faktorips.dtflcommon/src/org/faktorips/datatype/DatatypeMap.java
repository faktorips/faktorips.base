package org.faktorips.datatype;


/**
 * A map that stores <code>Datatype</code>s as values and uses their name as the access key.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeMap {

    /**
     * Returns the Datatype with the given name, if the registry contains a datatype with the given name.
     * Returns null, if the map does not contain a datatype with the given name.
     * 
     * @throws IllegalArgumentException if the name is null.
     */
    public abstract Datatype getDatatype(String name) throws IllegalArgumentException;
    
    /**
     * Returns the datatyppes available in the map.
     */
    public abstract Datatype[] getDatatypes();

}
