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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.faktorips.devtools.core.ui.actions.ProductEditorDeleteAction;

/**
 * The ProductCmptEditorActionContributor distinguishes between multiple instances of
 * ProductCmptEditor and organizes actions accordingly. The contributor makes sure retargetable
 * actions for the ProductCmptEditor always use the currently active editor as a selectionprovider.
 * <p>
 * Note: The contributor uses the selectionprovider returned by the editorsite. Which GUI element
 * actually provides the selection is up to the Editor. If no selectionprovider can be retrieved 
 * from the editorsite no actions are created/activated. 
 *
 * @author Stefan Widmaier
 */
public class ProductCmptEditorActionContributor extends EditorActionBarContributor {
    
    private ProductEditorDeleteAction deleteAction= null;

    public ProductCmptEditorActionContributor() {
        super();
    }

    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
    }

    /**
     * Communicates the new SelectionProvider (of the currently active editor) to all actions used
     * in the ProductCmptEditor.
     * <p>
     * This is necessary because the worbench (and the plugin-mechanism) can't distinguisch between
     * instances of ProductCmptEditors as they all posses the same extension-ID. This method is
     * automatically called by the workbench every time a ProductCmptEditor is activated.
     */
    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
        if(deleteAction!=null){
            deleteAction.deregister();
        }
        ISelectionProvider provider= targetEditor.getEditorSite().getSelectionProvider();
        if(provider!=null){
            deleteAction = new ProductEditorDeleteAction(provider);
            targetEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
        }
    }
    
}
