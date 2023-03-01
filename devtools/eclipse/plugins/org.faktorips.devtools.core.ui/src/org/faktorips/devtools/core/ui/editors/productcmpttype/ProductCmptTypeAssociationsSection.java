/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AssociationsSection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IType;

/**
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociationsSection extends AssociationsSection {

    public ProductCmptTypeAssociationsSection(IProductCmptType productCmptType, Composite parent,
            IWorkbenchPartSite site, UIToolkit toolkit) {

        super(productCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ProductCmptTypeAssociationsComposite(getProductCmptType(), parent, getSite(), toolkit);
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    private static class ProductCmptTypeAssociationsComposite extends AssociationsComposite {

        private ProductCmptTypeAssociationsComposite(IProductCmptType productCmptType, Composite parent,
                IWorkbenchPartSite site, UIToolkit toolkit) {

            super(productCmptType, parent, site, toolkit);
        }

        @Override
        protected IpsAction createOpenTargetAction() {
            return new OpenTargetProductCmptTypeInEditorAction(getViewer(), getType());
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AssociationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

    }

    private static class OpenTargetProductCmptTypeInEditorAction extends IpsAction {

        private final IType type;

        private OpenTargetProductCmptTypeInEditorAction(ISelectionProvider selectionProvider, IType type) {
            super(selectionProvider);
            this.type = type;
            setText(Messages.ProductCmptTypeAssociationsSection_menuOpenTargetInNewEditor);
        }

        @Override
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IProductCmptTypeAssociation);
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptTypeAssociation productCmptTypeAssociation) {
                IType target = productCmptTypeAssociation.findTarget(type.getIpsProject());
                IpsUIPlugin.getDefault().openEditor(target);
            }
        }
    }

}
