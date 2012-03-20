/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;

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
        setContentDescription(StringUtils.EMPTY);
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

    private void notifyGenerationChanged() {
        List<IActiveGenerationChangedListener> copy = new CopyOnWriteArrayList<IActiveGenerationChangedListener>(
                activeGenerationChangedListeners);
        for (IActiveGenerationChangedListener listener : copy) {
            // listener.activeGenerationChanged(generation);
        }
    }

}
