/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * An implementation of ISelectionProvider that dispatches selection requests to other
 * ISelectionProviders that have registered themselves at this dispatcher. This dispatcher registers
 * itself as ISelectionChangedListener on the ISelectionProviders that register on this dispatcher.
 * Change events of the currently active selection provider will be delegated to the
 * ISelectionChangeListeners that are registered with this dispatcher. To determine which of the
 * selection providers is the active one not exactly ISelectionProvider implementations but
 * ISelectionProviderActivation implementations are registered at this dispatcher. A
 * ISelectionProviderActivation can be asked for its ISelectionProvider and it is currently active.
 * This dispatcher is used by the IpsObjectEditor and is the ISelectionProvider for it. Since
 * according to the eclipse architecture it is only possible to register one ISelectionProvider for
 * an IEditorPart it is necessary to provide this dispatcher mechanism.
 * 
 * @author Peter Erzberger
 */
public class SelectionProviderDispatcher implements ISelectionProvider, ISelectionChangedListener {

    private List<ISelectionProviderActivation> activiations = new ArrayList<ISelectionProviderActivation>();
    private List<ISelectionChangedListener> changeListeners = new ArrayList<ISelectionChangedListener>();
    private ISelectionProviderActivation currentActivation;

    /**
     * Registers the provided listener with this ISelectionProvider.
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Determines the currently active ISelectionProvider and returns its selection. If no active
     * ISelectionProvider could be found <code>null</code> is returned.
     */
    @Override
    public ISelection getSelection() {

        determineCurrentActivation();
        if (currentActivation != null) {
            return currentActivation.getSelectionProvider().getSelection();
        }
        return null;
    }

    private ISelectionProviderActivation determineCurrentActivation() {
        if (currentActivation != null && !currentActivation.isDisposed() && currentActivation.isActivated()) {
            return currentActivation;
        }
        currentActivation = null;
        for (Iterator<ISelectionProviderActivation> iterator = activiations.iterator(); iterator.hasNext();) {
            ISelectionProviderActivation activation = iterator.next();
            if (activation.isDisposed()) {
                iterator.remove();
                continue;
            }
            if (activation.isActivated()) {
                currentActivation = activation;
                break;
            }
        }

        return currentActivation;
    }

    /**
     * Unregisters the provided listener with this ISelectionProvider.
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Determines the currently active ISelectionProvider and sets the provided ISelection. If no
     * ISelectionProvider could be found the ISelection will not be set.
     */
    @Override
    public void setSelection(ISelection selection) {
        determineCurrentActivation();
        if (currentActivation != null) {
            currentActivation.getSelectionProvider().setSelection(selection);
        }
    }

    /**
     * Registers the ISelectionProviderActivations (which are basically the ISelectionProviders that
     * are controlled by this dispatcher) with this dispatcher.
     */
    public void addSelectionProviderActivation(ISelectionProviderActivation activation) {
        if (activation == null || activation.isDisposed() || activation.getSelectionProvider() == null) {
            return;
        }
        activiations.add(activation);
        activation.getSelectionProvider().addSelectionChangedListener(this);
    }

    /**
     * Disposes this dispatcher and hence unregisters this dispatcher in the role of
     * ISelectionChangeListener.
     */
    public void dispose() {
        for (ISelectionProviderActivation activation : activiations) {
            activation.getSelectionProvider().removeSelectionChangedListener(this);
        }
    }

    /**
     * Informs the ISelectionChangedListeners registered with this dispatcher when a changed event
     * of the currently active ISelectionProvider occurs.
     */
    @Override
    public final void selectionChanged(SelectionChangedEvent event) {
        for (ISelectionChangedListener listener : changeListeners) {
            listener.selectionChanged(event);
        }
    }
}
