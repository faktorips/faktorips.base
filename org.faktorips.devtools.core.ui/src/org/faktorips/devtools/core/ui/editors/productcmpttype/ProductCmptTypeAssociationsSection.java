/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AssociationsSection;

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
        return new ProductCmptTypeAssociationsComposite(getProductCmptType(), parent, toolkit);
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    private class ProductCmptTypeAssociationsComposite extends AssociationsComposite {

        private ProductCmptTypeAssociationsComposite(IProductCmptType productCmptType, Composite parent,
                UIToolkit toolkit) {

            super(productCmptType, parent, toolkit);
        }

        @Override
        protected IpsAction createOpenTargetAction() {
            return new OpenTargetProductCmptTypeInEditorAction(getViewer());
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new AssociationEditDialog((IProductCmptTypeAssociation)part, shell);
        }

    }

    private class OpenTargetProductCmptTypeInEditorAction extends IpsAction {

        private OpenTargetProductCmptTypeInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
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
            if (selected instanceof IProductCmptTypeAssociation) {
                IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)selected;
                try {
                    IType target = productCmptTypeAssociation.findTarget(getProductCmptType().getIpsProject());
                    IpsUIPlugin.getDefault().openEditor(target);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
