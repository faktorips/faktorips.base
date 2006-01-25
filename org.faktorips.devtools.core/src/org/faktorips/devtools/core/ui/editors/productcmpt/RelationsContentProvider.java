package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class RelationsContentProvider implements ITreeContentProvider {

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

    	if (inputElement instanceof IProductCmptGeneration) {
    		IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
    		String[] result = getPcTypeRelations(generation); 
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
    }

    /**
     * Returns all PcType relations that are defined either in the generation
     * or in the PcType the generation is based on.
     */
    private String[] getPcTypeRelations(IProductCmptGeneration generation) {
        List result = new ArrayList();
        try {
            IPolicyCmptType pcType = generation.getProductCmpt().findPolicyCmptType();
            if (pcType!=null) {
                IRelation[] pcTypeRelations = pcType.getRelations();
                for (int i=0; i<pcTypeRelations.length; i++) {
                    result.add(pcTypeRelations[i].getName());
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
		IProductCmptRelation[] relations = generation.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (!result.contains(relations[i].getPcTypeRelation())) {
                result.add(relations[i].getPcTypeRelation());
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IRelation) {
			
		}
		return false;
	}
}
