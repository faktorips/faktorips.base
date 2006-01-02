package org.faktorips.devtools.core.model;

import java.util.GregorianCalendar;


/**
 * A point in time where the contents of a product collection changes.
 */
public interface ChangePoint {
    
    /**
     * Returns the date when the change occurs.
     */
    public GregorianCalendar getDate();
    
    /**
     * Sets the date when the change occurs.
     */
    public void setDate(GregorianCalendar newDate);
    
    /**
     * Returns true if the product collection's data valid from this change point
     * to the next one, can be modified by the user. 
     */
    public boolean isModifiable();
    
    /**
     * Sets if the collection's data valid from this change point
     * to the next one, can be modified by the user. 
     */
    public void setModifiable(boolean modifiable);
    
}
