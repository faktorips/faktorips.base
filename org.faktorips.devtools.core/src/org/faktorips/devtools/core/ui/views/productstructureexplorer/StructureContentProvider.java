package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class StructureContentProvider implements ITreeContentProvider {

    private Hashtable hasRelationToMap = new Hashtable();
    
    /**
     * Overridden.
     */
    public Object[] getChildren(Object parentElement) {

        if (parentElement instanceof DummyRoot) {
            return (Object[])hasRelationToMap.get(((DummyRoot)parentElement).data);
        }
        
        if (!(parentElement instanceof IProductCmpt)) {
            return new Object[0];
        }
        
        Object[] values = (Object[])hasRelationToMap.get(parentElement);
        return values;
    }

    /**
     * Overridden method.
     */
    public Object getParent(Object element) {
        return null;
    }

    /**
     * Overridden method.
     */
    public boolean hasChildren(Object element) {
        if (element == null) {
            return false;
        }
        try {
            if (element instanceof DummyRoot) {
                Object cached = hasRelationToMap.get(((DummyRoot)element).data);
                if (cached != null) {
                    return ((Object[])cached).length > 0;
                }
                else {
                    return false;
                }
            }
            
            Object cached = this.hasRelationToMap.get(element);
            if (cached != null) {
                return ((Object[])cached).length > 0;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
    }

    /**
     * Overridden method.
     */
    public void dispose() {
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IProductCmpt) {
            DummyRoot root = new DummyRoot();
            root.data = (IProductCmpt)inputElement;
            return new Object[] {root};
        }
        else {
            return new Object[0];
        }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.hasRelationToMap.clear();

        if (newInput == null) {
            return;
        }
        
        ArrayList products = new ArrayList();
        try {
            buildProductList(products, ((IIpsElement) newInput).getIpsProject());
            
            for (Iterator i = products.iterator(); i.hasNext(); ) {
                putRelatedObjects((IProductCmpt)i.next());
            }
            
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        
    }

    private void putRelatedObjects(IProductCmpt cmpt) throws CoreException {
        IProductCmptGeneration activeGeneration = (IProductCmptGeneration)cmpt.findGenerationEffectiveOn(IpsPreferences.getWorkingDate());
        IProductCmptRelation[] relations = activeGeneration.getRelations();
        ArrayList related = new ArrayList();
        for (int i = 0; i < relations.length; i ++) {
            IProductCmpt p = (IProductCmpt)cmpt.getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, relations[i].getTarget()); 
            related.add(p);
        }
        this.hasRelationToMap.put(cmpt, related.toArray());
    }

    /**
     * Processes the IpsModel recursively to find all IProductCmpt.
     * 
     * @param products List to put all found products into.
     * @param element Node of the IpsModel to start at.
     * @throws CoreException 
     * @throws CoreException
     */
    private void buildProductList(ArrayList products, IIpsElement element) throws CoreException {
        IIpsElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof IProductCmpt) {
                products.add(children[i]);
            }
            else if (children[i] == null) {
                System.out.println("null"); //$NON-NLS-1$
            }
            else {
                buildProductList(products, children[i]);
            }
        }
    }
    
}
