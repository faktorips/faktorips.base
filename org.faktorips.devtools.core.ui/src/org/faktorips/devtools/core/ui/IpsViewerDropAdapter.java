/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TransferData;

/**
 * Handle a validation or drop event for multiple drop listeners registered to one drop target.
 * <p>
 * The current implementation of the {@link ViewerDropAdapter} does not work correctly if multiple
 * instances are added as drop target listeners to one {@link DropTarget}. So we need to provide the
 * ability to let multiple {@link ViewerDropAdapter} instances act as if there is only one.
 * 
 * @author Thorsten Günther
 */
public abstract class IpsViewerDropAdapter extends ViewerDropAdapter {

    private Set<IpsViewerDropAdapter> adapters;
    private IpsViewerDropAdapter currentAdapter;

    public IpsViewerDropAdapter(Viewer viewer) {
        super(viewer);
    }

    /**
     * Set the list of adapters which have to act as an unit. The instance this method is called is
     * added to the set during the execution of this method.
     */
    public void setPartnerDropAdapters(Set<IpsViewerDropAdapter> adapters) {
        this.adapters = new HashSet<IpsViewerDropAdapter>(adapters);
        this.adapters.add(this);
    }

    /**
     * {@inheritDoc}
     * 
     * Iterates over all partner adapters, calling
     * {@link #validateDropSingle(Object, int, TransferData)}. If at least one of these calls
     * returns <code>true</code>, this method returns <code>true</code> also.
     * <p>
     * The first adapter returning <code>true</code> on the call to
     * {@link #validateDropSingle(Object, int, TransferData)} is addressed if a
     * {@link #performDrop(Object)} occurs.
     * 
     * @see #setPartnerDropAdapters(Set)
     * 
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData data) {
        if (adapters == null) {
            return false;
        }

        for (IpsViewerDropAdapter adapter : adapters) {
            if (adapter.validateDropSingle(target, operation, data)) {
                currentAdapter = adapter;
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * Must only be called after a call to {@link #validateDrop(Object, int, TransferData)} has
     * returned true. The partner adapter which has returned true is addressed to perform the drop
     * operation.
     */
    @Override
    public boolean performDrop(Object data) {
        if (currentAdapter == null) {
            throw new NullPointerException("Drop performed without succuessfully calling validateDrop"); //$NON-NLS-1$
        }

        if (this != currentAdapter) {
            return true;
        }

        boolean result = currentAdapter.performDropSingle(data);
        currentAdapter = null;
        return result;
    }

    public abstract boolean validateDropSingle(Object target, int operation, TransferData data);

    public abstract boolean performDropSingle(Object data);
}
