/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;

/**
 * ContentProvider for {@link IpsPackageSortDefDialog}. Use Presentationmodel
 * {@link IpsProjectSortOrdersPM} for retrieving the data.
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefContentProvider implements ITreeContentProvider{

    protected static final Object[] EMPTY_ARRAY = new Object[0];
    private IpsProjectSortOrdersPM sortOrderPM;

    /**
     * New Instance.
     *
     * @param sortOrderPM Presentationmodel of the chosen IpsProject.
     */
    public IpsPackageSortDefContentProvider(IpsProjectSortOrdersPM sortOrderPM) {
        Assert.isNotNull(sortOrderPM);
        this.sortOrderPM = sortOrderPM;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)parentElement;

            try {
                return sortOrderPM.getChildIpsPackageFragments(fragment);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        }

        return EMPTY_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {

        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;

            // packages are not allowed to be moved between different parents => no wrapper-sortOrderPM function needed.
            return fragment.getParentIpsPackageFragment();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;

            // packages are not allowed to be moved between different parents => no wrapper-sortOrderPM function needed.

            try {
                IIpsPackageFragment[] children = fragment.getChildIpsPackageFragments();
                return ((children.length > 0) ? true : false);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {

        if (inputElement instanceof IpsProjectSortOrdersPM) {
            IpsProjectSortOrdersPM sortOrderPO = (IpsProjectSortOrdersPM) inputElement;

            try {
                return sortOrderPO.getDefaultPackageFragments();
            } catch (CoreException e) {
                return EMPTY_ARRAY;
            }
        }

        return EMPTY_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to implement
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to implement
    }

    /**
     * @return Returns the sortOrderPM.
     */
    public IpsProjectSortOrdersPM getSortOrderPM() {
        return sortOrderPM;
    }
}
