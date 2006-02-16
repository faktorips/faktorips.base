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
 * Provides the content for a generation-based relations-tree. The relations-types are
 * requested from the given generation and all supertypes the type containing this generation
 * is based on.
 * 
 * @author Thorsten Guenther
 */
public class RelationsContentProvider implements ITreeContentProvider {

	private IProductCmptGeneration generation;
	
    /**
     * {@inheritDoc}
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
    			
				List result = new ArrayList();
    			while (pcType != null) {
					IProductCmptTypeRelation[] relations = pcType.getRelations();
					for (int i = 0; i < relations.length; i++) {
						if (!relations[i].isAbstract() && !relations[i].isAbstractContainer()) {
							result.add(relations[i]);
						}
					}
					pcType = pcType.findSupertype();
    			}
    			
				return (IProductCmptTypeRelation[])result.toArray(new IProductCmptTypeRelation[result.size()]);
				
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
    	}
    	
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     */ 
    public void dispose() {
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */ 
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProductCmptTypeRelation && generation != null) {
			IProductCmptTypeRelation relation = (IProductCmptTypeRelation)parentElement;
			return generation.getRelations(relation.getName());
		}
		return null;
	}

    /**
     * {@inheritDoc}
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
		IpsPlugin.log(new IpsStatus(Messages.RelationsContentProvider_msg_UnknownElementClass + element.getClass()));
		return null;
	}

    /**
     * {@inheritDoc}
     */ 
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		if (children==null) {
			return false;
		}
		return children.length > 0;
	}
}
