/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.CommonTypeFinder;

public class NewProductTemplateWizard extends NewProductWizard {

    public static final String PRODUCT_TEMPLATE_WIZARD_ID = "newProductTemplateWizard"; //$NON-NLS-1$

    public NewProductTemplateWizard() {
        super(new NewProductCmptPMO(true));
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

    public static void open(IWorkbenchWindow window, ISelection selection) {
        TypedSelection<IProductCmpt> typedSelection = TypedSelection.<IProductCmpt> createAnyCount(IProductCmpt.class,
                selection);
        if (typedSelection.isValid()) {
            Collection<IProductCmpt> productCmpts = typedSelection.getElements();
            IProductCmptType commonType = CommonTypeFinder.commonTypeOf(productCmpts);
            boolean preconditionsOK = checkPreconditions(window, commonType, productCmpts);
            if (preconditionsOK) {
                open(window, (StructuredSelection)selection, commonType);
            }
        }
    }

    private static boolean checkPreconditions(IWorkbenchWindow window,
            IProductCmptType commonType,
            Collection<IProductCmpt> productCmpts) {
        boolean preconditionsMet = true;
        if (commonType == null) {
            preconditionsMet &= false;
            MessageDialog.openWarning(window.getShell(), Messages.NewProductTemplateWizard_NoCommonType_title,
                    Messages.NewProductTemplateWizard_NoCommonType_message);
        }
        if (atLeastOneHasTemplate(productCmpts)) {
            MessageDialog.openInformation(window.getShell(),
                    Messages.NewProductTemplateWizard_Precondition_TemplateDefined_title,
                    Messages.NewProductTemplateWizard_Precondition_TemplateDefined_message);
        }
        return preconditionsMet;
    }

    private static boolean atLeastOneHasTemplate(Collection<IProductCmpt> productCmpts) {
        boolean templateFound = false;
        for (IProductCmpt productCmpt : productCmpts) {
            if (productCmpt.isUsingTemplate()) {
                templateFound = true;
                break;
            }
        }
        return templateFound;
    }

    private static void open(IWorkbenchWindow window, StructuredSelection selection, IProductCmptType commonType) {
        NewProductTemplateWizard wizard = new NewProductTemplateWizard();
        wizard.init(window.getWorkbench(), selection);
        wizard.setSingleProcutCmptType(commonType);
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

}
