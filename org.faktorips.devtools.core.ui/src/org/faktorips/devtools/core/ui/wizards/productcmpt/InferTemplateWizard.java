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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.util.ArgumentCheck;

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
        ArgumentCheck.atLeast(typedSelection.getElements(), 1);
        init(window.getWorkbench(), typedSelection.asStructuredSelection());
        getPmo().setProductCmptsToInferTemplateFrom(typedSelection.getElements());
        if (getPmo().isSingleTypeSelection()) {
            if (openWarningIfTemplateIsUsed(window, typedSelection.getElements())) {
                WizardDialog wizard = new WizardDialog(window.getShell(), this);
                wizard.open();
            }
        } else {
            MessageDialog.openError(window.getShell(), Messages.NewProductTemplateWizard_NoCommonType_title,
                    Messages.NewProductTemplateWizard_NoCommonType_message);
        }
    }

    private static boolean openWarningIfTemplateIsUsed(IWorkbenchWindow window, Collection<IProductCmpt> productCmpts) {
        List<IProductCmpt> productCmptsWithTemplate = templateExistsIn(productCmpts);
        if (!productCmptsWithTemplate.isEmpty()) {
            ExistingTemplateDialog dialog = new ExistingTemplateDialog(window.getShell(),
                    Messages.NewProductTemplateWizard_Precondition_TemplateDefined_title,
                    Messages.NewProductTemplateWizard_Precondition_TemplateDefined_message, productCmptsWithTemplate);
            int dialogResult = dialog.open();
            return dialogResult == Window.OK;
        }
        return true;
    }

    private static List<IProductCmpt> templateExistsIn(Collection<IProductCmpt> productCmpts) {
        List<IProductCmpt> result = new ArrayList<>();
        for (IProductCmpt productCmpt : productCmpts) {
            if (productCmpt.isUsingTemplate()) {
                result.add(productCmpt);
            }
        }
        return result;
    }

    private static class ExistingTemplateDialog extends IconAndMessageDialog {

        private String title;

        private Collection<IProductCmpt> productCmpts;

        public ExistingTemplateDialog(Shell parentShell, String title, String message,
                Collection<IProductCmpt> productCmpts) {
            super(parentShell);
            this.title = title;
            this.message = message;
            this.productCmpts = productCmpts;
        }

        @Override
        protected Image getImage() {
            return getInfoImage();
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText(title);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            UIToolkit toolkit = new UIToolkit(null);
            Composite area = toolkit.createGridComposite(parent, 2, false, true);
            area.setLayout(new GridLayout(2, false));
            createMessageArea(area);
            toolkit.createVerticalSpacer(area, 0);
            TableViewer tableViewer = new TableViewer(area, SWT.HIDE_SELECTION | SWT.BORDER);
            tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            tableViewer.setLabelProvider(new DefaultLabelProvider());
            tableViewer.setContentProvider(new ArrayContentProvider());
            tableViewer.setInput(productCmpts);
            makeTableUnselectable(tableViewer.getTable());
            return parent;
        }

        private void makeTableUnselectable(final Table table) {
            table.addListener(SWT.EraseItem, $ -> table.setSelection(-1));
            table.setSelection(-1);
        }

        @Override
        public int open() {
            return super.open();
        }

    }

}
