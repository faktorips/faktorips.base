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
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class ProductEditorDeleteAction extends AbstractSelectionChangedListenerAction {

    public ProductEditorDeleteAction(ISelectionProvider provider) {
        super(provider);
    }    

    /**
     * Removes all <code>IIpsObjectPart</code>s in the selection from their <code>IIpsObject</code>s.
     * {@inheritDoc}
     */
    protected void execute(IStructuredSelection selection) {
        Object[] items= selection.toArray();
        for (int i = 0; i < items.length; i++) {
            IpsObjectPart part = canBeProcessed(items[i]);
            if(part != null){
                part.delete();
            }
        }
    }

    /**
     * Defines which IpsObjectParts can be process by this action. Returns the IpsObjectPart if it can be processed, returns 
     * <code>null</code> if the provided object cannot be processed.
     */
    protected IpsObjectPart canBeProcessed(Object selectedIpsObjectPart){
        if(selectedIpsObjectPart instanceof IProductCmptRelation){
            return (IpsObjectPart)selectedIpsObjectPart;
        }
        return null;
    }
    
    /**
     * Removes this action and all delegates as listener of the selectionprovider given at
     * instanciation and disables them.
     */
    public void disposeInternal(){
        setEnabled(false);
    }
    
    /** 
     * Activates the action if the selection is a <code>IStructuredSelection</code> and 
     * does not contain <code>IProductCmptTypeRelation</code>s. Disables this action otherwise.
     * 
     * {@inheritDoc}
     */
    protected boolean isEnabled(ISelection selection){
        Object[] items= ((IStructuredSelection)selection).toArray();
        for (int i = 0; i < items.length; i++) {
            IpsObjectPart part = canBeProcessed(items[i]);
            if (part != null) {
                return true;
            }
        }
        return false;
    }
}
