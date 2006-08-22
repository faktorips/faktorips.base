/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

public class ProductEditorDeleteAction extends IpsDeleteAction {

    public ProductEditorDeleteAction(ISelectionProvider provider) {
        super(provider);
    }    

    /**
     * Removed all <code>IIpsObjectPart</code>s in the selection from their <code>IIpsObject</code>s.
     * {@inheritDoc}
     */
    protected void deleteSelection(IStructuredSelection selection) {
        Object[] items= selection.toArray();
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof IIpsObjectPart & !(items[i] instanceof IProductCmptTypeRelation)) {
                ((IIpsObjectPart)items[i]).delete();
            }
        }
    }

    /**
     * Removes this action and all delegates as listener of the selectionprovider given at
     * instanciation and disables them.
     * 
     */
    public void deregister(){
        setEnabled(false);
        selectionProvider.removeSelectionChangedListener(this);
        // avoid further usage
        selectionProvider= null;
    }
    
    /** 
     * Activates the action if the selection is a <code>IStructuredSelection</code> and 
     * does not contain <code>IProductCmptTypeRelation</code>s. Disables this action otherwise.
     * {@inheritDoc}
     */
    protected void setEnabledState(ISelection selection){
        if(selection instanceof IStructuredSelection){
            Object[] items= ((IStructuredSelection)selection).toArray();
            boolean enabled= false;
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof IIpsObjectPart & !(items[i] instanceof IProductCmptTypeRelation)) {
                    enabled= true;
                }
            }
            setEnabled(enabled);
        }else{
            setEnabled(false);
        }
    }

}
