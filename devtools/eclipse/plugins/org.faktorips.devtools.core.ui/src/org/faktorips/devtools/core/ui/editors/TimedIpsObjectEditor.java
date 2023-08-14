/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * An abstract editor for timed objects.
 */
public abstract class TimedIpsObjectEditor extends IpsObjectEditor {

    private final List<IActiveGenerationChangedListener> activeGenerationChangedListeners = new CopyOnWriteArrayList<>();

    private IIpsObjectGeneration generation;

    public TimedIpsObjectEditor() {
        super();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setContentDescription(IpsStringUtils.EMPTY);
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
        for (IActiveGenerationChangedListener listener : activeGenerationChangedListeners) {
            listener.activeGenerationChanged(generation);
        }
    }

    @Override
    protected void refreshInternalState() {
        super.refreshInternalState();
        // replace the generation with the "same" current generation if the object was reloaded from
        // file

        Optional.ofNullable(generation).stream()
                .map(g -> (ITimedIpsObject)g.getParent())
                .flatMap(g -> g.getGenerations().stream())
                .filter(Predicate.isEqual(generation))
                .findFirst()
                .ifPresent(this::setActiveGeneration);
    }

}
