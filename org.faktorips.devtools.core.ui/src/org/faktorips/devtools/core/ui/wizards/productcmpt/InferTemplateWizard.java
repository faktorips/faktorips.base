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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;

public class InferTemplateWizard extends NewProductWizard {

    public static final String INFER_TEMPLATE_WIZARD_ID = "inferTemplateWizard"; //$NON-NLS-1$

    public InferTemplateWizard() {
        super(new InferTemplatePmo());
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductTemplateWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected String getDialogId() {
        return INFER_TEMPLATE_WIZARD_ID;
    }

    @Override
    public InferTemplatePmo getPmo() {
        return (InferTemplatePmo)super.getPmo();
    }

    @Override
    protected NewProductDefinitionOperation<? extends NewProductDefinitionPMO> getOperation() {
        return new InferTemplateOperation(getPmo());
    }

    public static void open(IWorkbenchWindow window, ISelection selection) {
        TypedSelection<IProductCmpt> typedSelection = TypedSelection.<IProductCmpt> createAnyCount(IProductCmpt.class,
                selection);
        if (typedSelection.isValid()) {
            InferTemplateWizard wizard = new InferTemplateWizard();
            wizard.open(window, typedSelection);
        }
    }

    private void open(IWorkbenchWindow window, TypedSelection<IProductCmpt> typedSelection) {
        init(window.getWorkbench(), typedSelection.asStructuredSelection());
        getPmo().setProductCmptsToInferTemplate(typedSelection.getElements());
        if (getPmo().isSingleTypeSelection()) {
            WizardDialog dialog = new WizardDialog(window.getShell(), this);
            dialog.open();
        } else {
            MessageDialog.openWarning(window.getShell(), Messages.NewProductTemplateWizard_NoCommonType_title,
                    Messages.NewProductTemplateWizard_NoCommonType_message);
        }
    }
}
