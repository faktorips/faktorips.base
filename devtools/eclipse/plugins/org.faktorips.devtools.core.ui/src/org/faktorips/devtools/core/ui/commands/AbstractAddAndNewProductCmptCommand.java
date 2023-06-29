/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * This class test, whether the target type of the selected element is present and valid. If the
 * target type is not present or valid, both Commands
 * {@link org.faktorips.devtools.core.ui.editors.productcmpt.AddProductCmptLinkCommand} and
 * {@link org.faktorips.devtools.core.ui.wizards.productcmpt.AddNewProductCmptCommand} will be not
 * enabled.
 * 
 * 
 */
public abstract class AbstractAddAndNewProductCmptCommand extends AbstractHandler {

    @Override
    public void setEnabled(Object evaluationContext) {

        IWorkbenchWindow activeWorkbenchWindow = IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();

        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection structuredSelection)) {
            return;
        }

        Object selectedElement = structuredSelection.getFirstElement();
        if (selectedElement instanceof IProductCmptReference) {
            setBaseEnabled(((IProductCmptReference)selectedElement).hasAssociationChildren());
        } else if (selectedElement instanceof String) {
            setBaseEnabled(isValidAssociationName((String)selectedElement, activeWorkbenchWindow));
        } else {
            setBaseEnabled(true);
        }
    }

    /**
     * Queries to the target type of the selected element, target type must be found.
     * 
     */
    private boolean isValidAssociationName(String associationName, IWorkbenchWindow activeWorkbenchWindow) {

        ProductCmptEditor productCmptEditor = (ProductCmptEditor)activeWorkbenchWindow.getActivePage()
                .getActiveEditor();
        if (productCmptEditor == null) {
            return false;
        }

        IProductCmpt productCmpt = productCmptEditor.getProductCmpt();

        IProductCmptType productCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
        if (productCmptType == null) {
            return false;
        }
        IProductCmptTypeAssociation typeAssociation = (IProductCmptTypeAssociation)productCmptType
                .findAssociation(associationName, productCmpt.getIpsProject());
        if (typeAssociation == null) {
            return false;
        }

        return typeAssociation.findTargetProductCmptType(productCmpt.getIpsProject()) != null;

    }

}
