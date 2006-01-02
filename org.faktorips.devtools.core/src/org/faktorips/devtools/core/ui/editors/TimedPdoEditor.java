package org.faktorips.devtools.core.ui.editors;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;


/**
 * An abstract editor for timed objects.
 */
public abstract class TimedPdoEditor extends IpsObjectEditor {

    /**
     * 
     */
    public TimedPdoEditor() {
        super();
    }
    
    /**
     * Returns the generation currently selected to display and edit.
     */
    public IIpsObjectGeneration getActiveGeneration() {
        GregorianCalendar workingDate = IpsPreferences.getWorkingDate();
        ITimedIpsObject object = (ITimedIpsObject)getIpsObject();
        IIpsObjectGeneration generation = object.getGenerationByEffectiveDate(workingDate);
        if (generation==null && object.getNumOfGenerations()>0) {
            generation = object.getGenerations()[object.getNumOfGenerations()-1];            
        }
        return generation;
    }
    
}
