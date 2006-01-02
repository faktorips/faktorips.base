package org.faktorips.devtools.core.ui.controller;


/**
 * Mapping between an edit field and a property of an object. 
 * 
 * @author Jan Ortmann
 */
public interface FieldPropertyMapping {

    /**
     * Returns the field this is a mapping for.
     */
    public EditField getField();
    
    /**
     * Returns the object this is a mapping for one of it's properties.
     */
    public Object getObject();

    /**
     * Returns the property's name this is a mapping for.
     */
    public String getPropertyName();

    /**
     * Updates the object's property with the value from the edit field.
     */
    public void setPropertyValue();

    /**
     * Updates the value in the edit field with the value from the obejct's property.
     */
    public void setControlValue();

}