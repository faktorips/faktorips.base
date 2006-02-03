package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.product.ProductCmptRelation;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
    			IProductCmpt pc = generation.getProductCmpt();
    			IProductCmptType pcType = pc.findProductCmptType();
    			
    			if (pcType == null) {
    				return new Object[0];
    			}
    			
				IProductCmptTypeRelation[] relations = pcType.getRelations();
				List result = new ArrayList(relations.length);
				for (int i = 0; i < relations.length; i++) {
					if (!relations[i].isAbstract()) {
						result.add(relations[i]);
					}
				}
				return (IProductCmptTypeRelation[])result.toArray(new IProductCmptTypeRelation[result.size()]);
				
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
		if (parentElement instanceof IProductCmptTypeRelation && generation != null) {
			IProductCmptTypeRelation relation = (IProductCmptTypeRelation)parentElement;
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
		if (element instanceof IProductCmptRelation) {
			try {
				return ((ProductCmptRelation)element).findProductCmptTypeRelation();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		IpsPlugin.log(new IpsStatus("Unknown element class " + element.getClass()));
		return new Object[0];
	}

	/**
	 * Overridden.
	 */
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		if (children==null) {
			return false;
		}
		return children.length > 0;
	}
}
