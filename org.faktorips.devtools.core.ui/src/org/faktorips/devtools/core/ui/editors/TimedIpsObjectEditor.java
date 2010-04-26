/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;

/**
 * An abstract editor for timed objects.
 */
public abstract class TimedIpsObjectEditor extends IpsObjectEditor {

    private List<IActiveGenerationChangedListener> activeGenerationChangedListeners = new ArrayList<IActiveGenerationChangedListener>(
            1);

    private IIpsObjectGeneration generation;

    public TimedIpsObjectEditor() {
        super();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setContentDescription(Messages.TimedIpsObjectEditor_actualWorkingDate
                + IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate());
        IpsPlugin.getDefault().getIpsPreferences().addChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(IpsPreferences.WORKING_DATE)) {
                    setContentDescription(Messages.TimedIpsObjectEditor_actualWorkingDate
                            + IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate());
                }
            }
        });
    }

    /**
     * Adds a new listener that is notified if the active generated has changed.
     */
    public void addListener(IActiveGenerationChangedListener listener) {
        if (!activeGenerationChangedListeners.contains(listener)) {
            activeGenerationChangedListeners.add(listener);
        }
    }

    /**
     * Removes the listener from the list of listeners that are informed about changes of the active
     * generation.
     */
    public void removeListener(IActiveGenerationChangedListener listener) {
        activeGenerationChangedListeners.remove(listener);
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

        notifyGenerationChanged();
    }

    /**
     *
     */
    private void notifyGenerationChanged() {
        List<IActiveGenerationChangedListener> copy = new ArrayList<IActiveGenerationChangedListener>(
                activeGenerationChangedListeners);
        for (IActiveGenerationChangedListener listener : copy) {
            listener.activeGenerationChanged(generation);
        }
    }

    /**
     * Returns <code>true</code> if the given generation is effective on the effective date
     * currently set in the preferences.
     */
    public boolean isEffectiveOnCurrentEffectiveDate(IIpsObjectGeneration gen) {
        if (gen == null) {
            return false;
        }
        return gen.equals(getGenerationEffectiveOnCurrentEffectiveDate());
    }

    /**
     * Returns the generation that is effective on the effective date currently set in the
     * preferences.
     */
    public IIpsObjectGeneration getGenerationEffectiveOnCurrentEffectiveDate() {
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        ITimedIpsObject object = (ITimedIpsObject)getIpsObject();
        if (object == null) {
            return null;
        }
        return object.getGenerationByEffectiveDate(workingDate);
    }

}
