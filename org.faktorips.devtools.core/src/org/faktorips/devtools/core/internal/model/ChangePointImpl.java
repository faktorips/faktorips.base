package org.faktorips.devtools.core.internal.model;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.model.ChangePoint;


/**
 *
 */
public class ChangePointImpl implements ChangePoint {
    
    private GregorianCalendar date;
    private boolean modifiable = true;

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ChangePoint#getDate()
     */
    public GregorianCalendar getDate() {
        return date;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ChangePoint#isModifiable()
     */
    public boolean isModifiable() {
        return modifiable;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ChangePoint#setDate(java.util.GregorianCalendar)
     */
    public void setDate(GregorianCalendar newDate) {
        date = newDate;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.ChangePoint#setModifiable(boolean)
     */
    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }
    

}
