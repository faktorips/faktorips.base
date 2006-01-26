package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeRelation;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Provides the content for a generation-based relations-tree.
 * 
 * @author Thorsten Guenther
 */
public class RelationsContentProvider implements ITreeContentProvider {

	private IProductCmptGeneration generation;
	
    /** 
     * Overridden.
     */
    public Object[] getElements(Object inputElement) {

    	if (inputElement instanceof IProductCmptGeneration) {
    		IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
    		
    		try {
				IRelation[] relations = generation.getProductCmpt().findPolicyCmptType().getRelations();
				List result = new ArrayList(relations.length);
				for (int i = 0; i < relations.length; i++) {
					if (relations[i].isProductRelevant()) {
						result.add(relations[i]);
					}
				}
				return (IRelation[])result.toArray(new IRelation[result.size()]);
				
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
    	}
    	
        return new Object[0];
    }

    /** 
     * Overridden.
     */
    public void dispose() {
    }

    /** 
     * Overridden.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	if (newInput instanceof IProductCmptGeneration) {
    		generation = (IProductCmptGeneration)newInput;
    	}
    	else {
    		generation = null;
    	}
    }

    /**
     * Overridden.
     */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IRelation && generation != null) {
			
			IRelation relation = (IRelation)parentElement;
			return generation.getRelations(relation.getName());
		}

		return null;
	}

	/**
	 * Overridden.
	 */
	public Object getParent(Object element) {
		if (element instanceof IProductCmptTypeRelation) {
			return ((ProductCmptTypeRelation)element).getParent();
		}
		return null;
	}

	/**
	 * Overridden.
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IRelation && generation != null) {
			IRelation relation = (IRelation)element;
			return generation.getRelations(relation.getName()).length > 0;
		}
		return false;
	}
}
