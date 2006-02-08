package org.faktorips.devtools.core.ui.editors;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;


/**
 * An abstract editor for timed objects.
 */
public abstract class TimedIpsObjectEditor extends IpsObjectEditor {

	private IIpsObjectGeneration generation;
	
    /**
     * 
     */
    public TimedIpsObjectEditor() {
        super();
    }
    
    /**
     * Returns the generation currently selected to display and edit.
     */
    public IIpsObjectGeneration getActiveGeneration() {
    	return generation;
    }
    
    /**
     * Sets the generation active on this editor.
     */
    public void setActiveGeneration(IIpsObjectGeneration generation) {
    	this.generation = generation;
    }
    
    /**
     * Returns the generation which is preferred to be displayed to match the 
     * working date set in preferences.
     */
    public IIpsObjectGeneration getPreferredGeneration() {
        GregorianCalendar workingDate = IpsPreferences.getWorkingDate();
        ITimedIpsObject object = (ITimedIpsObject)getIpsObject();
        IIpsObjectGeneration prefGen = object.getGenerationByEffectiveDate(workingDate);
        if (prefGen==null && object.getNumOfGenerations()>0) {
        	prefGen = object.getGenerations()[object.getNumOfGenerations()-1];            
        }
        return prefGen;    	
    }
    
}
