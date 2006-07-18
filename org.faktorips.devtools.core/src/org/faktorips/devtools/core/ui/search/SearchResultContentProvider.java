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
import org.faktorips.devtools.core.model.IIpsElement;
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
        	Object[] elementArray= (Object[])parentElement;
        	if(elementArray.length > 1){
        		IIpsElement[] children= new IIpsElement[elementArray.length-1];
        		System.arraycopy(elementArray, 1, children, 0, elementArray.length-1);
        		return children;
        	}
        }
        return new IIpsElement[0];
    }


    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
    	if(element instanceof IIpsElement){
	        if (element instanceof IProductCmptGeneration) {
	            return ((IProductCmptGeneration)element).getProductCmpt(); 
	        }else {
	        	// was ist parent von PCType? supertype? kann man den ohne Suche finden?
	        	return ((IIpsElement)element).getParent();
	        }
    	}
    	return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
    	if(element instanceof Object[]){
    		return ((Object[])element).length > 1;
    	}
        return false;
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
        if (inputElement instanceof ReferenceSearchResult) {
            ReferenceSearchResult result = (ReferenceSearchResult) inputElement;
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
