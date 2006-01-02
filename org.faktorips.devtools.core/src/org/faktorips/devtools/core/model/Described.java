package org.faktorips.devtools.core.model;


/**
 * An interface that marks an object as having a description.
 * 
 * @author Jan Ortmann
 */
public interface Described {
    
    /**
     * Sets the description.
     * 
     * @throws IllegalArgumentException if newDescription is null.
     */
    public abstract void setDescription(String newDescription);
    
    /**
     * Returns the object's description. This method never returns null.
     */
    public abstract String getDescription();

}
