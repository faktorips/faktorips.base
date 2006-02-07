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
