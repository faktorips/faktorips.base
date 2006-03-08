/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

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
