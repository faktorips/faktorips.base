/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorActionContributor;

/**
 * The ProductCmptEditorActionContributor distinguishes between multiple instances of
 * ProductCmptEditor and organizes actions accordingly. The contributor makes sure retargetable
 * actions for the ProductCmptEditor always use the currently active editor as a selectionprovider.
 * <p>
 * Note: The contributor uses the selectionprovider returned by the editorsite. Which GUI element
 * actually provides the selection is up to the Editor. If no selectionprovider can be retrieved
 * from the editorsite no actions are created/activated.
 * 
 * @author Stefan Widmaier, Peter Erzberger
 */
public class ProductCmptEditorActionContributor extends IpsObjectEditorActionContributor {

    private ProductEditorDeleteAction deleteAction = null;

    public ProductCmptEditorActionContributor() {
        super();
    }

    @Override
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
    }

    /**
     * Communicates the new SelectionProvider (of the currently active editor) to all actions used
     * in the ProductCmptEditor.
     * <p>
     * This is necessary because the workbench (and the plugin-mechanism) can't distinguish between
     * instances of ProductCmptEditors as they all possess the same extension-ID. This method is
     * automatically called by the workbench every time a ProductCmptEditor is activated.
     */
    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);

        if (!(targetEditor instanceof ProductCmptEditor)) {
            IpsPlugin.log(new IpsStatus(getClass().getName()
                    + ": This editor action contributor expects to be registered with an " + //$NON-NLS-1$
                    ProductCmptEditor.class.getName()));
        }
        ProductCmptEditor pcEditor = (ProductCmptEditor)targetEditor;
        if (deleteAction != null) {
            deleteAction.dispose();
        }
        deleteAction = new ProductEditorDeleteAction(pcEditor);
        targetEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
    }

}
