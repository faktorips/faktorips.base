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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptSturctureReference;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class ProductStructureContentProvider implements ITreeContentProvider {
	
	/**
	 * The root-node this content provider starts to evaluate the content.
	 */
	private IProductCmptStructure structure;
	
	/**
	 * Flag to tell the content provider to show (<code>true</code>) or not to show the
	 * Relation-Type as Node.
	 */
	private boolean fShowRelationType = false;
	
	private IProductCmptReference root;
	
	/**
	 * Creates a new content provider.
	 * 
	 * @param showRelationType <code>true</code> to show the relation types as nodes.
	 */
	public ProductStructureContentProvider(boolean showRelationType) {
		this.fShowRelationType = showRelationType;
	}
	
    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
    	if (!fShowRelationType && parentElement instanceof IProductCmptReference) {
    		return structure.getChildProductCmptReferences((IProductCmptReference)parentElement);
    	} 
    	else if (parentElement instanceof IProductCmptReference) {
    		return structure.getChildProductCmptTypeRelationReferences((IProductCmptReference)parentElement);
    	}
    	else if (parentElement instanceof IProductCmptSturctureReference) {
    		return structure.getChildProductCmptReferences((IProductCmptSturctureReference)parentElement);
    	}
    	else {
    		return new Object[0];
    	}
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
    	if (structure == null) {
    		return null;
    	}
    	
    	if (!fShowRelationType && element instanceof IProductCmptReference) {
    		return structure.getParentProductCmptReference((IProductCmptReference)element);
    	}
    	else if (element instanceof IProductCmptReference) {
    		return structure.getParentProductCmptTypeRelationReference((IProductCmptReference)element);
    	}
    	else if (element instanceof IProductCmptSturctureReference) {
    		return structure.getParentProductCmptReference((IProductCmptSturctureReference)element);
    	}

    	return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
    	return getChildren(element).length > 0;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
    	structure = null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (structure == inputElement) {
            return new Object[] {root};
        }
        else {
            return new Object[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null || !(newInput instanceof IProductCmptStructure)) {
        	structure = null;
            return;
        }
        
        structure = (IProductCmptStructure)newInput;
        root = structure.getRoot();
    }
    
    public boolean isRelationTypeShowing() {
    	return fShowRelationType;
    }
    
    public void setRelationTypeShowing(boolean showRelationType) {
    	fShowRelationType = showRelationType;
    }
}
