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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;


/**
 * An abstract editor for timed objects.
 */
public abstract class TimedIpsObjectEditor extends IpsObjectEditor {

	private IIpsObjectGeneration generation;
	private Image uneditableGenerationImage;
    
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
        if (TRACE) {
            System.out.println("TimedIpsObjectEditor.setActiveGeneration(): New generation " + generation); //$NON-NLS-1$
        }
    	this.generation = generation;
    }
    
    /**
     * Returns <code>true</code> if the given generation is effective on the
     * effective date currently set in the preferences.
     */
    public boolean isEffectiveOnCurrentEffectiveDate(IIpsObjectGeneration gen) {
        if (gen==null) {
            return false;
        }
        return gen.equals(getGenerationEffectiveOnCurrentEffectiveDate());
    }
    
    /**
     * Returns the generation that is effective on the effective date currently set
     * in the preferences.
     */
    public IIpsObjectGeneration getGenerationEffectiveOnCurrentEffectiveDate() {
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        ITimedIpsObject object = (ITimedIpsObject)getIpsObject();
        if (object==null) {
            return null;
        }
        return object.getGenerationByEffectiveDate(workingDate);
    }
    
    /**
     * Returns the image for uneditable generations.
     */
    public Image getUneditableGenerationImage(Image editableImage) {
        if (uneditableGenerationImage==null) {
            uneditableGenerationImage = new Image(Display.getDefault(), editableImage, SWT.IMAGE_DISABLE);
        }
        return uneditableGenerationImage;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void disposeInternal() {
        if (uneditableGenerationImage!=null) {
            uneditableGenerationImage.dispose();
        }
    }
}
