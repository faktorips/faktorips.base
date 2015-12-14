/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.internal.model.type.CommonTypeFinder;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;

public class NewProductTemplateWizard extends NewProductWizard {

    public static final String PRODUCT_TEMPLATE_WIZARD_ID = "newProductTemplateWizard"; //$NON-NLS-1$

    public NewProductTemplateWizard() {
        super(true);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductTemplateWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected String getDialogId() {
        return PRODUCT_TEMPLATE_WIZARD_ID;
    }

    /**
     * Configures this wizard to have only one selectable product component type.
     */
    public void setSingleProcutCmptType(IProductCmptType productCmptType) {
        getPmo().setSingleProductCmptType(productCmptType);
    }

    public static void openInferTemplateWizard(IWorkbenchWindow window, ISelection selection) {
        TypedSelection<IProductCmpt> typedSelection = TypedSelection.<IProductCmpt> createAnyCount(
                IProductCmpt.class, selection);
        if (typedSelection.isValid()) {
            IProductCmptType commonType = CommonTypeFinder.commonTypeOf(typedSelection.getElements());
            if (commonType == null) {
                MessageDialog.openWarning(window.getShell(), Messages.NewProductTemplateWizard_NoCommonType_title,
                        Messages.NewProductTemplateWizard_NoCommonType_message);
            } else {
                open(window, (StructuredSelection)selection, commonType);
            }
        }
    }

    private static void open(IWorkbenchWindow window, StructuredSelection selection, IProductCmptType commonType) {
        NewProductTemplateWizard wizard = new NewProductTemplateWizard();
        wizard.init(window.getWorkbench(), selection);
        wizard.setSingleProcutCmptType(commonType);
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

}
