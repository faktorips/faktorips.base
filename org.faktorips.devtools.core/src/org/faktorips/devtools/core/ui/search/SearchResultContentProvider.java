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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class SearchResultContentProvider implements ITreeContentProvider {

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Object[]) {
            return new Object[] {((Object[])parentElement)[1]};
        }
        return new Object[0];
    }


    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (element instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)element).getProductCmpt(); 
        }
        else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        return (element instanceof Object[]);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ReferencesToProductSearchResult) {
            ReferencesToProductSearchResult result = (ReferencesToProductSearchResult) inputElement;
            return result.getElements(); 
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do.
    }
    
}
