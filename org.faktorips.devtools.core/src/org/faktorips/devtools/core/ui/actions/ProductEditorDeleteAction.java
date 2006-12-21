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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

public class ProductEditorDeleteAction extends IpsAction {

    private ProductCmptEditor editor;
    
    public ProductEditorDeleteAction(ProductCmptEditor editor) {
        super(editor.getSelectionProviderDispatcher());
        this.editor = editor;
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
    
    public void dispose(){
        setEnabled(false);
        super.dispose();
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        if(!super.computeEnabledProperty(selection)){
            return false;
        }
        if(!editor.isActiveGenerationEditable()){
            return false;
        }
        Object[] items= ((IStructuredSelection)selection).toArray();
        for (int i = 0; i < items.length; i++) {
            IpsObjectPart part = canBeProcessed(items[i]);
            if (part != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IStructuredSelection selection) {
        Object[] items= selection.toArray();
        for (int i = 0; i < items.length; i++) {
            IpsObjectPart part = canBeProcessed(items[i]);
            if(part != null){
                part.delete();
            }
        }
    }
}
