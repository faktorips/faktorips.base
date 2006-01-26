package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

public class RelationsContentProvider implements ITreeContentProvider {

	private IProductCmptGeneration generation;
	
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

    	if (inputElement instanceof IProductCmptGeneration) {
    		IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
    		IProductCmptTypeRelation[] result = getPcTypeRelations(generation); 
            return result;
    	}
    	
        return new Object[0];
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
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
     * Returns all PcType relations that are defined either in the generation
     * or in the PcType the generation is based on.
     */
    private IProductCmptTypeRelation[] getPcTypeRelations(IProductCmptGeneration generation) {
        List result = new ArrayList();

        try {
        	IProductCmptType productType = generation.getProductCmpt().findProductCmptType();

        	if (productType != null) {
            	IProductCmptTypeRelation[] relations = productType.getRelations();
        		for (int i=0; i<relations.length; i++) {
        			result.add(relations[i]);
        		}
        	}
        	
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return (IProductCmptTypeRelation[])result.toArray(new IProductCmptTypeRelation[result.size()]);
    }

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProductCmptTypeRelation && generation != null) {
			IProductCmptTypeRelation relation = (IProductCmptTypeRelation)parentElement;
			try {
				return generation.getRelations(relation.findPolicyCmptTypeRelation().getName());
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}

		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof IProductCmptTypeRelation) {
			return ((ProductCmptTypeRelation)element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IProductCmptTypeRelation && generation != null) {
			IProductCmptTypeRelation relation = (IProductCmptTypeRelation)element;
			try {
				return generation.getRelations(relation.findPolicyCmptTypeRelation().getName()).length > 0;
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		return false;
	}
}
