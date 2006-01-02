package org.faktorips.devtools.core.model;


/**
 * The interface defines methods to access the values of extension properties.
 * 
 * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition
 *  
 * @author Jan Ortmann
 */
public interface IExtensionPropertyAccess {

    /**
     * Returns the object's value for extension property identified by the id.
     * If the extension property is not defined in the current installation
     * the method return <code>null</code>. 
     * 
     * @param propertyId The id of an extension property.
     * 
     * @throws IllegalArgumentException if no exentension property with given id is defined for
     * this object's type.
     */
    public Object getExtPropertyValue(String propertyId);

    /**
     * Sets the object's value for the extension property identified by the id.
     * The method does not check if extension property is defined in the current installation.
     * 
     * @param propertyId The id of an extension property.
     * @param value The object's new value for the property.
     * 
     * @throws IllegalArgumentException if no exentension property with given id is defined for
     * this object's type.
     */
    public void setExtPropertyValue(String propertyId, Object value);
    
}
